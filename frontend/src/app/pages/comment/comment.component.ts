import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PostComment {
    id: number;
    content: string;
    createdAt: string;
    authorName: string;
    authorId: number;

    authorAvatar?: string;
}

@Injectable({ providedIn: 'root' })
export class CommentService {
    private apiUrl = 'http://localhost:8080/api';

    constructor(private http: HttpClient) { }

    getCommentsByPost(postId: number): Observable<Comment[]> {
        return this.http.get<Comment[]>(`${this.apiUrl}/posts/${postId}/comments`);
    }

    addComment(postId: number, content: string): Observable<Comment> {
        return this.http.post<Comment>(`${this.apiUrl}/posts/${postId}/comments`, {
            content,
        });
    }

    deleteComment(commentId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/comments/${commentId}`);
    }
}
