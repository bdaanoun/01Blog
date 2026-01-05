import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  isLoggedIn$: Observable<boolean>;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    // Use the observable from AuthService
    this.isLoggedIn$ = this.authService.isAuthenticated$;
  }

  ngOnInit() {
    // Debug: Subscribe to check the authentication state
    this.isLoggedIn$.subscribe(status => {
      console.log('Header - User logged in status:', status);
    });
  }

  logout() {
    console.log('Logout clicked');
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}