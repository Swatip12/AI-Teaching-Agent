import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService, UserProfile, UpdatePreferencesRequest } from '../../services/user.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  userProfile: UserProfile | null = null;
  preferencesForm: FormGroup;
  isLoading = true;
  isSaving = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.preferencesForm = this.fb.group({
      learningPace: ['NORMAL'],
      preferredExplanationStyle: ['SIMPLE'],
      enableEncouragement: [true],
      theme: ['LIGHT']
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.userService.getUserProfile().subscribe({
      next: (profile) => {
        this.userProfile = profile;
        this.preferencesForm.patchValue(profile.preferences);
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open(
          'Failed to load profile. Please try again.',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  onSavePreferences(): void {
    if (this.preferencesForm.valid && !this.isSaving) {
      this.isSaving = true;
      
      const preferences: UpdatePreferencesRequest = this.preferencesForm.value;
      
      this.userService.updatePreferences(preferences).subscribe({
        next: () => {
          this.isSaving = false;
          this.snackBar.open('Preferences saved successfully!', 'Close', { duration: 3000 });
        },
        error: (error) => {
          this.isSaving = false;
          this.snackBar.open(
            'Failed to save preferences. Please try again.',
            'Close',
            { duration: 5000 }
          );
        }
      });
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}