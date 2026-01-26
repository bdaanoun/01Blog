// post-detail.component.ts
import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
// import { ErrorDialogComponent } from '../error-dialog/error-dialog.component';

import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

import { PostService, Post } from '../../services/post.service';
import { CommentService, PostComment } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';

import EditorJS from '@editorjs/editorjs';
import Header from '@editorjs/header';
import List from '@editorjs/list';
import Paragraph from '@editorjs/paragraph';
import ImageTool from '@editorjs/image';
import { Input } from '@angular/core';


import { ReportDialogComponent } from '../report/report-dialog.component';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.css'
})
export class PostDetailComponent implements OnInit, OnDestroy {
  @Input() postId?: number;
  @Input() adminPreview = false;


  post: Post | null = null;
  loading = true;
  error: string | null = null;
  renderedContent: any[] = [];

  // comments
  comments: PostComment[] = [];
  commentsLoading = false;
  commentsError: string | null = null;
  submittingComment = false;

  commentControl = new FormControl('', [
    Validators.required,
    Validators.maxLength(500)
  ]);

  // ---- Edit Post (EditorJS) ----
  @ViewChild('editEditor') editEditorRef!: ElementRef;
  private editEditor: EditorJS | null = null;

  isEditing = false;
  savingEdit = false;
  editError: string | null = null;

  editTitleControl = new FormControl('', [
    Validators.required,
    Validators.maxLength(255)
  ]);

