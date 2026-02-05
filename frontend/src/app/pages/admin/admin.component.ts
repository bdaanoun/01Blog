import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AdminService } from '../../services/admin.service';

import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PostDetailPreviewDialogComponent } from '../post-detail/post-detail-preview-dialog.component';
import { Toast } from '../../shared/toast/toast';


type AdminUser = {
    id: number;
    username: string;
    email: string;
    role?: string;
    status?: 'ACTIVE' | 'BANNED' | string;
    avatar?: string | null;
};


type ReportedPost = {
    reportId: number;
    postId: number;
    reason: string;
    reporterId: number;
    reporterUsername?: string;
    authorId: number;
    authorUsername?: string;
    reportedAt?: string;
    status?: 'OPEN' | 'RESOLVED' | string;
};

type ReportedUser = {
    reportId: number;
    reportedUserId: number;
    reportedUsername?: string;
    reporterId: number;
    reporterUsername?: string;
    reason: string;
    reportedAt?: string;
    status?: 'OPEN' | 'RESOLVED' | string;
};
type PostPannel = {
    authorUsername: string;
    id: number;
    status: string;
    title: string;
    userId: string;
};

@Component({
    selector: 'app-admin',
    standalone: true,
    imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule, MatDialogModule],
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css'],
})

export class AdminComponent implements OnInit {

    constructor(private http: HttpClient, private adminService: AdminService, private dialog: MatDialog, private toast: Toast) { }

    // activeTab: 'users' | 'reports' = 'users';
    activeTab: 'users' | 'posts' | 'reports' | 'userReports' = 'users';

    users: AdminUser[] = [];
    posts: PostPannel[] = [];
    reports: ReportedPost[] = [];
    userReports: ReportedUser[] = [];

    userReportsLoading = false;
    userReportsError = '';

    postsloading = false;
    postsError = '';


    usersLoading = false;
    reportsLoading = false;
    isForbidden = false;

    usersError = '';
    reportsError = '';

    private readonly adminBase = 'http://localhost:8080/api/admin';

    ngOnInit(): void {
        this.loadUsers();
    }

    openPostPreview(postId: number) {
        this.dialog.open(PostDetailPreviewDialogComponent, {
            width: '900px',
            maxWidth: '95vw',
            data: { postId }
        });
    }

    clearPostReport(r: any) {
        this.adminService.deletePostReport(r.reportId).subscribe({
            next: () => {
                this.reports = this.reports.filter(x => x.reportId !== r.reportId);
                console.log("clear report", r);
            },
            error: (err) => {
                console.error("Failed to clear report", err);
            }
        });
    }

    clearUserReport(r: ReportedUser) {
        this.adminService.deleteUserReport(r.reportId).subscribe({
            next: () => {
                this.userReports = this.userReports.filter(x => x.reportId !== r.reportId);
                console.log("clear user report", r);
            },
            error: (err) => {
                console.error("Failed to clear user report", err);
            }
        });
    }



