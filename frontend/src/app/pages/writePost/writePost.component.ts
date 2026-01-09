import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import EditorJS from '@editorjs/editorjs';
import Header from '@editorjs/header';
import List from '@editorjs/list';
import Paragraph from '@editorjs/paragraph';
import ImageTool from '@editorjs/image';

@Component({
  selector: 'app-write-post',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './writePost.component.html',
  styleUrls: ['./writePost.component.css']
})
export class WritePostComponent implements AfterViewInit, OnDestroy {

  @ViewChild('editor') editorRef!: ElementRef;
  @ViewChild('imageInput') imageInputRef!: ElementRef<HTMLInputElement>;

  editor!: EditorJS;

  bannerFile: File | null = null;
  bannerPreview: string | ArrayBuffer | null = null;
  title: string = '';

  // Slash menu
  showSlashMenu = false;
  menuX = 0;
  menuY = 0;
  slashItems = [
    { type: 'header', label: 'Header H2' },
    { type: 'list', label: 'List' },
    { type: 'image', label: 'Image' }
  ];

  ngAfterViewInit(): void {
    const token = localStorage.getItem('authToken');
    
    this.editor = new EditorJS({
      holder: this.editorRef.nativeElement,
      placeholder: 'Write your post content here...',
      autofocus: true,

      tools: {
        header: {
          class: Header as any,
          inlineToolbar: true,
          config: {
            levels: [2, 3],
            defaultLevel: 2
          }
        },
        list: { class: List as any, inlineToolbar: true },
        paragraph: { class: Paragraph as any },

        image: {
          class: ImageTool as any,
          inlineToolbar: true,
          config: {
            endpoints: {
              byFile: 'http://localhost:8080/api/posts/images/temp'
            },
            field: 'image',
            types: 'image/*',
            additionalRequestHeaders: {
              'Authorization': `Bearer ${token}`
            },
            uploader: {
              /**
               * Upload file to the server and return an uploaded image data
               * @param {File} file - file selected from the device or pasted by drag-n-drop
               * @return {Promise.<{success, file: {url}}>}
               */
              uploadByFile: async (file: File) => {
                const formData = new FormData();
                formData.append('image', file);

                try {
                  const response = await fetch('http://localhost:8080/api/posts/images/temp', {
                    method: 'POST',
                    headers: {
                      'Authorization': `Bearer ${token}`
                    },
                    body: formData
                  });

                  if (!response.ok) {
                    throw new Error('Upload failed');
                  }

                  const data = await response.json();
                  console.log('Image uploaded:', data);
                  console.log("image:  ",data);
                  

                  return data; // Should return { success: 1, file: { url: "..." } }
                  
                } catch (error) {
                  console.error('Error uploading image:', error);
                  return {
                    success: 0,
                    message: 'Upload failed'
                  };
                }
              }
            }
          }
        }
      }
    });

    // Listen to keyup events on the editor container
    this.editorRef.nativeElement.addEventListener('keyup', (event: KeyboardEvent) => {
      this.checkForSlash(event);
    });
  }

  // Banner preview
  onBannerSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.bannerFile = input.files[0];
      const reader = new FileReader();
      reader.onload = () => this.bannerPreview = reader.result;
      reader.readAsDataURL(this.bannerFile);
    }
  }

  // Slash menu
  checkForSlash(event: KeyboardEvent) {
    const selection = window.getSelection();
    if (!selection) return;

    const text = selection.anchorNode?.textContent;
    if (text && text.endsWith('/')) {
      const range = selection.getRangeAt(0);
      const rect = range.getBoundingClientRect();
      this.menuX = rect.left;
      this.menuY = rect.bottom + window.scrollY;
      this.showSlashMenu = true;
    }
  }

  // Handle local image selection for editor (from slash menu)
  onImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    const token = localStorage.getItem('authToken');
    
    // Upload to server instead of creating blob URL
    const formData = new FormData();
    formData.append('image', file);

    fetch('http://localhost:8080/api/posts/images/temp', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    .then(response => response.json())
    .then(data => {
      if (data.success === 1) {
        const index = this.editor.blocks.getCurrentBlockIndex() + 1;
        this.editor.blocks.insert('image', { 
          file: { url: data.file.url } 
        }, {}, index);
      }
    })
    .catch(error => {
      console.error('Error uploading image:', error);
      alert('Failed to upload image');
    });

    input.value = '';
  }

  insertSlashBlock(item: { type: string, label: string }) {
    const index = this.editor.blocks.getCurrentBlockIndex() + 1;

    switch (item.type) {
      case 'header':
        this.editor.blocks.insert('header', { text: 'New Header' }, {}, index);
        break;
      case 'list':
        this.editor.blocks.insert('list', { items: ['List item'] }, {}, index);
        break;
      case 'image':
        this.imageInputRef.nativeElement.click();
        break;
    }

    this.showSlashMenu = false;
  }

  async saveContent() {
    if (!this.title.trim()) { 
      alert('Please enter a title.'); 
      return; 
    }

    try {
      const editorData = await this.editor.save();
      const formData = new FormData();
      
      if (this.bannerFile) {
        formData.append('banner', this.bannerFile);
      }
      
      formData.append('title', this.title);
      formData.append('content', JSON.stringify(editorData));
      
      console.log("Editor data:", editorData);
      
      const token = localStorage.getItem("authToken");
      
      const response = await fetch('http://localhost:8080/api/posts', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });

      if (response.ok) {
        alert('Post saved successfully!');
        this.editor.clear();
        this.title = '';
        this.bannerFile = null;
        this.bannerPreview = null;
      } else {
        const error = await response.text();
        console.error('Error saving post:', error);
        alert('Failed to save post.');
      }
    } catch (err) {
      console.error('Error:', err);
      alert('An error occurred while saving the post.');
    }
  }

  ngOnDestroy(): void {
    if (this.editor) this.editor.destroy();
  }
}