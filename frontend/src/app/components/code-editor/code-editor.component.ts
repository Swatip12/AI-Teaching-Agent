import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { CodeExecutionService, CodeExecutionRequest, CodeExecutionResponse } from '../../services/code-execution.service';
import { Subscription } from 'rxjs';

export interface CodeEditorConfig {
  language: 'JAVA' | 'PYTHON' | 'JAVASCRIPT' | 'CPP';
  theme: 'vs-dark' | 'vs-light';
  readOnly: boolean;
  showMinimap: boolean;
  fontSize: number;
  wordWrap: 'on' | 'off';
}

export interface CodeValidationResult {
  isValid: boolean;
  expectedOutput?: string;
  actualOutput?: string;
  feedback?: string;
  hints?: string[];
}

@Component({
  selector: 'app-code-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    MatTooltipModule,
    MonacoEditorModule
  ],
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.scss']
})
export class CodeEditorComponent implements OnInit, OnDestroy {
  @Input() initialCode: string = '';
  @Input() language: 'JAVA' | 'PYTHON' | 'JAVASCRIPT' | 'CPP' = 'JAVA';
  @Input() lessonId?: number;
  @Input() expectedOutput?: string;
  @Input() readOnly: boolean = false;
  @Input() showRunButton: boolean = true;
  @Input() showHintsButton: boolean = true;
  @Input() showValidateButton: boolean = true;
  @Input() height: string = '400px';
  @Input() placeholder: string = 'Write your code here...';

  @Output() codeChange = new EventEmitter<string>();
  @Output() codeExecuted = new EventEmitter<CodeExecutionResponse>();
  @Output() validationResult = new EventEmitter<CodeValidationResult>();

  @ViewChild('monacoEditor', { static: false }) monacoEditor?: ElementRef;

  // Editor configuration
  editorOptions: any = {
    theme: 'vs-dark',
    language: 'java',
    automaticLayout: true,
    scrollBeyondLastLine: false,
    minimap: { enabled: true },
    fontSize: 14,
    wordWrap: 'on',
    lineNumbers: 'on',
    roundedSelection: false,
    scrollbar: {
      vertical: 'visible',
      horizontal: 'visible'
    },
    suggestOnTriggerCharacters: true,
    acceptSuggestionOnEnter: 'on',
    tabCompletion: 'on',
    wordBasedSuggestions: true,
    parameterHints: { enabled: true },
    autoIndent: 'full',
    formatOnPaste: true,
    formatOnType: true
  };

  // Component state
  code: string = '';
  isExecuting: boolean = false;
  isValidating: boolean = false;
  isGettingHints: boolean = false;
  executionResult?: CodeExecutionResponse;
  currentHints: string[] = [];
  supportedLanguages: string[] = ['JAVA', 'PYTHON', 'JAVASCRIPT', 'CPP'];
  
  // Input for program
  programInput: string = '';
  showInputField: boolean = false;

  private subscriptions: Subscription = new Subscription();

