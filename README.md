# 🚀 Project Architecture Deep Dive: From Startup to Runtime

## Table of Contents
1. [Backend Architecture (Spring Boot)](#backend-architecture-spring-boot)
2. [Frontend Architecture (Angular)](#frontend-architecture-angular)
3. [Complete Application Lifecycle](#complete-application-lifecycle)

---

# Backend Architecture (Spring Boot)

## 🏁 Application Startup Process

### 1. **JVM Bootstrap (Java Virtual Machine)**

When you run `./mvnw spring-boot:run` or `java -jar blog.jar`, here's what happens:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. JVM Starts                                               │
│    → Loads Java Runtime Environment (JRE)                   │
│    → Initializes Class Loader                               │
│    → Allocates Memory (Heap & Stack)                        │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Finds main() method in BlogApplication.java             │
│    → Entry point: public static void main(String[] args)   │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. SpringApplication.run(BlogApplication.class, args)      │
│    → Spring Boot takes control                              │
└─────────────────────────────────────────────────────────────┘
```

**Your Main Class:**
```java
@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
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
3. **@ComponentScan**: Scans `com.zerooneblog.blog` and all sub-packages for components

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
│    → Port: 8080 (default) or from application.yaml          │
│    → Context path: /                                        │
│    → Connectors configured                                  │
├─────────────────────────────────────────────────────────────┤
│ 5. DispatcherServlet registered                             │
│    → Front controller for all HTTP requests                 │
└─────────────────────────────────────────────────────────────┘
```

**Configuration from your application.yaml:**
```yaml
server:
  tomcat:
    max-swallow-size: 100MB  # Max size for request body
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

**Spring IoC:**
```java
// SPRING creates and injects dependencies
@Service
public class PostService {
    private final PostRepository repository;  // ✅ Loose coupling
    private final UserService userService;
    
    // Constructor Injection - Spring provides instances
    public PostService(PostRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }
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
│    → Scans com.zerooneblog.blog package                      │
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

### Types of DI in Your Project

#### 1. **Constructor Injection** (Recommended ✅)
```java
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    
    // Spring automatically injects dependencies
    public PostService(PostRepository postRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userService = userService;
    }
}
```

**Why Constructor Injection is Best:**
- ✅ Immutable (final fields)
- ✅ Testable (can pass mock dependencies)
- ✅ Required dependencies are explicit
- ✅ No need for @Autowired annotation (since Spring 4.3)

#### 2. **Field Injection** (Not recommended ⚠️)
```java
@Service
public class SomeService {
    @Autowired  // Injected via reflection
    private PostRepository postRepository;
}
```

---

## 🫘 Spring Beans Deep Dive

### What is a Bean?

**A bean is simply an object managed by the Spring IoC container.**

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
│ 3. BEAN NAME AWARE                                             │
│    → setBeanName() if implements BeanNameAware                │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 4. BEAN FACTORY AWARE                                          │
│    → setBeanFactory() if implements BeanFactoryAware          │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 5. PRE-INITIALIZATION                                          │
│    → BeanPostProcessor.postProcessBeforeInitialization()      │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 6. INITIALIZATION                                              │
│    → @PostConstruct method called                              │
│    → afterPropertiesSet() if implements InitializingBean      │
│    → Custom init method (if defined)                           │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 7. POST-INITIALIZATION                                         │
│    → BeanPostProcessor.postProcessAfterInitialization()       │
│    → Bean is ready to use! 🎉                                  │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ BEAN IN USE                                                    │
│ Application uses the bean...                                   │
└────────────────────────────────────────────────────────────────┘
           ↓
┌────────────────────────────────────────────────────────────────┐
│ 8. DESTRUCTION (Application Shutdown)                          │
│    → @PreDestroy method called                                 │
│    → destroy() if implements DisposableBean                    │
│    → Custom destroy method (if defined)                        │
└────────────────────────────────────────────────────────────────┘
```

### Bean Scopes

```java
@Service  // Default: Singleton scope
public class PostService { }

// Available scopes:
// @Scope("singleton")    - One instance per container (DEFAULT)
// @Scope("prototype")    - New instance every time
// @Scope("request")      - One instance per HTTP request
// @Scope("session")      - One instance per HTTP session
// @Scope("application")  - One instance per ServletContext
```

### Your Beans in the Project

```
ApplicationContext (IoC Container)
├── Controllers (@RestController)
│   ├── PostController
│   ├── UserController
│   ├── AuthController
│   ├── CommentController
│   ├── LikeController
│   ├── NotificationController
│   ├── ReportController
│   ├── AdminController
│   └── UploadController
│
├── Services (@Service)
│   ├── PostService
│   ├── UserService
│   ├── CommentService
│   ├── LikeService
│   ├── ReportService
│   ├── FileStorageService
│   └── CustomUserDetailsService
│
├── Repositories (@Repository)
│   ├── PostRepository (extends JpaRepository)
│   ├── UserRepository
│   ├── CommentRepository
│   ├── LikeRepository
│   ├── ReportRepository
│   └── SubscriptionRepository
│
├── Components (@Component)
│   ├── AdminInitializer
│   └── StartupInfo
│
└── Configuration Beans (@Configuration, @Bean)
    ├── SecurityConfig
    ├── JwtUtil
    └── Other config beans
```

---

## 🗄️ JPA (Java Persistence API)

### What is JPA?

**JPA is a specification (interface) for object-relational mapping (ORM).**
- It defines HOW to map Java objects to database tables
- It's NOT an implementation, just a specification
- Hibernate is the implementation you're using

### How JPA Works

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

### Entity Mapping Example

```java
@Entity  // Marks class as JPA entity
@Table(name = "posts")  // Maps to 'posts' table
public class Post {
    
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;
    
    @Column(nullable = false, length = 500)  // Maps to 'title' column
    private String title;
    
    @Lob  // Large object (TEXT/BLOB)
    private String content;
    
    @ManyToOne  // Many posts belong to one user
    @JoinColumn(name = "user_id")  // Foreign key column
    private User author;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments;
}
```

### JPA Repository Magic

```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Spring Data JPA automatically implements these methods!
    // No need to write SQL or implementation
    
    // Derived query methods (parsed from method name)
    List<Post> findByAuthorId(Long authorId);
    List<Post> findByTitleContaining(String keyword);
    
    // Custom query
    @Query("SELECT p FROM Post p WHERE p.published = true ORDER BY p.createdAt DESC")
    List<Post> findPublishedPosts();
}
```

**Spring Data JPA Auto-Implementation:**
```
┌──────────────────────────────────────────────────────────────┐
│ 1. Spring scans @Repository interfaces                       │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. Creates proxy implementation at runtime                   │
│    → Uses JDK Dynamic Proxy or CGLIB                         │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. Registers as Spring Bean                                  │
│    → You can inject it like any other bean                   │
└──────────────────────────────────────────────────────────────┘
```

**Provided methods (from JpaRepository):**
- `save(entity)` - INSERT or UPDATE
- `findById(id)` - SELECT by primary key
- `findAll()` - SELECT all
- `deleteById(id)` - DELETE by primary key
- `count()` - COUNT(*)
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
6. Provides caching mechanisms

### Hibernate Architecture in Your App

```
┌──────────────────────────────────────────────────────────────┐
│ SessionFactory (Created at startup)                          │
│                                                               │
│ • One per application                                        │
│ • Thread-safe                                                │
│ • Heavy object (expensive to create)                         │
│ • Caches metadata (entity mappings, SQL generators)          │
└──────────────────────────────────────────────────────────────┘
           ↓ (creates)
┌──────────────────────────────────────────────────────────────┐
│ EntityManager / Session (per request/transaction)            │
│                                                               │
│ • One per request (in web apps)                              │
│ • NOT thread-safe                                            │
│ • Manages Persistence Context                                │
└──────────────────────────────────────────────────────────────┘
           ↓ (uses)
┌──────────────────────────────────────────────────────────────┐
│ Persistence Context (First-Level Cache)                      │
│                                                               │
│ • Cache of entities within a transaction                     │
│ • Tracks entity state (new, managed, detached, removed)     │
│ • Automatic dirty checking                                   │
└──────────────────────────────────────────────────────────────┘
           ↓ (generates)
┌──────────────────────────────────────────────────────────────┐
│ SQL Query Generator                                          │
│                                                               │
│ • HQL → SQL translation                                      │
│ • Criteria API → SQL                                         │
│ • Dialect-specific SQL (PostgreSQL dialect)                  │
└──────────────────────────────────────────────────────────────┘
```

### Entity States in Hibernate

```
┌──────────────┐
│  TRANSIENT   │  Object created but not associated with Hibernate
│    (New)     │  Post post = new Post();
└──────────────┘
       ↓ (save/persist)
┌──────────────┐
│   MANAGED    │  Associated with persistence context
│ (Persistent) │  entityManager.persist(post);
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

### Configuration from application.yaml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Automatically update database schema
      # Options:
      # - create: Drop and recreate tables
      # - create-drop: Create tables, drop on shutdown
      # - update: Update schema (safe for production)
      # - validate: Only validate schema
      # - none: Do nothing
    show-sql: true  # Print SQL queries to console
```

### Lazy vs Eager Loading

```java
@Entity
public class Post {
    @ManyToOne(fetch = FetchType.LAZY)  // Default for @ManyToOne
    private User author;  // Not loaded until accessed
    
    @OneToMany(fetch = FetchType.LAZY)  // Default for @OneToMany
    private List<Comment> comments;  // Not loaded until accessed
}

// When you do:
Post post = postRepository.findById(1L);
// SQL: SELECT * FROM posts WHERE id = 1
// Does NOT fetch author or comments yet!

// When you access:
String authorName = post.getAuthor().getName();
// SQL: SELECT * FROM users WHERE id = post.user_id
// NOW it fetches the author!
```

---

## 🔌 JDBC (Java Database Connectivity)

### JDBC Layer

**JDBC is the low-level API for database communication.**

```
┌──────────────────────────────────────────────────────────────┐
│ JDBC API (java.sql package)                                  │
│                                                               │
│ • DriverManager: Manages database drivers                    │
│ • Connection: Represents database connection                 │
│ • Statement/PreparedStatement: Executes SQL                  │
│ • ResultSet: Contains query results                          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ JDBC Driver (PostgreSQL Driver)                              │
│                                                               │
│ • Vendor-specific implementation                             │
│ • Translates JDBC calls to database protocol                 │
│ • From pom.xml: org.postgresql:postgresql                    │
└──────────────────────────────────────────────────────────────┘
```

### Configuration

```yaml
# Your application.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blog_db
    #    └─┬─┘ └────────┬──────┘  └──┬──┘ └──┬──┘
    #   Protocol   Driver Type   Host:Port  DB Name
    username: ZAKRI
    password: 'jw52U[6^K/8v'
```

**What happens:**
1. Spring Boot auto-configures HikariCP connection pool
2. Creates database connections on startup
3. Hibernate uses these connections via JDBC
4. Connections are pooled and reused (efficient!)

### Connection Pool (HikariCP)

```
┌──────────────────────────────────────────────────────────────┐
│ HikariCP Connection Pool (Fast & Lightweight)                │
│                                                               │
│ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐               │
│ │ DB   │ │ DB   │ │ DB   │ │ DB   │ │ DB   │               │
│ │Conn 1│ │Conn 2│ │Conn 3│ │Conn 4│ │Conn 5│               │
│ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘               │
│   ✓Free   In-Use   ✓Free   In-Use   ✓Free                  │
│                                                               │
│ Benefits:                                                     │
│ • Reuses connections (fast!)                                 │
│ • Manages connection lifecycle                               │
│ • Handles connection timeouts                                │
│ • Optimal pool size based on CPU cores                       │
└──────────────────────────────────────────────────────────────┘
```

---

## 🌐 Request Processing Flow

### Complete Flow: HTTP Request → Response

```
┌──────────────────────────────────────────────────────────────┐
│ 1. HTTP REQUEST                                              │
│    POST /api/posts                                           │
│    { "title": "My Post", "content": "..." }                  │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. EMBEDDED TOMCAT SERVER                                    │
│    • Receives HTTP request on port 8080                      │
│    • Creates HttpServletRequest object                       │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. DISPATCHER SERVLET (Front Controller)                     │
│    • Central entry point for all requests                    │
│    • Finds appropriate handler (controller method)           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. HANDLER MAPPING                                           │
│    • Maps /api/posts → PostController.createPost()          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. INTERCEPTORS (if any)                                     │
│    • Security checks (JWT validation)                        │
│    • Logging, authentication, etc.                           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 6. CONTROLLER                                                │
│    @RestController                                           │
│    public class PostController {                             │
│        @PostMapping("/api/posts")                            │
│        public Post create(@RequestBody Post post) {          │
│            return postService.save(post);                    │
│        }                                                      │
│    }                                                          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 7. SERVICE LAYER (Business Logic)                           │
│    @Service                                                  │
│    public class PostService {                                │
│        public Post save(Post post) {                         │
│            // Business logic here                            │
│            return postRepository.save(post);                 │
│        }                                                      │
│    }                                                          │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 8. REPOSITORY (Data Access)                                  │
│    @Repository                                               │
│    interface PostRepository extends JpaRepository<Post,Long> │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 9. HIBERNATE/JPA                                             │
│    • Translates Java method to SQL                           │
│    • INSERT INTO posts (title, content) VALUES (?, ?)        │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 10. JDBC / DATABASE                                          │
│     • Executes SQL via connection pool                       │
│     • Returns result                                         │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 11. RESPONSE FLOWS BACK UP                                   │
│     Database → JDBC → Hibernate → Repository → Service       │
│     → Controller → DispatcherServlet → Tomcat → HTTP Response│
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 12. HTTP RESPONSE                                            │
│     Status: 200 OK                                           │
│     Content-Type: application/json                           │
│     Body: { "id": 1, "title": "My Post", ... }               │
└──────────────────────────────────────────────────────────────┘
```

---

## 📦 Spring Dependencies from pom.xml

```xml
<dependencies>
    <!-- Web: Tomcat, Spring MVC, REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        ➜ Includes: Tomcat, Jackson, Spring MVC
    </dependency>
    
    <!-- JPA: Hibernate, Transaction Management -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        ➜ Includes: Hibernate, JPA API, Spring Data JPA
    </dependency>
    
    <!-- Security: Authentication, Authorization -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        ➜ Includes: Spring Security Core
    </dependency>
    
    <!-- PostgreSQL JDBC Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        ➜ Database driver for PostgreSQL
    </dependency>
    
    <!-- Validation: @Valid, @NotNull, etc. -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
        ➜ Includes: Hibernate Validator
    </dependency>
</dependencies>
```

---

# Frontend Architecture (Angular)

## 🏁 Angular Application Bootstrap

### 1. **Application Startup Process**

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
│ 3. bootstrapApplication() called                             │
│    • Creates Angular platform (Zone.js initialized)          │
│    • Creates root injector (DI container)                    │
│    • Registers all providers                                 │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. App Component created                                     │
│    @Component({ selector: 'app-root', ... })                 │
│    • Component instance created                              │
│    • Dependencies injected                                   │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. Change Detection runs                                     │
│    • Renders component template                              │
│    • Replaces <app-root> with actual content                 │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 6. Router initialized                                        │
│    • Reads current URL                                       │
│    • Matches route                                           │
│    • Loads corresponding component                           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ APPLICATION READY! 🎉                                         │
└──────────────────────────────────────────────────────────────┘
```

### Your main.ts Breakdown

```typescript
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>'
})
export class App {}

