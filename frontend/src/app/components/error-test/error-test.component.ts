import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Test component for demonstrating error handling
 * This component is for testing purposes only
 */
@Component({
  selector: 'app-error-test',
  template: `
    <div class="error-test-container">
      <h3>Error Handling Test</h3>
      <div class="test-buttons">
        <button mat-raised-button color="warn" (click)="testNetworkError()">
          Test Network Error
        </button>
        <button mat-raised-button color="warn" (click)="test404Error()">
          Test 404 Error
        </button>
        <button mat-raised-button color="warn" (click)="test500Error()">
          Test Server Error
        </button>
        <button mat-raised-button color="warn" (click)="testClientError()">
          Test Client Error
        </button>
      </div>
    </div>
  `,
  styles: [`
    .error-test-container {
      padding: 2rem;
      text-align: center;
    }
    .test-buttons {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
      margin-top: 1rem;
    }
  `]
})
export class ErrorTestComponent {

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  testNetworkError(): void {
    // Try to call a non-existent endpoint
    this.http.get('http://localhost:9999/nonexistent').subscribe({
      next: () => console.log('Unexpected success'),
      error: (error) => console.log('Expected network error:', error)
    });
  }

  test404Error(): void {
    this.http.get('/api/nonexistent-endpoint').subscribe({
      next: () => console.log('Unexpected success'),
      error: (error) => console.log('Expected 404 error:', error)
    });
  }

  test500Error(): void {
    // This would need a backend endpoint that returns 500
    this.http.get('/api/test-error').subscribe({
      next: () => console.log('Unexpected success'),
      error: (error) => console.log('Expected server error:', error)
    });
  }

  testClientError(): void {
    // Throw a client-side error
    throw new Error('Test client-side error for global error handler');
  }
}