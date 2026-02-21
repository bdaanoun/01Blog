# 🚀 01Blog

A fullstack social blogging platform where students share their learning journey, interact, and grow together.

---

## 📌 Overview

**01Blog** is a social platform designed for students to document their progress, share discoveries, and engage with a community of learners.

Users can:

* Create and manage posts
* Follow other users
* Like and comment on content
* Receive notifications
* Report inappropriate behavior

Admins can:

* Moderate content
* Manage users
* Handle reports

---

## 🧠 Learning Objectives

* Master **Spring Boot** (REST APIs, security, services)
* Build dynamic UIs with **Angular**
* Understand **fullstack architecture**
* Handle **authentication & role-based access**
* Work with **relational databases**
* Manage **user-generated content**
* Collaborate using **Git & GitHub**

---

## 🏗️ Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security / JWT
* JPA (Hibernate)
* PostgreSQL / MySQL

### Frontend

* Angular
* Angular Material / Bootstrap
* RxJS

### Others

* REST API
* File Upload (Media handling)
* Git & GitHub

---

## 🔐 Features

### 👤 Authentication

* User registration & login
* Secure password hashing
* JWT-based authentication
* Role-based access (User / Admin)

### 📝 Posts

* Create, edit, delete posts
* Upload images/videos
* Like & comment system
* Timestamp & media preview

### 👥 User Profiles (Blocks)

* Public user profiles
* View all posts by user
* Subscribe / unsubscribe
* Followers receive notifications

### 🔔 Notifications

* New posts from subscribed users
* Mark as read/unread

### 🚨 Reports

* Report users for inappropriate content
* Include reason + timestamp
* Visible only to admins

### 🛠️ Admin Panel

* Manage users (ban/delete)
* Moderate posts
* Handle reports
* Secure admin-only routes

---

## 🎨 Frontend Features

* Responsive UI
* Feed from subscribed users
* Profile (block) management
* Media upload with preview
* Clean UX with Angular Material / Bootstrap

---

## ⚙️ Installation & Setup

### 🔧 Backend (Spring Boot)

```bash
git clone https://github.com/your-username/01Blog.git
cd backend

# Configure application.properties
# (DB credentials, JWT secret, etc.)

./mvnw spring-boot:run
```

Backend runs on: [http://localhost:8080](http://localhost:8080)

---

### 💻 Frontend (Angular)

```bash
cd frontend
npm install
ng serve
```

Frontend runs on: [http://localhost:4200](http://localhost:4200)

---

## 🗄️ Database

Relational database (PostgreSQL or MySQL)

Entities:

* Users
* Posts
* Comments
* Likes
* Subscriptions
* Reports

---

## 🔒 Security

* JWT authentication
* Protected routes
* Role-based access control
* Secure password storage (BCrypt)

---

## 📦 Project Structure

```
01Blog/
├── backend/
├── frontend/
└── README.md
```

---

## ⭐ Bonus Features (Optional)

* WebSockets (real-time updates)
* Infinite scroll
* Dark mode 🌙
* Admin analytics
* Markdown support

---

## 📊 Evaluation Criteria

* Functionality
* Security
* UI/UX
* Code quality

---

## 👨‍💻 Author

**Bilal Daanouni**
Fullstack Developer

---

## 💡 Final Note

01Blog simulates a real-world social platform, combining backend and frontend skills to build a scalable and secure application.