bootstrapApplication(App, {
  providers: [
    provideRouter(routes),              // Routing configuration
    provideHttpClient(                  // HTTP client with interceptors
      withInterceptors([
        authInterceptor,                // Adds JWT token to requests
        errorInterceptor                // Handles HTTP errors
      ])
    ),
    provideAnimations()                 // Angular Material animations
  ]
}).catch(err => console.error(err));
```

---

## 🌊 Zone.js - The Magic Behind Change Detection

### What is Zone.js?

**Zone.js is a library that intercepts ALL asynchronous operations in JavaScript.**

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
│                                                               │
│ button.addEventListener('click', () => {                     │
│   this.count++;  // Value changes                            │
│   // 😞 View NOT automatically updated                       │
│ });                                                           │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│ With Zone.js (Angular uses this)                             │
│                                                               │
│ button.addEventListener('click', () => {                     │
│   // Zone.js wraps this callback!                            │
│   this.count++;  // Value changes                            │
│   // 🎉 Zone.js notifies Angular                             │
│   // → Angular runs change detection                         │
│   // → View automatically updated!                           │
│ });                                                           │
└──────────────────────────────────────────────────────────────┘
```

### Zone.js Execution Context

```
┌─────────────────────────────────────────────────────────────┐
│ NgZone (Angular's Zone)                                     │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ Task 1: User clicks button                              ││
│ │   → Execute event handler                               ││
│ │   → Run change detection when done                      ││
│ └─────────────────────────────────────────────────────────┘│
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ Task 2: HTTP request completes                          ││
│ │   → Execute observable callback                         ││
│ │   → Run change detection when done                      ││
│ └─────────────────────────────────────────────────────────┘│
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ Task 3: setTimeout fires                                ││
│ │   → Execute timeout callback                            ││
│ │   → Run change detection when done                      ││
│ └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

**Key Points:**
- Every async operation runs inside a Zone
- When async operation completes, Zone.js notifies Angular
- Angular runs change detection to update the view
- You don't need to manually trigger updates!

---

## 🔄 Change Detection

### What is Change Detection?

**The process of synchronizing the component's data model with the view (DOM).**

```typescript
@Component({
  template: '<h1>Count: {{ count }}</h1>'  // View
})
export class CounterComponent {
  count = 0;  // Model
  
