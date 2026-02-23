import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
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

  @ViewChild('notifBox') notifBox!: ElementRef;
  @ViewChild('profileBox') profileBox!: ElementRef;


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
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {

    const target = event.target as HTMLElement;

    // Close notifications if clicked outside
    if (
      this.showNotifications &&
      this.notifBox &&
      !this.notifBox.nativeElement.contains(target)
    ) {
      this.showNotifications = false;
    }

    // Close profile dropdown if clicked outside
    if (
      this.showProfileDropdown &&
      this.profileBox &&
      !this.profileBox.nativeElement.contains(target)
    ) {
      this.showProfileDropdown = false;
    }
  }



  ngOnInit(): void {
    this.isLoggedIn$.subscribe(isLogged => {
      if (isLogged) {
        this.refreshUnreadCount();
      } else {
        this.unreadCount = 0;
        this.notifications = [];
        this.showNotifications = false;
        this.showProfileDropdown = false;
      }
    });
  }

  private refreshUnreadCount() {
    this.notificationService.getUnreadCount().subscribe({
      next: (count) => this.unreadCount = count,
      error: (err) => console.error('Failed to load unread count', err)
    });
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

        this.notifications = data.sort((a, b) => {
          if (a.status === b.status) return 0;
          return a.status === 'UNREAD' ? -1 : 1;
        });

        this.unreadCount = this.notifications.filter(n => n.status === 'UNREAD').length;

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

  // openNotification(n: NotificationDto) {
  //   // mark as read (optimistic)
  //   if (n.status === 'UNREAD') {
  //     n.status = 'READ';
  //     this.unreadCount = Math.max(0, this.unreadCount - 1);

  //     this.notificationService.markAsRead(n.id).subscribe({
  //       error: (err) => console.error('markAsRead failed', err)
  //     });
  //   }
  // }


  toggleRead(n: NotificationDto, ev: MouseEvent) {
    ev.stopPropagation();

    const makeUnread = n.status === 'READ';

    if (makeUnread) {
      n.status = 'UNREAD';
      this.unreadCount += 1;
    } else {
      n.status = 'READ';
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    }

    const req$ = makeUnread
      ? this.notificationService.markAsUnread(n.id)
      : this.notificationService.markAsRead(n.id);

    req$.subscribe({
      error: () => {
        if (makeUnread) {
          n.status = 'READ';
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        } else {
          n.status = 'UNREAD';
          this.unreadCount += 1;
        }
      }
    });
  }

  openNotification(n: NotificationDto) {
    // mark as read when opening
    if (n.status === 'UNREAD') {
      n.status = 'READ';
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.notificationService.markAsRead(n.id).subscribe({ error: () => { } });
    }

    this.showNotifications = false;

    if (n.notifType === 'NEW_POST' && n.postId) {
      this.router.navigate(['/post', n.postId])
    } else if (n.notifType === 'FOLLOW') {
      this.router.navigate(['/profile', n.senderId]);
    }
  }

}