import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, throwError, catchError } from 'rxjs';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private tokenSubject = new BehaviorSubject<string | null>(null);

  public currentUser$ = this.currentUserSubject.asObservable();
  public token$ = this.tokenSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    // Check for existing token on service initialization
    const token = localStorage.getItem('auth_token');
    const user = localStorage.getItem('current_user');
    
    if (token && user) {
      this.tokenSubject.next(token);
      this.currentUserSubject.next(JSON.parse(user));
    }
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(
        tap((response: AuthResponse) => {
          this.setAuthData(response.token, response.user);
          this.snackBar.open('Welcome back!', 'Dismiss', { duration: 3000 });
        }),
        catchError((error: HttpErrorResponse) => this.handleAuthError(error, 'login'))
      );
  }

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData)
      .pipe(
        tap((response: AuthResponse) => {
          this.setAuthData(response.token, response.user);
          this.snackBar.open('Account created successfully!', 'Dismiss', { duration: 3000 });
        }),
        catchError((error: HttpErrorResponse) => this.handleAuthError(error, 'register'))
      );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
    this.tokenSubject.next(null);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.tokenSubject.value;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  private setAuthData(token: string, user: User): void {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('current_user', JSON.stringify(user));
    this.tokenSubject.next(token);
    this.currentUserSubject.next(user);
  }

  private handleAuthError(error: HttpErrorResponse, operation: string): Observable<never> {
    let errorMessage = 'An error occurred. Please try again.';
    
    if (error.status === 401) {
      errorMessage = operation === 'login' ? 
        'Invalid username or password.' : 
        'Authentication failed.';
    } else if (error.status === 409) {
      errorMessage = 'Username or email already exists.';
    } else if (error.status === 400) {
      errorMessage = error.error?.message || 'Invalid input. Please check your information.';
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to the server. Please check your internet connection.';
    } else if (error.status >= 500) {
      errorMessage = 'Server error. Please try again later.';
    }

    this.snackBar.open(errorMessage, 'Dismiss', { 
      duration: 6000,
      panelClass: ['snackbar-error']
    });

    return throwError(() => error);
  }
}