# School LMS Backend API

A secure, multi-layered, andbackend built for a single-school Learning Management System (LMS). Designed to provide a comprehensive REST API infrastructure for administrators, teachers, students, and parents, with a clean architecture ready for frontend integration (Quasar/Vue)*.

## Core Features
- **JWT & Stateless Security:** Secure authentication and role-based access control (RBAC) via Spring Security.
- **Academic Management:** Course creation, student course enrollments, and term management.
- **Assignments & Submissions:** Assignment publishing, student file uploads, and a comprehensive grading/feedback workflow.
- **Attendance & Announcements:** Daily attendance tracking with status enums and school-wide announcement broadcasting.
- **Robust Error Handling:** Global exception handling (`@RestControllerAdvice`) and strict input validation (`@Valid`).

## Tech Stack
- **Language:** Java 27
- **Framework:** Spring Boot (Web, Security, Data JPA, Validation, Actuator)
- **Database:** H2 In-Memory Database (Easily configurable to PostgreSQL/MySQL)
- **Security:** JJWT (JSON Web Tokens), BCrypt Password Hashing
- **Build Tool:** Maven
- **Utilities:** Lombok
