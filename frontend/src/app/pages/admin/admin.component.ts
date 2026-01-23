import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';


type AdminUser = {
    id: number;
    username: string;
    email: string;
    role?: string;
    status?: string;
    avatar?: string | null;
};

type ReportedPost = {
    reportId: number;
    postId: number;
    reason: string;
    reporterId?: number;
    reporterUsername?: string;
    createdAt?: string;
    status?: 'OPEN' | 'RESOLVED' | string;
};

@Component({
    selector: 'app-admin',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css'],
})
export class AdminComponent implements OnInit {

    constructor(private http: HttpClient) { }

    activeTab: 'users' | 'reports' = 'users';

    users: AdminUser[] = [];
    reports: ReportedPost[] = [];

    usersLoading = false;
    reportsLoading = false;

    usersError = '';
    reportsError = '';

    // Adjust if your API base is different
    private readonly adminBase = 'http://localhost:8080/api/admin';

    ngOnInit(): void {
        this.loadUsers();
    }

    //UI actions
    switchTab(tab: 'users' | 'reports') {
        this.activeTab = tab;

        if (tab === 'users' && this.users.length === 0) this.loadUsers();
        if (tab === 'reports' && this.reports.length === 0) this.loadReports();
    }

    //API calls (using fetch so you don't need HttpClient setup here)
    loadUsers() {
        // console.log("users; ", this.users);

        this.usersLoading = true;
        this.usersError = '';

        this.http.get<AdminUser[]>(`${this.adminBase}/users`).subscribe({
            next: (users) => {
                this.users = users;
                this.usersLoading = false;
            },
            error: (err) => {
                this.usersError =
                    err?.error?.message ||
                    `Failed to load users (${err.status})`;
                this.usersLoading = false;
            }
        });
    }



    async deleteUser(user: AdminUser) {
        const ok = confirm(`Delete user "${user.username}"? This cannot be undone.`);
        if (!ok) return;

        try {
            const res = await fetch(`${this.adminBase}/users/${user.id}`, {
                method: 'DELETE',
                credentials: 'include',
            });

            if (!res.ok) {
                const text = await res.text();
                throw new Error(text || `Failed to delete user (${res.status})`);
            }

            // remove from UI
            this.users = this.users.filter(u => u.id !== user.id);
        } catch (e: any) {
            alert(e?.message || 'Failed to delete user');
        }
    }

    loadReports() {
        this.reportsLoading = true;
        this.reportsError = '';

        this.http.get<ReportedPost[]>(`${this.adminBase}/reported-posts`).subscribe({
            next: (reports) => {
                this.reports = Array.isArray(reports) ? reports : [];
                this.reportsLoading = false;
            },
            error: (err) => {
                const msg =
                    err?.error?.message ||
                    (typeof err?.error === 'string' ? err.error : '') ||
                    err?.message ||
                    `Failed to load reports (${err.status})`;

                this.reportsError = msg;
                this.reportsLoading = false;
            }
        });
    }


    async deletePostFromReport(r: ReportedPost) {
        const ok = confirm(`Delete post #${r.postId}? (reported)`);
        if (!ok) return;

        try {
            const res = await fetch(`${this.adminBase}/posts/${r.postId}`, {
                method: 'DELETE',
                credentials: 'include',
            });

            if (!res.ok) {
                const text = await res.text();
                throw new Error(text || `Failed to delete post (${res.status})`);
            }

            // Option 1: remove the report row after deleting the post
            this.reports = this.reports.filter(x => x.reportId !== r.reportId);
        } catch (e: any) {
            alert(e?.message || 'Failed to delete post');
        }
    }

    // helpers
    formatDate(value?: string) {
        if (!value) return '-';
        const d = new Date(value);
        if (isNaN(d.getTime())) return value;
        return d.toLocaleString();
    }

    safeText(s?: string | null, fallback = '-') {
        const v = (s ?? '').trim();
        return v.length ? v : fallback;
    }
}
