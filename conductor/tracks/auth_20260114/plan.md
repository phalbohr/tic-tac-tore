# Plan: User Authentication Flow

## Phase 1: Backend Scaffolding & Security Config [checkpoint: 90664a8]
- [x] Task: Configure Spring Boot with `spring-boot-starter-oauth2-client` and `spring-boot-starter-security` [f00c598]
    - [x] Write tests to verify security configuration blocks unauthorized access
    - [x] Implement initial security configuration
- [x] Task: Set up Google OAuth2 properties in `application.yml` [2917a81]
    - [x] Write tests to verify configuration loading
    - [x] Implement configuration
- [x] Task: Conductor - User Manual Verification 'Backend Scaffolding & Security Config' (Protocol in workflow.md)

## Phase 2: JWT Implementation & User Persistence
- [x] Task: Implement `User` entity and `UserRepository` [224ca3e]
    - [x] Write unit tests for User persistence
    - [x] Implement User entity and Repository
- [x] Task: Implement JWT Service for token generation and validation
    - [x] Write unit tests for JWT generation and claims extraction
    - [x] Implement JWT Service
- [ ] Task: Implement OAuth2 Success Handler to issue JWT
    - [ ] Write tests for the success handler behavior
    - [ ] Implement Custom OAuth2 Success Handler
- [ ] Task: Conductor - User Manual Verification 'JWT Implementation & User Persistence' (Protocol in workflow.md)

## Phase 3: Frontend Login Integration
- [ ] Task: Scaffold Vue 3 frontend with Pinia and Vue Router
    - [ ] Write tests for initial router and store setup
    - [ ] Implement scaffolding
- [ ] Task: Implement `authStore` in Pinia
    - [ ] Write tests for login/logout actions and state changes
    - [ ] Implement authStore
- [ ] Task: Create Login Page with Google sign-in button
    - [ ] Write component tests for the Login page
    - [ ] Implement Login page
- [ ] Task: Conductor - User Manual Verification 'Frontend Login Integration' (Protocol in workflow.md)

## Phase 4: Final Integration & Verification
- [ ] Task: Implement Authentication Middleware/Guards on Frontend
    - [ ] Write tests for route protection
    - [ ] Implement navigation guards
- [ ] Task: Verify end-to-end flow
    - [ ] Write integration tests for the full login flow
    - [ ] Perform manual end-to-end verification
- [ ] Task: Conductor - User Manual Verification 'Final Integration & Verification' (Protocol in workflow.md)
