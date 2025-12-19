package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.CodeExecutionRequest;
import com.aiteachingplatform.dto.CodeExecutionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for CodeExecutionService
 * Tests core functionality without requiring Docker
 */
public class CodeExecutionServiceTest {
    
    private CodeExecutionService codeExecutionService;
    
    @BeforeEach
    void setUp() {
        codeExecutionService = new CodeExecutionService();
    }
    
    @Test
    void testServiceInitialization() {
        assertNotNull(codeExecutionService);
        assertNotNull(codeExecutionService.getServiceStatus());
    }
    
    @Test
    void testSecurityValidation() {
        // Test that dangerous code is detected
        CodeExecutionRequest dangerousRequest = new CodeExecutionRequest();
        dangerousRequest.setCode("public class Main { public static void main(String[] args) { Runtime.getRuntime().exec(\"rm -rf /\"); } }");
        dangerousRequest.setLanguage(CodeExecutionRequest.Language.JAVA);
        
        CodeExecutionResponse response = codeExecutionService.executeCode(dangerousRequest);
        
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(CodeExecutionResponse.ExecutionStatus.SECURITY_VIOLATION, response.getStatus());
    }
    
    @Test
    void testEmptyCodeHandling() {
        CodeExecutionRequest emptyRequest = new CodeExecutionRequest();
        emptyRequest.setCode("");
        emptyRequest.setLanguage(CodeExecutionRequest.Language.JAVA);
        
        CodeExecutionResponse response = codeExecutionService.executeCode(emptyRequest);
        
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(CodeExecutionResponse.ExecutionStatus.SECURITY_VIOLATION, response.getStatus());
    }
    
    @Test
    void testLongCodeHandling() {
        // Create code that exceeds length limit
        StringBuilder longCode = new StringBuilder();
        for (int i = 0; i < 10001; i++) {
            longCode.append("a");
        }
        
        CodeExecutionRequest longRequest = new CodeExecutionRequest();
        longRequest.setCode(longCode.toString());
        longRequest.setLanguage(CodeExecutionRequest.Language.JAVA);
        
        CodeExecutionResponse response = codeExecutionService.executeCode(longRequest);
        
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(CodeExecutionResponse.ExecutionStatus.SECURITY_VIOLATION, response.getStatus());
    }
    
    @Test
    void testResponseStructure() {
        CodeExecutionRequest request = new CodeExecutionRequest();
        request.setCode("System.out.println(\"Hello World\");");
        request.setLanguage(CodeExecutionRequest.Language.JAVA);
        
        CodeExecutionResponse response = codeExecutionService.executeCode(request);
        
        // Basic response structure validation
        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertTrue(response.getExecutionTimeMs() >= 0);
        assertNotNull(response.getExecutedAt());
    }
    
    @Test
    void testDockerAvailabilityCheck() {
        // This test just verifies the method doesn't crash
        boolean dockerAvailable = codeExecutionService.isDockerAvailable();
        // We can't assert the result since Docker may or may not be available
        // But we can verify the method completes without exception
        assertTrue(true); // If we get here, the method didn't throw an exception
    }
}