  increment() {
    this.count++;  // Model changes
    // Change detection runs automatically
    // View updates to show new count
  }
}
```

### Change Detection Strategies

```typescript
// Default Strategy
@Component({
  selector: 'app-post',
  changeDetection: ChangeDetectionStrategy.Default  // Check every time
})

// OnPush Strategy (Optimized)
@Component({
  selector: 'app-post',
  changeDetection: ChangeDetectionStrategy.OnPush  // Check only when:
  // 1. @Input() reference changes
  // 2. Event fired from component
  // 3. Async pipe emits new value
  // 4. Manually triggered
})
```

### Change Detection Tree

```
          App Component
           /          \
          /            \
    Header           Feed Component (OnPush)
                      /           \
                     /             \
              Post List        Sidebar
              /      \
             /        \
        Post Card   Post Card
```

**When change detection runs:**
1. Starts from root component
2. Checks component + all children (unless OnPush)
3. Updates DOM if changes detected

---

## 📡 RxJS - Reactive Programming

### What is RxJS?

**RxJS (Reactive Extensions for JavaScript) is a library for reactive programming using Observables.**

### Observable Pattern

```typescript
// Traditional (Promises)
fetch('/api/posts')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error(error));
// ✅ Handles ONE value
// ❌ Can't cancel
// ❌ Can't retry easily

