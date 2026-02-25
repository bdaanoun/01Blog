# 📚 01blog — Project Concepts Reference

A complete guide to every concept used in this fullstack blog application.

---

## Table of Contents

1. [Fullstack Architecture](#1-fullstack-architecture)
2. [Backend — Spring Boot](#2-backend--spring-boot)
3. [Security — JWT & Spring Security](#3-security--jwt--spring-security)
4. [Database — JPA, Hibernate & PostgreSQL](#4-database--jpa-hibernate--postgresql)
5. [REST API Design](#5-rest-api-design)
6. [Error Handling](#6-error-handling)
7. [File Upload & Storage](#7-file-upload--storage)
8. [Frontend — Angular](#8-frontend--angular)
9. [Angular Routing & Guards](#9-angular-routing--guards)
10. [HTTP Interceptors](#10-http-interceptors)
11. [Angular Services & RxJS](#11-angular-services--rxjs)
12. [Role-Based Access Control (RBAC)](#12-role-based-access-control-rbac)
13. [Notifications System](#13-notifications-system)
14. [Reports System](#14-reports-system)
15. [Post Interactions](#15-post-interactions)
16. [Admin Panel](#16-admin-panel)

---

## 1. Fullstack Architecture

```
Browser (Angular) ──► REST API (Spring Boot) ──► PostgreSQL
        ↑                        |
        └──── JSON Response ◄────┘
```

| Layer      | Technology         | Port  |
|------------|--------------------|-------|
| Frontend   | Angular 17+        | 4200  |
| Backend    | Spring Boot 3      | 8080  |
| Database   | PostgreSQL         | 5432  |

- **Frontend** sends HTTP requests with a JWT token in the `Authorization` header.
- **Backend** validates the token, processes the request, talks to the DB, and returns JSON.
- **CORS** is configured on the backend to only allow requests from `http://localhost:4200`.

---

## 2. Backend — Spring Boot

### Key Annotations

| Annotation           | What it does                                          |
|----------------------|-------------------------------------------------------|
| `@SpringBootApplication` | Enables auto-config, component scan, config     |
| `@RestController`    | Combines `@Controller` + `@ResponseBody`              |
| `@RequestMapping`    | Maps a base URL path to a controller                  |
| `@GetMapping`        | Handles GET requests                                  |
| `@PostMapping`       | Handles POST requests                                 |
| `@PatchMapping`      | Handles partial-update PATCH requests                 |
| `@DeleteMapping`     | Handles DELETE requests                               |
| `@PathVariable`      | Extracts value from URL path (`/users/{id}`)          |
| `@RequestParam`      | Extracts query or form param from request             |
| `@RequestBody`       | Deserializes JSON body into a Java object             |
| `@RequestPart`       | Extracts one part of a multipart request              |
| `@Service`           | Marks a class as a business-logic service bean        |
| `@Repository`        | Marks a class as a data-access bean                   |
| `@Component`         | Generic Spring-managed bean                           |
| `@Configuration`     | Marks a class as a source of `@Bean` definitions      |
| `@Bean`              | Defines a managed bean inside a `@Configuration`      |
| `@RequiredArgsConstructor` | Lombok: generates constructor for `final` fields |
| `@PrePersist`        | JPA lifecycle hook: called before entity is saved     |

### Layers

```
Controller  →  Service  →  Repository  →  Database
(HTTP)        (Logic)       (JPA)        (PostgreSQL)
```

- **Controller**: Receives HTTP request, calls service, returns response.
- **Service**: Contains business logic. No direct DB access.
- **Repository**: Interface extending `JpaRepository`. Spring generates the SQL.
- **DTO (Data Transfer Object)**: Plain object used to shape API inputs/outputs (e.g., `PostRequest`, `PostResponse`). Avoids exposing entity internals.

### Lombok

Lombok reduces boilerplate Java code:

| Annotation       | What it generates                        |
|------------------|------------------------------------------|
| `@Data`          | Getters, setters, `toString`, `equals`   |
| `@Builder`       | Builder pattern (`User.builder().build()`)|
| `@NoArgsConstructor` | Empty constructor                    |
| `@AllArgsConstructor` | Constructor with all fields          |
| `@RequiredArgsConstructor` | Constructor for `final` fields |

---

## 3. Security — JWT & Spring Security

### How Authentication Works

```
1. User logs in → POST /api/login
2. Backend validates credentials → generates JWT token
3. Token is returned to frontend
4. Frontend stores token in localStorage
5. All future requests include:  Authorization: Bearer <token>
6. JwtAuthenticationFilter validates the token on every request
```

### JWT (JSON Web Token)

A JWT has 3 parts separated by dots:

```
HEADER.PAYLOAD.SIGNATURE
eyJhb...  .  eyJ1c2Vya...  .  Xyz...
```

- **Header**: Algorithm used (e.g., HS256).
- **Payload**: Claims — userId, username, role, expiry.
- **Signature**: Ensures the token hasn't been tampered with.

```java
// Token is created in JwtUtil:
String token = Jwts.builder()
    .setSubject(username)
    .claim("role", user.getRole())
    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
    .signWith(secretKey)
    .compact();
```

### Spring Security Filter Chain

Every HTTP request passes through filters before hitting the controller:

```
Request → JwtAuthenticationFilter → SecurityFilterChain → Controller
```

The `JwtAuthenticationFilter`:
1. Reads the `Authorization` header.
2. Extracts and validates the JWT.
3. Loads the `UserDetails` from DB.
4. Sets the authentication in `SecurityContextHolder`.

### BCrypt Password Hashing

Passwords are **never stored in plain text**. BCrypt adds a salt and applies multiple hash rounds:

```java
// When registering:
String hashed = passwordEncoder.encode(rawPassword);  // Stored in DB

// When logging in:
passwordEncoder.matches(rawPassword, hashedFromDB);   // Returns true/false
```

### Role-Based Access

```java
// In SecurityConfig:
.requestMatchers("/api/admin/**").hasRole("ADMIN")  // Only ADMIN can access
.anyRequest().authenticated()                        // All others need a valid token
```

### Stateless Sessions

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

No server-side session is stored. The JWT itself carries the user's identity on every request.

---

## 4. Database — JPA, Hibernate & PostgreSQL

### Entity = Database Table

```java
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne                     // Many posts → one user
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
}
```

### Relationships Summary

| Relationship | Meaning                         | Example                     |
|--------------|---------------------------------|-----------------------------|
| `@ManyToOne` | Many records → one parent       | Many posts belong to one user |
| `@OneToMany` | One parent → many records       | One user has many posts     |
| `@ManyToMany`| Many → many (via join table)    | Post liked by many users    |

### Cascade Types

- `CascadeType.ALL` — if you delete the parent, all children are deleted too.
- `orphanRemoval = true` — if a child loses its parent, it is deleted.

### JPA Repository — No SQL Needed

```java
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);          // Derived query
    boolean existsByPostIdAndUserId(Long postId, Long userId); // Auto-implemented
}
```

Spring Data JPA reads the method name and generates the correct SQL automatically.

### Lazy vs Eager Loading

- **LAZY** (default): Related entity is NOT fetched until accessed. Better performance.
- **EAGER**: Related entity is fetched immediately with the parent.

```java
@ManyToOne(fetch = FetchType.LAZY)   // Only loaded when you call getUser()
private User user;
```

---

## 5. REST API Design

### HTTP Methods Convention

| Method   | Purpose              | Example                  |
|----------|----------------------|--------------------------|
| `GET`    | Read data            | `GET /api/posts`         |
| `POST`   | Create new resource  | `POST /api/posts`        |
| `PATCH`  | Partial update       | `PATCH /api/posts/1`     |
| `PUT`    | Full replace         | `PUT /api/users/1`       |
| `DELETE` | Remove resource      | `DELETE /api/posts/1`    |

### HTTP Status Codes

| Code | Meaning                  |
|------|--------------------------|
| 200  | OK                       |
| 201  | Created                  |
| 204  | No Content (deleted)     |
| 400  | Bad Request              |
| 401  | Unauthorized (no token)  |
| 403  | Forbidden (wrong role)   |
| 404  | Not Found                |
| 409  | Conflict (duplicate)     |
| 500  | Internal Server Error    |

### ResponseEntity

Lets you control the HTTP status code in the response:

```java
return ResponseEntity.ok(data);                    // 200
return ResponseEntity.noContent().build();         // 204
return ResponseEntity.status(403).body("Denied"); // 403
```

---

## 6. Error Handling

### Backend — GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handle(UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }
    // ...
}
```

- `@RestControllerAdvice` catches exceptions thrown anywhere in the controllers.
- Custom exception classes (e.g., `PostNotFoundException`) give meaningful messages.
- A consistent `ApiError` record is returned: `{ status, error, message, path, timestamp }`.

### Frontend — Error Handling in Services

```typescript
this.http.get('/api/posts').subscribe({
  next: (data) => { /* success */ },
  error: (err) => {
    const msg = err?.error?.message || 'Something went wrong';
    this.toast.error(msg);
  }
});
```

---

## 7. File Upload & Storage

### Backend

- Files are received as `MultipartFile` in a `multipart/form-data` request.
- `FileStorageService` saves them to a local `uploads/` directory on disk.
- Temp uploads are stored first (for preview), then moved to permanent storage on post creation.
- Files are served statically via `/uploads/**` (public route).

```java
@PostMapping(consumes = "multipart/form-data")
public ResponseEntity<PostResponse> createPost(
    @RequestParam("title") String title,
    @RequestPart(value = "banner", required = false) MultipartFile banner) { ... }
```

### Frontend

- Users select a file → Angular previews it with `URL.createObjectURL()` or a `FileReader`.
- The file is uploaded as `FormData` using `HttpClient`.

---

## 8. Frontend — Angular

### Component Anatomy

```typescript
@Component({
  selector: 'app-home',        // HTML tag used in templates
  standalone: true,            // No NgModule needed (Angular 17+)
  imports: [CommonModule, ...],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class Home implements OnInit {
  posts: Post[] = [];

  constructor(private postService: PostService) {}

  ngOnInit() {
    this.postService.getPosts().subscribe(data => this.posts = data);
  }
}
```

### Lifecycle Hooks

| Hook             | When it runs                                 |
|------------------|----------------------------------------------|
| `ngOnInit()`     | After component is initialized (use for API calls) |
| `ngOnDestroy()`  | Right before the component is destroyed      |
| `ngOnChanges()`  | When `@Input()` properties change            |

### Data Binding

| Syntax              | Type              | Direction              |
|---------------------|-------------------|------------------------|
| `{{ value }}`       | Interpolation     | Component → Template   |
| `[property]="val"`  | Property binding  | Component → Template   |
| `(event)="fn()"`    | Event binding     | Template → Component   |
| `[(ngModel)]="val"` | Two-way binding   | Both directions        |

### Structural Directives

```html
<div *ngIf="isLoggedIn">Welcome!</div>
<div *ngFor="let post of posts">{{ post.title }}</div>
```

### Angular Material

UI component library used in this project. Provides:
- `MatButtonModule` → styled buttons
- `MatIconModule` → Material icons
- `MatDialogModule` → modal dialogs
- `MatCardModule` → card layout

---

## 9. Angular Routing & Guards

### How Routes Work

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', component: LandingComponent, canActivate: [guestGuard] },
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'admin', loadComponent: () => import('./pages/admin/...') },
  { path: '**', redirectTo: '' }   // Wildcard → redirect unknown routes
];
```

### Lazy Loading

```typescript
loadComponent: () => import('./pages/home/home.component').then(m => m.Home)
```
The component bundle is only downloaded when the user navigates to that route. Improves initial load time.

### Guards

| Guard       | Purpose                                      |
|-------------|----------------------------------------------|
| `authGuard` | Blocks unauthenticated users (redirects to `/login`) |
| `guestGuard`| Blocks logged-in users from seeing login/register pages |

```typescript
export const authGuard = () => {
  const authService = inject(AuthService);
  return authService.isAuthenticated() ? true : inject(Router).navigate(['/login']);
};
```

---

## 10. HTTP Interceptors

An interceptor sits between every outgoing HTTP request and the server. This project uses `authInterceptor`:

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('authToken');

  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 403 && error.error?.message === 'Your account has been banned.') {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```

**What it does:**
1. Attaches the JWT token to every outgoing request automatically.
2. If the server returns 403 with "banned" message, logs the user out immediately.

---

## 11. Angular Services & RxJS

### Services

Services are singletons shared across components. They handle API calls and state.

```typescript
@Injectable({ providedIn: 'root' })
export class PostService {
  constructor(private http: HttpClient) {}

  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>('/api/posts');
  }
}
```

### RxJS Concepts

| Concept           | Description                                          |
|-------------------|------------------------------------------------------|
| `Observable`      | A stream of async values (like a promise, but richer)|
| `subscribe()`     | Executes the Observable, gets the value              |
| `pipe()`          | Chains operators onto the stream                     |
| `catchError()`    | Handles errors in the stream                         |
| `map()`           | Transforms each emitted value                        |
| `throwError()`    | Re-emits an error in the pipeline                    |

```typescript
this.http.get<User>('/api/users/me').pipe(
  map(user => user.username),
  catchError(err => {
    console.error(err);
    return throwError(() => err);
  })
).subscribe(name => console.log(name));
```

---

## 12. Role-Based Access Control (RBAC)

### Backend Enforcement

```java
// SecurityConfig.java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

Even if a non-admin sends a request to `/api/admin/**`, Spring Security returns **403 Forbidden** before the controller is ever called.

### Frontend Enforcement

The admin component checks for a 403 response and shows "You don't have access here":

```typescript
error: (err) => {
  if (err.status === 403) {
    this.isForbidden = true;
  }
}
```

The header hides admin links for regular users by checking the user's role from `AuthService`.

> ⚠️ Frontend RBAC is **UI-only** — it makes the experience cleaner. The real protection is always on the **backend**.

---

## 13. Notifications System

### How It Works

1. User A follows User B → a `FOLLOW` notification is created for User B.
2. User B publishes a post → all followers of User B receive a `NEW_POST` notification.
3. Notifications are stored in the `notifications` table with `UNREAD`/`READ` status.

### Notification Model

```java
public class Notification {
    private User sender;          // Who triggered the notification
    private User receiver;        // Who receives it
    private String content;       // Human-readable message
    private Long postId;          // Reference to the post (if applicable)
    private NotificationType notifType;    // FOLLOW or NEW_POST
    private NotificationStatus status;    // UNREAD or READ
    private LocalDateTime createdAt;
}
```

---

## 14. Reports System

### User Reports

- Any user can report another user with a reason.
- Stored in `user_reports` table with: reporter, reportedUser, reason, timestamp.
- Only visible to admins via `GET /api/admin/reported-users`.

### Post Reports

- Any user can report a post with a reason.
- Stored in `post_reports` table.
- Only visible to admins via `GET /api/admin/reported-posts`.

### Why Reports Are Admin-Only

The report endpoints are under `/api/admin/**`, which is protected by `.hasRole("ADMIN")` in `SecurityConfig`. Regular users cannot access them regardless of what the frontend shows.

---

## 15. Post Interactions

### Like / Unlike (Toggle)

```java
// PostService
public boolean toggleLike(Long postId, Long userId) {
    Optional<Like> existing = likeRepository.findByPostIdAndUserId(postId, userId);
    if (existing.isPresent()) {
        likeRepository.delete(existing.get()); // Unlike
        return false;
    } else {
        likeRepository.save(new Like(post, user)); // Like
        return true;
    }
}
```

### Comments

- Comments are stored with a reference to the post and the user.
- Deleting a comment removes it from the UI immediately (optimistic update).

### Following / Subscriptions

- Stored in the `follows` table (follower_id → following_id).
- When a followed user publishes a post, a `NEW_POST` notification is sent to each follower.

### Post Status

Posts can have statuses:
- `PUBLISHED` — visible to all users.
- `HIDDEN` — hidden by admin; not visible in the main feed.

---

## 16. Admin Panel

### What the Admin Can Do

| Action                  | Endpoint                            |
|-------------------------|-------------------------------------|
| View all users          | `GET /api/admin/users`              |
| Ban a user              | `PATCH /api/admin/users/{id}/ban`   |
| Unban a user            | `PATCH /api/admin/users/{id}/unban` |
| Delete a user           | `DELETE /api/admin/users/{id}`      |
| View all posts          | `GET /api/admin/posts`              |
| Hide a post             | `PATCH /api/admin/posts/{id}/hide`  |
| Show a post             | `PATCH /api/admin/posts/{id}/show`  |
| Delete a post           | `DELETE /api/admin/posts/{id}`      |
| View reported posts     | `GET /api/admin/reported-posts`     |
| View reported users     | `GET /api/admin/reported-users`     |
| Change user role        | `PATCH /api/admin/users/{id}/role`  |

### Confirmation Before Actions

Every destructive action (delete, ban) shows a confirmation dialog before executing:

```typescript
openConfirm(message: string, action: () => void) {
    this.confirmMessage = message;
    this.confirmAction = action;
    this.showConfirm = true;  // Shows confirmation modal
}
confirmYes() {
    this.confirmAction?.();   // Only then executes
}
```

### Admin Dashboard Tabs

The admin component has 4 tabs:
1. **Users** — view, ban/unban, delete, change role.
2. **Posts** — view, hide/show, delete.
3. **Post Reports** — view reports on posts, delete or act on them.
4. **User Reports** — view reports on users, delete or act on them.

---

## Quick Glossary

| Term         | Definition                                                  |
|--------------|-------------------------------------------------------------|
| JWT          | JSON Web Token — self-contained auth token                  |
| BCrypt       | Secure password hashing algorithm                           |
| ORM          | Object-Relational Mapping — maps Java classes to DB tables  |
| DTO          | Data Transfer Object — shapes API request/response data     |
| CORS         | Controls which origins can call your API                    |
| Guard        | Angular route protection function                           |
| Interceptor  | Middleware that processes every HTTP request/response       |
| Observable   | RxJS async stream used for HTTP calls in Angular            |
| RBAC         | Role-Based Access Control                                   |
| Standalone   | Angular component with no NgModule (Angular 14+)            |
| Lazy Loading | Loading component/module only when the route is visited     |
