import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { LessonService, LessonContent, LessonProgress, CheckpointQuestion, PracticeQuestion, QuestionResponse } from '../../services/lesson.service';
import { CodeEditorComponent, CodeValidationResult } from '../code-editor/code-editor.component';
import { CodeExecutionResponse } from '../../services/code-execution.service';
import { CrossDeviceSyncService } from '../../services/cross-device-sync.service';

@Component({
  selector: 'app-lesson',
  standalone: true,
  imports: [CommonModule, FormsModule, CodeEditorComponent],
  templateUrl: './lesson.component.html',
  styleUrls: ['./lesson.component.scss']
})
export class LessonComponent implements OnInit, OnDestroy {
  lesson: LessonContent | null = null;
  progress: LessonProgress | null = null;
  currentStep: 'content' | 'checkpoint' | 'practice' = 'content';
  currentQuestionIndex = 0;
  userAnswer = '';
  feedback: QuestionResponse | null = null;
  isSubmitting = false;
  showExplanation = false;
  loading = true;
  error: string | null = null;
  showHints = false;
  
  // Code editor properties
  codeExecutionResult: CodeExecutionResponse | null = null;
  codeValidationResult: CodeValidationResult | null = null;
  
  // Cross-device sync properties
  isOnline = true;
  deviceType = 'desktop';
  lastSyncTime: Date | null = null;

  private subscriptions: Subscription[] = [];

  constructor(
    private lessonService: LessonService,
    private route: ActivatedRoute,
    private router: Router,
    private crossDeviceSyncService: CrossDeviceSyncService
  ) {}

  ngOnInit(): void {
    this.initializeCrossDeviceSupport();
    this.loadLesson();
    this.subscribeToProgress();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.lessonService.clearCurrentLesson();
    this.syncProgressBeforeExit();
  }
  
  private initializeCrossDeviceSupport(): void {
    // Get device type
    this.deviceType = this.crossDeviceSyncService.getCurrentDeviceType();
    
    // Subscribe to network status
    const networkSub = this.crossDeviceSyncService.networkStatus$.subscribe(isOnline => {
      this.isOnline = isOnline;
      if (isOnline && this.progress) {
        // Auto-sync when coming back online
        this.syncCurrentProgress();
      }
    });
    this.subscriptions.push(networkSub);
    
    // Get continuity state to restore lesson position
    this.restoreLessonContinuity();
  }
  
  private restoreLessonContinuity(): void {
    this.crossDeviceSyncService.getContinuityState().subscribe({
      next: (response) => {
        if (response.success && response.continuityState) {
          const state = response.continuityState;
          
          // Check if we should restore to a specific lesson step
          if (state.currentStep && state.currentLessonId) {
            const lessonId = this.route.snapshot.params['id'];
            if (parseInt(lessonId) === state.currentLessonId) {
              this.restoreStepFromContinuity(state.currentStep);
            }
          }
        }
      },
      error: (error) => {
        console.warn('Could not restore lesson continuity:', error);
      }
    });
  }
  
  private restoreStepFromContinuity(stepInfo: string): void {
    try {
      // Parse step information (format: "step_type:index")
      const [stepType, indexStr] = stepInfo.split(':');
      const index = parseInt(indexStr) || 0;
      
      switch (stepType) {
        case 'content':
          this.currentStep = 'content';
          break;
        case 'checkpoint':
          this.currentStep = 'checkpoint';
          this.currentQuestionIndex = index;
          break;
        case 'practice':
          this.currentStep = 'practice';
          this.currentQuestionIndex = index;
          break;
      }
      
      console.log(`Restored lesson continuity to ${stepType} step ${index}`);
    } catch (error) {
      console.warn('Could not parse step information:', stepInfo, error);
    }
  }
  
  private syncCurrentProgress(): void {
    if (!this.progress || !this.lesson) return;
    
    const currentStepInfo = `${this.currentStep}:${this.currentQuestionIndex}`;
    
    // Since LessonProgress doesn't have an id, we'll use the lesson id
    // This assumes the backend can find the progress by user and lesson
    this.crossDeviceSyncService.updateProgressWithDeviceInfo(
      this.lesson.id, 
      currentStepInfo
    ).subscribe({
      next: (response) => {
        if (response.success) {
          this.lastSyncTime = new Date();
          console.log('Progress synced across devices');
        }
      },
      error: (error) => {
        console.warn('Could not sync progress:', error);
      }
    });
  }
  
