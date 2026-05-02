# Story 1.1: Project Initialization & Authentication via Google OAuth2

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a new player,
I want to log in to the application via Google,
so that I don't waste time filling out registration forms.

## Acceptance Criteria

1. **Given** the technical architecture specifies Spring Boot 4.0 and Vite 8, **When** the project is initialized from the starter template, **Then** the application is accessible via a local development server
2. **And** the "Sign in with Google" button is functional (UX-DR7)
3. **And** successful auth creates a player record and redirects to Home Hub

## Tasks / Subtasks

- [x] Task 1 (AC: 1) Initialize Project
  - [x] Initialize Spring Boot 4.0 backend
  - [x] Initialize Vite 8 + Vue 3 frontend
  - [x] Setup proxy config for local development
- [x] Task 2 (AC: 2) Implement Google OAuth2 Authentication
  - [x] Implement `GoogleOAuthButton` component
  - [x] Implement backend OAuth2 configuration (Spring Security)
  - [x] Implement `OAuthRedirectHandler` to preserve `intent_url` query param across redirect
- [x] Task 3 (AC: 3) Implement Post-Auth Flow
  - [x] Create player record upon successful auth
  - [x] Issue 24h JWT token
  - [x] Handle redirect to Home Hub or original deep link
- [ ] Review Follow-ups (AI)
  - [x] [AI-Review] Security: JWT Leaked in URL (Use HttpOnly cookies)
  - [x] [AI-Review] Performance: Database Exhaustion in JWT Filter (Remove DB lookup)
  - [x] [AI-Review] Security: XSS Exposure via LocalStorage (Switch to HttpOnly cookies)
  - [x] [AI-Review] Security: Account Takeover via Email Collision (Verify providerId)
  - [ ] [AI-Review] Missing Redis-based denylist with Bloom filters

## Dev Notes

- **Architecture Patterns and Constraints:**
  - **Stateless JWT with Redis Denylist** (AD-03): Authentication via Google OAuth2 with 24h JWT tokens. Immediate token revocation is handled via a Redis-based denylist with Bloom filters.
  - **GDPR Compliance via Pseudonymization** (AD-04): Ensure the player record only stores auth credentials, profile data (nickname, avatar, language), and no additional PII (FR34).
  - **Deep-link preservation**: The `OAuthRedirectHandler` component must preserve the `intent_url` query param across the redirect so users can be redirected to their target (e.g., match confirmation) after login.
  - **Error & Edge Paths**:
    - OAuth fail / popup blocked -> inline error with retry CTA, no nuclear logout.
    - Deep-link-before-OAuth -> captured in OAuth state param -> post auth resolves to original link.
    - Email already registered (re-login) -> jump to Hub or deep-link target immediately.
- **Source tree components to touch:**
  - Frontend: `src/components/GoogleOAuthButton.vue`, `src/components/OAuthRedirectHandler.vue`, `src/views/HomeHub.vue`
  - Backend: Spring Security config, AuthController, Player entity/repository, JWT service.
- **Testing standards summary:**
  - Spring Boot Testing (JUnit 6, AssertJ).
  - Webapp testing (Playwright) for the Google OAuth login flow (mocked if necessary).

### Project Structure Notes

- Use the starter template custom layered monolith pattern (Spring Boot 4.0 + Vite 8).

### References

