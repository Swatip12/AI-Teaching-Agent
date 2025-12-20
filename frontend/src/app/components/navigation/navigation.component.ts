import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService, User } from '../../services/auth.service';
import { CrossDeviceSyncService } from '../../services/cross-device-sync.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    MatTooltipModule
  ],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  isAuthenticated = false;
  
  // Cross-device sync properties
  isOnline = true;
  syncStatus = 'idle';
  deviceType = 'desktop';
  
  private subscriptions: Subscription[] = [];

  constructor(
    private authService: AuthService,
    private crossDeviceSyncService: CrossDeviceSyncService
  ) {}

  ngOnInit(): void {
    // Subscribe to authentication state
    const authSub = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAuthenticated = !!user;
      
      if (user) {
        // Initialize cross-device sync when user logs in
        this.initializeCrossDeviceSync();
      }
    });
    this.subscriptions.push(authSub);
    
    // Initialize device type detection
    this.deviceType = this.crossDeviceSyncService.getCurrentDeviceType();
    
    // Subscribe to network status
    const networkSub = this.crossDeviceSyncService.networkStatus$.subscribe(isOnline => {
      this.isOnline = isOnline;
    });
    this.subscriptions.push(networkSub);
    
    // Subscribe to sync status
    const syncSub = this.crossDeviceSyncService.syncStatus$.subscribe(status => {
      this.syncStatus = status;
    });
    this.subscriptions.push(syncSub);
  }
  
  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
  
  private initializeCrossDeviceSync(): void {
    // Sync progress when user logs in
    this.crossDeviceSyncService.syncProgress().subscribe({
      next: (response) => {
        if (response.success) {
          console.log('Initial progress sync completed');
        }
      },
      error: (error) => {
        console.error('Initial progress sync failed:', error);
      }
    });
    
    // Get offline data package for reliability
    this.crossDeviceSyncService.getOfflineDataPackage().subscribe({
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
  
  onSyncProgress(): void {
    this.crossDeviceSyncService.syncProgress().subscribe({
      next: (response) => {
        if (response.success) {
          console.log('Manual progress sync completed');
        }
      },
      error: (error) => {
        console.error('Manual progress sync failed:', error);
      }
    });
  }
  
  getSyncStatusIcon(): string {
    switch (this.syncStatus) {
      case 'syncing': return 'sync';
      case 'completed': return 'sync_disabled';
      case 'failed': return 'sync_problem';
      default: return 'sync';
    }
  }
  
  getSyncStatusTooltip(): string {
    if (!this.isOnline) {
      return 'Offline mode - using cached data';
    }
    
    switch (this.syncStatus) {
      case 'syncing': return 'Syncing progress across devices...';
      case 'completed': return 'Progress synchronized';
      case 'failed': return 'Sync failed - click to retry';
      default: return 'Click to sync progress across devices';
    }
  }

  onLogout(): void {
    this.authService.logout();
  }
}