import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
    selector: 'app-report-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule
    ],
    templateUrl: './report-dialog.component.html',
    styleUrl: './report-dialog.component.css'
})
export class ReportDialogComponent {

    reasonControl = new FormControl('', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(200),
    ]);

    constructor(
        private dialogRef: MatDialogRef<ReportDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { postId?: number; userId?: number }
    ) { }

    close(): void {
        this.dialogRef.close(null);
    }

    submit(): void {
        const reason = this.reasonControl.value?.trim();
        if (!reason) return;

        this.dialogRef.close(reason);
    }
}
