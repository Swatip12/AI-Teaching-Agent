import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, SubjectService } from '../../services/subject.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatSnackBarModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  subjects: Subject[] = [];
  isLoading = true;

  constructor(
    private subjectService: SubjectService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadSubjects();
  }

  loadSubjects(): void {
    this.subjectService.getAvailableSubjects().subscribe({
      next: (subjects) => {
        this.subjects = subjects;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open(
          'Failed to load subjects. Please try again.',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  selectSubject(subject: Subject): void {
    if (subject.isAccessible) {
      // Navigate to subject lessons - will be implemented in later tasks
      this.snackBar.open(
        `Starting ${subject.name} - Lesson navigation will be implemented soon!`,
        'Close',
        { duration: 3000 }
      );
    } else {
      this.snackBar.open(
        'This subject is not yet accessible. Complete prerequisites first.',
        'Close',
        { duration: 3000 }
      );
    }
  }

  getDifficultyColor(difficulty: string): string {
    switch (difficulty) {
      case 'BEGINNER': return 'primary';
      case 'INTERMEDIATE': return 'accent';
      case 'ADVANCED': return 'warn';
      default: return 'primary';
    }
  }

  getSubjectIcon(subjectName: string): string {
    const name = subjectName.toLowerCase();
    if (name.includes('java')) return 'code';
    if (name.includes('data structures') || name.includes('algorithms')) return 'account_tree';
    if (name.includes('full stack')) return 'web';
    if (name.includes('logical') || name.includes('reasoning')) return 'psychology';
    if (name.includes('interview')) return 'work';
    return 'school';
  }
}