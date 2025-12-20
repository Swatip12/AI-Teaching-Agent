import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, timer, of } from 'rxjs';
import { catchError, retry, retryWhen, delay, take, concatMap, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface DeviceContinuityState {
  userId: number;
  userPreferences: any;
  recentProgress: any[];
  currentLessonId?: number;
  currentStep?: string;
  completionPercentage?: number;
  lastSyncTime: string;
}

export interface OfflineDataPackage {
  userId: number;
  userPreferences: any;
  progressData: any[];
  packageTimestamp: string;
  lessonSummaries: { [key: number]: LessonSummary };
}

export interface LessonSummary {
  lessonId: number;
  title: string;
  subject: string;
  completionPercentage: number;
}

export interface SyncResponse {
  success: boolean;
  message?: string;
  progressCount?: number;
  progress?: any[];
  continuityState?: DeviceContinuityState;
  offlinePackage?: OfflineDataPackage;
}

/**
 * Service for cross-device synchronization and reliability
 * Implements Requirements 8.1, 8.2, 8.3, 8.4
 */
@Injectable({
  providedIn: 'root'
})
export class CrossDeviceSyncService {
  private readonly apiUrl = `${environment.apiUrl}/sync`;
  private readonly maxRetries = 3;
  private readonly retryDelay = 1000; // 1 second
  
  // Observable for network status
  private networkStatusSubject = new BehaviorSubject<boolean>(navigator.onLine);
  public networkStatus$ = this.networkStatusSubject.asObservable();
  
  // Observable for sync status
  private syncStatusSubject = new BehaviorSubject<string>('idle');
  public syncStatus$ = this.syncStatusSubject.asObservable();
  
  // Offline data cache
  private offlineDataCache: OfflineDataPackage | null = null;
  
  constructor(private http: HttpClient) {
    this.initializeNetworkMonitoring();
    this.initializeOfflineSupport();
  }
  
  /**
   * Initialize network status monitoring
   * Requirement 8.3: Network error handling
   */
  private initializeNetworkMonitoring(): void {
    window.addEventListener('online', () => {
      this.networkStatusSubject.next(true);
      this.handleNetworkReconnection();
    });
    
    window.addEventListener('offline', () => {
      this.networkStatusSubject.next(false);
      this.handleNetworkDisconnection();
    });
  }
  
  /**
   * Initialize offline support capabilities
   * Requirement 8.3: Network error handling
   */
  private initializeOfflineSupport(): void {
    // Load cached offline data on service initialization
    const cachedData = localStorage.getItem('offlineDataPackage');
    if (cachedData) {
      try {
        this.offlineDataCache = JSON.parse(cachedData);
      } catch (error) {
        console.error('Error loading cached offline data:', error);
        localStorage.removeItem('offlineDataPackage');
      }
    }
  }
  
  /**
   * Detect current device type for responsive design
   * Requirement 8.4: Responsive design support
   */
  private detectDeviceType(): string {
    const userAgent = navigator.userAgent;
    const screenWidth = window.screen.width;
    
    if (/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(userAgent)) {
      if (screenWidth < 768) {
        return 'mobile';
      } else {
        return 'tablet';
      }
    } else if (screenWidth < 1024) {
      return 'tablet';
    } else {
      return 'desktop';
    }
  }
  
  /**
   * Synchronize user progress across devices
   * Requirement 8.2: Progress synchronization across devices
   */
  syncProgress(): Observable<SyncResponse> {
    this.syncStatusSubject.next('syncing');
    
    const deviceType = this.detectDeviceType();
    const request = { deviceType };
    
    return this.http.post<SyncResponse>(`${this.apiUrl}/progress`, request)
      .pipe(
        retry(this.maxRetries),
        retryWhen(errors => this.handleRetryLogic(errors)),
        map(response => {
          this.syncStatusSubject.next('completed');
          return response;
        }),
        catchError(error => {
          this.syncStatusSubject.next('failed');
          return this.handleSyncError(error);
        })
      );
  }
  
  /**
   * Get user's current learning state for device continuity
   * Requirement 8.1: Consistent functionality across devices
   */
  getContinuityState(): Observable<SyncResponse> {
    if (!navigator.onLine && this.offlineDataCache) {
      // Return cached data when offline
      return of({
        success: true,
        continuityState: this.createContinuityStateFromCache()
      });
    }
    
    return this.http.get<SyncResponse>(`${this.apiUrl}/continuity`)
      .pipe(
        retry(this.maxRetries),
        retryWhen(errors => this.handleRetryLogic(errors)),
        catchError(error => this.handleSyncError(error))
      );
  }
  
  /**
   * Update progress with device-specific information
   * Requirement 8.2: Progress synchronization
   */
  updateProgressWithDeviceInfo(progressId: number, currentStep: string): Observable<SyncResponse> {
    const deviceType = this.detectDeviceType();
    const request = { deviceType, currentStep };
    
    return this.http.put<SyncResponse>(`${this.apiUrl}/progress/${progressId}`, request)
      .pipe(
        retry(this.maxRetries),
        retryWhen(errors => this.handleRetryLogic(errors)),
        catchError(error => this.handleSyncError(error))
      );
  }
  
  /**
   * Get offline data package for network error recovery
   * Requirement 8.3: Network error handling
   */
  getOfflineDataPackage(): Observable<SyncResponse> {
    return this.http.get<SyncResponse>(`${this.apiUrl}/offline-package`)
      .pipe(
        retry(this.maxRetries),
        retryWhen(errors => this.handleRetryLogic(errors)),
        map(response => {
          // Cache the offline data package
          if (response.success && response.offlinePackage) {
            this.offlineDataCache = response.offlinePackage;
            localStorage.setItem('offlineDataPackage', JSON.stringify(response.offlinePackage));
          }
          return response;
        }),
        catchError(error => this.handleSyncError(error))
      );
  }
  
  /**
   * Check service health for reliability monitoring
   * Requirement 8.3: Network error handling
   */
  checkServiceHealth(): Observable<SyncResponse> {
    return this.http.get<SyncResponse>(`${this.apiUrl}/health`)
      .pipe(
        catchError(error => {
          console.error('Service health check failed:', error);
          return of({
            success: false,
            message: 'Service unavailable'
          });
        })
      );
  }
  
  /**
   * Handle retry logic for network requests
   * Requirement 8.3: Network error handling
   */
  private handleRetryLogic(errors: Observable<any>): Observable<any> {
    return errors.pipe(
      concatMap((error, index) => {
        if (index >= this.maxRetries - 1) {
          return throwError(error);
        }
        
        // Exponential backoff
        const delayTime = this.retryDelay * Math.pow(2, index);
        console.log(`Retrying request in ${delayTime}ms (attempt ${index + 1}/${this.maxRetries})`);
        
        return timer(delayTime);
      })
    );
  }
  
  /**
   * Handle synchronization errors with fallback strategies
   * Requirement 8.3: Network error handling
   */
  private handleSyncError(error: HttpErrorResponse): Observable<SyncResponse> {
    console.error('Sync error:', error);
    
    // If offline and we have cached data, use it
    if (!navigator.onLine && this.offlineDataCache) {
      return of({
        success: true,
        message: 'Using cached offline data',
        offlinePackage: this.offlineDataCache
      });
    }
    
    // Return error response
    return of({
      success: false,
      message: error.error?.message || 'Network error occurred'
    });
  }
  
  /**
   * Handle network reconnection
   * Requirement 8.3: Network error handling
   */
  private handleNetworkReconnection(): void {
    console.log('Network reconnected - attempting to sync');
    this.syncProgress().subscribe({
      next: (response) => {
        if (response.success) {
          console.log('Auto-sync completed after reconnection');
        }
      },
      error: (error) => {
        console.error('Auto-sync failed after reconnection:', error);
      }
    });
  }
  
  /**
   * Handle network disconnection
   * Requirement 8.3: Network error handling
   */
  private handleNetworkDisconnection(): void {
    console.log('Network disconnected - switching to offline mode');
    
    // Prepare offline data if not already cached
    if (!this.offlineDataCache) {
      // Try to get offline package before complete disconnection
      this.getOfflineDataPackage().subscribe({
        next: (response) => {
          if (response.success) {
            console.log('Offline data package prepared');
          }
        },
        error: (error) => {
          console.warn('Could not prepare offline data package:', error);
        }
      });
    }
  }
  
  /**
   * Create continuity state from cached offline data
   * Requirement 8.1: Consistent functionality across devices
   */
  private createContinuityStateFromCache(): DeviceContinuityState | undefined {
    if (!this.offlineDataCache) {
      return undefined;
    }
    
    const recentProgress = this.offlineDataCache.progressData || [];
    const activeProgress = recentProgress.find(p => p.status === 'IN_PROGRESS');
    
    return {
      userId: this.offlineDataCache.userId,
      userPreferences: this.offlineDataCache.userPreferences,
      recentProgress: recentProgress,
      currentLessonId: activeProgress?.lesson?.id,
      currentStep: activeProgress?.currentStep,
      completionPercentage: activeProgress?.completionPercentage,
      lastSyncTime: this.offlineDataCache.packageTimestamp
    };
  }
  
  /**
   * Get cached offline data
   * Requirement 8.3: Network error handling
   */
  getCachedOfflineData(): OfflineDataPackage | null {
    return this.offlineDataCache;
  }
  
  /**
   * Clear cached offline data
   */
  clearOfflineCache(): void {
    this.offlineDataCache = null;
    localStorage.removeItem('offlineDataPackage');
  }
  
  /**
   * Check if device is currently online
   */
  isOnline(): boolean {
    return navigator.onLine;
  }
  
  /**
   * Get current device type
   * Requirement 8.4: Responsive design support
   */
  getCurrentDeviceType(): string {
    return this.detectDeviceType();
  }
}