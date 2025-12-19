import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import * as fc from 'fast-check';

import { DashboardComponent } from './dashboard.component';
import { SubjectService, Subject } from '../../services/subject.service';
import { AuthService, User } from '../../services/auth.service';

// Generator for valid Subject objects
const subjectGenerator = fc.record({
  id: fc.string({ minLength: 1 }),
  name: fc.string({ minLength: 1 }),
  description: fc.string({ minLength: 1 }),
  difficulty: fc.constantFrom('BEGINNER' as const, 'INTERMEDIATE' as const, 'ADVANCED' as const),
  prerequisites: fc.array(fc.string()),
  isAccessible: fc.boolean()
});

/**
 * **Feature: ai-teaching-platform, Property 1: Subject accessibility for beginners**
 * **Validates: Requirements 1.1**
 */
describe('DashboardComponent - Subject Accessibility Property', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let subjectService: jasmine.SpyObj<SubjectService>;
  let authService: jasmine.SpyObj<AuthService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    const subjectServiceSpy = jasmine.createSpyObj('SubjectService', ['getAvailableSubjects']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        DashboardComponent,
        HttpClientTestingModule,
        MatSnackBarModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: SubjectService, useValue: subjectServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    subjectService = TestBed.inject(SubjectService) as jasmine.SpyObj<SubjectService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    // Mock current user
    const mockUser: User = { id: 1, username: 'testuser', email: 'test@example.com' };
    authService.getCurrentUser.and.returnValue(mockUser);
  });

  it('should display all available subjects without requiring prior knowledge indicators', () => {
    fc.assert(fc.property(
      fc.array(subjectGenerator, { minLength: 1, maxLength: 10 }),
      (subjects: Subject[]) => {
        // Setup
        subjectService.getAvailableSubjects.and.returnValue(of(subjects));
        
        // Execute
        component.ngOnInit();
        fixture.detectChanges();

        // Verify: All subjects should be displayed regardless of prerequisites
        expect(component.subjects).toEqual(subjects);
        expect(component.subjects.length).toBeGreaterThan(0);
        
        // Verify: Each subject should be accessible to view (even if locked)
        component.subjects.forEach(subject => {
          expect(subject.id).toBeDefined();
          expect(subject.name).toBeDefined();
          expect(subject.description).toBeDefined();
          expect(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']).toContain(subject.difficulty);
          expect(Array.isArray(subject.prerequisites)).toBe(true);
          expect(typeof subject.isAccessible).toBe('boolean');
        });

        // Verify: No prior knowledge assumptions - subjects are shown regardless of user background
        const beginnerSubjects = component.subjects.filter(s => s.difficulty === 'BEGINNER');
        const subjectsWithoutPrereqs = component.subjects.filter(s => s.prerequisites.length === 0);
        
        // At least some subjects should be beginner-friendly or have no prerequisites
        expect(beginnerSubjects.length + subjectsWithoutPrereqs.length).toBeGreaterThan(0);
      }
    ), { numRuns: 100 });
  });
});

/**
 * **Feature: ai-teaching-platform, Property 2: Subject progression starts at basics**
 * **Validates: Requirements 1.2**
 */
describe('DashboardComponent - Subject Progression Property', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let subjectService: jasmine.SpyObj<SubjectService>;
  let authService: jasmine.SpyObj<AuthService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    const subjectServiceSpy = jasmine.createSpyObj('SubjectService', ['getAvailableSubjects']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        DashboardComponent,
        HttpClientTestingModule,
        MatSnackBarModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: SubjectService, useValue: subjectServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    subjectService = TestBed.inject(SubjectService) as jasmine.SpyObj<SubjectService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    // Mock current user
    const mockUser: User = { id: 1, username: 'testuser', email: 'test@example.com' };
    authService.getCurrentUser.and.returnValue(mockUser);
  });

  it('should ensure subject progression starts with basic concepts', () => {
    fc.assert(fc.property(
      fc.array(subjectGenerator, { minLength: 1, maxLength: 10 }),
      (subjects: Subject[]) => {
        // Setup
        subjectService.getAvailableSubjects.and.returnValue(of(subjects));
        snackBar.open.calls.reset();
        
        // Execute
        component.ngOnInit();
        fixture.detectChanges();

        // Verify: For any subject selection, there should be accessible entry points
        const accessibleSubjects = component.subjects.filter(s => s.isAccessible);
        
        if (accessibleSubjects.length > 0) {
          // At least one accessible subject should be beginner-friendly
          const beginnerAccessibleSubjects = accessibleSubjects.filter(s => 
            s.difficulty === 'BEGINNER' || s.prerequisites.length === 0
          );
          
          expect(beginnerAccessibleSubjects.length).toBeGreaterThan(0);
          
          // Verify that beginner subjects don't have complex prerequisites
          beginnerAccessibleSubjects.forEach(subject => {
            if (subject.difficulty === 'BEGINNER') {
              // Beginner subjects should have minimal or no prerequisites
              expect(subject.prerequisites.length).toBeLessThanOrEqual(1);
            }
            
            if (subject.prerequisites.length === 0) {
              // Subjects with no prerequisites should be accessible
              expect(subject.isAccessible).toBe(true);
            }
          });
        }

        // Verify: Subject progression logic - basic concepts come first
        component.subjects.forEach(subject => {
          // Test the selectSubject method behavior
          const initialCallCount = snackBar.open.calls.count();
          
          component.selectSubject(subject);
          
          if (subject.isAccessible) {
            // Should allow selection of accessible subjects
            expect(snackBar.open).toHaveBeenCalled();
          } else {
            // Should prevent selection of locked subjects with appropriate message
            expect(snackBar.open).toHaveBeenCalledWith(
              jasmine.stringMatching(/not yet accessible|prerequisites/i),
              'Close',
              { duration: 3000 }
            );
          }
        });
      }
    ), { numRuns: 100 });
  });
});