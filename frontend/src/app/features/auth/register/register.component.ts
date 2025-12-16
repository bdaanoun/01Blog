import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, RegisterRequest } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  user = {
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  submitted = false;
  passwordMismatch = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = '';
    this.successMessage = '';

    if (this.user.password !== this.user.confirmPassword) {
      this.passwordMismatch = true;
      return;
    }

    this.passwordMismatch = false;

    if (!this.user.username ||
      !this.user.email || !this.user.password) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    const registerData: RegisterRequest = {
      username: this.user.username,
      email: this.user.email,
      password: this.user.password
    };

    this.isLoading = true;

    this.authService.register(registerData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Registration successful!';

        if (response.token) {
          this.authService.saveToken(response.token);
        }

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;

        if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = 'Registration failed. Please try again.';
        }

        console.error('Registration error:', error);
      }
    });
  }

  resetForm() {
    this.user = {
      username: '',
      email: '',
      password: '',
      confirmPassword: ''
    };
    this.submitted = false;
    this.passwordMismatch = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  isFieldInvalid(fieldValue: string): boolean {
    return this.submitted && !fieldValue;
  }
}