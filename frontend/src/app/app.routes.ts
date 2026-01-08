import { Routes } from '@angular/router';
import { LandingComponent } from './pages/landing/landing.component';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
    {
        path: '',
        canActivate: [guestGuard],
        component: LandingComponent,
        // loadComponent: () => import('./pages/landing/landing.component')
        //     .then(m => m.LandingComponent)
    },
    {
        path: 'home',
        canActivate: [authGuard],
        loadComponent: () => import('./pages/home/home.component')
            .then(m => m.Home)
    },
    {
        path: 'register',
        canActivate: [guestGuard],
        loadComponent: () => import('./features/auth/register/register.component')
            .then(m => m.RegisterComponent)
    },
    {
        path: 'login',
        canActivate: [guestGuard],
        loadComponent: () => import('./features/auth/login/login.component')
            .then(m => m.LoginComponent)
    },
    {
        path: 'writePost',
        canActivate: [authGuard],
        // component : WritePostComponent
        loadComponent: () => import('./pages/writePost/writePost.component')
            .then(m => m.WritePostComponent)
    },
    {
        path: '**',
        redirectTo: ''
    }
];