  private syncProgressBeforeExit(): void {
    // Sync progress when leaving the lesson
    if (this.progress && this.isOnline) {
      this.syncCurrentProgress();
    }
  }

  private loadLesson(): void {
    const lessonId = this.route.snapshot.paramMap.get('id');
    if (!lessonId) {
      this.error = 'No lesson ID provided';
      this.loading = false;
      return;
    }

    const subscription = this.lessonService.getLessonById(+lessonId).subscribe({
      next: (lesson) => {
        if (!this.lessonService.isLessonStructureValid(lesson)) {
          this.error = 'Invalid lesson structure';
          this.loading = false;
          return;
        }
        
        this.lesson = lesson;
        this.lessonService.setCurrentLesson(lesson);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading lesson:', error);
        this.error = 'Failed to load lesson';
        this.loading = false;
      }
    });

    this.subscriptions.push(subscription);
  }

  private subscribeToProgress(): void {
    const subscription = this.lessonService.getLessonProgress().subscribe(progress => {
      this.progress = progress;
      this.updateCurrentStep();
    });

    this.subscriptions.push(subscription);
  }

  private updateCurrentStep(): void {
    if (!this.progress || !this.lesson) return;

    const step = this.progress.currentStep;
    
    if (step === 1) {
      this.currentStep = 'content';
    } else if (step <= 1 + this.lesson.checkpointQuestions.length) {
      this.currentStep = 'checkpoint';
      this.currentQuestionIndex = step - 2;
    } else {
      this.currentStep = 'practice';
      this.currentQuestionIndex = step - 2 - this.lesson.checkpointQuestions.length;
    }
  }

  // Navigation methods
  proceedToCheckpoints(): void {
    if (this.lesson && this.lesson.checkpointQuestions.length > 0) {
      this.lessonService.advanceToNextStep();
      this.resetQuestionState();
      this.syncCurrentProgress(); // Sync when moving to checkpoints
    }
  }

  proceedToPractice(): void {
    if (this.lesson && this.lesson.practiceQuestions.length > 0) {
      this.lessonService.advanceToNextStep();
      this.resetQuestionState();
      this.syncCurrentProgress(); // Sync when moving to practice
    }
  }

  proceedToNextQuestion(): void {
    this.lessonService.advanceToNextStep();
    this.resetQuestionState();
    this.syncCurrentProgress(); // Sync when advancing questions
  }

  private resetQuestionState(): void {
    this.userAnswer = '';
    this.feedback = null;
    this.showExplanation = false;
    this.isSubmitting = false;
  }

  // Question submission methods
  submitCheckpointAnswer(): void {
    if (!this.lesson || this.currentStep !== 'checkpoint' || !this.userAnswer.trim()) {
      return;
    }

    const question = this.getCurrentCheckpointQuestion();
    if (!question) return;

    this.isSubmitting = true;
    
    const subscription = this.lessonService.submitCheckpointAnswer(question.id, this.userAnswer).subscribe({
      next: (response) => {
        this.feedback = response;
        this.showExplanation = true;
        this.isSubmitting = false;
        
        if (response.isCorrect) {
          this.lessonService.markCheckpointCompleted(question.id);
        }
      },
      error: (error) => {
        console.error('Error submitting checkpoint answer:', error);
        this.provideFallbackFeedback(question);
        this.isSubmitting = false;
      }
    });

    this.subscriptions.push(subscription);
  }

  submitPracticeAnswer(): void {
    if (!this.lesson || this.currentStep !== 'practice' || !this.userAnswer.trim()) {
      return;
    }

    const question = this.getCurrentPracticeQuestion();
    if (!question) return;

    this.isSubmitting = true;
    
    const subscription = this.lessonService.submitPracticeAnswer(question.id, this.userAnswer).subscribe({
      next: (response) => {
        this.feedback = response;
        this.showExplanation = true;
        this.isSubmitting = false;
        
        if (response.isCorrect) {
          this.lessonService.markPracticeQuestionCompleted(question.id);
        }
      },
      error: (error) => {
        console.error('Error submitting practice answer:', error);
        this.provideFallbackFeedback(question);
        this.isSubmitting = false;
      }
    });

    this.subscriptions.push(subscription);
  }

