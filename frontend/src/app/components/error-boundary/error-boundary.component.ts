import { Component, Input, OnInit } from '@angular/core';

/**
 * Error boundary component for displaying user-friendly error messages
 * Requirement 6.3: Error handling for code execution
 * Requirement 8.3: Network error handling
 */
@Component({
  selector: 'app-error-boundary',
  templateUrl: './error-boundary.component.html',
  styleUrls: ['./error-boundary.component.scss']
})
export class ErrorBoundaryComponent implements OnInit {
  @Input() error: Error | null = null;
  @Input() errorMessage: string = '';
  @Input() showRetry: boolean = true;
  @Input() showDetails: boolean = false;

  constructor() {}

  ngOnInit(): void {}

  onRetry(): void {
    // Reload the current page
    window.location.reload();
  }

  onGoHome(): void {
    window.location.href = '/dashboard';
  }

  toggleDetails(): void {
    this.showDetails = !this.showDetails;
  }

  getErrorMessage(): string {
    if (this.errorMessage) {
      return this.errorMessage;
    }
    
    if (this.error) {
      if (this.error.message.includes('ChunkLoadError')) {
        return 'Application update detected. Please refresh the page to continue.';
      } else if (this.error.message.includes('Network Error')) {
        return 'Network connection lost. Please check your internet connection and try again.';
      } else if (this.error.message.includes('timeout')) {
        return 'Request timed out. Please try again.';
      }
      return this.error.message;
    }
    
    return 'An unexpected error occurred. Please try again.';
  }

  getErrorTitle(): string {
    if (this.error?.message.includes('ChunkLoadError')) {
      return 'Update Available';
    } else if (this.error?.message.includes('Network Error')) {
      return 'Connection Problem';
    }
    return 'Something went wrong';
  }
}