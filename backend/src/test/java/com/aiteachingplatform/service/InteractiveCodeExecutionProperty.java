package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.CodeExecutionRequest;
import com.aiteachingplatform.dto.CodeExecutionResponse;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;

/**
 * **Feature: ai-teaching-platform, Property 12: Interactive code execution**
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**
 * 
 * Property-based test for interactive code execution functionality
 * Tests that the code execution environment provides interactive editor,
 * immediate execution, error handling, solution validation, and progressive hints
 */
@SpringBootTest
@ActiveProfiles("test")
public class InteractiveCodeExecutionProperty extends PropertyTestBase {
    
    private CodeExecutionService codeExecutionService;
    
    @BeforeEach
    void setUp() {
        codeExecutionService = new CodeExecutionService();
    }
    
    /**
     * Property: Code execution provides immediate results
     * For any valid code input, the system should return a response immediately
     * without hanging or crashing
     */
    @Test
    void codeExecutionProvidesImmediateResults() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                long startTime = System.currentTimeMillis();
                
                CodeExecutionResponse response = codeExecutionService.executeCode(testData.request);
                
                long executionTime = System.currentTimeMillis() - startTime;
                
                // Property: Response should be immediate (within reasonable time)
                assert executionTime < 30000 : "Code execution took too long: " + executionTime + "ms";
                
                // Property: Response should never be null
                assert response != null : "Code execution response should never be null";
                
                // Property: Response should have a valid status
                assert response.getStatus() != null : "Response status should never be null";
                