// RxJS (Observables)
http.get('/api/posts')
  .pipe(
    retry(3),              // Retry on error
    timeout(5000),         // Cancel after 5s
    catchError(handleError)
  )
  .subscribe({
    next: data => console.log(data),
    error: err => console.error(err),
    complete: () => console.log('Done')
  });
// ✅ Handles MULTIPLE values
// ✅ Can cancel
// ✅ Rich operators (map, filter, retry, etc.)
```

### Observable Lifecycle

```
┌──────────────────────────────────────────────────────────────┐
│ 1. CREATION                                                  │
│    const obs$ = http.get('/api/posts');                      │
│    → Observable created (COLD - not executing yet)           │
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

### Common RxJS Operators in Your Project

```typescript
// Service example
@Injectable()
export class PostService {
  private http = inject(HttpClient);
  
  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>('/api/posts').pipe(
      map(response => response.data),        // Transform data
      tap(posts => console.log(posts)),      // Side effect (logging)
      catchError(error => {                  // Error handling
        console.error(error);
        return of([]);  // Return empty array
      }),
      shareReplay(1)                         // Cache result
    );
  }
  
  searchPosts(term: string): Observable<Post[]> {
    return this.searchTerm$.pipe(
      debounceTime(300),        // Wait 300ms after user stops typing
      distinctUntilChanged(),   // Only if term changed
      switchMap(term =>         // Cancel previous request
        this.http.get(`/api/posts/search?q=${term}`)
      )
    );
  }
}
```

