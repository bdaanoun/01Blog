import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PostService, Post } from '../../services/post.service';

interface User {
  id: number;
  username: string;
  email?: string;
  bio?: string;
  avatar?: string;
  followersCount?: number;
  followingCount?: number;
  isFollowing?: boolean;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class Home implements OnInit {
  posts: Post[] = [];
  writers: User[] = [];
  loading = true;
  error: string | null = null;
  activeTab: 'explore' | 'feed' | 'writers' = 'explore';
  currentUserId: number | null = null;

  constructor(private postService: PostService) { }

  ngOnInit(): void {
    this.currentUserId = this.getCurrentUserId();
    this.loadContent();
  }

  getCurrentUserId(): number | null {
    const token = localStorage.getItem('authToken');
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payload));
      return decodedPayload.id ?? null;
    } catch (e) {
      console.error('Invalid token', e);
      return null;
    }
  }

  setActiveTab(tab: 'explore' | 'feed' | 'writers'): void {
    this.activeTab = tab;
    this.loadContent();
  }

  loadContent(): void {
    this.loading = true;
    this.error = null;

    switch (this.activeTab) {
      case 'explore':
        this.loadAllPosts();
        break;
      case 'feed':
        this.loadFollowingPosts();
        break;
      case 'writers':
        this.loadWriters();
        break;
    }
  }

  loadAllPosts(): void {
    this.postService.getAllPosts().subscribe({
      next: (posts) => {
        this.posts = posts.reverse();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load posts';
        this.loading = false;
        console.error('Error loading posts:', err);
      }
    });
  }

  loadFollowingPosts(): void {
    this.postService.getFollowingPosts().subscribe({
      next: (posts) => {
        console.log("pppppp", posts);

        this.posts = posts.reverse();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load feed posts';
        this.loading = false;
        console.error('Error loading feed posts:', err);
      }
    });
  }

  loadWriters(): void {
    this.postService.getAllWriters().subscribe({
      next: (writers) => {
        // Filter out the current user from the writers list
        this.writers = writers.filter(writer => writer.id !== this.currentUserId);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load writers';
        this.loading = false;
        console.error('Error loading writers:', err);
      }
    });
  }

  getUserInitial(username: string | undefined): string {
    return username ? username.charAt(0).toUpperCase() : 'U';
  }

  getAvatarUrl(avatar: string | null | undefined): string {
    if (!avatar) return '';
    return `http://localhost:8080/uploads/${avatar}`;
  }

  toggleLike(post: Post, event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    this.postService.toggleLike(post.id).subscribe({
      next: (response) => {
        post.likedByCurrentUser = response.liked;
        post.likesCount = response.likesCount;
      },
      error: (err) => {
        console.error('Error toggling like:', err);
      }
    });
  }

  toggleFollow(user: User, event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    this.postService.toggleFollow(user.id).subscribe({
      next: (response) => {
        console.log('Toggle response:', response);
        user.isFollowing = response.isFollowing;
        if (user.followersCount !== undefined) {
          user.followersCount = response.isFollowing
            ? user.followersCount + 1
            : user.followersCount - 1;
        }
      },
      error: (err) => {
        console.error('Error toggling follow:', err);
      }
    });
  }

  getPlainTextPreview(content: string, maxLength: number = 150): string {
    try {
      const editorData = JSON.parse(content);
      let text = '';

      editorData.blocks.forEach((block: any) => {
        if (block.type === 'paragraph' || block.type === 'header') {
          text += block.data.text + ' ';
        }
        else if (block.type === 'list') {
          if (block.data.style === 'unordered' || block.data.style === 'ordered') {
            block.data.items.forEach((item: any) => {
              text += item + ' ';
            });
          }
          if (block.data.style === 'checklist') {
            block.data.items.forEach((item: any) => {
              text += item.content + ' ';
            });
          }
        }
      });

      return text.length > maxLength
        ? text.substring(0, maxLength) + '...'
        : text;

    } catch (e) {
      return 'No preview available';
    }
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