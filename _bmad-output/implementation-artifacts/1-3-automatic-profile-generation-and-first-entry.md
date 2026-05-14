# Story 1.3: Automatic Profile Generation & First Entry

Status: ready-for-dev

## Story

As a new player,
I want my profile to be created automatically,
so that I can start recording matches immediately.

## Acceptance Criteria

1. **Given** first-time authentication via Google OAuth2, **When** profile is created, **Then** nickname is generated from the email prefix (everything before the `@` symbol, e.g., `john.doe@gmail.com` → `john.doe`).
2. **Given** first-time authentication, **When** profile is created, **Then** a default placeholder avatar is assigned (value: `"placeholder"`).
3. **Given** an authenticated user, **When** they call `GET /api/v1/users/me`, **Then** the endpoint returns the current user's profile including `id`, `email`, `nickname`, `avatar`, and `language`.
4. **Given** a returning user (re-login), **When** `findOrCreate` is called, **Then** the existing nickname and avatar are preserved (not overwritten).

## Tasks / Subtasks

- [ ] Task 1: Enhance User Entity (AC: 1, 2, 4)
  - [ ] Add `nickname` field (`String`, nullable = false) to `User.java`
  - [ ] Add `@Version Long version` field to `User.java` (mandatory per write rules — optimistic locking for all mutable entities)
  - [ ] Ensure `avatar` field remains present (already exists)

- [ ] Task 2: Update UserService — Profile Initialization (AC: 1, 2, 4)
  - [ ] Update `findOrCreate()` in `UserService` to set `nickname = emailPrefix(email)` and `avatar = "placeholder"` on new user creation only
  - [ ] Add private `emailPrefix(String email)` helper method — extracts substring before `@`
  - [ ] Returning user path must NOT overwrite `nickname` or `avatar` (idempotency preserved)
  - [ ] Write unit tests in `UserServiceTest` for: nickname from simple email, nickname from email with dots/dashes, returning user preserves profile

- [ ] Task 3: Expose Profile API Endpoint (AC: 3)
  - [ ] Create `UserProfileResponse` record/DTO in `com.tictactore.controller` (fields: `UUID id`, `String email`, `String nickname`, `String avatar`, `String language`)
  - [ ] Create `UserController` in `com.tictactore.controller` with `GET /api/v1/users/me`
  - [ ] Extract current user UUID from JWT claims via `SecurityContextHolder.getContext().getAuthentication().getName()` → parse as `UUID`
  - [ ] Add `findById(UUID id)` method to `UserService` annotated `@Transactional(readOnly = true)`; throw `ResponseStatusException(HttpStatus.NOT_FOUND)` if absent
  - [ ] Secure endpoint: `ROLE_USER` required (inherits from existing `SecurityConfig` — no changes needed there)
  - [ ] Write integration test `UserControllerIT` verifying authenticated request returns 200 with correct profile fields

- [ ] Task 4: Frontend Profile Display (AC: 1, 2, 3)
  - [ ] Create `frontend/src/features/profile/api/profileApi.ts` — `GET /api/users/me` using the centralized API client in `frontend/src/core/api/`
  - [ ] Create `frontend/src/features/profile/stores/profileStore.ts` (Pinia, named `useProfileStore`) — state: `{ id, email, nickname, avatar, language }`, action: `fetchProfile()`
  - [ ] Update `HomeHub.vue` to call `profileStore.fetchProfile()` on mount and display nickname
  - [ ] Avatar: when `avatar === 'placeholder'` render default SVG fallback — do NOT add external image dependencies (Story 1.6 handles real avatars)
  - [ ] Write Vitest unit test for `profileStore` — mock `profileApi`, verify state populated correctly

- [ ] Task 5: Verify CI passes
  - [ ] Run `./scripts/ci-local.sh` — all tests green before marking done

## Dev Notes

### Critical Context: What Already Exists

**`User.java`** (`src/main/java/com/tictactore/model/User.java`):
- Current fields: `id` (UUID), `email`, `name`, `providerId`, `avatar`, `language`
- **GAP 1**: No `nickname` field — Google's `name` (display name e.g. "John Doe") is NOT the same as email-prefix nickname
- **GAP 2**: No `@Version` field — mandatory for all mutable `@Entity` per project write rules (1-write.md §2)
- `avatar` field already declared but never populated (left `null` on creation)

**`UserService.findOrCreate()`** (`src/main/java/com/tictactore/service/UserService.java`):
- Creates user with `email`, `name`, `providerId` only — `avatar` and `language` left `null`
- Race-condition guard via `catch (DataIntegrityViolationException)` — **must be preserved**
- This is the exact integration point for nickname/avatar initialization

**`CustomOAuth2SuccessHandler.java`** — calls `userService.findOrCreate(email, name, providerId)`. No changes needed here.

**Actual package**: `com.tictactore` — NOT `com.itemis.tictactore` (planning docs are wrong)
**Actual Spring Boot**: 3.4.5 — NOT 4.0 (planning docs are wrong)

### Architecture Patterns & Constraints

- **Strict Layering** (write rule §6): `UserController` must call `UserService`, NOT `UserRepository` directly
- **Optimistic Locking** (write rule §2): `@Version Long version` — use wrapper `Long`, NOT primitive `long`. No `@Column` annotation on it.
- **@Transactional** (write rule §3): `findOrCreate` already `@Transactional`. New `findById` must be `@Transactional(readOnly = true)`
- **Fail-Fast** (write rule §4): If user not found in `/api/v1/users/me`, throw immediately — `ResponseStatusException(HttpStatus.NOT_FOUND)`
- **Tell, Don't Ask** (write rule §5): Nickname generation logic belongs in service, not controller
- **API naming** (architecture): endpoint `GET /api/v1/users/me` — kebab-case, plural, versioned
- **JSON**: all `UserProfileResponse` fields in camelCase
- **500-line rule** (AD-IP-04): no source file exceeds 500 lines
- **DTO boundary**: controller returns `UserProfileResponse` record, never the `User` entity

