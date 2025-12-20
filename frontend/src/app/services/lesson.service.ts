import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';

export interface LessonContent {
  id: number;
  title: string;
  subject: string;
  sequenceOrder: number;
  content: string;
  objectives: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  estimatedDurationMinutes: number;
  prerequisiteLessonIds: number[];
  checkpointQuestions: CheckpointQuestion[];
  practiceQuestions: PracticeQuestion[];
  canAccess: boolean;
}

export interface CheckpointQuestion {
  id: number;
  question: string;
  correctAnswer: string;
  explanation: string;
  questionType: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'CODE_COMPLETION';
  sequenceOrder: number;
}

export interface PracticeQuestion {
  id: number;
  question: string;
  expectedSolution: string;
  hints: string;
  questionType: 'CODING' | 'ALGORITHM' | 'DEBUGGING' | 'DESIGN';
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  sequenceOrder: number;
  starterCode?: string;
  testCases?: string;
  expectedOutput?: string;
  language?: 'JAVA' | 'PYTHON' | 'JAVASCRIPT' | 'CPP';
}

export interface QuestionResponse {
  questionId: number;
  userAnswer: string;
  isCorrect: boolean;
  feedback: string;
  explanation: string;
  timestamp: Date;
}

export interface LessonProgress {
  lessonId: number;
  currentStep: number;
  totalSteps: number;
  completedCheckpoints: number[];
  completedPracticeQuestions: number[];
  isCompleted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class LessonService {
  private readonly API_URL = '/api/lessons';
  private currentLessonSubject = new BehaviorSubject<LessonContent | null>(null);
  private lessonProgressSubject = new BehaviorSubject<LessonProgress | null>(null);

  constructor(private http: HttpClient) {}

  // Observable streams
  getCurrentLesson(): Observable<LessonContent | null> {
    return this.currentLessonSubject.asObservable();
  }

  getLessonProgress(): Observable<LessonProgress | null> {
    return this.lessonProgressSubject.asObservable();
  }

  // API calls
  getLessonById(lessonId: number): Observable<LessonContent> {
    return this.http.get<LessonContent>(`${this.API_URL}/${lessonId}`);
  }

  getLessonsBySubject(subject: string): Observable<LessonContent[]> {
    return this.http.get<LessonContent[]>(`${this.API_URL}/subject/${subject}`);
  }

  getFirstLessonInSubject(subject: string): Observable<LessonContent> {
    return this.http.get<LessonContent>(`${this.API_URL}/subject/${subject}/first`);
  }

  getNextLessonInSubject(subject: string, currentSequenceOrder: number): Observable<LessonContent> {
    return this.http.get<LessonContent>(`${this.API_URL}/subject/${subject}/next/${currentSequenceOrder}`);
  }

  checkLessonAccess(lessonId: number): Observable<boolean> {
    return this.http.get<{canAccess: boolean}>(`${this.API_URL}/${lessonId}/access-check`)
      .pipe(map(response => response.canAccess));
  }

  // Question submission and feedback
  submitCheckpointAnswer(questionId: number, answer: string): Observable<QuestionResponse> {
    const startTime = Date.now();
    return this.http.post<QuestionResponse>(`${this.API_URL}/checkpoint-questions/${questionId}/submit`, {
      answer: answer
    }).pipe(
      map(response => {
        // Ensure immediate feedback (Requirements 3.1)
        const responseTime = Date.now() - startTime;
        console.log(`Checkpoint feedback received in ${responseTime}ms`);
        
        // Validate response structure for immediate feedback
        if (!response.feedback || !response.explanation) {
          throw new Error('Invalid feedback response: missing required fields');
        }
        
        return {
          ...response,
          timestamp: response.timestamp || new Date()
        };
      })
    );
  }

  submitPracticeAnswer(questionId: number, answer: string): Observable<QuestionResponse> {
    const startTime = Date.now();
    return this.http.post<QuestionResponse>(`${this.API_URL}/practice-questions/${questionId}/submit`, {
      answer: answer
    }).pipe(
      map(response => {
        // Ensure immediate feedback (Requirements 3.3)
        const responseTime = Date.now() - startTime;
        console.log(`Practice feedback received in ${responseTime}ms`);
        
        // Validate response structure for immediate feedback
        if (!response.feedback || !response.explanation) {
          throw new Error('Invalid feedback response: missing required fields');
        }
        
        return {
          ...response,
          timestamp: response.timestamp || new Date()
        };
      })
    );
  }

  // Lesson state management
  setCurrentLesson(lesson: LessonContent): void {
    this.currentLessonSubject.next(lesson);
    this.initializeLessonProgress(lesson);
  }

  updateLessonProgress(progress: Partial<LessonProgress>): void {
    const currentProgress = this.lessonProgressSubject.value;
    if (currentProgress) {
      const updatedProgress = { ...currentProgress, ...progress };
      this.lessonProgressSubject.next(updatedProgress);
    }
  }

  markCheckpointCompleted(questionId: number): void {
    const currentProgress = this.lessonProgressSubject.value;
    if (currentProgress && !currentProgress.completedCheckpoints.includes(questionId)) {
      const updatedProgress = {
        ...currentProgress,
        completedCheckpoints: [...currentProgress.completedCheckpoints, questionId]
      };
      this.lessonProgressSubject.next(updatedProgress);
    }
  }

  markPracticeQuestionCompleted(questionId: number): void {
    const currentProgress = this.lessonProgressSubject.value;
    if (currentProgress && !currentProgress.completedPracticeQuestions.includes(questionId)) {
      const updatedProgress = {
        ...currentProgress,
        completedPracticeQuestions: [...currentProgress.completedPracticeQuestions, questionId]
      };
      this.lessonProgressSubject.next(updatedProgress);
    }
  }

  advanceToNextStep(): void {
    const currentProgress = this.lessonProgressSubject.value;
    if (currentProgress && currentProgress.currentStep < currentProgress.totalSteps) {
      const updatedProgress = {
        ...currentProgress,
        currentStep: currentProgress.currentStep + 1
      };
      this.lessonProgressSubject.next(updatedProgress);
    }
  }

  private initializeLessonProgress(lesson: LessonContent): void {
    const totalSteps = this.calculateTotalSteps(lesson);
    const progress: LessonProgress = {
      lessonId: lesson.id,
      currentStep: 1,
      totalSteps: totalSteps,
      completedCheckpoints: [],
      completedPracticeQuestions: [],
      isCompleted: false
    };
    this.lessonProgressSubject.next(progress);
  }

  private calculateTotalSteps(lesson: LessonContent): number {
    // Content explanation + checkpoint questions + practice questions
    return 1 + lesson.checkpointQuestions.length + lesson.practiceQuestions.length;
  }

  // Helper methods for lesson structure validation
  isLessonStructureValid(lesson: LessonContent): boolean {
    // Validate lesson follows the required structure from requirements 2.1-2.5
    return !!lesson.content && 
           lesson.content.trim().length > 0 &&
           lesson.checkpointQuestions.length >= 1 && 
           lesson.practiceQuestions.length >= 2 && 
           lesson.practiceQuestions.length <= 3;
  }

  clearCurrentLesson(): void {
    this.currentLessonSubject.next(null);
    this.lessonProgressSubject.next(null);
  }
}