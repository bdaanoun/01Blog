import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PostComment {
    id: number;
    content: string;
    createdAt: string;
    authorName: string;
    authorId: number;
}

@Injectable({ providedIn: 'root' })
export class CommentService {
    private apiUrl = 'http://localhost:8080/api';

    constructor(private http: HttpClient) { }

    getCommentsByPost(postId: number): Observable<PostComment[]> {
        return this.http.get<PostComment[]>(`${this.apiUrl}/posts/${postId}/comments`);
    }

    addComment(postId: number, content: string): Observable<PostComment> {
        return this.http.post<PostComment>(`${this.apiUrl}/posts/${postId}/comments`, { content });
    }
}