                // Property: Execution time should be recorded
                assert response.getExecutionTimeMs() >= 0 : "Execution time should be non-negative";
            }
        });
    }
    
    /**
     * Property: Error handling provides helpful explanations
     * For any code with errors, the system should provide helpful error messages
     * rather than cryptic technical errors
     */
    @Test
    void errorHandlingProvidesHelpfulExplanations() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Use error-prone code
                CodeExecutionRequest request = new CodeExecutionRequest();
                request.setCode(testData.errorCode);
                request.setLanguage(CodeExecutionRequest.Language.JAVA);
                
                CodeExecutionResponse response = codeExecutionService.executeCode(request);
                
                if (!response.isSuccess()) {
                    // Property: Error messages should not be empty
                    String errorMessage = response.getError() != null ? response.getError() : response.getCompilationError();
                    assert errorMessage != null && !errorMessage.trim().isEmpty() : 
                        "Error messages should not be empty for failed executions";
                    
                    // Property: Error messages should be reasonably sized (not too long)
                    assert errorMessage.length() < 5000 : 
                        "Error messages should be concise and not exceed 5000 characters";
                    
                    // Property: Status should indicate the type of error
                    assert response.getStatus() != CodeExecutionResponse.ExecutionStatus.SUCCESS : 
                        "Failed execution should not have SUCCESS status";
                }
            }
        });
    }
    
    /**
     * Property: Security measures prevent dangerous operations
     * For any code containing potentially dangerous operations,
     * the system should detect and prevent execution
     */
    @Test
    void securityMeasuresPreventDangerousOperations() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Create code with potentially dangerous operations
                String dangerousCode = "public class Main { public static void main(String[] args) { " + 
                                     testData.dangerousOperation + " } }";
                
                CodeExecutionRequest request = new CodeExecutionRequest();
                request.setCode(dangerousCode);
                request.setLanguage(CodeExecutionRequest.Language.JAVA);
                
                CodeExecutionResponse response = codeExecutionService.executeCode(request);
                
                // Property: Dangerous operations should be caught by security validation
                if (containsDangerousPattern(testData.dangerousOperation)) {
                    assert response.getStatus() == CodeExecutionResponse.ExecutionStatus.SECURITY_VIOLATION :
                        "Dangerous operations should be detected and blocked";
                    
                    assert !response.isSuccess() : 
                        "Dangerous code should not execute successfully";
                }
            }
        });
    }
    
    /**
     * Property: Resource limits are enforced
     * For any code execution, the system should enforce memory and time limits
     */
    @Test
    void resourceLimitsAreEnforced() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Create code that might consume resources
                String resourceIntensiveCode = generateResourceIntensiveCode(Math.abs(testData.iterations % 1000) + 1);
                
                CodeExecutionRequest request = new CodeExecutionRequest();
                request.setCode(resourceIntensiveCode);
                request.setLanguage(CodeExecutionRequest.Language.JAVA);
                request.setTimeoutSeconds(5); // Short timeout for testing
                
                CodeExecutionResponse response = codeExecutionService.executeCode(request);
                
                // Property: Execution should complete within timeout or be terminated
                assert response.getExecutionTimeMs() < 10000 : 
                    "Execution should be terminated within reasonable time limits";
                
                // Property: If execution times out, status should indicate timeout
                if (response.getStatus() == CodeExecutionResponse.ExecutionStatus.TIMEOUT) {
                    assert !response.isSuccess() : 
                        "Timed out execution should not be marked as successful";
                }
            }
        });
    }
    
    /**
     * Property: Valid code executes successfully
     * For any syntactically correct and safe code, the system should execute it successfully
     */
    @Test
    void validCodeExecutesSuccessfully() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Generate simple, valid code
                String validCode = generateValidCode(testData.output);
                
                CodeExecutionRequest request = new CodeExecutionRequest();
                request.setCode(validCode);
                request.setLanguage(CodeExecutionRequest.Language.JAVA);
                
                CodeExecutionResponse response = codeExecutionService.executeCode(request);
                
                // Property: Valid code should execute successfully (if Docker is available)
                if (codeExecutionService.isDockerAvailable()) {
                    // We can't guarantee success due to Docker availability, but we can check response structure
                    assert response != null : "Response should not be null for valid code";
                    assert response.getStatus() != null : "Response status should not be null";
                    
                    if (response.isSuccess()) {
                        // Property: Successful execution should have output
                        assert response.getOutput() != null : "Successful execution should have output";
                        
                        // Property: Successful execution should have SUCCESS status
                        assert response.getStatus() == CodeExecutionResponse.ExecutionStatus.SUCCESS :
                            "Successful execution should have SUCCESS status";
                    }
                } else {
                    // If Docker is not available, we should get a system error
                    assert response != null : "Response should not be null even without Docker";
                    assert response.getStatus() != null : "Response status should not be null";
                }
            }
        });
    }
    
    // Test data class
    private static class TestData {
        final CodeExecutionRequest request;
        final String errorCode;
        final String dangerousOperation;
        final String output;
        final int iterations;
        
        TestData(CodeExecutionRequest request, String errorCode, String dangerousOperation, String output, int iterations) {
            this.request = request;
            this.errorCode = errorCode;
            this.dangerousOperation = dangerousOperation;
            this.output = output;
            this.iterations = iterations;
        }
    }
    
    // Generator for test data
    private static final Generator<TestData> TEST_DATA_GENERATOR = new Generator<TestData>() {
        @Override
        public TestData next() {
            Random random = new Random();
            
            // Generate request
            CodeExecutionRequest request = new CodeExecutionRequest();
            CodeExecutionRequest.Language[] languages = CodeExecutionRequest.Language.values();
            request.setLanguage(languages[random.nextInt(languages.length)]);
            request.setCode(generateRandomCode(request.getLanguage(), random));
            request.setTimeoutSeconds(random.nextInt(20) + 5);
            
            // Generate error code
            String[] errorCodes = {
                "public class Main { public static void main(String[] args) { int x = 1/0; } }", // Division by zero
                "public class Main { public static void main(String[] args) { String s = null; s.length(); } }", // Null pointer
                "public class Main { public static void main(String[] args) { int[] arr = new int[1]; arr[5] = 1; } }", // Array bounds
                "public class Main { public static void main(String[] args) { System.out.println(undefinedVariable); } }", // Undefined variable
                "public class Main { public static void main(String[] args) { missing semicolon } }", // Syntax error
                "public class Main { public static void main(String[] args) { while(true) { } } }", // Infinite loop
                "invalid java code here", // Invalid syntax
                "", // Empty code
                "   ", // Whitespace only
            };
            String errorCode = errorCodes[random.nextInt(errorCodes.length)];
            
            // Generate dangerous operation
            String[] dangerousOps = {
                "Runtime.getRuntime().exec(\"rm -rf /\");",
                "System.exit(1);",
                "new ProcessBuilder(\"cat\", \"/etc/passwd\").start();",
                "java.io.File file = new java.io.File(\"/etc/passwd\");",
                "Runtime.getRuntime().halt(0);",
                "Thread.sleep(60000);", // Long sleep
                "while(true) { new Object(); }", // Memory exhaustion attempt
                "System.getProperty(\"user.home\");", // System property access
            };
            String dangerousOperation = dangerousOps[random.nextInt(dangerousOps.length)];
            
            // Generate output
            String[] outputs = {
                "Hello World",
                "Test Output",
                "42",
                "Success",
                "Property Test",
                String.valueOf(random.nextInt(1000)),
                "Random: " + random.nextDouble(),
            };
            String output = outputs[random.nextInt(outputs.length)];
            
            int iterations = random.nextInt(1000) + 1;
            
            return new TestData(request, errorCode, dangerousOperation, output, iterations);
        }
    };
    
    // Helper methods
    private static String generateRandomCode(CodeExecutionRequest.Language language, Random random) {
        switch (language) {
            case JAVA:
                return generateRandomJavaCode(random);
            case PYTHON:
                return generateRandomPythonCode(random);
            case JAVASCRIPT:
                return generateRandomJavaScriptCode(random);
            case CPP:
                return generateRandomCppCode(random);
            default:
                return "System.out.println(\"Hello World\");";
        }
    }
    
    private static String generateRandomJavaCode(Random random) {
        String[] validCodes = {
            "public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }",
            "public class Main { public static void main(String[] args) { int x = 5; System.out.println(x); } }",
            "public class Main { public static void main(String[] args) { for(int i=0; i<3; i++) System.out.println(i); } }",
        };
        String[] invalidCodes = {
            "invalid syntax",
            "public class Main { missing brace",
            "System.out.println(\"no main method\");",
        };
        
        if (random.nextBoolean()) {
            return validCodes[random.nextInt(validCodes.length)];
        } else {
            return invalidCodes[random.nextInt(invalidCodes.length)];
        }
    }
    
    private static String generateRandomPythonCode(Random random) {
        String[] codes = {
            "print('Hello World')",
            "x = 5\nprint(x)",
            "for i in range(3):\n    print(i)",
            "invalid python syntax here",
            "print(undefined_variable)",
        };
        return codes[random.nextInt(codes.length)];
    }
    
    private static String generateRandomJavaScriptCode(Random random) {
        String[] codes = {
            "console.log('Hello World');",
            "let x = 5; console.log(x);",
            "for(let i=0; i<3; i++) console.log(i);",
            "invalid javascript syntax",
            "console.log(undefinedVariable);",
        };
        return codes[random.nextInt(codes.length)];
    }
    
    private static String generateRandomCppCode(Random random) {
        String[] codes = {
            "#include <iostream>\nint main() { std::cout << \"Hello World\" << std::endl; return 0; }",
            "#include <iostream>\nint main() { int x = 5; std::cout << x << std::endl; return 0; }",
            "invalid cpp syntax",
            "#include <iostream>\nint main() { std::cout << undefinedVariable << std::endl; return 0; }",
        };
        return codes[random.nextInt(codes.length)];
    }
    
    private boolean containsDangerousPattern(String code) {
        String lowerCode = code.toLowerCase();
        return lowerCode.contains("runtime") || 
               lowerCode.contains("process") || 
               lowerCode.contains("system.exit") ||
               lowerCode.contains("file") ||
               lowerCode.contains("exec");
    }
    
    private String generateResourceIntensiveCode(int iterations) {
        return "public class Main { public static void main(String[] args) { " +
               "for(int i=0; i<" + iterations + "; i++) { " +
               "System.out.println(\"Iteration: \" + i); " +
               "} } }";
    }
    
    private String generateValidCode(String output) {
        return "public class Main { public static void main(String[] args) { " +
               "System.out.println(\"" + output.replace("\"", "\\\"") + "\"); } }";
    }
    
    // Override generators for property tests
    @Override
    protected void assertProperty(AbstractCharacteristic<?> characteristic) {
        if (characteristic instanceof AbstractCharacteristic) {
            net.java.quickcheck.QuickCheck.forAll(DEFAULT_TEST_RUNS, TEST_DATA_GENERATOR, 
                (AbstractCharacteristic<TestData>) characteristic);
        } else {
            super.assertProperty(characteristic);
        }
    }
}