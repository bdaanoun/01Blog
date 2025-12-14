// src/app/pages/landing/landing.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent {
  features = [
    {
      icon: '🚀',
      title: 'Fast & Modern',
      description: 'Built with the latest Angular standalone components'
    },
    {
      icon: '🔒',
      title: 'Secure',
      description: 'Industry-standard authentication and security practices'
    },
    {
      icon: '💡',
      title: 'Easy to Use',
      description: 'Intuitive interface designed for the best user experience'
    }
  ];
}