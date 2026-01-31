import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { PostDetailComponent } from '../post-detail/post-detail.component';

@Component({
    standalone: true,
    imports: [MatDialogModule, MatButtonModule, PostDetailComponent],
    template: `
    <div class="dialog-wrap">
      <app-post-detail [postId]="data.postId" [adminPreview]="true"></app-post-detail>
    </div>
  `,
    styles: [`
    .dialog-wrap { max-height: 80vh; overflow: auto; }
    .actions { display: flex; justify-content: flex-end; padding: 8px; }
  `]
})
export class PostDetailPreviewDialogComponent {
    constructor(@Inject(MAT_DIALOG_DATA) public data: { postId: number }) { }
}
