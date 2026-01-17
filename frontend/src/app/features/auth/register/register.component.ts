import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService, RegisterRequest } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  user = {
    username: '',
    email: '',
    avatar: '',
    password: '',
    confirmPassword: '',
    bio: ''
  };

  avatarFile: File | undefined = undefined;
  avatarPreview: string | null = null;

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

    // basic required fields
    if (!this.user.username || !this.user.email || !this.user.password || !this.user.confirmPassword) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    // password match
    if (this.user.password !== this.user.confirmPassword) {
      this.passwordMismatch = true;
      return;
    }
    this.passwordMismatch = false;

    const formData = new FormData();
    formData.append('username', this.user.username)
    formData.append('email', this.user.email)
    formData.append('password', this.user.password)
    if (this.avatarFile) {
      formData.append('avatar', this.avatarFile)
    }

    if (this.user.bio) {
      formData.append('bio', this.user.bio)
    }

    // const registerData: RegisterRequest = {
    //   username: this.user.username,
    //   avatar: this.avatarFile,
    //   bio: this.user.bio,
    //   email: this.user.email,
    //   password: this.user.password
    // };

    this.isLoading = true;

    this.authService.register(formData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Registration successful!';

        if (response.token) {
          this.authService.saveToken(response.token);
        }

        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.isLoading = false;

        if (error?.error?.message) {
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

  onAvatarSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    this.avatarFile = input.files[0];

    const reader = new FileReader();
    reader.onload = () => (this.avatarPreview = reader.result as string);
    reader.readAsDataURL(this.avatarFile);
  }

  // optional: call this on (ngModelChange) of password/confirmPassword
  clearPasswordMismatch() {
    if (this.passwordMismatch) this.passwordMismatch = false;
  }

  resetForm() {
    this.user = {
      username: '',
      email: '',
      avatar: '',
      password: '',
      confirmPassword: '',
      bio: ''
    };
    this.avatarFile = undefined;
    this.avatarPreview = null;


    this.submitted = false;
    this.passwordMismatch = false;
    this.isLoading = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  isFieldInvalid(value: string): boolean {
    return this.submitted && !value;
  }
}