### Hot vs Cold Observables

```typescript
// COLD Observable (most HTTP requests)
const cold$ = http.get('/api/posts');
cold$.subscribe(data => console.log('Sub 1:', data));
cold$.subscribe(data => console.log('Sub 2:', data));
// → Makes 2 separate HTTP requests (one per subscription)

// HOT Observable (shared)
const hot$ = http.get('/api/posts').pipe(shareReplay(1));
hot$.subscribe(data => console.log('Sub 1:', data));
hot$.subscribe(data => console.log('Sub 2:', data));
// → Makes only 1 HTTP request (shared between subscriptions)
```

---

## 🎯 Signals (Angular 16+)

### What are Signals?

**Signals are a new reactive primitive in Angular for managing state with fine-grained reactivity.**

### Signals vs RxJS

```typescript
// Traditional (RxJS)
export class PostComponent {
  private postsSubject = new BehaviorSubject<Post[]>([]);
  posts$ = this.postsSubject.asObservable();
  
  loadPosts() {
    this.http.get<Post[]>('/api/posts')
      .subscribe(posts => this.postsSubject.next(posts));
  }
}
// Template: posts$ | async

// Modern (Signals)
export class PostComponent {
  posts = signal<Post[]>([]);  // Writable signal
  
  loadPosts() {
    this.http.get<Post[]>('/api/posts')
      .subscribe(posts => this.posts.set(posts));
  }
}
// Template: posts()  (no async pipe needed!)
```

