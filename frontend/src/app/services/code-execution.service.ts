import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface CodeExecutionRequest {
  code: string;
  language: 'JAVA' | 'PYTHON' | 'JAVASCRIPT' | 'CPP';
  input?: string;
  stdin?: string;
  timeoutSeconds?: number;
}

export interface CodeExecutionResponse {
  success: boolean;
  output?: string;
  error?: string;
  compilationError?: string;
  status: 'SUCCESS' | 'COMPILATION_ERROR' | 'RUNTIME_ERROR' | 'TIMEOUT' | 'MEMORY_LIMIT_EXCEEDED' | 'SECURITY_VIOLATION' | 'SYSTEM_ERROR';
  executionTimeMs: number;
  memoryUsageMB: number;
  executedAt: string;
  language?: string;
}

export interface HintResponse {
  hint: string;
}

export interface ValidationResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class CodeExecutionService {
  private apiUrl = `${environment.apiUrl}/api/code`;

  constructor(private http: HttpClient) {}

  /**
   * Execute code in secure container
   * Requirements: 6.1, 6.2, 6.3, 6.4
   */
  executeCode(request: CodeExecutionRequest): Observable<CodeExecutionResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<CodeExecutionResponse>(`${this.apiUrl}/execute`, request, { headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Execute code with lesson context for additional validation
   * Requirements: 6.4, 6.5
   */
  executeCodeForLesson(lessonId: number, request: CodeExecutionRequest): Observable<CodeExecutionResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<CodeExecutionResponse>(`${this.apiUrl}/execute/lesson/${lessonId}`, request, { headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Validate code without executing it
   * Performs security checks and basic syntax validation
   */
  validateCode(request: CodeExecutionRequest): Observable<ValidationResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<ValidationResponse>(`${this.apiUrl}/validate`, request, { headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get execution hints for common errors
   * Requirements: 6.5
   */
  getExecutionHints(request: CodeExecutionRequest): Observable<HintResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<HintResponse>(`${this.apiUrl}/hints`, request, { headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get supported programming languages
   */
  getSupportedLanguages(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/languages`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get code execution service status
   */
  getServiceStatus(): Observable<{status: string, dockerAvailable: boolean}> {
    return this.http.get<{status: string, dockerAvailable: boolean}>(`${this.apiUrl}/status`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Format error messages for user display
   */
  formatErrorMessage(response: CodeExecutionResponse): string {
    switch (response.status) {
      case 'COMPILATION_ERROR':
        return this.formatCompilationError(response.compilationError || 'Compilation failed');
      case 'RUNTIME_ERROR':
        return this.formatRuntimeError(response.error || 'Runtime error occurred');
      case 'TIMEOUT':
        return 'Your code took too long to execute. Check for infinite loops or optimize your algorithm.';
      case 'MEMORY_LIMIT_EXCEEDED':
        return 'Your code used too much memory. Consider using more efficient data structures.';
      case 'SECURITY_VIOLATION':
        return 'Your code contains potentially unsafe operations. Please use only basic programming constructs.';
      case 'SYSTEM_ERROR':
        return 'A system error occurred. Please try again later.';
      default:
        return response.error || 'An unknown error occurred';
    }
  }

  /**
   * Format compilation errors for better readability
   */
  private formatCompilationError(error: string): string {
    // Extract the most relevant part of the error message
    const lines = error.split('\n');
    const relevantLines = lines.filter(line => 
      line.includes('error:') || 
      line.includes('cannot find symbol') ||
      line.includes('expected') ||
      line.includes('illegal')
    );

    if (relevantLines.length > 0) {
      return relevantLines[0].replace(/^.*error:\s*/, '').trim();
    }

    return error.length > 200 ? error.substring(0, 200) + '...' : error;
  }

  /**
   * Format runtime errors for better readability
   */
  private formatRuntimeError(error: string): string {
    // Extract the exception type and message
    const lines = error.split('\n');
    const exceptionLine = lines.find(line => 
      line.includes('Exception') || 
      line.includes('Error') ||
      line.includes('at ')
    );

    if (exceptionLine) {
      return exceptionLine.replace(/^\s*at\s+.*/, '').trim();
    }

    return error.length > 200 ? error.substring(0, 200) + '...' : error;
  }

  /**
   * Check if the execution was successful
   */
  isExecutionSuccessful(response: CodeExecutionResponse): boolean {
    return response.success && response.status === 'SUCCESS';
  }

  /**
   * Get execution time in a human-readable format
   */
  formatExecutionTime(timeMs: number): string {
    if (timeMs < 1000) {
      return `${timeMs}ms`;
    } else {
      return `${(timeMs / 1000).toFixed(2)}s`;
    }
  }

  /**
   * Get memory usage in a human-readable format
   */
  formatMemoryUsage(memoryMB: number): string {
    if (memoryMB < 1) {
      return `${(memoryMB * 1024).toFixed(0)}KB`;
    } else {
      return `${memoryMB.toFixed(1)}MB`;
    }
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    }
    
    console.error('CodeExecutionService Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}