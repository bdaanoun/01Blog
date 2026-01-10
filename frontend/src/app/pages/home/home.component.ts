import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PostService, Post } from '../../services/post.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class Home implements OnInit {
  posts: Post[] = [];
  loading = true;
  error: string | null = null;
  activeTab: 'feed' | 'explore' | 'writers' = 'feed';


  constructor(private postService: PostService) { }

  ngOnInit(): void {
    this.loadPosts();
  }

  setActiveTab(tab: 'feed' | 'explore' | 'writers'): void {
    this.activeTab = tab;
  }

  loadPosts(): void {
    this.loading = true;
    this.error = null;

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

  getPlainTextPreview(content: string, maxLength: number = 150): string {
    try {
      const editorData = JSON.parse(content);
      let text = '';

      editorData.blocks.forEach((block: any) => {
        if (block.type === 'paragraph' || block.type === 'header') {
          text += block.data.text + ' ';
        }

        else if (block.type === 'list') {
          // unordered / ordered lists
          if (block.data.style === 'unordered' || block.data.style === 'ordered') {
            block.data.items.forEach((item: any) => {
              text += item + ' ';
            });
          }

          // checklist
          if (block.data.style === 'checklist') {
            block.data.items.forEach((item: any) => {
              text += item.content + ' ';
            });
          }
        }
      });

      // console.log("text==>   ",text);
      
      // text = text.replace(/<[^>]*>/g, '').trim();

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

      return Math.max(1, Math.ceil(wordCount / 100)); // 100 words per minute
    } catch (e) {
      return 1;
    }
  }
}