  private provideFallbackFeedback(question: CheckpointQuestion | PracticeQuestion): void {
    // Provide immediate feedback even if API fails (Requirements 3.1, 3.3)
    const isCheckpoint = 'correctAnswer' in question;
    const isCorrect = isCheckpoint ? 
      this.userAnswer.toLowerCase().trim() === (question as CheckpointQuestion).correctAnswer.toLowerCase().trim() :
      false; // For practice questions, we can't easily determine correctness without backend

    // Ensure immediate feedback with contextual messages
    const encouragingMessages = [
      'Great job! That\'s absolutely correct.',
      'Excellent! You understood this concept perfectly.',
      'Perfect! You\'re really getting the hang of this.'
    ];
    
    const constructiveMessages = [
      'That\'s not quite right, but don\'t worry - learning takes practice!',
      'Not quite there yet, but you\'re thinking about this correctly.',
      'That\'s a good attempt! Let me help you understand this better.'
    ];

    this.feedback = {
      questionId: question.id,
      userAnswer: this.userAnswer,
      isCorrect: isCorrect,
      feedback: isCorrect ? 
        encouragingMessages[Math.floor(Math.random() * encouragingMessages.length)] : 
        constructiveMessages[Math.floor(Math.random() * constructiveMessages.length)],
      explanation: this.generateContextualExplanation(question, isCorrect, isCheckpoint),
      timestamp: new Date()
    };
    
    this.showExplanation = true;
    
    // Log immediate feedback provision for monitoring
    console.log(`Immediate fallback feedback provided for question ${question.id} at ${this.feedback.timestamp}`);
  }

  private generateContextualExplanation(question: CheckpointQuestion | PracticeQuestion, isCorrect: boolean, isCheckpoint: boolean): string {
    let explanation = '';
    
    if (isCheckpoint) {
      const checkpointQ = question as CheckpointQuestion;
      explanation = checkpointQ.explanation || '';
      
      if (!isCorrect) {
        explanation += ' Remember: this concept is fundamental to understanding the bigger picture. ';
        explanation += 'Take your time to review the material and try to connect the dots.';
      } else {
        explanation += ' You\'ve demonstrated a solid understanding of this concept!';
      }
    } else {
      const practiceQ = question as PracticeQuestion;
      explanation = practiceQ.hints || '';
      
      if (!isCorrect) {
        explanation += ' For practice problems like this, try breaking it down step by step: ';
        explanation += '1) Understand what the problem is asking, 2) Identify the key concepts involved, ';
        explanation += '3) Think about similar problems you\'ve solved before.';
      } else {
        explanation += ' Excellent problem-solving approach! You applied the concepts correctly.';
      }
    }
    
    return explanation;
  }

  // Getter methods for current questions
  getCurrentCheckpointQuestion(): CheckpointQuestion | null {
    if (!this.lesson || this.currentStep !== 'checkpoint') return null;
    return this.lesson.checkpointQuestions[this.currentQuestionIndex] || null;
  }

  getCurrentPracticeQuestion(): PracticeQuestion | null {
    if (!this.lesson || this.currentStep !== 'practice') return null;
    return this.lesson.practiceQuestions[this.currentQuestionIndex] || null;
  }

  // Progress calculation methods
  getProgressPercentage(): number {
    if (!this.progress) return 0;
    return Math.round((this.progress.currentStep / this.progress.totalSteps) * 100);
  }

  isLastCheckpointQuestion(): boolean {
    if (!this.lesson || this.currentStep !== 'checkpoint') return false;
    return this.currentQuestionIndex === this.lesson.checkpointQuestions.length - 1;
  }

  isLastPracticeQuestion(): boolean {
    if (!this.lesson || this.currentStep !== 'practice') return false;
    return this.currentQuestionIndex === this.lesson.practiceQuestions.length - 1;
  }

  canProceedToNext(): boolean {
    return this.feedback !== null && this.showExplanation;
  }

  // Lesson completion
  completeLesson(): void {
    if (this.progress) {
      this.lessonService.updateLessonProgress({ isCompleted: true });
    }
    
    // Navigate to next lesson or back to dashboard
    this.navigateToNextLesson();
  }

  private navigateToNextLesson(): void {
    if (!this.lesson) return;

    const subscription = this.lessonService.getNextLessonInSubject(
      this.lesson.subject, 
      this.lesson.sequenceOrder
    ).subscribe({
      next: (nextLesson) => {
        this.router.navigate(['/lesson', nextLesson.id]);
      },
      error: () => {
        // No next lesson available, go back to dashboard
        this.router.navigate(['/dashboard']);
      }
    });

    this.subscriptions.push(subscription);
  }

  // Utility methods
  formatContent(content: string): string {
    // Simple formatting for lesson content
    return content.replace(/\n/g, '<br>');
  }

