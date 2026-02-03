import { Component, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
    providedIn: 'root'
})
export class Toast {

    constructor(private snackBar: MatSnackBar) { }

    success(message: string) {
        this.open(message, 'toast-success');
    }

    error(message: string) {
        this.open(message, 'toast-error');
    }

    info(message: string) {
        this.open(message, 'toast-info');
    }

    private open(message: string, panelClass: string) {
        this.snackBar.open(message, 'Close', {
            duration: 3000,
            horizontalPosition: 'left',
            verticalPosition: 'bottom',
            panelClass: [panelClass]
        });
    }
}
