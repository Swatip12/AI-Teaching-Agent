import { Injectable, ErrorHandler } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Global error handler service for the AI Teaching Platform
 * Requirement 6.3: Error handling for code execution
 * Requirement 8.3: Network error handling
 */
@Injectable({
  providedIn: 'root'
})
export class GlobalErrorHandler implements ErrorHandler {

  constructor(private snackBar: MatSnackBar) {}

  handleError(error: any): void {
    console.error('Global error caught:', error);

    if (error instanceof HttpErrorResponse) {
      this.handleHttpError(error);
    } else if (error instanceof Error) {
      this.handleClientError(error);
    } else {
      this.handleUnknownError(error);
    }
  }

  private handleHttpError(error: HttpErrorResponse): void {
    let userMessage = 'An error occurred. Please try again.';
    
    switch (error.status) {
      case 0:
        userMessage = 'Unable to connect to the server. Please check your internet connection.';
        break;
      case 400:
        userMessage = this.extractErrorMessage(error) || 'Invalid request. Please check your input.';
        break;
      case 401:
        userMessage = 'Your session has expired. Please log in again.';
        // Could trigger automatic logout here
        break;
      case 403:
        userMessage = 'You do not have permission to perform this action.';
        break;
      case 404:
        userMessage = 'The requested resource was not found.';
        break;
      case 429:
        userMessage = 'Too many requests. Please wait a moment before trying again.';
        break;
      case 500:
        userMessage = 'Server error. Our team has been notified.';
        break;
      case 503:
        userMessage = 'Service temporarily unavailable. Please try again later.';
        break;
      default:
        userMessage = `An error occurred (${error.status}). Please try again.`;
    }

    this.showUserFriendlyMessage(userMessage, 'error');
  }

  private handleClientError(error: Error): void {
    let userMessage = 'An unexpected error occurred.';
    
    if (error.message.includes('ChunkLoadError') || error.message.includes('Loading chunk')) {
      userMessage = 'Application update detected. Please refresh the page.';
    } else if (error.message.includes('Network Error')) {
      userMessage = 'Network connection lost. Please check your internet connection.';
    } else if (error.message.includes('timeout')) {
      userMessage = 'Request timed out. Please try again.';
    }

    this.showUserFriendlyMessage(userMessage, 'error');
  }

  private handleUnknownError(error: any): void {
    console.error('Unknown error type:', error);
    this.showUserFriendlyMessage('An unexpected error occurred. Please refresh the page.', 'error');
  }

  private extractErrorMessage(error: HttpErrorResponse): string | null {
    if (error.error && typeof error.error === 'object') {
      return error.error.message || error.error.error || null;
    }
    return null;
  }

  private showUserFriendlyMessage(message: string, type: 'error' | 'warning' | 'info' = 'error'): void {
    this.snackBar.open(message, 'Dismiss', {
      duration: type === 'error' ? 8000 : 5000,
      panelClass: [`snackbar-${type}`],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }
}