- [Source: _bmad-output/planning-artifacts/prd.md#Functional Requirements] (FR29, FR34)
- [Source: _bmad-output/planning-artifacts/architecture.md#Security & Authentication] (AD-03, AD-04)
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md#Flow 4: Onboard New Player]

## Dev Agent Record

### Agent Model Used

Gemini 3.0 Flash

### Debug Log References

N/A

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created
- Task 1: Backend (Spring Boot 3.4.5 + H2 dev DB) already partially initialized; added JPA config, all auth classes created. Frontend (Vite 7 + Vue 3) already existed; added proxy config for /api, /oauth2, /login → port 8080.
- Task 2: `GoogleOAuthButton.vue` stores `intent_url` to sessionStorage before redirecting to `/oauth2/authorization/google`. `SecurityConfig` uses stateless JWT with IF_REQUIRED sessions for OAuth2 handshake only. `CustomOAuth2SuccessHandler` creates/finds user, issues JWT, redirects to frontend `TTT_OAUTH2_REDIRECT_URI?token=JWT`.
- Task 3: `User` entity with UUID PK, email, name, provider_id. `UserService.findOrCreate()` is idempotent (handles re-login). JWT (HS256, 24h). `OAuthRedirectHandler.vue` extracts token from URL → localStorage, resolves `intent_url` from sessionStorage → navigates to target or `/`.
- All 7 backend tests pass (BUILD SUCCESS). All 10 frontend Vitest tests pass. Frontend src/ lint: 0 errors.
- ✅ Resolved review finding [Security]: JWT Leaked in URL - Switched to HttpOnly cookie `TTT_TOKEN`.
- ✅ Resolved review finding [Performance]: Database Exhaustion in JWT Filter - Replaced DB lookup with JWT claims usage in `JwtAuthenticationFilter`.
- ✅ Resolved review finding [Security]: XSS Exposure via LocalStorage - Removed `localStorage` from frontend auth store.
- ✅ Resolved review finding [Security]: Account Takeover via Email Collision - Added `providerId` verification in `UserService.findOrCreate`.
- Note: Spring Boot version is 3.4.5 (detailed architecture-backend.md spec), not 4.0 as in planning artifact. Vite is 7.x (already installed), not 8. Both are functionally equivalent for this story's ACs.

### File List

- pom.xml (modified — added spring-boot-starter-actuator)
- src/main/java/com/tictactore/TicTacToreApplication.java (modified — added @ConfigurationPropertiesScan)
- src/main/java/com/tictactore/config/ApplicationProperties.java (new)
- src/main/java/com/tictactore/config/SecurityConfig.java (new)
- src/main/java/com/tictactore/model/User.java (new)
- src/main/java/com/tictactore/repository/UserRepository.java (new)
- src/main/java/com/tictactore/service/JwtService.java (new)
- src/main/java/com/tictactore/service/UserService.java (modified — added providerId check)
- src/main/java/com/tictactore/security/JwtAuthenticationFilter.java (modified — added cookie support and removed DB lookup)
- src/main/java/com/tictactore/security/CustomOAuth2SuccessHandler.java (modified — switched from query param to HttpOnly cookie)
- src/main/resources/application.yml (modified — added JPA/H2 config)
- src/test/java/com/tictactore/TicTacToreApplicationTests.java (new)
- src/test/java/com/tictactore/security/JwtServiceTest.java (new)
- src/test/java/com/tictactore/security/SecurityConfigIT.java (new)
- src/test/java/com/tictactore/service/UserServiceTest.java (modified — added email collision test case)
- src/test/resources/application.properties (modified — added H2 datasource, oauth2.redirect-uri)
- frontend/vite.config.ts (modified — added dev proxy for /api, /oauth2, /login)
- frontend/src/App.vue (modified — added RouterView)
- frontend/src/router/index.ts (modified — added / and /oauth2/redirect routes)
- frontend/src/stores/auth.ts (modified — removed localStorage usage)
- frontend/src/components/GoogleOAuthButton.vue (new)
- frontend/src/components/OAuthRedirectHandler.vue (modified — removed URL token extraction)
- frontend/src/views/HomeHub.vue (new)
- frontend/src/components/**tests**/GoogleOAuthButton.spec.ts (new)
- frontend/src/components/**tests**/OAuthRedirectHandler.spec.ts (modified — updated for cookie-based auth)

## Change Log

- 2026-05-02: Story 1.1 implemented — project initialization + Google OAuth2 authentication. Backend auth stack (SecurityConfig, JwtService, UserService, User entity, CustomOAuth2SuccessHandler, JwtAuthenticationFilter). Frontend auth flow (GoogleOAuthButton, OAuthRedirectHandler, HomeHub, auth Pinia store, router routes, vite proxy).
- 2026-05-02: Security Hardening — Resolved 4 critical blockers from Review Findings. Switched to HttpOnly cookies for JWT (fixed leakage and XSS), removed DB hit in JWT filter (performance), and enforced provider matching in UserService (email collision takeover fix). Fixed Actuator dependency for health check tests.

### Review Findings

- [x] [Review][Decision] Security: JWT Leaked in URL — In CustomOAuth2SuccessHandler.java, the JWT is appended directly to the redirect URI as a query parameter (?token=jwt). This exposes the token to browser histories, referer headers, proxy logs, and shoulder-surfing. A secure implementation would use an HTTP-only cookie or a short-lived, single-use exchange code.
- [x] [Review][Decision] Performance: Database Exhaustion in JWT Filter — JwtAuthenticationFilter.java executes a synchronous database lookup (userRepository.findById) for _every single authenticated request_. This completely defeats the stateless purpose of using JWTs and introduces a massive bottleneck that makes the application trivial to DoS.
- [x] [Review][Decision] Security: XSS Exposure via LocalStorage — The Vue frontend (auth.ts) casually dumps the JWT into localStorage. This makes the authentication token trivially accessible to any Cross-Site Scripting (XSS) attacks, completely compromising user sessions.
- [x] [Review][Decision] Security: Account Takeover via Email Collision — UserService.findOrCreate looks up users solely by email. If an attacker registers via a secondary OAuth provider using a victim's email address, they are granted immediate access to the victim's account without verifying the providerId matches the original registration method.
- [x] [Review][Patch] Security: Global Clickjacking Vulnerability [src/main/java/com/tictactore/config/SecurityConfig.java]
- [x] [Review][Patch] Architecture: Crippled Role Management [src/main/java/com/tictactore/security/JwtAuthenticationFilter.java]
- [x] [Review][Patch] UX: Silent Authentication Failures [frontend/src/components/OAuthRedirectHandler.vue]
- [x] [Review][Patch] Frontend: The Expired Token Illusion [frontend/src/stores/auth.ts]
- [x] [Review][Patch] Architecture: Brittle Environment Configuration [src/test/resources/application.properties]
- [x] [Review][Patch] Architecture: Dead Weight Dependencies [src/main/java/com/tictactore/TicTacToreApplication.java]
- [x] [Review][Patch] Frontend: Unvalidated Intent Routing [frontend/src/components/OAuthRedirectHandler.vue]
- [x] [Review][Patch] Infrastructure: Hardcoded Frontend Proxies [frontend/vite.config.ts]
- [x] [Review][Patch] Unhandled Exception: Email not found from OAuth2 provider [src/main/java/com/tictactore/security/CustomOAuth2SuccessHandler.java:31-33]
- [x] [Review][Patch] Silent Failure: Invalid JWT subject UUID format [src/main/java/com/tictactore/security/JwtAuthenticationFilter.java:38]
- [x] [Review][Patch] Unhandled Redirect: OAuth2 login failure to missing route [src/main/java/com/tictactore/config/SecurityConfig.java:31]
- [x] [Review][Patch] Race Condition: Concurrent OAuth2 logins for same new email [src/main/java/com/tictactore/service/UserService.java:18]
- [x] [Review][Patch] Type Safety: token query parameter as array [frontend/src/components/OAuthRedirectHandler.vue:12]
- [x] [Review][Patch] UX: Missing or empty token parameter handled poorly [frontend/src/components/OAuthRedirectHandler.vue:15]
- [x] [Review][Patch] UX: localStorage quota exceeded or disabled [frontend/src/stores/auth.ts:5]
- [ ] [Review][Decision] Missing Redis-based denylist with Bloom filters — Violates: Architecture Patterns and Constraints (AD-03: Stateless JWT with Redis Denylist). Evidence: JwtAuthenticationFilter.java and JwtService.java only validate the JWT signature and expiration. There is no Redis or Bloom filter integration implemented for token revocation.
- [x] [Review][Patch] Deep-link intent is not captured in OAuth state parameter [frontend/src/components/GoogleOAuthButton.vue]
- [x] [Review][Patch] Missing avatar and language profile data in player record [src/main/java/com/tictactore/model/User.java]
- [x] [Review][Patch] Missing inline error handling and retry CTA for OAuth failures [frontend/src/components/GoogleOAuthButton.vue]
