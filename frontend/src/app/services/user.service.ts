import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserPreferences {
  learningPace: 'SLOW' | 'NORMAL' | 'FAST';
  preferredExplanationStyle: 'SIMPLE' | 'DETAILED' | 'TECHNICAL';
  enableEncouragement: boolean;
  theme: 'LIGHT' | 'DARK';
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  preferences: UserPreferences;
  createdAt: string;
  lastLoginAt: string;
}

export interface UpdatePreferencesRequest {
  learningPace?: 'SLOW' | 'NORMAL' | 'FAST';
  preferredExplanationStyle?: 'SIMPLE' | 'DETAILED' | 'TECHNICAL';
  enableEncouragement?: boolean;
  theme?: 'LIGHT' | 'DARK';
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = '/api/users';

  constructor(private http: HttpClient) {}

  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/profile`);
  }

  updatePreferences(preferences: UpdatePreferencesRequest): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/preferences`, preferences);
  }
}