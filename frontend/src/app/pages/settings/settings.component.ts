import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
// import { ReactiveFormsModule, FormBuilder, Validators } from "@angular/forms";
import { HttpClient } from "@angular/common/http";
import { MatIconModule } from "@angular/material/icon";
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from "@angular/forms";

type ProfileDTO = {
    id?: number;
    username: string;
    email: string;
    bio?: string | null;
    avatar?: string | null; // can be URL from backend
};

@Component({
    standalone: true,
    selector: "app-settings",
    imports: [CommonModule, ReactiveFormsModule, MatIconModule],
    templateUrl: "./settings.component.html",
    styleUrls: ["./settings.component.css"],
})
export class SettingsComponent implements OnInit {
    form!: FormGroup;
    userId: number | null = null;

    private readonly usersUrl = "/api/users";


    isLoading = false;
    isSaving = false;
    isEditing = false;

    errorMessage = "";
    successMessage = "";

    originalProfile: ProfileDTO | null = null;

    // avatar
    selectedAvatarFile: File | null = null;
    avatarPreviewUrl: string | null = null;

    constructor(private fb: FormBuilder, private http: HttpClient) { }

    ngOnInit(): void {
        this.form = this.fb.group({
            username: ["", [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
            email: [{ value: "", disabled: true }],
            bio: ["", [Validators.maxLength(500)]],
        });

        this.form.disable(); // start in view mode

        this.userId = this.getCurrentUserId();
        if (!this.userId) {
            this.errorMessage = "Not logged in.";
            return;
        }

        this.loadMyProfile(this.userId);
    }


    loadMyProfile(userId: number): void {
        this.isLoading = true;
        this.errorMessage = "";
        this.successMessage = "";

        this.http.get<ProfileDTO>(`${this.usersUrl}/${userId}`).subscribe({
            next: (p) => {
                this.originalProfile = p;

                this.form.patchValue({
                    username: p.username ?? "",
                    email: p.email ?? "",
                    bio: p.bio ?? "",
                });

                this.avatarPreviewUrl = p.avatar ? this.getAvatarUrl(p.avatar) : null;
                this.selectedAvatarFile = null;

                this.isLoading = false;
            },
            error: () => {
                this.isLoading = false;
                this.errorMessage = "Failed to load profile.";
            },
        });
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

    startEdit(): void {
        // console.log(this.profileUrl);
        this.isEditing = true;
        this.successMessage = "";
        this.errorMessage = "";
        this.form.enable();
        // keep email disabled even in edit mode
        this.form.get("email")?.disable();

    }

    cancelEdit(): void {
        this.isEditing = false;
        this.successMessage = "";
        this.errorMessage = "";

        // restore original values
        if (this.originalProfile) {
            this.form.reset({
                username: this.originalProfile.username ?? "",
                email: this.originalProfile.email ?? "",
                bio: this.originalProfile.bio ?? "",
            });

            this.avatarPreviewUrl = this.originalProfile.avatar ? this.originalProfile.avatar : null;
            this.selectedAvatarFile = null;
        }

        this.form.disable();
    }

    onAvatarSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (!file) return;

        // simple client-side checks
        const allowed = ["image/png", "image/jpeg", "image/webp"];
        if (!allowed.includes(file.type)) {
            this.errorMessage = "Avatar must be PNG, JPG, or WEBP.";
            input.value = "";
            return;
        }

        if (file.size > 3 * 1024 * 1024) {
            this.errorMessage = "Avatar is too large. Max 3MB.";
            input.value = "";
            return;
        }

        this.errorMessage = "";
        this.selectedAvatarFile = file;

        // preview
        this.avatarPreviewUrl = URL.createObjectURL(file);
    }

    removeAvatar(): void {
        if (!this.isEditing) return;
        this.selectedAvatarFile = null;
        this.avatarPreviewUrl = null;
    }

    save(): void {
        if (!this.isEditing) return;

        this.successMessage = "";
        this.errorMessage = "";

        if (this.form.invalid) {
            this.errorMessage = "Please fix the errors in the form.";
            this.form.markAllAsTouched();
            return;
        }

        this.isSaving = true;

        // Use FormData so avatar upload works
        const fd = new FormData();
        fd.append("username", this.form.get("username")?.value || "");
        fd.append("bio", this.form.get("bio")?.value || "");

        // If your backend accepts avatar file:
        if (this.selectedAvatarFile) {
            fd.append("avatar", this.selectedAvatarFile);
        }

        if (!this.userId) {
            this.errorMessage = "Not logged in.";
            return;
        }
        this.http.patch<ProfileDTO>(`${this.usersUrl}/${this.userId}`, fd).subscribe({
            next: (updated) => {
                this.isSaving = false;
                this.successMessage = "Profile updated successfully.";
                this.originalProfile = updated;

                // exit edit mode
                this.isEditing = false;
                this.form.disable();

                // refresh preview url (if backend returns avatar url)
                if (updated.avatar) this.avatarPreviewUrl = updated.avatar;
                this.selectedAvatarFile = null;
            },
            error: (err) => {
                this.isSaving = false;
                this.errorMessage =
                    err?.error?.message || "Failed to update profile. Check your endpoint and payload.";
            },
        });
    }
    getAvatarUrl(path: string | null): string {
        if (!path) return '';
        return `http://localhost:8080/uploads/${path}`;
    }


    // helpers for template
    get username() {
        return this.form.get("username");
    }
    get bio() {
        return this.form.get("bio");
    }
}
