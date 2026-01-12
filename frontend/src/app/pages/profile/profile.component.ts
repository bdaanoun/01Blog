import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PostService, Post, User } from '../../services/post.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  userPosts: Post[] = [];
  loading = true;
  error: string | null = null;
  isOwnProfile = false;
  activeTab: 'posts' | 'about' = 'posts';

  constructor(
    private route: ActivatedRoute,
    private postService: PostService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const userId = +params['id'];
      this.loadUserProfile(userId);
      this.loadUserPosts(userId);
    });
  }

  loadUserProfile(userId: number): void {
    this.loading = true;
    this.error = null;

    this.postService.getUserById(userId).subscribe({
      next: (user) => {
        this.user = user;
        this.checkIfOwnProfile();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load user profile';
        this.loading = false;
        console.error('Error loading profile:', err);
      }
    });
  }

  loadUserPosts(userId: number): void {
    this.postService.getUserPosts(userId).subscribe({
      next: (posts) => {
        this.userPosts = posts.reverse();
      },
      error: (err) => {
        console.error('Error loading user posts:', err);
      }
    });
  }

  checkIfOwnProfile(): void {
    const currentUserId = this.getCurrentUserId();
    // console.log("checker:  ", currentUserId, "  ", this.user?.id);
    // console.log("t or f:  ", this.isOwnProfile);

    if (currentUserId && this.user) {
      this.isOwnProfile = currentUserId === this.user.id;
    }
  }

  getCurrentUserId(): number | null {

    const token = localStorage.getItem('authToken');
    if (!token) return null;
    
    try {
      const payload = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payload));
      // console.log("t oken:  ", decodedPayload);
      return decodedPayload.id ?? null;
    } catch (e) {
      console.error('Invalid token', e);
      return null;
    }

  }

  toggleFollow(): void {
    if (!this.user) return;

    this.postService.toggleFollow(this.user.id).subscribe({
      next: (response) => {
        if (this.user) {
          this.user.isFollowing = response.isFollowing;
          if (this.user.followersCount !== undefined) {
            this.user.followersCount = response.isFollowing
              ? this.user.followersCount + 1
              : this.user.followersCount - 1;
          }
        }
      },
      error: (err) => {
        console.error('Error toggling follow:', err);
      }
    });
  }

  getUserInitial(username: string | undefined): string {
    return username ? username.charAt(0).toUpperCase() : 'U';
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
}