import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostService } from '../../services/post.service';

import { Editor } from '@tiptap/core';
import Placeholder from '@tiptap/extension-placeholder';

import StarterKit from '@tiptap/starter-kit';

@Component({
  selector: 'app-write-post',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './writePost.component.html',
  styleUrl: './writePost.component.css'
})
export class WritePostComponent implements OnInit, OnDestroy {

  @ViewChild('editor', { static: true }) editorElement!: ElementRef;

  editor!: Editor;
  content = '';
  isSubmitting = false;

  constructor(private postService: PostService) { }

  ngOnInit() {

    this.editor = new Editor({
      element: this.editorElement.nativeElement,
      extensions: [
        StarterKit,
        Placeholder.configure({
          placeholder: 'Write your post here...'
        })
      ],
      editorProps: {
        attributes: {
          class: 'tiptap-editor'
        }
      }
    });
  }

  submitPost() {
    const htmlContent = this.editor.getHTML();

    if (!htmlContent || htmlContent === '<p></p>') {
      alert('Please write something before posting!');
      return;
    }

    this.isSubmitting = true;

    this.postService.createPost({ content: htmlContent }).subscribe({
      next: (response) => {
        console.log('Post created:', response);
        alert('Post published successfully!');
        this.clearEditor();
        this.isSubmitting = false;
      },
      error: (error) => {
        console.error('Error creating post:', error);
        alert('Failed to publish post.');
        this.isSubmitting = false;
      }
    });
  }

  clearEditor() {
    this.editor.commands.clearContent();
  }

  ngOnDestroy() {
    this.editor.destroy();
  }
}
