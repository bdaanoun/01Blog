import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, LoginRequest } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  credentials = {
    identifier: '',
    password: ''
  };

  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  onSubmit() {
    this.errorMessage = '';

    if (!this.credentials.identifier || !this.credentials.password) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    this.isLoading = true;

    const loginData: LoginRequest = {
      identifier: this.credentials.identifier,
      password: this.credentials.password
    };

    this.authService.login(loginData).subscribe({      
      next: (response) => {
        // console.log("=====> ",response);
        // if (response.token)
        
        this.isLoading = false;
        this.authService.saveToken(response.token);
        this.router.navigate(['/home']);
      },
      error: (error) => {
        this.isLoading = false;
        
        if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server';
        } else {
          this.errorMessage = 'Login failed. Please check your credentials.';
        }
        
        console.error('Login error:', error);
      }
    });
  }
}