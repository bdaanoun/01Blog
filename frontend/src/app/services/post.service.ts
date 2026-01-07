import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Post {
  id?: number;
  title?: string;
  content: string;
  author?: string;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface CreatePostRequest {
  title?: string;
  content: string;
}

export interface PostResponse {
  success: boolean;
  message: string;
  data?: Post;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = 'http://localhost:8080/api/posts'; // Change to your API URL

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt_token'); // or however you store your token
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // Create a new post
  createPost(post: CreatePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(
      this.apiUrl,
      post,
      { headers: this.getHeaders() }
    );
  }

  // Get all posts
  getAllPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.apiUrl);
  }

  // Get a single post by ID
  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  // Update a post
  updatePost(id: number, post: CreatePostRequest): Observable<PostResponse> {
    return this.http.put<PostResponse>(
      `${this.apiUrl}/${id}`,
      post,
      { headers: this.getHeaders() }
    );
  }

  // Delete a post
  deletePost(id: number): Observable<PostResponse> {
    return this.http.delete<PostResponse>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // Get posts by author
  getPostsByAuthor(authorId: string): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}/author/${authorId}`);
  }
}