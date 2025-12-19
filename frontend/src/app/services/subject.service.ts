import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Subject {
  id: string;
  name: string;
  description: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  prerequisites: string[];
  isAccessible: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SubjectService {
  private readonly API_URL = '/api/curriculum';

  constructor(private http: HttpClient) {}

  getAvailableSubjects(): Observable<Subject[]> {
    return this.http.get<Subject[]>(`${this.API_URL}/subjects`);
  }

  getSubjectDetails(subjectId: string): Observable<Subject> {
    return this.http.get<Subject>(`${this.API_URL}/subjects/${subjectId}`);
  }
}