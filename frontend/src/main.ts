import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withEnabledBlockingInitialNavigation } from '@angular/router';
import { AppComponent } from './app/app.component';
import { authInterceptor } from './app/interceptors/auth.interceptor';
import { routes } from './app/app.routes';

bootstrapApplication(AppComponent, {
  providers: [
    // HTTP client with auth interceptor
    provideHttpClient(withInterceptors([authInterceptor])),

    // Router
    provideRouter(routes, withEnabledBlockingInitialNavigation())
  ]
})
  .catch((err) => console.error(err));
