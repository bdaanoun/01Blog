# 01Blog — Full-Stack Social Blogging Platform

A full-stack blogging platform built with **Angular 20** and **Spring Boot 4**, featuring rich text editing, social interactions (likes, follows, comments), notifications, a reporting system, and an admin dashboard.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Features](#features)
3. [Project Structure](#project-structure)
4. [Getting Started](#getting-started)
5. [API Endpoints](#api-endpoints)
6. [Backend Architecture (Spring Boot)](#backend-architecture-spring-boot)
7. [Frontend Architecture (Angular)](#frontend-architecture-angular)
8. [Complete Application Lifecycle](#complete-application-lifecycle)

---

## Tech Stack

| Layer        | Technology                                                        |
| ------------ | ----------------------------------------------------------------- |
| **Frontend** | Angular 20, Angular Material, Tiptap / EditorJS, SSR              |
| **Backend**  | Spring Boot 4, Java 21, Spring Security, Spring Data JPA, Lombok  |
| **Database** | PostgreSQL 15 (via Docker)                                        |
| **Auth**     | JWT, BCrypt password hashing                                      |
| **API Docs** | SpringDoc OpenAPI (Swagger UI)                                    |

---

## Features

### Authentication & Users
- Register with username, email, password, optional bio & avatar
- Login with JWT-based authentication
- Profile pages with avatar, bio, post history
- Edit profile (username, email, bio, avatar)
- User roles: `USER` and `ADMIN`
- User status: `ACTIVE` and `BANNED`

### Blog Posts
- Create posts with a rich text editor (Tiptap) and banner image
- Upload images and videos inline while writing
- Edit and delete your own posts
- View all posts on the home feed
- View posts from followed users only
- Post statuses: `PUBLISHED` and `HIDDEN`

### Social Features
- **Like / Unlike** posts
- **Follow / Unfollow** users
- **Comment** on posts
- **Notifications** (like, follow, comment) with read/unread status

### Reporting System
- Report posts with a reason
- Report users with a reason
- Admins can review and act on reports

### Admin Dashboard
- View all users and posts
- Ban / Unban users
- Hide / Show posts
- Change user roles
- View & manage reported users and posts
- Delete users, posts, and reports

---

## Project Structure

```
01blog/
├── backend/01blog/                  # Spring Boot API
│   ├── src/main/java/com/o1blog/_blog/
│   │   ├── controller/              # REST controllers (7)
│   │   │   ├── AuthController       # Register & Login
│   │   │   ├── PostController       # CRUD posts, likes, media uploads
│   │   │   ├── UserController       # User profiles & updates
│   │   │   ├── CommentController    # Post comments
│   │   │   ├── FollowController     # Follow/Unfollow
│   │   │   ├── NotificationController # Notifications
│   │   │   ├── ReportController     # Report posts & users
│   │   │   └── AdminController      # Admin operations
│   │   ├── service/                 # Business logic (9 services)
│   │   ├── repository/              # JPA repositories (8)
│   │   ├── model/                   # Entity classes (8)
│   │   │   ├── User (Role, Status)
│   │   │   ├── Post (PostStatus)
│   │   │   ├── Comment, Like, Follow
│   │   │   ├── Notification
│   │   │   └── PostReport, UserReport
│   │   ├── dto/                     # Request/Response DTOs (16)
│   │   ├── security/               # JWT filter, util, user details, admin seeder
│   │   ├── config/                  # SecurityConfig, WebConfig, JacksonConfig
│   │   └── exeption/                # Custom exceptions
│   └── src/main/resources/
│       └── application.properties
│
├── frontend/                        # Angular SPA
│   └── src/app/
│       ├── pages/                   # Landing, Home, Post Detail, Write Post,
│       │                            # Profile, Settings, Admin, Report
│       ├── features/auth/           # Login & Register components
│       ├── services/                # Auth, Post, Comment, Notification, Admin
│       ├── shared/                  # Header, Toast, Confirm Dialog
│       ├── guards/                  # Auth guard, Guest guard
│       ├── interceptors/            # JWT auth interceptor
│       └── app.routes.ts            # Route definitions
│
└── docker-compose.yaml              # PostgreSQL container
```

---

## Getting Started

### Prerequisites

- **Java 21**
- **Node.js** (v20+) and **npm**
- **Docker** & **Docker Compose** (for PostgreSQL)

### 1. Start the Database

```bash
docker compose up -d
```

This starts a PostgreSQL 15 container (`blog_postgres`) on port **5432** with database `01blog_db`.

### 2. Run the Backend

```bash
cd backend/01blog
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

> **Swagger UI** is available at `http://localhost:8080/swagger-ui.html`

### 3. Run the Frontend

```bash
cd frontend
npm install
npm start
```

The app starts at `http://localhost:4200`.

---

## API Endpoints

### Auth
| Method | Endpoint          | Description          |
| ------ | ----------------- | -------------------- |
| POST   | `/api/register`   | Register a new user  |
| POST   | `/api/login`      | Login & get JWT      |

### Posts
| Method | Endpoint                    | Description                   |
| ------ | --------------------------- | ----------------------------- |
| GET    | `/api/posts`                | Get all posts                 |
| GET    | `/api/posts/{id}`           | Get single post               |
| POST   | `/api/posts`                | Create a post                 |
| PATCH  | `/api/posts/{id}`           | Update a post                 |
| DELETE | `/api/posts/{id}`           | Delete a post                 |
| POST   | `/api/posts/{id}/like`      | Toggle like on a post         |
| GET    | `/api/posts/following`      | Get posts from followed users |
| POST   | `/api/posts/images/temp`    | Upload temp image             |
| POST   | `/api/posts/video/temp`     | Upload temp video             |

### Users
| Method | Endpoint                        | Description    |
| ------ | ------------------------------- | -------------- |
| GET    | `/api/users`                    | Get all users  |
| GET    | `/api/users/{id}`               | Get profile    |
| PATCH  | `/api/users/updateprofile/{id}` | Update profile |
| GET    | `/api/users/{id}/posts`         | Get user posts |
| POST   | `/api/users/follow/{id}`        | Toggle follow  |

### Comments
| Method | Endpoint                       | Description       |
| ------ | ------------------------------ | ----------------- |
| GET    | `/api/posts/{postId}/comments` | Get post comments |
| POST   | `/api/posts/{postId}/comments` | Add a comment     |

### Notifications
| Method | Endpoint                          | Description      |
| ------ | --------------------------------- | ---------------- |
| GET    | `/api/notifications`              | Get notifications |
| GET    | `/api/notifications/unread-count` | Get unread count  |
| PATCH  | `/api/notifications/{id}/read`    | Mark as read      |
| PATCH  | `/api/notifications/{id}/unread`  | Mark as unread    |

### Reports
| Method | Endpoint                     | Description   |
| ------ | ---------------------------- | ------------- |
| POST   | `/api/report/posts/{postId}` | Report a post |
| POST   | `/api/report/users/{userId}` | Report a user |

### Admin (`ADMIN` role required)
| Method | Endpoint                     | Description        |
| ------ | ---------------------------- | ------------------ |
| GET    | `/api/admin/users`           | Get all users      |
| GET    | `/api/admin/posts`           | Get all posts      |
| GET    | `/api/admin/posts/{id}`      | Get post details   |
| GET    | `/api/admin/reported-users`  | Get reported users |
| GET    | `/api/admin/reported-posts`  | Get reported posts |
| PATCH  | `/api/admin/users/{id}/ban`  | Ban a user         |
| PATCH  | `/api/admin/users/{id}/unban`| Unban a user       |
| PATCH  | `/api/admin/users/{id}/role` | Update user role   |
| PATCH  | `/api/admin/posts/{id}/hide` | Hide a post        |
| PATCH  | `/api/admin/posts/{id}/show` | Show a post        |
| DELETE | `/api/admin/users/{id}`      | Delete a user      |
| DELETE | `/api/admin/posts/{id}`      | Delete a post      |
| DELETE | `/api/admin/reports/{id}`    | Delete a report    |

---

## Environment Configuration

### Backend (`application.properties`)

| Property                                 | Default          | Description           |
| ---------------------------------------- | ---------------- | --------------------- |
| `spring.jpa.hibernate.ddl-auto`          | `update`         | Auto-update DB schema |
| `spring.jpa.show-sql`                    | `true`           | Log SQL queries       |
| `spring.servlet.multipart.max-file-size` | `50MB`           | Max upload file size  |
| `jwt.expiration`                         | `86400000` (24h) | JWT token duration    |

### Docker Compose

| Variable            | Default        |
| ------------------- | -------------- |
| `POSTGRES_DB`       | `01blog_db`    |
| `POSTGRES_USER`     | `root`         |
| `POSTGRES_PASSWORD` | `yourpassword` |

---

---

# 🚀 Architecture Deep Dive: From Startup to Runtime

The sections below explain how every layer of 01Blog works under the hood, using actual code from this project.

---

# Backend Architecture (Spring Boot)

## 🏁 Application Startup Process

### 1. **JVM Bootstrap (Java Virtual Machine)**

When you run `./mvnw spring-boot:run` , here's what happens:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. JVM Starts                                               │
│    → Loads Java Runtime Environment (JRE)                   │
│    → Initializes Class Loader                               │
│    → Allocates Memory (Heap & Stack)                        │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Finds main() method in Application.java                  │
│    → Entry point: public static void main(String[] args)   │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. SpringApplication.run(Application.class, args)           │
│    → Spring Boot takes control                              │
└─────────────────────────────────────────────────────────────┘
```

**Your Main Class:**
```java
// com.o1blog._blog.Application
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. **@SpringBootApplication Annotation Magic**

This single annotation is a combination of three powerful annotations:

```java
@SpringBootApplication = 
    @Configuration +          // Marks class as configuration source
    @EnableAutoConfiguration + // Enables Spring Boot auto-configuration
    @ComponentScan            // Scans for components in package
```

**What happens:**
1. **@Configuration**: Tells Spring this class contains bean definitions
2. **@EnableAutoConfiguration**: Spring Boot automatically configures beans based on classpath dependencies
3. **@ComponentScan**: Scans `com.o1blog._blog` and all sub-packages for components

---

## 🔧 Spring Boot Auto-Configuration

### Embedded Server Creation (Tomcat)

**How the embedded Tomcat server gets created:**

```
┌─────────────────────────────────────────────────────────────┐
│ Auto-Configuration Process                                  │
├─────────────────────────────────────────────────────────────┤
│ 1. Spring Boot detects spring-boot-starter-web in pom.xml   │
│    → Knows you need a web server                            │
├─────────────────────────────────────────────────────────────┤
│ 2. Checks for Tomcat classes on classpath                   │
│    → org.apache.catalina.startup.Tomcat found!              │
├─────────────────────────────────────────────────────────────┤
│ 3. ServletWebServerFactory bean created                     │
│    → TomcatServletWebServerFactory instantiated             │
├─────────────────────────────────────────────────────────────┤
│ 4. Embedded Tomcat Container initialized                    │
│    → Port: 8080 (default)                                   │
│    → Context path: /                                        │
│    → Connectors configured                                  │
├─────────────────────────────────────────────────────────────┤
│ 5. DispatcherServlet registered                             │
│    → Front controller for all HTTP requests                 │
└─────────────────────────────────────────────────────────────┘
```

**Configuration from your application.properties:**
```properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

---

## 🌱 Spring IoC Container (Inversion of Control)

### What is IoC?

**Traditional Programming:**
```java
// YOU create and manage dependencies
public class PostService {
    private PostRepository repository = new PostRepository(); // ❌ Tight coupling
    private UserService userService = new UserService();
}
```

**Spring IoC (how 01Blog does it):**
```java
// SPRING creates and injects dependencies
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;   // ✅ Loose coupling
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;
    private final NotificationRepository notificationRepository;
    
    // Lombok's @RequiredArgsConstructor generates the constructor
    // Spring automatically injects all dependencies at startup
}
```

### IoC Container Creation Process

```
┌──────────────────────────────────────────────────────────────┐
│ 1. ApplicationContext Creation                               │
│    → AnnotationConfigApplicationContext created              │
│    → This is the IoC Container (Bean Factory)                │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. Component Scanning                                        │
│    → Scans com.o1blog._blog package                          │
│    → Finds classes with:                                     │
│      • @Component                                            │
│      • @Service                                              │
│      • @Repository                                           │
│      • @Controller / @RestController                         │
│      • @Configuration                                        │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. Bean Definition Registration                              │
│    → Creates BeanDefinition objects                          │
│    → Metadata: class type, scope, dependencies               │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. Bean Instantiation (Creation)                             │
│    → Creates actual objects (beans)                          │
│    → Singleton scope (default) = one instance per container  │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. Dependency Injection                                      │
│    → Injects dependencies into beans                         │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 6. Post-Processing                                           │
│    → @PostConstruct methods executed                         │
│    → AOP proxies created                                     │
│    → Event listeners registered                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 💉 Dependency Injection (DI)

### Types of DI in This Project

#### 1. **Constructor Injection via Lombok** (Most common in 01Blog ✅)
```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor  // Lombok generates constructor
public class PostController {
    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;
    // All three are automatically injected by Spring
}
```

#### 2. **Manual Constructor Injection**
```java
@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
}
```

#### 3. **Field Injection** (used in SecurityConfig ⚠️)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired  // Injected via reflection
    private JwtAuthenticationFilter jwtAuthenticationFilter;
}
```

---

## 🫘 Spring Beans in 01Blog

### What is a Bean?

**A bean is simply an object managed by the Spring IoC container.**

### All Beans in This Project

```
ApplicationContext (IoC Container)
├── Controllers (@RestController)
│   ├── AuthController          → /api (register, login)
│   ├── PostController          → /api/posts
│   ├── UserController          → /api/users
│   ├── CommentController       → /api/posts/{postId}/comments
│   ├── FollowController        → /api/users/follow
│   ├── NotificationController  → /api/notifications
│   ├── ReportController        → /api/report
│   └── AdminController         → /api/admin
│
├── Services (@Service)
│   ├── AuthService
│   ├── PostService
│   ├── UserService
│   ├── CommentService
│   ├── FollowService
│   ├── AdminService
│   ├── ReportService
│   ├── FileStorageService
│   └── CustomUserDetailsService
│
├── Repositories (@Repository / JpaRepository)
│   ├── PostRepository
│   ├── UserRepository
│   ├── CommentRepository
│   ├── LikeRepository
│   ├── FollowRepository
│   ├── NotificationRepository
│   ├── PostReportRepository
│   └── UserReportRepository
│
├── Security Components
│   ├── JwtAuthenticationFilter (@Component) → Validates JWT on every request
│   ├── JwtUtil                              → Generates & parses JWT tokens
│   ├── CustomUserDetails                    → Wraps User entity for Spring Security
│   └── AdminSeeder (@Component)             → Seeds default admin user on startup
│
└── Configuration Beans (@Configuration)
    ├── SecurityConfig   → Security filter chain, CORS, BCrypt, AuthManager
    ├── WebConfig        → Static resource serving (/uploads/**)
    └── JacksonConfig    → JSON serialization settings
```

### Bean Lifecycle

```
┌────────────────────────────────────────────────────────────────┐
│ 1. INSTANTIATION                                               │
│    → Constructor called                                        │
│    → Object created in memory                                  │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 2. POPULATE PROPERTIES                                         │
│    → Dependencies injected                                     │
│    → @Autowired fields set                                     │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 3. INITIALIZATION                                              │
│    → @PostConstruct method called                              │
│    → Bean is ready to use! 🎉                                  │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ BEAN IN USE                                                    │
│ Application uses the bean...                                   │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 4. DESTRUCTION (Application Shutdown)                          │
│    → @PreDestroy method called                                 │
│    → Cleanup resources                                         │
└────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ JPA (Java Persistence API)

### What is JPA?

**JPA is a specification for object-relational mapping (ORM).**
- It defines HOW to map Java objects to database tables
- Hibernate is the implementation 01Blog uses

### How JPA Works in 01Blog

```
┌──────────────────────────────────────────────────────────────┐
│ YOUR CODE                                                     │
│                                                               │
│ postRepository.save(post);  // High-level Java method        │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ JPA (Interface/Specification)                                │
│                                                               │
│ Defines: EntityManager, persist(), merge(), find()...        │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ HIBERNATE (JPA Implementation)                               │
│                                                               │
│ • Implements JPA interfaces                                  │
│ • Manages Persistence Context                                │
│ • Tracks entity state changes                                │
│ • Generates SQL queries                                      │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ JDBC (Java Database Connectivity)                            │
│                                                               │
│ • Database driver (PostgreSQL JDBC Driver)                   │
│ • Handles low-level database communication                   │
│ • Executes SQL: INSERT INTO posts VALUES (...)               │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ DATABASE (PostgreSQL)                                        │
│                                                               │
│ • Stores actual data                                         │
└──────────────────────────────────────────────────────────────┘
```

### Entity Mapping — Post Entity

```java
@Entity
@Table(name = "posts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String banner;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.PUBLISHED;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostReport> reports = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum PostStatus { PUBLISHED, HIDDEN }
}
```

### JPA Repository — Derived Query Methods

```java
public interface PostRepository extends JpaRepository<Post, Long> {
    // Spring Data JPA automatically implements these from the method name!
    List<Post> findByUserId(Long userId);
    List<Post> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds);
    List<Post> findAllByUserIn(List<User> authors);

    // Filtered by post status (PUBLISHED / HIDDEN)
    List<Post> findByUserIdAndStatus(Long userId, PostStatus status);
    List<Post> findByUserIdInAndStatusOrderByCreatedAtDesc(List<Long> userIds, PostStatus status);
    List<Post> findAllByStatusOrderByCreatedAtDesc(PostStatus status);
}
```

**Provided methods (from JpaRepository):**
- `save(entity)` — INSERT or UPDATE
- `findById(id)` — SELECT by primary key
- `findAll()` — SELECT all
- `deleteById(id)` — DELETE by primary key
- `count()` — COUNT(*)
- And many more...

---

## 🔥 Hibernate Deep Dive

### What is Hibernate?

**Hibernate is the JPA implementation (ORM framework) that:**
1. Implements all JPA interfaces
2. Maps Java objects to database tables
3. Generates SQL queries automatically
4. Manages database connections
5. Tracks entity state changes

### Entity States in Hibernate

```
┌──────────────┐
│  TRANSIENT   │  Object created but not associated with Hibernate
│    (New)     │  Post post = new Post();
└──────────────┘
       ↓ (save/persist)
┌──────────────┐
│   MANAGED    │  Associated with persistence context
│ (Persistent) │  postRepository.save(post);
└──────────────┘  Changes are automatically detected!
       ↓ (transaction ends)
┌──────────────┐
│   DETACHED   │  No longer tracked by persistence context
│              │  Session closed, but object still exists
└──────────────┘
       ↓ (delete)
┌──────────────┐
│   REMOVED    │  Marked for deletion
│              │  Will be deleted on commit
└──────────────┘
```

### Configuration from application.properties

```properties
# Hibernate auto-updates the database schema on startup
spring.jpa.hibernate.ddl-auto=update
# Options:
# - create:      Drop and recreate tables
# - create-drop: Create tables, drop on shutdown
# - update:      Update schema (adds new columns/tables)
# - validate:    Only validate schema
# - none:        Do nothing

# Print SQL queries to console for debugging
spring.jpa.show-sql=true
```

### Lazy vs Eager Loading (used in 01Blog)

```java
@Entity
public class Post {
    @ManyToOne(fetch = FetchType.LAZY)  // Post → User
    private User user;  // Not loaded until accessed
    
    @OneToMany(mappedBy = "post")       // Post → Comments
    private List<Comment> comments;     // Not loaded until accessed
}

// When you do:
Post post = postRepository.findById(1L);
// SQL: SELECT * FROM posts WHERE id = 1
// Does NOT fetch user or comments yet!

// When you access:
String authorName = post.getUser().getUsername();
// SQL: SELECT * FROM users WHERE id = post.user_id
// NOW it fetches the user!
```

---

## 🔌 JDBC (Java Database Connectivity)

### JDBC Layer

**JDBC is the low-level API for database communication.**

```
┌──────────────────────────────────────────────────────────────┐
│ JDBC API (java.sql package)                                  │
│                                                              │
│ • DriverManager: Manages database drivers                    │
│ • Connection: Represents database connection                 │
│ • Statement/PreparedStatement: Executes SQL                  │
│ • ResultSet: Contains query results                          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ JDBC Driver (PostgreSQL Driver)                              │
│                                                              │
│ • Vendor-specific implementation                             │
│ • Translates JDBC calls to database protocol                 │
│ • From pom.xml: org.postgresql:postgresql                    │
└──────────────────────────────────────────────────────────────┘
```

### Connection Pool (HikariCP)

```
┌──────────────────────────────────────────────────────────────┐
│ HikariCP Connection Pool (Fast & Lightweight)                │
│                                                              │
│ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                 │
│ │ DB   │ │ DB   │ │ DB   │ │ DB   │ │ DB   │                 │
│ │Conn 1│ │Conn 2│ │Conn 3│ │Conn 4│ │Conn 5│                 │
│ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘                 │
│   ✓Free   In-Use   ✓Free   In-Use   ✓Free                  │
│                                                              │
│ Benefits:                                                    │
│ • Reuses connections (fast!)                                 │
│ • Manages connection lifecycle                               │
│ • Handles connection timeouts                                │
│ • Optimal pool size based on CPU cores                       │
└──────────────────────────────────────────────────────────────┘
```

Spring Boot auto-configures HikariCP. Connections are pooled and reused.

---

## 🔐 Security Architecture

### Security Filter Chain

01Blog uses **stateless JWT authentication** with Spring Security:

```java
// SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/register", "/api/login").permitAll()
                .requestMatchers("/api/posts/**").authenticated()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### JWT Authentication Flow

```
┌──────────────────────────────────────────────────────────────┐
│ 1. User sends POST /api/login { email, password }            │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. AuthService validates credentials with BCrypt             │
│    → JwtUtil generates JWT token (24h expiry)                │
│    → Returns token + user data                               │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. Frontend stores token in localStorage                     │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. Every subsequent request:                                 │
│    → Auth interceptor adds "Authorization: Bearer <token>"   │
│    → JwtAuthenticationFilter validates token                 │
│    → Sets SecurityContext with CustomUserDetails             │
│    → Controller can access current user via SecurityContext  │
└──────────────────────────────────────────────────────────────┘
```

---

## 🌐 Request Processing Flow

### Complete Flow: HTTP Request → Response

```
┌──────────────────────────────────────────────────────────────┐
│ 1. HTTP REQUEST                                              │
│    POST /api/posts                                           │
│    Authorization: Bearer eyJhbGci...                         │
│    Body: { title, content, banner }                          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. EMBEDDED TOMCAT SERVER                                    │
│    • Receives HTTP request on port 8080                      │
│    • Creates HttpServletRequest object                       │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. SECURITY FILTER CHAIN                                     │
│    • JwtAuthenticationFilter extracts & validates JWT        │
│    • Sets CustomUserDetails in SecurityContext               │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. DISPATCHER SERVLET (Front Controller)                     │
│    • Central entry point for all requests                    │
│    • Maps /api/posts → PostController.createPost()           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. CONTROLLER                                                │
│    PostController.createPost() →                             │
│    Gets current user from SecurityContext                    │
│    Calls postService.createPost(...)                         │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 6. SERVICE LAYER (Business Logic)                            │
│    PostService.createPost() →                                │
│    Processes EditorJS images, saves banner,                  │
│    builds Post entity                                        │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 7. REPOSITORY (Data Access)                                  │
│    postRepository.save(post) → JpaRepository                 │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 8. HIBERNATE/JPA                                             │
│    • Translates to SQL                                       │
│    • INSERT INTO posts (title, content, ...) VALUES (?, ?)   │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 9. JDBC → PostgreSQL                                         │
│    • Executes SQL via HikariCP connection pool               │
│    • Returns result                                          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 10. RESPONSE FLOWS BACK UP                                   │
│     DB → JDBC → Hibernate → Repository → Service             │
│     → Controller → Jackson → Tomcat → HTTP Response          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 11. HTTP RESPONSE                                            │
│     Status: 200 OK                                           │
│     Content-Type: application/json                           │
│     Body: { "id": 1, "title": "My Post", ... }               │
└──────────────────────────────────────────────────────────────┘
```

---

## 📦 Spring Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Web: Embedded Tomcat, Spring MVC, REST, Jackson -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- JPA: Hibernate ORM, Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Security: Authentication, Authorization, BCrypt -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT: Token generation & validation -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    
    <!-- PostgreSQL JDBC Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Lombok: Reduces boilerplate (@Data, @Builder, @RequiredArgsConstructor) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
    
    <!-- SpringDoc OpenAPI: Swagger UI for API docs -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.6.0</version>
    </dependency>
</dependencies>
```

---

---

# Frontend Architecture (Angular)

## 🏁 Angular Application Bootstrap

### Application Startup Process

```
┌──────────────────────────────────────────────────────────────┐
│ 1. Browser loads index.html                                  │
│    <app-root></app-root>  ← Empty placeholder                │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. Browser loads main.ts (entry point)                       │
│    Angular CLI bundles: main.ts + all dependencies           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. bootstrapApplication(AppComponent, appConfig)             │
│    • Creates Angular platform                                │
│    • Zone.js initialized (event coalescing enabled)          │
│    • Creates root injector (DI container)                    │
│    • Registers all providers                                 │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. AppComponent created                                      │
│    @Component({ selector: 'app-root', standalone: true })    │
│    • Imports: CommonModule, RouterOutlet, HeaderComponent    │
│    • Renders <app-header> + <router-outlet>                  │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. Router initialized                                        │
│    • Reads current URL                                       │
│    • Matches route from app.routes.ts                        │
│    • Loads corresponding component (lazy-loaded)             │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ APPLICATION READY! 🎉                                        │
└──────────────────────────────────────────────────────────────┘
```

### Your Actual main.ts

```typescript
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig)
  .catch(err => console.error(err));
```

### Your Actual app.config.ts

```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),  // Optimized change detection
    provideRouter(routes),                                  // Route configuration
    provideHttpClient(withInterceptors([authInterceptor]))  // HTTP + JWT interceptor
  ]
};
```

---

## 🌊 Zone.js — The Magic Behind Change Detection

### What is Zone.js?

**Zone.js is a library that intercepts ALL asynchronous operations in JavaScript.** 01Blog uses it with event coalescing enabled for better performance.

```javascript
// Zone.js patches these APIs:
setTimeout()        // Timers
setInterval()       // Intervals  
Promise.then()      // Promises
fetch()             // Network requests
addEventListener()  // DOM events
XMLHttpRequest      // AJAX calls
```

### How Zone.js Works

```
┌──────────────────────────────────────────────────────────────┐
│ Original JavaScript (without Zone.js)                        │
│                                                              │
│ button.addEventListener('click', () => {                     │
│   this.count++;  // Value changes                            │
│   // 😞 View NOT automatically updated                       │
│ });                                                          │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│ With Zone.js (Angular uses this)                             │
│                                                              │
│ button.addEventListener('click', () => {                     │
│   // Zone.js wraps this callback!                            │
│   this.count++;  // Value changes                            │
│   // 🎉 Zone.js notifies Angular                             │
│   // → Angular runs change detection                         │
│   // → View automatically updated!                           │
│ });                                                          │
└──────────────────────────────────────────────────────────────┘
```

**Key Points:**
- Every async operation runs inside a Zone
- When async operation completes, Zone.js notifies Angular
- Angular runs change detection to update the view
- **Event coalescing** (enabled in 01Blog) batches multiple events into a single change detection cycle

---

## 📡 RxJS — Reactive Programming

### What is RxJS?

**RxJS (Reactive Extensions for JavaScript) is the library Angular uses for handling async data streams.** 01Blog uses it for all HTTP communication and the auth interceptor.

### How 01Blog Uses RxJS

```typescript
// auth.interceptor.ts — Real code from this project
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('authToken');

  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req).pipe(                            // Observable pipeline
    catchError((error: HttpErrorResponse) => {      // RxJS operator
      if (error.status === 403 &&
          error.error?.message === 'Your account has been banned.') {
        localStorage.removeItem('authToken');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```

### Observable Lifecycle

```
┌──────────────────────────────────────────────────────────────┐
│ 1. CREATION                                                  │
│    const obs$ = http.get('/api/posts');                      │
│    → Observable created (COLD — not executing yet)           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. SUBSCRIPTION                                              │
│    obs$.subscribe(data => console.log(data));                │
│    → Observable starts executing (becomes HOT)               │
│    → HTTP request sent                                       │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. EMISSION                                                  │
│    → Data received: { posts: [...] }                         │
│    → next() callback called                                  │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. COMPLETION                                                │
│    → complete() callback called                              │
│    → Observable automatically unsubscribed                   │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔌 Dependency Injection (DI) in Angular

### How DI Works in 01Blog

```typescript
// Service (providedIn: 'root' = singleton across app)
@Injectable({ providedIn: 'root' })
export class PostService {
  private http = inject(HttpClient);  // Modern inject() API

  getPosts() {
    return this.http.get('/api/posts');
  }
}

// Component uses constructor injection
@Component({...})
export class AppComponent {
  constructor(private router: Router) {}
}
```

### Injection Hierarchy

```
┌──────────────────────────────────────────────────────────────┐
│ Root Injector (providedIn: 'root')                          │
│ • Services live here by default                             │
│ • Singleton across entire application                        │
│ • Examples: HttpClient, Router, PostService, AuthService     │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Component Injector                                           │
│ • Component-specific services                                │
│ • New instance for each component                            │
└──────────────────────────────────────────────────────────────┘
```

---

## 🌐 HTTP Communication

### Auth Interceptor Flow

01Blog has a single interceptor (`authInterceptor`) that:
1. Adds the JWT token to every outgoing request
2. Catches 403 errors for banned users and auto-redirects to login

```
┌──────────────────────────────────────────────────────────────┐
│ Component calls: postService.getPosts()                      │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Service: http.get('/api/posts')                              │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Auth Interceptor:                                            │
│   → Adds "Authorization: Bearer <token>" header              │
│   → Wraps response with banned-user error handling           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ HttpClient: Makes actual HTTP request to localhost:8080      │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Backend: Processes request                       │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Response flows back through interceptor                     │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Component receives data (via Observable subscription)        │
└──────────────────────────────────────────────────────────────┘
```

---

## 🧩 Angular Component Architecture

### Standalone Components (used throughout 01Blog)

```typescript
// Real AppComponent from 01Blog
@Component({
  selector: 'app-root',
  standalone: true,                            // No NgModule needed
  imports: [CommonModule, RouterOutlet, HeaderComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  showHeader = true;
  constructor(private router: Router) {}
}
```

### Route Configuration with Lazy Loading

```typescript
// app.routes.ts — 01Blog uses lazy loading for most pages
export const routes: Routes = [
  { path: '',         component: LandingComponent, canActivate: [guestGuard] },
  { path: 'login',    loadComponent: () => import('./features/auth/login/login.component')
                        .then(m => m.LoginComponent), canActivate: [guestGuard] },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component')
                        .then(m => m.RegisterComponent), canActivate: [guestGuard] },
  { path: 'home',     loadComponent: () => import('./pages/home/home.component')
                        .then(m => m.Home), canActivate: [authGuard] },
  { path: 'post/:id', loadComponent: () => import('./pages/post-detail/post-detail.component')
                        .then(m => m.PostDetailComponent), canActivate: [authGuard] },
  { path: 'writePost',loadComponent: () => import('./pages/writePost/writePost.component')
                        .then(m => m.WritePostComponent), canActivate: [authGuard] },
  { path: 'admin',    loadComponent: () => import('./pages/admin/admin.component')
                        .then(m => m.AdminComponent), canActivate: [authGuard] },
  { path: 'profile/:id', component: ProfileComponent },
  { path: 'settings', component: SettingsComponent },
  { path: '**',       redirectTo: '' }
];
```

### Route Guards

- **`authGuard`** — Redirects unauthenticated users to login
- **`guestGuard`** — Redirects authenticated users away from login/register/landing

### Component Lifecycle Hooks

```
┌──────────────────────────────────────────────────────────────┐
│ 1. constructor()                                             │
│    → DI happens here                                         │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. ngOnChanges()                                             │
│    → When @Input() properties change                         │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. ngOnInit()                                                │
│    → Component initialized (called ONCE)                     │
│    → Best place for initialization logic                     │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. ngAfterViewInit()                                         │
│    → After component view initialized                        │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ COMPONENT IN USE                                             │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. ngOnDestroy()                                             │
│    → Before component destroyed                              │
│    → Cleanup: unsubscribe, clear timers                      │
└──────────────────────────────────────────────────────────────┘
```

---

---

# Complete Application Lifecycle

## 🎬 Full Stack Request Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ USER ACTION: Clicks "Create Post" button                        │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ ANGULAR FRONTEND (localhost:4200)                               │
│                                                                 │
│ 1. Zone.js detects click event                                  │
│ 2. Component method executed: createPost()                      │
│ 3. PostService.create() called                                  │
│ 4. HttpClient.post('/api/posts', data) — Observable created     │
│ 5. Auth Interceptor adds JWT token                              │
│ 6. HTTP request sent to backend                                 │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ SPRING BOOT BACKEND (localhost:8080)                            │
│                                                                 │
│ 1. Tomcat receives HTTP request on port 8080                    │
│ 2. JwtAuthenticationFilter validates JWT token                  │
│ 3. DispatcherServlet routes to PostController.createPost()      │
│ 4. PostController gets current user from SecurityContext        │
│ 5. PostService processes content (EditorJS images)              │
│ 6. FileStorageService saves banner image                        │
│ 7. PostRepository.save() called                                 │
│ 8. Hibernate translates to SQL                                  │
│ 9. JDBC executes: INSERT INTO posts...                          │
│ 10. PostgreSQL stores data                                      │
│ 11. JSON response sent back                                     │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ ANGULAR FRONTEND RECEIVES RESPONSE                              │
│                                                                 │
│ 1. HttpClient Observable emits response                         │
│ 2. subscribe() callback executed                                │
│ 3. Component updates state                                      │
│ 4. Zone.js triggers change detection                            │
│ 5. View updated with new post                                   │
│ 6. User sees success                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Concepts Summary

### Backend (Spring Boot)

| Concept | Purpose |
|---------|---------|
| **IoC Container** | Manages bean lifecycle and dependencies |
| **Dependency Injection** | Automatically injects dependencies into beans |
| **Beans** | Objects managed by Spring container |
| **JPA** | Specification for ORM (Object-Relational Mapping) |
| **Hibernate** | JPA implementation that manages persistence |
| **JDBC** | Low-level API for database communication |
| **Embedded Tomcat** | Built-in web server (no external server needed) |
| **DispatcherServlet** | Front controller that routes HTTP requests |
| **Spring Security** | Authentication & authorization framework |
| **JWT** | Stateless token-based authentication |

### Frontend (Angular)

| Concept | Purpose |
|---------|---------|
| **Zone.js** | Intercepts async operations for change detection |
| **Change Detection** | Synchronizes model with view (DOM) |
| **RxJS/Observables** | Reactive programming for async data streams |
| **Dependency Injection** | Provides services to components |
| **Interceptors** | Modify HTTP requests/responses globally |
| **Standalone Components** | Self-contained UI building blocks (no NgModules) |
| **Services** | Reusable business logic and data management |
| **Route Guards** | Control access to routes based on auth state |
| **Lazy Loading** | Load components on demand for better performance |

---

## 📚 Additional Resources

- Spring Boot: https://spring.io/projects/spring-boot
- Angular: https://angular.dev
- RxJS: https://rxjs.dev
- Hibernate: https://hibernate.org

---

**Project:** 01Blog — Social Blogging Platform  
**Tech Stack:** Spring Boot 4.0 + Angular 20 + PostgreSQL 15