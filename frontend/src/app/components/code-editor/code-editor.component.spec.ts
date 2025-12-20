import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CodeEditorComponent } from './code-editor.component';
import { CodeExecutionService } from '../../services/code-execution.service';
import { of, throwError } from 'rxjs';
import { NGX_MONACO_EDITOR_CONFIG } from 'ngx-monaco-editor-v2';

describe('CodeEditorComponent', () => {
  let component: CodeEditorComponent;
  let fixture: ComponentFixture<CodeEditorComponent>;
  let codeExecutionService: jasmine.SpyObj<CodeExecutionService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CodeExecutionService', [
      'executeCode',
      'executeCodeForLesson',
      'validateCode',
      'getExecutionHints',
      'getSupportedLanguages',
      'formatErrorMessage',
      'isExecutionSuccessful',
      'formatExecutionTime',
      'formatMemoryUsage'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        CodeEditorComponent,
        HttpClientTestingModule,
        MatSnackBarModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: CodeExecutionService, useValue: spy },
        { provide: NGX_MONACO_EDITOR_CONFIG, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CodeEditorComponent);
    component = fixture.componentInstance;
    codeExecutionService = TestBed.inject(CodeExecutionService) as jasmine.SpyObj<CodeExecutionService>;
  });

  beforeEach(() => {
    // Setup default spy returns
    codeExecutionService.getSupportedLanguages.and.returnValue(of(['JAVA', 'PYTHON', 'JAVASCRIPT', 'CPP']));
    codeExecutionService.isExecutionSuccessful.and.returnValue(true);
    codeExecutionService.formatExecutionTime.and.returnValue('100ms');
    codeExecutionService.formatMemoryUsage.and.returnValue('10MB');
    codeExecutionService.formatErrorMessage.and.returnValue('Test error');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.language).toBe('JAVA');
    expect(component.readOnly).toBe(false);
    expect(component.showRunButton).toBe(true);
    expect(component.showHintsButton).toBe(true);
    expect(component.showValidateButton).toBe(true);
    expect(component.height).toBe('400px');
  });

  it('should handle code changes', () => {
    spyOn(component.codeChange, 'emit');
    const testCode = 'public class Test {}';
    
    component.onCodeChange(testCode);
    
    expect(component.code).toBe(testCode);
    expect(component.codeChange.emit).toHaveBeenCalledWith(testCode);
    expect(component.executionResult).toBeUndefined();
    expect(component.currentHints).toEqual([]);
  });

  it('should execute code successfully', () => {
    const mockResponse = {
      success: true,
      output: 'Hello World',
      status: 'SUCCESS' as const,
      executionTimeMs: 100,
      memoryUsageMB: 10,
      executedAt: '2023-01-01T00:00:00Z'
    };
    
    codeExecutionService.executeCode.and.returnValue(of(mockResponse));
    spyOn(component.codeExecuted, 'emit');
    
    component.code = 'System.out.println("Hello World");';
    component.executeCode();
    
    expect(codeExecutionService.executeCode).toHaveBeenCalledWith({
      code: 'System.out.println("Hello World");',
      language: 'JAVA',
      stdin: undefined,
      timeoutSeconds: 10
    });
    expect(component.executionResult).toBe(mockResponse);
    expect(component.codeExecuted.emit).toHaveBeenCalledWith(mockResponse);
  });

  it('should execute code for lesson when lessonId is provided', () => {
    const mockResponse = {
      success: true,
      output: 'Hello World',
      status: 'SUCCESS' as const,
      executionTimeMs: 100,
      memoryUsageMB: 10,
      executedAt: '2023-01-01T00:00:00Z'
    };
    
    component.lessonId = 123;
    codeExecutionService.executeCodeForLesson.and.returnValue(of(mockResponse));
    
    component.code = 'System.out.println("Hello World");';
    component.executeCode();
    
    expect(codeExecutionService.executeCodeForLesson).toHaveBeenCalledWith(123, {
      code: 'System.out.println("Hello World");',
      language: 'JAVA',
      stdin: undefined,
      timeoutSeconds: 10
    });
  });

  it('should handle execution errors', () => {
    codeExecutionService.executeCode.and.returnValue(throwError(() => new Error('Execution failed')));
    
    component.code = 'invalid code';
    component.executeCode();
    
    expect(component.isExecuting).toBe(false);
    expect(component.executionResult).toBeUndefined();
  });

  it('should validate code', () => {
    const mockResponse = { message: 'Code validation passed' };
    codeExecutionService.validateCode.and.returnValue(of(mockResponse));
    
    component.code = 'public class Test {}';
    component.validateCode();
    
    expect(codeExecutionService.validateCode).toHaveBeenCalledWith({
      code: 'public class Test {}',
      language: 'JAVA'
    });
  });

  it('should get hints', () => {
    const mockResponse = { hint: 'Check your syntax' };
    codeExecutionService.getExecutionHints.and.returnValue(of(mockResponse));
    
    component.code = 'public class Test {';
    component.getHints();
    
    expect(codeExecutionService.getExecutionHints).toHaveBeenCalledWith({
      code: 'public class Test {',
      language: 'JAVA'
    });
    expect(component.currentHints).toEqual(['Check your syntax']);
  });

  it('should prevent execution with empty code', () => {
    component.code = '';
    component.executeCode();
    
    expect(codeExecutionService.executeCode).not.toHaveBeenCalled();
  });

  it('should prevent validation with empty code', () => {
    component.code = '';
    component.validateCode();
    
    expect(codeExecutionService.validateCode).not.toHaveBeenCalled();
  });

  it('should prevent getting hints with empty code', () => {
    component.code = '';
    component.getHints();
    
    expect(codeExecutionService.getExecutionHints).not.toHaveBeenCalled();
  });

  it('should update editor language when language changes', () => {
    component.language = 'PYTHON';
    component.onLanguageChange();
    
    expect(component.editorOptions.language).toBe('python');
    expect(component.executionResult).toBeUndefined();
    expect(component.currentHints).toEqual([]);
  });

  it('should toggle input field visibility', () => {
    expect(component.showInputField).toBe(false);
    
    component.toggleInputField();
    expect(component.showInputField).toBe(true);
    
    component.toggleInputField();
    expect(component.showInputField).toBe(false);
  });

  it('should clear code and results', () => {
    component.code = 'test code';
    component.executionResult = {
      success: true,
      output: 'test',
      status: 'SUCCESS' as const,
      executionTimeMs: 100,
      memoryUsageMB: 10,
      executedAt: '2023-01-01T00:00:00Z'
    };
    component.currentHints = ['test hint'];
    component.programInput = 'test input';
    
    component.clearCode();
    
    expect(component.code).toBe('');
    expect(component.executionResult).toBeUndefined();
    expect(component.currentHints).toEqual([]);
    expect(component.programInput).toBe('');
  });

  it('should validate solution against expected output', () => {
    const mockResponse = {
      success: true,
      output: 'Hello World',
      status: 'SUCCESS' as const,
      executionTimeMs: 100,
      memoryUsageMB: 10,
      executedAt: '2023-01-01T00:00:00Z'
    };
    
    component.expectedOutput = 'Hello World';
    codeExecutionService.executeCode.and.returnValue(of(mockResponse));
    spyOn(component.validationResult, 'emit');
    
    component.code = 'System.out.println("Hello World");';
    component.executeCode();
    
    expect(component.validationResult.emit).toHaveBeenCalledWith({
      isValid: true,
      expectedOutput: 'Hello World',
      actualOutput: 'Hello World',
      feedback: 'Great job! Your solution produces the correct output.',
      hints: []
    });
  });

  it('should handle validation failure with mismatched output', () => {
    const mockResponse = {
      success: true,
      output: 'Hello Universe',
      status: 'SUCCESS' as const,
      executionTimeMs: 100,
      memoryUsageMB: 10,
      executedAt: '2023-01-01T00:00:00Z'
    };
    
    component.expectedOutput = 'Hello World';
    codeExecutionService.executeCode.and.returnValue(of(mockResponse));
    spyOn(component.validationResult, 'emit');
    
    component.code = 'System.out.println("Hello Universe");';
    component.executeCode();
    
    expect(component.validationResult.emit).toHaveBeenCalledWith({
      isValid: false,
      expectedOutput: 'Hello World',
      actualOutput: 'Hello Universe',
      feedback: 'Your solution doesn\'t match the expected output. Check your logic and try again.',
      hints: jasmine.any(Array)
    });
  });
});