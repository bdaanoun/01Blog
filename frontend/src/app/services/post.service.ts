import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Post {
  id: number;
  title: string;
  content: string;
  banner: string | null;
  status: string;
  createdAt: string;
  userId: number;
  authorName?: string;
  authorAvatar?: string;
  likesCount: number;
  likedByCurrentUser: boolean;
}

export interface User {
  id: number;
  username: string;
  email?: string;
  bio?: string;
  avatar?: string;
  followersCount?: number;
  followingCount?: number;
  isFollowing?: boolean;
}

export interface FollowResponse {
  isFollowing: boolean;
  followersCount?: number;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = 'http://localhost:8080/api/posts';
  private usersApiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) { }

  // Get all posts (Explore tab)
  getAllPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.apiUrl);
  }
  //
  updatePostFormData(postId: number, fd: FormData) {
    return this.http.patch<Post>(`http://localhost:8080/api/posts/${postId}`, fd);
  }

  // Get posts from users the current user follows (Feed tab)
  getFollowingPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}/following`);
  }

  // Get all writers/users (Writers tab)
  getAllWriters(): Observable<User[]> {
    // console.log("------->",this.usersApiUrl);    
    return this.http.get<User[]>(this.usersApiUrl);
  }

  // Get a single post by ID
  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }


  getPostByIdForAdmin(id: number) {
    return this.http.get<Post>(`http://localhost:8080/api/admin/posts/${id}`);
  }

  //report post
  reportPost(postId: number, reason: string) {
    return this.http.post<{ message: string }>(
      `http://localhost:8080/api/report/posts/${postId}`,
      { reason }
    );
  }
  //report user
  reportUser(userId: number, reason: string) {
    return this.http.post<{ message: string }>(
      `http://localhost:8080/api/report/users/${userId}`,
      { reason }
    );
  }

  // Toggle like on a post
  toggleLike(postId: number): Observable<{ liked: boolean; likesCount: number }> {
    return this.http.post<{ liked: boolean; likesCount: number }>(
      `${this.apiUrl}/${postId}/like`,
      {}
    );
  }


  //edit post
  updatePost(id: number, body: { title: string; content: string }) {
    return this.http.patch<Post>(`http://localhost:8080/api/posts/${id}`, body);
  }


  // Toggle follow on a user
  toggleFollow(userId: number): Observable<FollowResponse> {
    // console.log("url---->  ", this.usersApiUrl);
    return this.http.post<FollowResponse>(
      `${this.usersApiUrl}/follow/${userId}`,
      {}
    );
  }

  // Delete a post
  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.usersApiUrl}/${userId}`);
  }

  // Get posts by user ID
  getUserPosts(userId: number): Observable<Post[]> {
    // console.log("url:   ", `${this.usersApiUrl}/${userId}/posts`);
    return this.http.get<Post[]>(`${this.usersApiUrl}/${userId}/posts`);
  }
}