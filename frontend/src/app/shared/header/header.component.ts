import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { MatIconModule } from '@angular/material/icon';
import { NotificationDto, NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  isLoggedIn$: Observable<boolean>;
  currentUser$: Observable<any>;
  showProfileDropdown = false;
  showNotifications = false;

  unreadCount = 0;

  notifications: NotificationDto[] = [];
  loadingNotifications = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.isLoggedIn$ = this.authService.isAuthenticated$;
    this.currentUser$ = this.authService.currentUser$;
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;

    // load notifications when opening dropdown
    if (this.showNotifications) {
      this.fetchNotifications();
    }
  }

  private fetchNotifications() {
    this.loadingNotifications = true;

    this.notificationService.getMyNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.unreadCount = data.filter(n => n.status === 'UNREAD').length;
        this.loadingNotifications = false;
      },
      error: (err) => {
        console.error('Failed to load notifications', err);
        this.loadingNotifications = false;
      }
    });
  }

  goToMyProfile() {
    const id = this.authService.getUserIdFromToken();
    if (id) this.router.navigate(['/profile', id]);
    else this.router.navigate(['/login']);
    this.showProfileDropdown = false;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getUserInitial(): string {
    const username = this.authService.getUsernameFromToken();
    return username ? username.charAt(0).toUpperCase() : 'U';
  }

  toggleProfileDropdown() {
    this.showProfileDropdown = !this.showProfileDropdown;
  }

  getAvatarUrl(path: string): string {  
    return `http://localhost:8080/uploads/${path}`;
  }

  openNotification(n: NotificationDto) {
    // mark as read (optimistic)
    if (n.status === 'UNREAD') {
      n.status = 'READ';
      this.unreadCount = Math.max(0, this.unreadCount - 1);

      this.notificationService.markAsRead(n.id).subscribe({
        error: (err) => console.error('markAsRead failed', err)
      });
    }
  }
}