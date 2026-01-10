import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PostService, Post } from '../../services/post.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
    selector: 'app-post-detail',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './post-detail.component.html',
    styleUrl: './post-detail.component.css'
})
export class PostDetailComponent implements OnInit {
    post: Post | null = null;
    loading = true;
    error: string | null = null;
    renderedContent: any[] = [];

    constructor(
        private route: ActivatedRoute,
        private postService: PostService,
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
            },
            error: (err) => {
                this.error = 'Failed to load post';
                this.loading = false;
                console.error('Error loading post:', err);
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

    getAuthorInitial(authorName: string | undefined): string {
        return authorName ? authorName.charAt(0).toUpperCase() : 'U';
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