import { Component, signal } from '@angular/core';
import  { RegisterComponent } from '../app/features/auth/register/register.component'
@Component({
  selector: 'app-root',
  imports: [RegisterComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  // protected readonly title = signal('01Blog');
}
