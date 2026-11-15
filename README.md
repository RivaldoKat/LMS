# School LMS Backend API

A Spring Boot REST API for a single-school Learning Management System, designed for administrators, teachers, learners, and parents.

## Core Features
- **JWT & Stateless Security:** Secure authentication and role-based access control (RBAC) via Spring Security.
- **Academic Management:** Course creation, student course enrollments, and term management.
- **Assignments & Submissions:** Assignment publishing, student file uploads, and a comprehensive grading/feedback workflow.
- **Attendance & Announcements:** Daily attendance tracking and announcements that can target a course or the whole school.
- **Learner data scope:** Learners receive only their enrolled courses, assignments for those courses, and school-wide or enrolled-course announcements. Enrollment and attendance management is staff-only.
- **Robust Error Handling:** Global exception handling (`@RestControllerAdvice`) and strict input validation (`@Valid`).

## Configuration

- `JWT_SECRET` is required and must contain at least 32 bytes.
- Browser clients are allowed from `http://localhost:9000`, `http://127.0.0.1:9000`, and `http://localhost:5173` by default. Set `LMS_CORS_ALLOWED_ORIGINS` to a comma-separated list of trusted frontend origins when deploying elsewhere.
- `LMS_ADMIN_EMAIL` and `LMS_ADMIN_PASSWORD` bootstrap the initial admin when no admin exists. Both must be set together. The password is stored with BCrypt.
- Optional sample records can be generated at startup by setting `LMS_SAMPLE_DATA_ENABLED=true` and `LMS_SAMPLE_DATA_PASSWORD` to a password for the demo accounts. This creates 120 learners, 15 teachers, 30 courses, 12 assignments per course, enrollments, attendance records, submissions, and announcements; repeated starts reuse those records. Demo accounts use `learner001@example.test` through `learner120@example.test` and `teacher001@example.test` through `teacher015@example.test`. Leave sample data disabled for production databases.
- PostgreSQL uses the Render internal JDBC URL in `src/main/resources/application.yaml`; set `DB_USER` and `DB_PASSWORD` in the Render web service environment. Set `JWT_SECRET` to a value of at least 32 bytes. `PORT` is read from Render automatically and defaults to `8080` locally. Set `LMS_CORS_ALLOWED_ORIGINS` to the deployed frontend origin.
- Sample data is disabled by default. To use uploaded files with a Render persistent disk, mount the disk (for example at `/var/data`) and set `STORAGE_LOCATION=/var/data/uploads`. Without a persistent disk, uploads on Render's local filesystem may be lost when the service restarts or redeploys.

## Deploying to Render

This repository includes a `render.yaml` Blueprint and a `Dockerfile`. In Render, create a new Blueprint from this repository and provide `DB_URL`, `DB_USER`, `DB_PASSWORD`, and `LMS_CORS_ALLOWED_ORIGINS` when prompted. Use the PostgreSQL database's internal JDBC URL, for example `jdbc:postgresql://HOST:5432/DATABASE?sslmode=require`, and set the CORS value to the frontend's origin only (for example `https://YOURNAME.github.io`). Render generates `JWT_SECRET` automatically. The service runs Flyway migrations at startup and exposes `/actuator/health` for health checks.

The Blueprint uses Render's free web-service plan. Its local upload directory is ephemeral, so uploaded files can be lost when the service restarts or redeploys; durable uploads require persistent or external storage.


## Tech Stack
- **Language:** Java 27
- **Framework:** Spring Boot (Web, Security, Data JPA, Validation, Actuator)
- **Database:** PostgreSQL (Render)
- **Security:** JJWT (JSON Web Tokens), BCrypt Password Hashing
- **Build Tool:** Maven
- **Utilities:** Lombok
