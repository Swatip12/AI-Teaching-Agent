package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.CodeExecutionRequest;
import com.aiteachingplatform.dto.CodeExecutionResponse;
import com.aiteachingplatform.exception.CodeExecutionException;
import com.aiteachingplatform.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * Service for executing code in secure Docker containers
 * Provides safe code execution with resource limits and security measures
 */
@Service
public class CodeExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionService.class);
    
    @Value("${code.execution.enabled:true}")
    private boolean executionEnabled;
    
    @Value("${code.execution.timeout.seconds:10}")
    private int defaultTimeoutSeconds;
    
    @Value("${code.execution.memory.limit.mb:128}")
    private int memoryLimitMB;
    
    @Value("${code.execution.temp.dir:/tmp/code-execution}")
    private String tempDir;
    
    // Security patterns to detect potentially dangerous code
    private static final Pattern[] SECURITY_PATTERNS = {
        Pattern.compile("\\bRuntime\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bProcess\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bSystem\\.exit\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bfile\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bopen\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b__import__\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\beval\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bexec\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bos\\.", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bsubprocess\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bsocket\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bhttp\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\burllib\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\brequests\\b", Pattern.CASE_INSENSITIVE)
    };
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Autowired
    private LoggingService loggingService;
    
    /**
     * Execute code in a secure Docker container
     */
    public CodeExecutionResponse executeCode(CodeExecutionRequest request) {
        if (!executionEnabled) {
            return CodeExecutionResponse.systemError("Code execution is disabled");
        }
        
        // Security validation
        String securityViolation = validateCodeSecurity(request.getCode());
        if (securityViolation != null) {
            return CodeExecutionResponse.securityViolation(securityViolation);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create temporary directory for this execution
            String executionId = generateExecutionId();
            Path executionDir = createExecutionDirectory(executionId);
            
            try {
                // Write code to file
                Path codeFile = writeCodeToFile(executionDir, request.getCode(), request.getLanguage());
                
                // Execute code in Docker container
                CodeExecutionResponse response = executeInDocker(
                    executionDir, 
                    codeFile, 
                    request.getLanguage(), 
                    request.getStdin(),
                    request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : defaultTimeoutSeconds
                );
                
                response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                response.setLanguage(request.getLanguage().getValue());
                
                return response;
                
            } finally {
                // Clean up temporary files
                cleanupExecutionDirectory(executionDir);
                
                // Log code execution
                long executionTime = System.currentTimeMillis() - startTime;
                loggingService.logCodeExecution(
                    request.getLanguage().toString(),
                    response != null && response.isSuccess(),
                    executionTime,
                    response != null ? response.getStatus().toString() : "UNKNOWN",
                    null, // userId not available in this context
                    response != null && !response.isSuccess() ? response.getError() : null
                );
            }
            
        } catch (Exception e) {
            logger.error("Error executing code", e);
            return CodeExecutionResponse.systemError(e.getMessage());
        }
    }
    
    /**
     * Validate code for security violations
     */
    private String validateCodeSecurity(String code) {
        for (Pattern pattern : SECURITY_PATTERNS) {
            if (pattern.matcher(code).find()) {
                return "Potentially unsafe code detected: " + pattern.pattern();
            }
        }
        
        // Check for excessive length
        if (code.length() > 10000) {
            return "Code exceeds maximum length limit";
        }
        
        return null; // No violations found
    }
    
    /**
     * Generate unique execution ID
     */
    private String generateExecutionId() {
        return "exec_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
    
    /**
     * Create temporary directory for code execution
     */
    private Path createExecutionDirectory(String executionId) throws IOException {
        Path baseDir = Paths.get(tempDir);
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        
        Path executionDir = baseDir.resolve(executionId);
        Files.createDirectories(executionDir);
        return executionDir;
    }
    
    /**
     * Write code to appropriate file based on language
     */
    private Path writeCodeToFile(Path executionDir, String code, CodeExecutionRequest.Language language) throws IOException {
        String fileName;
        String fileContent = code;
        
        switch (language) {
            case JAVA:
                fileName = "Main.java";
                // Ensure the class name is Main for Java
                if (!code.contains("class Main")) {
                    fileContent = code.replaceFirst("class\\s+\\w+", "class Main");
                }
                break;
            case PYTHON:
                fileName = "main.py";
                break;
            case JAVASCRIPT:
                fileName = "main.js";
                break;
            case CPP:
                fileName = "main.cpp";
                break;
            default:
                throw new CodeExecutionException("Unsupported language: " + language, language);
        }
        
        Path codeFile = executionDir.resolve(fileName);
        Files.write(codeFile, fileContent.getBytes());
        return codeFile;
    }
    
    /**
     * Execute code in Docker container with security restrictions
     */
    private CodeExecutionResponse executeInDocker(Path executionDir, Path codeFile, 
                                                 CodeExecutionRequest.Language language, 
                                                 String stdin, int timeoutSeconds) {
        try {
            String dockerImage = getDockerImage(language);
            String[] compileCommand = getCompileCommand(language, codeFile.getFileName().toString());
            String[] runCommand = getRunCommand(language);
            
            // Compile if necessary
            if (compileCommand != null) {
                CodeExecutionResponse compileResult = runDockerCommand(
                    executionDir, dockerImage, compileCommand, null, timeoutSeconds
                );
                
                if (!compileResult.isSuccess()) {
                    return CodeExecutionResponse.compilationError(compileResult.getError());
                }
            }
            
            // Execute the code
            return runDockerCommand(executionDir, dockerImage, runCommand, stdin, timeoutSeconds);
            
        } catch (Exception e) {
            logger.error("Error executing code in Docker", e);
            return CodeExecutionResponse.systemError(e.getMessage());
        }
    }
    
    /**
     * Get Docker image for language
     */
    private String getDockerImage(CodeExecutionRequest.Language language) {
        switch (language) {
            case JAVA:
                return "openjdk:21-slim";
            case PYTHON:
                return "python:3.11-slim";
            case JAVASCRIPT:
                return "node:18-slim";
            case CPP:
                return "gcc:latest";
            default:
                throw new CodeExecutionException("Unsupported language: " + language, language);
        }
    }
    
    /**
     * Get compile command for language (null if no compilation needed)
     */
    private String[] getCompileCommand(CodeExecutionRequest.Language language, String fileName) {
        switch (language) {
            case JAVA:
                return new String[]{"javac", fileName};
            case CPP:
                return new String[]{"g++", "-o", "main", fileName};
            case PYTHON:
            case JAVASCRIPT:
                return null; // No compilation needed
            default:
                return null;
        }
    }
    
    /**
     * Get run command for language
     */
    private String[] getRunCommand(CodeExecutionRequest.Language language) {
        switch (language) {
            case JAVA:
                return new String[]{"java", "Main"};
            case PYTHON:
                return new String[]{"python", "main.py"};
            case JAVASCRIPT:
                return new String[]{"node", "main.js"};
            case CPP:
                return new String[]{"./main"};
            default:
                throw new CodeExecutionException("Unsupported language: " + language, language);
        }
    }
    
    /**
     * Run Docker command with security restrictions
     */
    private CodeExecutionResponse runDockerCommand(Path executionDir, String image, 
                                                  String[] command, String stdin, int timeoutSeconds) {
        try {
            // Build Docker command with security restrictions
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(
                "docker", "run",
                "--rm",
                "--network=none", // No network access
                "--memory=" + memoryLimitMB + "m", // Memory limit
                "--cpus=0.5", // CPU limit
                "--user=nobody", // Run as non-root user
                "--read-only", // Read-only filesystem
                "--tmpfs=/tmp", // Temporary filesystem
                "-v", executionDir.toString() + ":/workspace",
                "-w", "/workspace",
                image
            );
            
            // Add the actual command
            for (String arg : command) {
                pb.command().add(arg);
            }
            
            pb.directory(executionDir.toFile());
            
            Process process = pb.start();
            
            // Handle stdin if provided
            if (stdin != null && !stdin.isEmpty()) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.println(stdin);
                    writer.flush();
                }
            }
            
            // Capture output with timeout
            Future<String> outputFuture = executorService.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });
            
            Future<String> errorFuture = executorService.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                    return error.toString();
                }
            });
            
            // Wait for process completion with timeout
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return CodeExecutionResponse.timeout();
            }
            
            String output = outputFuture.get(1, TimeUnit.SECONDS);
            String error = errorFuture.get(1, TimeUnit.SECONDS);
            
            if (process.exitValue() == 0) {
                return CodeExecutionResponse.success(output, 0);
            } else {
                return CodeExecutionResponse.runtimeError(error.isEmpty() ? output : error);
            }
            
        } catch (TimeoutException e) {
            return CodeExecutionResponse.timeout();
        } catch (Exception e) {
            logger.error("Error running Docker command", e);
            return CodeExecutionResponse.systemError(e.getMessage());
        }
    }
    
    /**
     * Clean up execution directory
     */
    private void cleanupExecutionDirectory(Path executionDir) {
        try {
            Files.walk(executionDir)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        logger.warn("Failed to delete temporary file: " + path, e);
                    }
                });
        } catch (IOException e) {
            logger.warn("Failed to clean up execution directory: " + executionDir, e);
        }
    }
    
    /**
     * Check if Docker is available
     */
    public boolean isDockerAvailable() {
        try {
            Process process = new ProcessBuilder("docker", "--version").start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get execution service status
     */
    public String getServiceStatus() {
        if (!executionEnabled) {
            return "Code execution is disabled";
        }
        
        if (!isDockerAvailable()) {
            return "Docker is not available";
        }
        
        return "Code execution service is ready";
    }
}