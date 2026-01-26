export default class VideoTool {
    static get toolbox() {
        return {
            title: 'Video',
            icon: `<svg width="18" height="18" viewBox="0 0 24 24">
              <path d="M10 8l6 4-6 4V8z"></path>
            </svg>`
        };
    }

    private data: { url?: string } | any;
    private wrapper!: HTMLElement;
    private config: any;

    constructor({ data, config }: any) {
        this.data = data || {};
        this.config = config || {};
    }

    render() {
        this.wrapper = document.createElement('div');
        this.wrapper.className = 'video-tool';

        if (this.data.url) {
            this.wrapper.appendChild(this.createVideo(this.data.url));
            return this.wrapper;
        }

        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'video/*';
        input.hidden = true;

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = 'Choose video';
        btn.className = 'video-btn';

        const status = document.createElement('div');
        status.className = 'video-status';

        btn.onclick = () => input.click();

        input.onchange = async () => {
            if (!input.files?.length) return;

            const file = input.files[0];
            status.textContent = 'Uploading...';

            try {
                const result = await this.config.uploader.uploadByFile(file);

                if (result?.success === 1 && result?.file?.url) {
                    this.data.url = result.file.url;

                    // Replace UI with video
                    this.wrapper.innerHTML = '';
                    this.wrapper.appendChild(this.createVideo(this.data.url));
                } else {
                    status.textContent = result?.message || 'Upload failed';
                }
            } catch {
                status.textContent = 'Upload error';
            }
        };

        this.wrapper.appendChild(btn);
        this.wrapper.appendChild(input);
        this.wrapper.appendChild(status);

        return this.wrapper;
    }

    private createVideo(url: string) {
        const video = document.createElement('video');
        video.src = url;
        video.controls = true;
        video.style.width = '100%';
        video.style.borderRadius = '8px';
        return video;
    }

    save() {
        return { url: this.data.url || '' };
    }

    validate(savedData: any) {
        return !!savedData.url;
    }
}
