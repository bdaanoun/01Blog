import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-core',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './core.html',
  styleUrl: './core.css'
})
export class CoreComponent {}
