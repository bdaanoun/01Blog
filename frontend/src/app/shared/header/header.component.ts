import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';


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

  notifications = [
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
    { id: 1, message: 'New comment on your post' },
    { id: 2, message: 'User X liked your post' },
    { id: 3, message: 'New follower' },
  ];

  constructor(private authService: AuthService, private router: Router) {
    this.isLoggedIn$ = this.authService.isAuthenticated$;
    this.currentUser$ = this.authService.currentUser$;

  }

  // getAuthorInitial(authorName: string | undefined): string {
  //   return authorName ? authorName.charAt(0).toUpperCase() : 'U';
  // }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  toggleProfileDropdown() {
    this.showProfileDropdown = !this.showProfileDropdown;
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
  }
}