  constructor(
    private codeExecutionService: CodeExecutionService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.code = this.initialCode;
    this.updateEditorLanguage();
    this.loadSupportedLanguages();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   * Handle code changes in the editor
   * Requirements: 6.1
   */
  onCodeChange(code: string): void {
    this.code = code;
    this.codeChange.emit(code);
    
    // Clear previous execution results when code changes
    this.executionResult = undefined;
    this.currentHints = [];
  }

  /**
   * Execute the code
   * Requirements: 6.1, 6.2, 6.3
   */
  executeCode(): void {
    if (!this.code.trim()) {
      this.showMessage('Please enter some code to execute', 'error');
      return;
    }

    this.isExecuting = true;
    this.executionResult = undefined;
    this.currentHints = [];

    const request: CodeExecutionRequest = {
      code: this.code,
      language: this.language,
      stdin: this.programInput || undefined,
      timeoutSeconds: 10
    };

    const executeObservable = this.lessonId 
      ? this.codeExecutionService.executeCodeForLesson(this.lessonId, request)
      : this.codeExecutionService.executeCode(request);

    const subscription = executeObservable.subscribe({
      next: (response: CodeExecutionResponse) => {
        this.isExecuting = false;
        this.executionResult = response;
        this.codeExecuted.emit(response);
        
        if (this.codeExecutionService.isExecutionSuccessful(response)) {
          this.showMessage('Code executed successfully!', 'success');
          this.validateSolution(response);
        } else {
          this.showMessage('Code execution failed', 'error');
        }
      },
      error: (error) => {
        this.isExecuting = false;
        this.showMessage(`Execution error: ${error.message}`, 'error');
        console.error('Code execution error:', error);
      }
    });

    this.subscriptions.add(subscription);
  }

  /**
   * Validate code without executing
   * Requirements: 6.3
   */
  validateCode(): void {
    if (!this.code.trim()) {
      this.showMessage('Please enter some code to validate', 'error');
      return;
    }

    this.isValidating = true;

    const request: CodeExecutionRequest = {
      code: this.code,
      language: this.language
    };

    const subscription = this.codeExecutionService.validateCode(request).subscribe({
      next: (response) => {
        this.isValidating = false;
        this.showMessage(response.message, 'success');
      },
      error: (error) => {
        this.isValidating = false;
        this.showMessage(`Validation error: ${error.message}`, 'error');
      }
    });

    this.subscriptions.add(subscription);
  }

  /**
   * Get hints for the current code
   * Requirements: 6.5
   */
  getHints(): void {
    if (!this.code.trim()) {
      this.showMessage('Please enter some code to get hints', 'error');
      return;
    }

    this.isGettingHints = true;
    this.currentHints = [];

    const request: CodeExecutionRequest = {
      code: this.code,
      language: this.language
    };

    const subscription = this.codeExecutionService.getExecutionHints(request).subscribe({
      next: (response) => {
        this.isGettingHints = false;
        this.currentHints = [response.hint];
        this.showMessage('Hints generated!', 'success');
      },
      error: (error) => {
        this.isGettingHints = false;
        this.showMessage(`Error getting hints: ${error.message}`, 'error');
      }
    });

    this.subscriptions.add(subscription);
  }

  /**
   * Validate solution against expected output
   * Requirements: 6.4
   */
  private validateSolution(response: CodeExecutionResponse): void {
    if (!this.expectedOutput) {
      return;
    }

    const actualOutput = response.output?.trim() || '';
    const expectedOutput = this.expectedOutput.trim();
    
    const validationResult: CodeValidationResult = {
      isValid: actualOutput === expectedOutput,
      expectedOutput: expectedOutput,
      actualOutput: actualOutput,
      feedback: actualOutput === expectedOutput 
        ? 'Great job! Your solution produces the correct output.' 
        : 'Your solution doesn\'t match the expected output. Check your logic and try again.',
      hints: actualOutput === expectedOutput ? [] : this.generateValidationHints(actualOutput, expectedOutput)
    };

    this.validationResult.emit(validationResult);
  }

  /**
   * Generate hints based on output comparison
   */
  private generateValidationHints(actual: string, expected: string): string[] {
    const hints: string[] = [];

    if (actual.length === 0 && expected.length > 0) {
      hints.push('Your code doesn\'t produce any output. Make sure you\'re printing the result.');
    } else if (actual.length > expected.length) {
      hints.push('Your output is longer than expected. Check if you\'re printing extra information.');
    } else if (actual.length < expected.length) {
      hints.push('Your output is shorter than expected. Make sure you\'re printing all required information.');
    } else if (actual !== expected) {
      hints.push('Your output format doesn\'t match exactly. Check spacing, capitalization, and punctuation.');
    }

    return hints;
  }

  /**
   * Change programming language
   */
  onLanguageChange(): void {
    this.updateEditorLanguage();
    this.executionResult = undefined;
    this.currentHints = [];
  }

  /**
   * Update Monaco Editor language configuration
   */
  private updateEditorLanguage(): void {
    const languageMap: { [key: string]: string } = {
      'JAVA': 'java',
      'PYTHON': 'python',
      'JAVASCRIPT': 'javascript',
      'CPP': 'cpp'
    };

    this.editorOptions = {
      ...this.editorOptions,
      language: languageMap[this.language] || 'java'
    };
  }

  /**
   * Load supported languages from backend
   */
  private loadSupportedLanguages(): void {
    const subscription = this.codeExecutionService.getSupportedLanguages().subscribe({
      next: (languages) => {
        this.supportedLanguages = languages;
      },
      error: (error) => {
        console.warn('Could not load supported languages:', error);
        // Keep default languages
      }
    });

    this.subscriptions.add(subscription);
  }

  /**
   * Toggle input field visibility
   */
  toggleInputField(): void {
    this.showInputField = !this.showInputField;
  }

  /**
   * Clear the editor
   */
  clearCode(): void {
    this.code = '';
    this.executionResult = undefined;
    this.currentHints = [];
    this.programInput = '';
  }

  /**
   * Copy code to clipboard
   */
  copyCode(): void {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(this.code).then(() => {
        this.showMessage('Code copied to clipboard!', 'success');
      }).catch(() => {
        this.showMessage('Failed to copy code', 'error');
      });
    }
  }

  /**
   * Get formatted execution time
   */
  getFormattedExecutionTime(): string {
    if (!this.executionResult) return '';
    return this.codeExecutionService.formatExecutionTime(this.executionResult.executionTimeMs);
  }

  /**
   * Get formatted memory usage
   */
  getFormattedMemoryUsage(): string {
    if (!this.executionResult) return '';
    return this.codeExecutionService.formatMemoryUsage(this.executionResult.memoryUsageMB);
  }

  /**
   * Get formatted error message
   */
  getFormattedErrorMessage(): string {
    if (!this.executionResult) return '';
    return this.codeExecutionService.formatErrorMessage(this.executionResult);
  }

  /**
   * Check if execution was successful
   */
  isExecutionSuccessful(): boolean {
    if (!this.executionResult) return false;
    return this.codeExecutionService.isExecutionSuccessful(this.executionResult);
  }

  /**
   * Show snackbar message
   */
  private showMessage(message: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.snackBar.open(message, 'Close', {
      duration: type === 'error' ? 5000 : 3000,
      panelClass: [`snackbar-${type}`]
    });
  }
}