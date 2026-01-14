# Tech Stack - Foosball Statistics Platform

## Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3.x
- **Persistence:** Spring Data JPA
- **Security:** Spring Security
- **Database:** PostgreSQL (Supabase-compatible)

## Frontend
- **Framework:** Vue 3 (Composition API)
- **Language:** TypeScript
- **State Management:** Pinia
- **Routing:** Vue Router
- **Styling:** TailwindCSS + SCSS

## Authentication & Authorization
- **Provider:** Google OAuth2 (via `spring-boot-starter-oauth2-client`)
- **Session Management:** Backend-issued custom JWT for secure, stateless authorization.

## Core Infrastructure
- **Environment:** Compatible with modern cloud-native deployment patterns (e.g., Supabase for DB, standard CI/CD pipelines).
