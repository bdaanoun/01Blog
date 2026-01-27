import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

export interface NotificationDto {
    id: number;
    senderId: number;
    postId?: number | null;
    senderUsername: string;
    content: string;
    createdAt: string;
    status: 'UNREAD' | 'READ';
    notifType: 'FOLLOW' | 'NEW_POST';
}


@Injectable({ providedIn: 'root' })
export class NotificationService {
    private apiUrl = 'http://localhost:8080/api/notifications';

    constructor(private http: HttpClient) { }

    getMyNotifications() {
        return this.http.get<NotificationDto[]>(this.apiUrl);
    }

    getUnreadCount() {
        return this.http.get<number>(`${this.apiUrl}/unread-count`);
    }

    markAsRead(id: number) {
        return this.http.patch<void>(`${this.apiUrl}/${id}/read`, {});
    }

    markAsUnread(id: number) {
        return this.http.patch<void>(`${this.apiUrl}/${id}/unread`, {});
    }

    markAllAsRead() {
        return this.http.patch<void>(`${this.apiUrl}/read-all`, {});
    }
}
