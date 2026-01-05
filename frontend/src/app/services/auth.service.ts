import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';

// Define your data models
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface RegisterResponse {
  success: boolean;
  message: string;
  userId?: string;
  token?: string;
}

export interface LoginRequest {
  identifier: string;  // Can be email or username
  password: string;
}

export interface LoginResponse {
  token: string;
  message: string;
  // user: {
  //   id: string;
  //   identifier: string;
  //   email: string;
  // };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api';

  // Observable to track authentication state
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  // Observable to track current user
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    // Initialize authentication state on service creation
    this.checkAuthStatus();
  }

  // Check authentication status on init
  private checkAuthStatus(): void {
    const isAuth = this.hasToken() && this.isAuthenticated();
    this.isAuthenticatedSubject.next(isAuth);
    
    if (isAuth) {
      const user = this.getStoredUser();
      this.currentUserSubject.next(user);
    }
  }

  // Register new user
  register(userData: RegisterRequest): Observable<RegisterResponse> {
    const url = `${this.apiUrl}/register`;

    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<RegisterResponse>(url, userData, { headers }).pipe(
      tap(response => {
        // Optionally auto-login after registration if token is provided
        if (response.token) {
          this.saveToken(response.token);
          this.isAuthenticatedSubject.next(true);
        }
      })
    );
  }

  // Login user
  login(credentials: LoginRequest): Observable<LoginResponse> {
    const url = `${this.apiUrl}/login`;

    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<LoginResponse>(url, credentials, { headers }).pipe(
      tap(response => {
        if (response.token) {
          this.saveToken(response.token);
          this.isAuthenticatedSubject.next(true);
        }
      })
    );
  }

  // Logout user
  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    this.isAuthenticatedSubject.next(false);
    this.currentUserSubject.next(null);
  }

  // Save token
  saveToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  // Get token
  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  // Save user info
  saveUser(user: any): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  // Get stored user
  getStoredUser(): any {
    const userJson = localStorage.getItem('currentUser');
    return userJson ? JSON.parse(userJson) : null;
  }

  // Check if token exists
  private hasToken(): boolean {
    return !!this.getToken();
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    // Optional: Check if token is expired
    return !this.isTokenExpired(token);
  }

  // Check if token is expired
  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      if (!payload.exp) {
        return false;
      }
      const expirationDate = new Date(payload.exp * 1000);
      return expirationDate < new Date();
    } catch (error) {
      return true;
    }
  }

  // Decode JWT token
  private decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (error) {
      return null;
    }
  }

  // Get current user
  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  // Get user ID from token
  getUserIdFromToken(): string | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }
    const decoded = this.decodeToken(token);
    return decoded?.userId || decoded?.sub || null;
  }
}