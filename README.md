# School LMS Backend API

A Spring Boot REST API for a single-school Learning Management System, designed for administrators, teachers, learners, and parents.

## Core Features
- **JWT & Stateless Security:** Secure authentication and role-based access control (RBAC) via Spring Security.
- **Academic Management:** Course creation, student course enrollments, and term management.
- **Assignments & Submissions:** Assignment publishing, student file uploads, and a comprehensive grading/feedback workflow.
- **Attendance & Announcements:** Daily attendance tracking with status enums and school-wide announcement broadcasting.
- **Robust Error Handling:** Global exception handling (`@RestControllerAdvice`) and strict input validation (`@Valid`).

## Configuration

- `JWT_SECRET` is required and must contain at least 32 bytes. Never use a committed or publicly known signing key.
- `LMS_ADMIN_EMAIL` and `LMS_ADMIN_PASSWORD` bootstrap the initial admin when no admin exists. Both must be set together. The password is stored with BCrypt.
- By default, the app uses an ephemeral H2 database for local development. Set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` to use PostgreSQL. Flyway manages schema migrations and Hibernate validates the schema at startup.
- The H2 console is disabled by default; only enable `H2_CONSOLE_ENABLED` in a trusted local environment.

The default H2 database resets on restart. Do not use it for production data.

## Tech Stack
- **Language:** Java 27
- **Framework:** Spring Boot (Web, Security, Data JPA, Validation, Actuator)
- **Database:** H2 In-Memory Database (Easily configurable to PostgreSQL/MySQL)
- **Security:** JJWT (JSON Web Tokens), BCrypt Password Hashing
- **Build Tool:** Maven
- **Utilities:** Lombok
