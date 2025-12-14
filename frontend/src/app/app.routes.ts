import { Routes } from '@angular/router';
import { LandingComponent } from './pages/landing/landing.component';

export const routes: Routes = [
    {
        path: '',
        component : LandingComponent,
        // loadComponent: () => import('./pages/landing/landing.component')
        //     .then(m => m.LandingComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component')
            .then(m => m.RegisterComponent)
    },
    {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component')
            .then(m => m.LoginComponent)
    },
    {
        path: '**',
        redirectTo: ''
    }
];