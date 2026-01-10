import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { HeaderComponent } from './shared/header/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    HeaderComponent,
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  showHeader = true;

  constructor(private router: Router) {}

  // ngOnInit() {
  //   // Debug: Log to console to verify header visibility
  //   console.log('AppComponent initialized, showHeader:', this.showHeader);

  //   // Optional: Hide header on specific routes
  //   this.router.events.pipe(
  //     filter(event => event instanceof NavigationEnd)
  //   ).subscribe((event: NavigationEnd) => {
  //     console.log('Current route:', event.url);
  //     // this.showHeader = !['/login', '/register', '/home'].includes(event.url);
  //   });
  // }
}