### Signal Types

```typescript
// 1. Writable Signal
count = signal(0);              // Create
count.set(5);                   // Set value
count.update(c => c + 1);       // Update based on current

// 2. Computed Signal (readonly, derived)
doubled = computed(() => this.count() * 2);
// Automatically updates when count changes!

// 3. Effect (side effects)
effect(() => {
  console.log('Count changed:', this.count());
  // Runs automatically when count changes
});
```

### Signal Benefits

```
RxJS Observables                    Signals
├─ Need subscribe/unsubscribe      ├─ No subscription needed
├─ Async pipe in templates         ├─ Direct value access
├─ Memory leak risk                ├─ No memory leaks
├─ Complex for simple state        ├─ Simple and intuitive
├─ Great for async/events          ├─ Great for sync state
└─ Need shareReplay for caching    └─ Automatically cached
```

**When to use what:**
- **Signals**: Component state, derived values, simple reactivity
- **RxJS**: HTTP requests, complex async flows, event streams

---

## 🔌 Dependency Injection (DI) in Angular

### How DI Works

```typescript
// Service
@Injectable({
  providedIn: 'root'  // Singleton across app
})
export class PostService {
  private http = inject(HttpClient);  // Modern inject()
  
  getPosts() {
    return this.http.get('/api/posts');
  }
}

// Component (OLD way)
@Component({...})
export class PostListComponent {
  constructor(private postService: PostService) {}
  // Angular injects PostService instance
}

// Component (NEW way - recommended)
@Component({...})
export class PostListComponent {
  private postService = inject(PostService);  // Cleaner!
}
```

### Injection Hierarchy

```
┌──────────────────────────────────────────────────────────────┐
│ Root Injector (providedIn: 'root')                          │
│ • Services live here by default                             │
│ • Singleton across entire application                        │
│ • Examples: HttpClient, Router, PostService                  │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Platform Injector                                            │
│ • Platform-level services                                    │
│ • Rarely used directly                                       │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Module Injector (if using NgModules)                         │
│ • Module-level services                                      │
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

### HTTP Client with Interceptors

```typescript
// Your HTTP setup in main.ts
provideHttpClient(
  withInterceptors([
    authInterceptor,      // Adds JWT token
    errorInterceptor      // Handles errors
  ])
)
```

### Auth Interceptor Example

```typescript
// Intercepts ALL HTTP requests
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('jwt_token');
  
  if (token) {
    // Clone request and add Authorization header
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  
  return next(req);  // Pass to next interceptor or HttpClient
};
```

### HTTP Request Flow

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
│ Auth Interceptor: Adds JWT token                             │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Error Interceptor: Wraps with error handling                 │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ HttpClient: Makes actual HTTP request                        │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Backend Server: Processes request                            │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Response flows back through interceptors                     │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ Component receives data (via Observable)                     │
└──────────────────────────────────────────────────────────────┘
```

---

## 🧩 Angular Component Architecture

### Component Structure

```typescript
@Component({
  selector: 'app-post-card',           // <app-post-card>
  standalone: true,                    // No NgModule needed
  imports: [CommonModule, RouterLink], // Dependencies
  templateUrl: './post-card.component.html',
  styleUrl: './post-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PostCardComponent {
  // Input (data from parent)
  @Input() post!: Post;
  
  // Output (events to parent)
  @Output() liked = new EventEmitter<void>();
  
  // Dependency injection
  private router = inject(Router);
  
  // Lifecycle hooks
  ngOnInit() {
    console.log('Component initialized');
  }
  
  ngOnDestroy() {
    console.log('Component destroyed');
  }
  
  // Methods
  onLike() {
    this.liked.emit();
  }
}
```

### Component Lifecycle Hooks

