import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, timer } from 'rxjs';
import { catchError, retry, retryWhen, mergeMap, finalize } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

/**
 * HTTP Error Interceptor for handling API errors
 * Requirement 6.3: Error handling for code execution
 * Requirement 8.3: Network error handling
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  
  private readonly MAX_RETRIES = 3;
  private readonly RETRY_DELAY = 1000; // milliseconds

  constructor(
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      retryWhen(errors => this.handleRetry(errors)),
      catchError((error: HttpErrorResponse) => this.handleError(error, req))
    );
  }

  private handleRetry(errors: Observable<any>): Observable<any> {
    return errors.pipe(
      mergeMap((error, index) => {
        // Only retry on specific error codes
        if (this.shouldRetry(error) && index < this.MAX_RETRIES) {
          const delayTime = this.RETRY_DELAY * Math.pow(2, index); // Exponential backoff
          console.log(`Retrying request (attempt ${index + 1}/${this.MAX_RETRIES}) after ${delayTime}ms`);
          return timer(delayTime);
        }
        return throwError(() => error);
      })
    );
  }

  private shouldRetry(error: HttpErrorResponse): boolean {
    // Retry on network errors and specific HTTP status codes
    return error.status === 0 || // Network error
           error.status === 408 || // Request timeout
           error.status === 429 || // Too many requests
           error.status === 500 || // Internal server error
           error.status === 502 || // Bad gateway
           error.status === 503 || // Service unavailable
           error.status === 504;   // Gateway timeout
  }

  private handleError(error: HttpErrorResponse, req: HttpRequest<any>): Observable<never> {
    console.error('HTTP Error:', error);

    // Handle specific error scenarios
    if (error.status === 401) {
      this.handleUnauthorized();
    } else if (error.status === 403) {
      this.handleForbidden();
    } else if (error.status === 0) {
      this.handleNetworkError();
    } else if (error.status >= 500) {
      this.handleServerError(error);
    } else if (error.status === 400) {
      this.handleBadRequest(error);
    }

    return throwError(() => error);
  }

  private handleUnauthorized(): void {
    this.showError('Your session has expired. Please log in again.');
    // Clear local storage and redirect to login
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  private handleForbidden(): void {
    this.showError('You do not have permission to access this resource.');
  }

  private handleNetworkError(): void {
    this.showError('Unable to connect to the server. Please check your internet connection.');
  }

  private handleServerError(error: HttpErrorResponse): void {
    const message = this.extractErrorMessage(error) || 
                   'A server error occurred. Please try again later.';
    this.showError(message);
  }

  private handleBadRequest(error: HttpErrorResponse): void {
    const message = this.extractErrorMessage(error) || 
                   'Invalid request. Please check your input.';
    this.showError(message);
  }

  private extractErrorMessage(error: HttpErrorResponse): string | null {
    if (error.error && typeof error.error === 'object') {
      return error.error.message || error.error.error || null;
    }
    return null;
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      duration: 6000,
      panelClass: ['snackbar-error'],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }
}