  // ---- Edit Banner ----
  selectedBannerFile: File | null = null;
  bannerPreviewUrl: string | null = null;
  removeBannerFlag = false;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    private commentService: CommentService,
    private sanitizer: DomSanitizer,
    private dialog: MatDialog,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    if (this.postId) {
      this.loadPost(this.postId);
      return;
    }

    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadPost(+id);
  }


  ngOnDestroy(): void {
    this.destroyEditEditor();
    this.cleanupBannerPreview();
  }

  //report 
  isLoggedIn(): boolean {
    const myIdStr = this.authService.getUserIdFromToken();
    return !!myIdStr;
  }

  isMyPost(): boolean {
    if (!this.post) return false;
    const myIdStr = this.authService.getUserIdFromToken();
    const myId = myIdStr ? Number(myIdStr) : null;
    return myId !== null && myId === this.post.userId;
  }

  reporting = false;

  openReport(): void {
    if (!this.post) return;

    // extra safety: don't allow reporting your own post
    if (this.canEdit()) return;

    const dialogRef = this.dialog.open(ReportDialogComponent, {
      width: '420px',
      data: { postId: this.post.id }
    });

    dialogRef.afterClosed().subscribe((reason: string | null) => {
      if (!reason || !this.post || this.reporting) return;

      this.reporting = true;

      this.postService.reportPost(this.post.id, reason).subscribe({
        next: (res) => {
          alert(res.message || 'Report sent successfully');
          this.reporting = false;
        },
        error: (err) => {
          console.error('Report failed:', err);
          alert(err?.error?.message || 'Failed to report post');
          this.reporting = false;
        }
      });
    });
  }


  // Load post & comments

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


  // Render helpers

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

  getBannerUrl(path: string | null): string {
    if (!path) return '';
    return `http://localhost:8080/uploads/${path}`;
  }

  getUserInitial(username: string | undefined): string {
    return username ? username.charAt(0).toUpperCase() : 'U';
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
          wordCount += (block.data?.text || '').split(/\s+/).filter(Boolean).length;
        } else if (block.type === 'list') {
          const txt = JSON.stringify(block.data?.items || '');
          wordCount += txt.split(/\s+/).filter(Boolean).length;
        }
      });

      return Math.max(1, Math.ceil(wordCount / 100));
    } catch {
      return 1;
    }
  }

  // Like
  toggleLike(): void {
    if (!this.post) return;

    this.postService.toggleLike(this.post.id).subscribe({
      next: (response) => {
        if (!this.post) return;
        this.post.likedByCurrentUser = response.liked;
        this.post.likesCount = response.likesCount;
      },
      error: (err) => {
        console.error('Error toggling like:', err);
      }
    });
  }


  // Edit permissions

  canEdit(): boolean {
    if (!this.post) return false;

    const myIdStr = this.authService.getUserIdFromToken();
    const myId = myIdStr ? Number(myIdStr) : null;

    return myId !== null && myId === this.post.userId;
  }


  // Banner edit handlers

  onBannerSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.selectedBannerFile = file;
    this.removeBannerFlag = false;

    this.cleanupBannerPreview();
    this.bannerPreviewUrl = URL.createObjectURL(file);

    // allow selecting same file again
    input.value = '';
  }

  removeBanner(): void {
    this.selectedBannerFile = null;
    this.cleanupBannerPreview();
    this.bannerPreviewUrl = null;

    this.removeBannerFlag = true;
  }

  private cleanupBannerPreview(): void {
    if (this.bannerPreviewUrl) {
      URL.revokeObjectURL(this.bannerPreviewUrl);
    }
  }

  private resetBannerEditState(): void {
    this.selectedBannerFile = null;
    this.cleanupBannerPreview();
    this.bannerPreviewUrl = null;
    this.removeBannerFlag = false;
  }


  // private showErrorPopup(message: string) {
  //   this.dialog.open(ErrorDialogComponent, {
  //     width: '380px',
  //     data: { message }
  //   });
  // }


  // Edit Post (EditorJS)

  startEdit(): void {
    if (!this.post) return;

    this.isEditing = true;
    this.editError = null;
    this.editTitleControl.setValue(this.post.title || '');

    this.resetBannerEditState();

    setTimeout(() => this.initEditEditor(), 0);
  }

  private async initEditEditor(): Promise<void> {
    if (!this.post) return;

    await this.destroyEditEditor();

    // parse stored EditorJS JSON
    let data: any = { blocks: [] };
    try {
      data = JSON.parse(this.post.content || '{"blocks":[]}');
    } catch {
      data = {
        blocks: [{ type: 'paragraph', data: { text: this.post.content || '' } }]
      };
    }

    this.editEditor = new EditorJS({
      holder: this.editEditorRef.nativeElement,
      placeholder: 'Edit your post content...',
      autofocus: true,
      data,
      tools: {
        header: {
          class: Header as any,
          inlineToolbar: true,
          config: { levels: [2, 3], defaultLevel: 2 }
        },
        list: { class: List as any, inlineToolbar: true },
        paragraph: { class: Paragraph as any },
        image: {
          class: ImageTool as any,
          inlineToolbar: true,
          config: {
            field: 'image',
            types: 'image/*',
            uploader: {
              uploadByFile: async (file: File) => {
                const token = localStorage.getItem('authToken');
                const formData = new FormData();
                formData.append('image', file);

                try {
                  const response = await fetch('http://localhost:8080/api/posts/images/temp', {
                    method: 'POST',
                    headers: {
                      Authorization: `Bearer ${token ?? ''}`,
                    },
                    body: formData,
                  });

                  //  error response (413, 401, 500...)
                  if (!response.ok) {
                    let message = 'Upload failed';

                    try {
                      const data = await response.json();
                      message = data?.message || message;
                    } catch {
                      const txt = await response.text();
                      message = txt || message;
                    }

                    return { success: 0, message: message };
                  }

                  const data = await response.json();

                  // safety: if backend returns something else, still show error
                  if (data?.success !== 1 || !data?.file?.url) {
                    return { success: 0, message: 'Upload failed: invalid server response.' };
                  }

                  return data;

                } catch (err) {
                  console.error('Image upload failed:', err);
                  return { success: 0, message: 'Network error while uploading.' };
                }
              }

            }
          }
        }
      }
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editError = null;
    this.resetBannerEditState();
    this.destroyEditEditor();
  }

  async saveEdit(): Promise<void> {
    if (!this.post) return;
    if (this.editTitleControl.invalid) return;
    if (!this.editEditor) {
      this.editError = 'Editor not ready.';
      return;
    }

    this.savingEdit = true;
    this.editError = null;

    try {
      const editorData = await this.editEditor.save();

      // Use FormData to support banner update
      const fd = new FormData();
      fd.append('title', this.editTitleControl.value!);
      fd.append('content', JSON.stringify(editorData));

      if (this.selectedBannerFile) {
        fd.append('banner', this.selectedBannerFile);
      }

      // if you support removing the banner in backend
      fd.append('removeBanner', String(this.removeBannerFlag));

      // IMPORTANT: your PostService must implement updatePostFormData()
      this.postService.updatePostFormData(this.post.id, fd).subscribe({
        next: (updated) => {
          this.post = updated;
          this.renderedContent = this.parseEditorJSContent(updated.content);

          this.isEditing = false;
          this.savingEdit = false;

          this.resetBannerEditState();
          this.destroyEditEditor();
        },
        error: (err) => {
          this.savingEdit = false;
          this.editError = err?.error?.message || 'Failed to update post.';
          console.error('Update post error:', err);
        }
      });
    } catch (e) {
      this.savingEdit = false;
      this.editError = 'Failed to read editor content.';
      console.error('Editor save error:', e);
    }
  }

  private async destroyEditEditor(): Promise<void> {
    if (this.editEditor) {
      try {
        await this.editEditor.destroy();
      } catch {
        // ignore
      }
      this.editEditor = null;
    }
  }
}