```
┌──────────────────────────────────────────────────────────────┐
│ 1. constructor()                                             │
│    → TypeScript class instantiation                          │
│    → DI happens here                                         │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. ngOnChanges()                                             │
│    → When @Input() properties change                         │
│    → Can be called multiple times                            │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. ngOnInit()                                                │
│    → Component initialized                                   │
│    → Called ONCE                                             │
│    → Best place for initialization logic                     │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. ngDoCheck()                                               │
│    → Every change detection cycle                            │
│    → Expensive, use carefully                                │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. ngAfterViewInit()                                         │
│    → After component view initialized                        │
│    → Can access ViewChild elements                           │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ COMPONENT IN USE                                             │
└──────────────────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│ 6. ngOnDestroy()                                             │
│    → Before component destroyed                              │
│    → Cleanup: unsubscribe, clear timers                      │
└──────────────────────────────────────────────────────────────┘
```

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
│                                                                  │
│ 1. Zone.js detects click event                                  │
│ 2. Component method executed: createPost()                      │
│ 3. PostService.create() called                                  │
│ 4. HttpClient.post('/api/posts', data) - Observable created    │
│ 5. Auth Interceptor adds JWT token                              │
│ 6. Error Interceptor wraps request                              │
│ 7. HTTP request sent to backend                                 │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ SPRING BOOT BACKEND (localhost:8080)                            │
│                                                                  │
│ 1. Tomcat receives HTTP request on port 8080                    │
│ 2. DispatcherServlet processes request                          │
│ 3. Security Filter Chain validates JWT token                    │
│ 4. Handler Mapping routes to PostController.create()            │
│ 5. PostController (Bean) method executed                        │
│ 6. Dependencies injected (PostService, UserService)             │
│ 7. Business logic in PostService                                │
│ 8. PostRepository.save() called                                 │
│ 9. Spring Data JPA generates implementation                     │
│ 10. Hibernate translates to SQL                                 │
│ 11. JDBC executes: INSERT INTO posts...                         │
│ 12. PostgreSQL stores data                                      │
│ 13. Result flows back through layers                            │
│ 14. JSON response sent to frontend                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ ANGULAR FRONTEND RECEIVES RESPONSE                              │
│                                                                  │
│ 1. HttpClient Observable emits response                         │
│ 2. subscribe() callback executed                                │
│ 3. Component updates state (signal/property)                    │
│ 4. Zone.js triggers change detection                            │
│ 5. View updated with new post                                   │
│ 6. User sees success message                                    │
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

### Frontend (Angular)

| Concept | Purpose |
|---------|---------|
| **Zone.js** | Intercepts async operations for change detection |
| **Change Detection** | Synchronizes model with view (DOM) |
| **RxJS/Observables** | Reactive programming for async data streams |
| **Signals** | New reactive primitive for fine-grained reactivity |
| **Dependency Injection** | Provides services to components |
| **Interceptors** | Modify HTTP requests/responses globally |
| **Components** | Building blocks of UI (template + logic) |
| **Services** | Reusable business logic and data management |

---

## 📚 Additional Resources

### Backend Learning Path
1. **Java Fundamentals** → **Spring Core (IoC, DI)** → **Spring Boot** → **Spring Data JPA** → **Spring Security**

### Frontend Learning Path
1. **TypeScript** → **Angular Basics** → **RxJS** → **Signals** → **Angular Advanced**

### Recommended Reading
- Spring Documentation: https://spring.io/projects/spring-boot
- Angular Documentation: https://angular.dev
- RxJS Documentation: https://rxjs.dev
- Hibernate Documentation: https://hibernate.org

---

## 🎯 Quick Reference

### Start Backend
```bash
cd backend
./mvnw spring-boot:run
# → Starts on http://localhost:8080
```

### Start Frontend
```bash
cd frontend
npm start
# → Starts on http://localhost:4200
```

### What Happens on Startup

**Backend:**
1. JVM starts
2. Spring Boot auto-configuration
3. IoC container created
4. All beans instantiated and configured
5. Embedded Tomcat starts
6. Database connection established
7. Application ready!

**Frontend:**
1. Browser loads index.html
2. Loads bundled JavaScript (main.ts)
3. Angular platform created
4. Zone.js initialized
5. Root component bootstrapped
6. Router initializes
7. Application ready!

---

**Created:** January 2026  
**Project:** ZeroOneBlog - Social Blogging Platform  
**Tech Stack:** Spring Boot 3.5.7 + Angular 20 + PostgreSQL