### JWT / Security Integration

- JWT stores user id in claims (established in Story 1.1/1.1a). `JwtAuthenticationFilter` populates `SecurityContext` with UUID as principal name.
- In `UserController` extract authenticated user id: `SecurityContextHolder.getContext().getAuthentication().getName()` → `UUID.fromString(...)`.
- Do NOT add another DB lookup in the filter — performance issue was fixed in Story 1.1 review.

### Database / Persistence

- Dev environment: H2 with `ddl-auto: create` — adding fields to `User.java` is sufficient; no Flyway needed for dev.
- Check `src/main/resources/db/migration/` — if empty or absent, skip SQL migration file.
- If Flyway IS active: add `V3__add_nickname_to_users.sql`:
  ```sql
  ALTER TABLE users ADD COLUMN nickname VARCHAR(255) NOT NULL DEFAULT '';
  ALTER TABLE users ADD COLUMN version BIGINT;
  ```

### Nickname Generation

```java
private String emailPrefix(String email) {
    int atIndex = email.indexOf('@');
    if (atIndex < 0) throw new IllegalArgumentException("Invalid email: " + email);
    return email.substring(0, atIndex);
}
```

- Keep dots, dashes, underscores as-is — no sanitization
- `john.doe@company.com` → `john.doe`
- `test_user123@gmail.com` → `test_user123`

### Avatar Placeholder

- Store literal string `"placeholder"` in `avatar` column for new users
- Frontend: `avatar === 'placeholder'` → render inline SVG fallback (generic person silhouette)
- Story 1.6 (Avatar Selection & Management) will replace placeholder with real avatar URLs — do not anticipate that work here

### Frontend File Locations

```
frontend/src/
├── features/
│   └── profile/
│       ├── api/
│       │   └── profileApi.ts          ← NEW
│       └── stores/
│           └── profileStore.ts        ← NEW
├── core/
│   └── api/                           ← EXISTING — use this client, not raw fetch
└── views/
    └── HomeHub.vue                    ← MODIFY: fetchProfile on mount, display nickname
```

- Pinia store naming convention: `useProfileStore`
- Vue custom events: use `ch:` prefix (e.g. `ch:profile-loaded`)

### Testing Standards

- **`UserServiceTest`** (unit, Mockito): test nickname = email prefix on create; avatar = "placeholder" on create; returning user: nickname/avatar NOT overwritten; malformed email → `IllegalArgumentException`
- **`UserControllerIT`** (integration, `@SpringBootTest` + `@AutoConfigureMockMvc`): authenticated `GET /api/v1/users/me` → 200 with correct fields; unauthenticated → 401/403
- **Never test Lombok-generated code** (test rule §8) — test behavior, not getters/setters
- **Frontend Vitest**: `profileStore.test.ts` — mock `profileApi.getProfile()`, call `fetchProfile()`, assert state fields set

### Previous Story Intelligence (1.1 & 1.1a)

- Review finding from 1.1: "Missing avatar and language profile data in player record" — Story 1.3 resolves `avatar` (placeholder). `language` remains `null` until Story 1.4.
- JWT is now HttpOnly cookie (`TTT_TOKEN`) — no URL param, no localStorage
- `SecurityContext` principal name = user UUID string (set by `JwtAuthenticationFilter`)
- Race-condition guard in `findOrCreate` via `DataIntegrityViolationException` — preserve unchanged
- `AuthController` pattern used interface + impl (`AuthApi` + `AuthController`) — for single endpoint `UserController`, one class is sufficient

### Project Structure Notes

- Backend: `src/main/java/com/tictactore/controller/UserController.java` (new), `UserProfileResponse.java` (new record)
- Backend: `src/main/java/com/tictactore/service/UserService.java` (modify — add `findById` and `emailPrefix`)
- Backend: `src/main/java/com/tictactore/model/User.java` (modify — add `nickname`, `@Version`)
- Backend test: `src/test/java/com/tictactore/controller/UserControllerIT.java` (new)
- Frontend: `frontend/src/features/profile/api/profileApi.ts` (new)
- Frontend: `frontend/src/features/profile/stores/profileStore.ts` (new)
- Frontend: `frontend/src/views/HomeHub.vue` (modify)

### References

- [Source: _project-spec/rules/1-write.md#2. Optimistic Locking] — `@Version Long version`
- [Source: _project-spec/rules/1-write.md#3. @Transactional] — `readOnly=true` for reads
- [Source: _project-spec/rules/1-write.md#6. Strict Layering] — service owns reads/writes
- [Source: _project-spec/rules/2-test.md#1. Core Validity Contract] — behavior, not implementation
- [Source: _bmad-output/planning-artifacts/architecture.md#Naming Conventions] — REST `/api/v1/users/me`, camelCase JSON
- [Source: _bmad-output/planning-artifacts/architecture.md#Complete Project Directory Structure] — `features/profile/` location
- [Source: _bmad-output/planning-artifacts/epics.md#Story 1.3] — AC source
- [Source: _bmad-output/implementation-artifacts/1-1-project-initialization-and-authentication-via-google-oauth2.md#Completion Notes] — package `com.tictactore`, Spring Boot 3.4.5
- [Source: _bmad-output/implementation-artifacts/1-1-project-initialization-and-authentication-via-google-oauth2.md#Review Findings 2] — missing avatar flagged; JWT HttpOnly cookie pattern

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
