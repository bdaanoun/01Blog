import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PostService, Post } from '../../services/post.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { CommentService, PostComment } from '../../services/comment.service';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
// import { CommentService, PostComment } from '../comment/comment.component';

@Component({
    selector: 'app-post-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule],
    templateUrl: './post-detail.component.html',
    styleUrl: './post-detail.component.css'
})
export class PostDetailComponent implements OnInit {
    post: Post | null = null;
    loading = true;
    error: string | null = null;
    renderedContent: any[] = [];

    //comments
    comments: PostComment[] = [];
    commentsLoading = false;
    commentsError: string | null = null;

    newComments = '';
    submittingComment = false;

    constructor(
        private route: ActivatedRoute,
        private postService: PostService,
        private commentService: CommentService,
        private sanitizer: DomSanitizer
    ) { }

    ngOnInit(): void {
        const postId = this.route.snapshot.paramMap.get('id');
        if (postId) {
            this.loadPost(+postId);
        }
    }

    loadPost(id: number): void {
        this.loading = true;
        this.error = null;

        this.postService.getPostById(id).subscribe({
            next: (post) => {
                this.post = post;
                this.renderedContent = this.parseEditorJSContent(post.content);
                this.loading = false;

                this.loadComments(post.id);
            },
            error: (err) => {
                this.error = 'Failed to load post';
                this.loading = false;
                console.error('Error loading post:', err);
            }
        });
    }

    loadComments(postId: number): void {
        this.commentsLoading = true;
        this.commentsError = null;

        this.commentService.getCommentsByPost(postId).subscribe({
            next: (comments) => {
                this.comments = comments || [];
                this.commentsLoading = false;
            },
            error: (err) => {
                this.commentsError = 'Failed to load comments';
                this.commentsLoading = false;
                console.error('Error loading comments:', err);
            }
        });
    }
    commentControl = new FormControl('', [
        Validators.required,
        Validators.maxLength(500)
    ]);

    // newComment = '';

    submitComment(): void {
        if (!this.post || this.commentControl.invalid) return;

        const content = this.commentControl.value?.trim();
        if (!content) return;

        this.submittingComment = true;

        this.commentService.addComment(this.post.id, content).subscribe({
            next: (created) => {
                this.comments = [created, ...this.comments];
                this.commentControl.reset();
                this.submittingComment = false;
            },
            error: (err) => {
                console.error('Error adding comment:', err);
                this.submittingComment = false;
            }
        });
    }

    parseEditorJSContent(content: string): any[] {
        try {
            const editorData = JSON.parse(content);
            return editorData.blocks || [];
        } catch (e) {
            console.error('Error parsing content:', e);
            return [];
        }
    }

    getSafeHtml(html: string): SafeHtml {
        if (!html) return '';
        return this.sanitizer.bypassSecurityTrustHtml(html);
    }

    toggleLike(): void {
        if (!this.post) return;

        this.postService.toggleLike(this.post.id).subscribe({
            next: (response) => {
                if (this.post) {
                    this.post.likedByCurrentUser = response.liked;
                    this.post.likesCount = response.likesCount;
                }
            },
            error: (err) => {
                console.error('Error toggling like:', err);
            }
        });
    }

    getBannerUrl(banner: string | null): string {
        if (!banner) return '';
        return `http://localhost:8080/uploads/${banner}`;
    }
    // getAvatarUrl(avatar: string | null): string {
    //     if (!avatar) return '';
    //     return `http://localhost:8080/uploads/${avatar}`;
    // }


    getTimeAgo(dateString: string): string {
        const date = new Date(dateString);
        const now = new Date();
        const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

        const intervals: { [key: string]: number } = {
            year: 31536000,
            month: 2592000,
            week: 604800,
            day: 86400,
            hour: 3600,
            minute: 60
        };

        for (const [unit, secondsInUnit] of Object.entries(intervals)) {
            const interval = Math.floor(seconds / secondsInUnit);
            if (interval >= 1) {
                return `${interval} ${unit}${interval > 1 ? 's' : ''} ago`;
            }
        }

        return 'Just now';
    }

    getUserInitial(username: string | undefined): string {
        return username ? username.charAt(0).toUpperCase() : 'U';
    }

    getReadTime(content: string): number {
        try {
            const editorData = JSON.parse(content);
            let wordCount = 0;

            editorData.blocks.forEach((block: any) => {
                if (block.type === 'paragraph' || block.type === 'header') {
                    wordCount += block.data.text.split(/\s+/).length;
                } else if (block.type === 'list') {
                    wordCount += block.data.items.join(' ').split(/\s+/).length;
                }
            });

            return Math.max(1, Math.ceil(wordCount / 100));
        } catch (e) {
            return 1;
        }
    }
}