    banUser(user: AdminUser) {
        this.http.patch(`${this.adminBase}/users/${user.id}/ban`, {}).subscribe({
            next: () => {
                user.status = 'BANNED';
                this.toast.success(`User "${user.username}" has been banned.`);
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to ban user')
        });
    }

    unbanUser(user: AdminUser) {
        this.http.patch(`${this.adminBase}/users/${user.id}/unban`, {}).subscribe({
            next: () => {
                user.status = 'ACTIVE';
                this.toast.success(`User "${user.username}" has been unbanned.`);
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to unban user')
        });
    }
    resolvePostReport(r: ReportedPost) {
        this.http.patch(`${this.adminBase}/post-reports/${r.reportId}/resolve`, {}).subscribe({
            next: () => {
                r.status = 'RESOLVED';
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to resolve report')
        });
    }
    resolveUserReport(r: ReportedUser) {
        this.http.patch(`${this.adminBase}/user-reports/${r.reportId}/resolve`, {}).subscribe({
            next: () => {
                r.status = 'RESOLVED';
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to resolve report')
        });
    }
    askBanUser(u: AdminUser) {
        this.openConfirm(`Ban "${u.username}"?`, () => this.banUser(u));
    }

    askUnbanUser(u: AdminUser) {
        this.openConfirm(`Unban "${u.username}"?`, () => this.unbanUser(u));
    }

    askResolvePostReport(r: ReportedPost) {
        this.openConfirm(`Mark report #${r.reportId} as RESOLVED?`, () => this.resolvePostReport(r));
    }

    askResolveUserReport(r: ReportedUser) {
        this.openConfirm(`Mark report #${r.reportId} as RESOLVED?`, () => this.resolveUserReport(r));
    }

    //actions
    switchTab(tab: 'users' | 'posts' | 'reports' | 'userReports') {
        this.activeTab = tab;

        if (tab === 'users' && this.users.length === 0) this.loadUsers();
        if (tab === 'posts' && this.posts.length === 0) this.loadPosts();
        if (tab === 'reports' && this.reports.length === 0) this.loadReports();
        if (tab === 'userReports' && this.userReports.length === 0) this.loadUserReports();
    }

    toggleRole(user: any) {
        const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';

        this.adminService.updateUserRole(user.id, newRole).subscribe({
            next: () => {
                // Update UI instantly
                user.role = newRole;
            },
            error: (err) => {
                console.error('Failed to update role', err);
                this.toast.error(err?.error?.message)
            }
        });
    }


    loadPosts() {
        this.postsloading = true;
        this.postsError = '';
        this.http.get<PostPannel[]>(`${this.adminBase}/posts`).subscribe({
            next: (posts) => {
                console.log("posts:   ", posts);

                this.posts = posts;
                this.postsloading = false;
            },
            error: (err) => {
                if (err.status === 403) {
                    this.postsError = "You don't have access here";
                    this.postsloading = false;
                    this.isForbidden = true;
                    return;
                }

                const msg =
                    err?.error?.message ||
                    (typeof err?.error === 'string' ? err.error : '') ||
                    err?.message ||
                    `Failed to load posts (${err.status})`;

                this.postsError = msg;
                this.postsloading = false;
            }
        });

    }
    loadUserReports() {
        this.userReportsLoading = true;
        this.userReportsError = '';

        this.http.get<ReportedUser[]>(`${this.adminBase}/reported-users`).subscribe({
            next: (data) => {
                this.userReports = Array.isArray(data) ? data : [];
                this.userReportsLoading = false;
            },
            error: (err) => {
                if (err.status === 403) {
                    this.userReportsError = "You don't have access here";
                    this.userReportsLoading = false;
                    this.isForbidden = true;
                    return;
                }

                const msg =
                    err?.error?.message ||
                    (typeof err?.error === 'string' ? err.error : '') ||
                    err?.message ||
                    `Failed to load reported users (${err.status})`;

                this.userReportsError = msg;
                this.userReportsLoading = false;
            }
        });
    }



    loadUsers() {
        this.usersLoading = true;
        this.usersError = '';

        this.http.get<AdminUser[]>(`${this.adminBase}/users`).subscribe({
            next: (users) => {
                this.users = users;
                this.usersLoading = false;
            },
            error: (err) => {
                if (err.status == 403) {
                    this.usersError = "You don't have access here";
                    this.usersLoading = false;
                    this.isForbidden = true;
                    return;
                }
                this.usersError =
                    err?.error?.message ||
                    `Failed to load users (${err.status})`;
                this.usersLoading = false;
            }
        });
    }

    askHideReportedPost(r: ReportedPost) {
        this.openConfirm(
            `Hide post #${r.postId}?`,
            () => this.hidePostById(r.postId)
        );
    }

    hidePostById(postId: number) {
        this.http.patch(`${this.adminBase}/posts/${postId}/hide`, {}).subscribe({
            next: () => {
                // optional: show feedback
                console.log(`Post ${postId} hidden`);
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to hide post')
        });
    }


    askHidePost(post: PostPannel) {
        this.openConfirm(`Hide post "${this.safeText(post.title)}"?`, () => this.hidePost(post));
    }

    hidePost(post: any) {
        this.http.patch(`${this.adminBase}/posts/${post.id}/hide`, {}).subscribe({
            next: () => {
                post.status = 'HIDDEN';
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to hide post')
        });
    }

    showPost(post: any) {
        this.http.patch(`${this.adminBase}/posts/${post.id}/show`, {}).subscribe({
            next: () => {
                post.status = 'PUBLISHED';
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to show post')
        });
    }

    askDeletePostFromAdmin(post: PostPannel) {
        this.openConfirm(
            `Delete post "${this.safeText(post.title)}"? This cannot be undone.`,
            () => this.deletePostById(post.id)
        );
    }

    deletePostById(postId: number) {
        this.http.delete(`${this.adminBase}/posts/${postId}`).subscribe({
            next: () => {
                this.posts = this.posts.filter(p => p.id !== postId);
                this.reports = this.reports.filter(r => r.postId !== postId);
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to delete post')
        });
    }



    deleteUser(user: AdminUser) {
        this.http.delete(`${this.adminBase}/users/${user.id}`).subscribe({
            next: () => {
                this.users = this.users.filter(u => u.id !== user.id);
            },
            error: (err) => {

                this.toast.error(err?.error?.message || 'Failed to delete user');
            }
        });
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

                if (err.status === 403) {
                    this.reportsError = "You don't have access here";
                    this.reportsLoading = false;
                    this.isForbidden = true;
                    return;
                }

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


    deletePost(postId: number, reportId: number) {
        this.http.delete(`${this.adminBase}/posts/${postId}`).subscribe({
            next: () => {
                this.reports = this.reports.filter(r => r.reportId !== reportId);
            },
            error: (err) => {
                this.toast.error(err?.error?.message || 'Failed to delete post');
            }
        });
    }

    confirmMessage = '';
    confirmAction: (() => void) | null = null;
    showConfirm = false;

    openConfirm(message: string, action: () => void) {
        this.confirmMessage = message;
        this.confirmAction = action;
        this.showConfirm = true;
    }

    confirmYes() {
        this.showConfirm = false;
        this.confirmAction?.();
        this.confirmAction = null;
    }


    confirmNo() {
        this.showConfirm = false;
        this.confirmAction = null;
    }

    askDeleteUser(user: AdminUser) {
        this.openConfirm(
            `Delete user "${user.username}"? This cannot be undone.`,
            () => this.deleteUser(user)
        );
    }

    askDeleteUserFromReport(r: ReportedUser) {
        const name = this.safeText(r.reportedUsername);
        this.openConfirm(
            `Delete user "${name}"? This cannot be undone.`,
            () => this.deleteUserById(r.reportedUserId)
        );
    }

    deleteUserById(userId: number) {
        this.http.delete(`${this.adminBase}/users/${userId}`).subscribe({
            next: () => {
                this.users = this.users.filter(u => u.id !== userId);
                this.userReports = this.userReports.filter(r => r.reportedUserId !== userId);
            },
            error: (err) => this.toast.error(err?.error?.message || 'Failed to delete user')
        });
    }

    askDeletePost(r: ReportedPost) {
        this.openConfirm(
            `Delete post #${r.postId}?`,
            () => this.deletePost(r.postId, r.reportId!)
        );
    }

    // helpers
    formatDate(value?: string) {
        if (!value) return '-';
        const d = new Date(value);
        if (isNaN(d.getTime())) return value;
        return d.toLocaleString();
    }

    safeText(s?: string | null, fallback = '-', max = 20) {
        const v = (s ?? '').trim();

        if (!v.length) return fallback;

        return v.length > max
            ? v.slice(0, max) + '...'
            : v;
    }

}
