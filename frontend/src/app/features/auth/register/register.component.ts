// register.component.ts (STANDALONE VERSION)
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register',
  standalone: true,  // 👈 Makes it standalone
  imports: [         // 👈 Component imports what it needs directly
    CommonModule,    // Provides *ngIf, *ngFor, etc.
    FormsModule      // Provides ngModel
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  // Form data model
  user = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  // Form state
  submitted = false;
  passwordMismatch = false;

  // Handle form submission
  onSubmit() {
    this.submitted = true;

    // Check if passwords match
    if (this.user.password !== this.user.confirmPassword) {
      this.passwordMismatch = true;
      return;
    }

    this.passwordMismatch = false;

    // Log the user data (in real app, send to backend)
    console.log('Registration data:', this.user);
    alert('Registration successful!');

    // Reset form
    this.resetForm();
  }

  // Reset form data
  resetForm() {
    this.user = {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: ''
    };
    this.submitted = false;
    this.passwordMismatch = false;
  }

  // Check if field is valid
  isFieldInvalid(fieldValue: string): boolean {
    return this.submitted && !fieldValue;
  }
}