  formatTimestamp(timestamp: Date): string {
    // Format timestamp to show immediate feedback timing
    const now = new Date();
    const diff = now.getTime() - new Date(timestamp).getTime();
    
    if (diff < 1000) {
      return 'Just now';
    } else if (diff < 60000) {
      return `${Math.floor(diff / 1000)}s ago`;
    } else {
      return new Date(timestamp).toLocaleTimeString();
    }
  }

  getDifficultyColor(difficulty: string): string {
    switch (difficulty) {
      case 'BEGINNER': return 'green';
      case 'INTERMEDIATE': return 'orange';
      case 'ADVANCED': return 'red';
      default: return 'gray';
    }
  }

  navigateToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  // Code editor event handlers
  onCodeChange(code: string): void {
    this.userAnswer = code;
    // Clear previous results when code changes
    this.codeExecutionResult = null;
    this.codeValidationResult = null;
  }

  onCodeExecuted(result: CodeExecutionResponse): void {
    this.codeExecutionResult = result;
    
    // If this is a coding question, automatically validate the solution
    const currentQuestion = this.getCurrentPracticeQuestion();
    if (currentQuestion && currentQuestion.questionType === 'CODING') {
      this.validateCodingSolution(result, currentQuestion);
    }
  }

  onValidationResult(result: CodeValidationResult): void {
    this.codeValidationResult = result;
    
    // Convert validation result to feedback format
    this.feedback = {
      questionId: this.getCurrentPracticeQuestion()?.id || 0,
      userAnswer: this.userAnswer,
      isCorrect: result.isValid,
      feedback: result.feedback || (result.isValid ? 'Great job!' : 'Keep trying!'),
      explanation: result.hints?.join('\n') || '',
      timestamp: new Date()
    };
    
    this.showExplanation = true;
  }

  private validateCodingSolution(executionResult: CodeExecutionResponse, question: PracticeQuestion): void {
    // Check if the code executed successfully and matches expected output
    const isSuccessful = executionResult.success && executionResult.status === 'SUCCESS';
    
    let isCorrect = false;
    let feedback = '';
    let explanation = '';

    if (isSuccessful) {
      // Compare output with expected result if available
      const expectedOutput = question.expectedOutput?.trim();
      const actualOutput = executionResult.output?.trim();
      
      if (expectedOutput && actualOutput) {
        isCorrect = actualOutput === expectedOutput;
        feedback = isCorrect 
          ? 'Excellent! Your code produces the correct output.' 
          : 'Your code runs but doesn\'t produce the expected output.';
        
        if (!isCorrect) {
          explanation = `Expected: "${expectedOutput}"\nActual: "${actualOutput}"\n\nCheck your logic and try again.`;
        }
      } else {
        // No expected output to compare, just check if it runs
        isCorrect = true;
        feedback = 'Great! Your code executed successfully.';
      }
    } else {
      isCorrect = false;
      feedback = 'Your code has errors that need to be fixed.';
      explanation = executionResult.compilationError || executionResult.error || 'Please fix the errors and try again.';
    }

    // Create feedback response
    this.feedback = {
      questionId: question.id,
      userAnswer: this.userAnswer,
      isCorrect: isCorrect,
      feedback: feedback,
      explanation: explanation,
      timestamp: new Date()
    };

    this.showExplanation = true;

    // Mark as completed if correct
    if (isCorrect) {
      this.lessonService.markPracticeQuestionCompleted(question.id);
    }
  }

  // Check if current question is a coding question
  isCurrentQuestionCoding(): boolean {
    const question = this.getCurrentPracticeQuestion();
    return question?.questionType === 'CODING' || false;
  }

  // Get the programming language for the current question
  getCurrentQuestionLanguage(): 'JAVA' | 'PYTHON' | 'JAVASCRIPT' | 'CPP' {
    const question = this.getCurrentPracticeQuestion();
    // Default to Java, but this could be determined from question metadata
    return (question?.language as 'JAVA' | 'PYTHON' | 'JAVASCRIPT' | 'CPP') || 'JAVA';
  }

  // Get starter code for current question
  getCurrentQuestionStarterCode(): string {
    const question = this.getCurrentPracticeQuestion();
    return question?.starterCode || '';
  }

  // Get expected output for current question
  getCurrentQuestionExpectedOutput(): string | undefined {
    const question = this.getCurrentPracticeQuestion();
    return question?.expectedOutput;
  }
}