# Story 1.5: Account Deletion with Anonymization

**Status:** ready-for-dev
**Epic:** 1 — Quick Start (Auth & Basic Profile)
**Story:** 5

---

## Story

As a user, I want to be able to delete my account, so that my personal data is removed while preserving match history.

---

## Acceptance Criteria

**AC-1 — Happy path deletion:**
- **Given** "Delete Account" is clicked inside the personal cabinet
- **When** the user confirms the destructive action in the confirmation dialog
- **Then** the user's `email` and `name` are replaced with `ex-player-{userId}` placeholders (AD-04)
- **And** `providerId`, `avatar`, and `language` are set to `null`
- **And** the current JWT token is immediately added to the Redis denylist (AD-03)
- **And** both auth cookies (`TTT_AUTH`, `TTT_SESSION`) are cleared in the response
- **And** the server responds with `204 No Content`
- **And** the frontend clears auth state and redirects to `/` (HomeHub, unauthenticated)

**AC-2 — Token revocation enforced:**
- **Given** a user's JWT token that was valid before account deletion
- **When** that token is used to call any authenticated API endpoint after deletion
- **Then** the server responds with `401 Unauthorized`

**AC-3 — Match history preserved:**
- **Given** a user has match records in the `matches` table
- **When** their account is deleted
- **Then** all match rows remain intact with the user's `UUID` still referenced in FK columns
- **And** no match records are deleted or modified

**AC-4 — Unauthenticated request blocked:**
- **Given** no auth cookie is present
- **When** `DELETE /api/v1/users/me` is called
- **Then** the server responds with `401 Unauthorized`

**AC-5 — CSRF protection:**
- **Given** a request to `DELETE /api/v1/users/me` is missing the `X-XSRF-TOKEN` header
- **When** the request is processed
- **Then** the server responds with `403 Forbidden`

---

## Tasks / Subtasks

### Backend

- [ ] **Task 1: Create `AccountApi` interface**
  - File: `src/main/java/com/tictactore/controller/AccountApi.java` (NEW)
  - OpenAPI `@Tag(name = "Account")` + `@Operation` + `@ApiResponses` (follow `AuthApi` pattern)
  - Declare: `ResponseEntity<Void> deleteAccount(HttpServletRequest request, HttpServletResponse response)`
  - `@ApiResponse(responseCode = "204")`, `@ApiResponse(responseCode = "401")`, `@ApiResponse(responseCode = "403")`

- [ ] **Task 2: Create `AccountController`**
  - File: `src/main/java/com/tictactore/controller/AccountController.java` (NEW)
  - `@Slf4j @RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor`
  - Implements `AccountApi`
  - Inject: `AccountService`, `JwtService`, `TokenRevocationService`
  - `@DeleteMapping("/me")` handler:
    1. Extract authenticated `User` from `SecurityContextHolder`
    2. Call `accountService.deleteAccount(user.getId())`
    3. Revoke token (guard pattern from `AuthController.logout()` — see Dev Notes)
    4. Clear both cookies (exact same `ResponseCookie` pattern as `AuthController.logout()`)
    5. Return `ResponseEntity.noContent().build()`
  - All String literals as `private static final String` constants

- [ ] **Task 3: Create `AccountService`**
  - File: `src/main/java/com/tictactore/service/AccountService.java` (NEW)
  - `@Service @RequiredArgsConstructor`
  - Inject: `UserRepository`
  - Method: `@Transactional public void deleteAccount(UUID userId)`
    - Load user via `userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(...))`
    - **Idempotency guard**: if `user.getEmail().startsWith("ex-player-")` → return immediately (already anonymized)
    - Set: `email = "ex-player-" + userId + "@deleted.local"`
    - Set: `name = "ex-player-" + userId`
    - Set: `providerId = null`, `avatar = null`, `language = null`
    - Save: `userRepository.save(user)`

- [ ] **Task 4: Write `AccountControllerTest`**
  - File: `src/test/java/com/tictactore/controller/AccountControllerTest.java` (NEW)
  - `@WebMvcTest(AccountController.class) @Import({SecurityConfig.class, JwtAuthenticationFilter.class})`
  - `@MockBean`: `AccountService`, `JwtService`, `TokenRevocationService`, `CustomOAuth2SuccessHandler`
  - Tests (each must FAIL when production code is broken):
    - `deleteAccount_withoutAuth_returns401()`
    - `deleteAccount_withoutCsrf_returns403()`
    - `deleteAccount_authenticated_withCsrf_returns204_andClearsCookies()` — verify `Set-Cookie` headers clear both cookies, `accountService.deleteAccount()` called once, `tokenRevocationService.revoke()` called once

- [ ] **Task 5: Write `AccountServiceTest`**
  - File: `src/test/java/com/tictactore/service/AccountServiceTest.java` (NEW)
  - `@ExtendWith(MockitoExtension.class)`
  - `@Mock UserRepository`
  - Tests:
    - `deleteAccount_anonymizesEmailAndName()` — verify all five fields (email, name, providerId, avatar, language) after `save()`
    - `deleteAccount_isIdempotent_whenAlreadyAnonymized()` — email starts with `"ex-player-"` → verify `save()` is NOT called
    - `deleteAccount_throwsEntityNotFound_whenUserMissing()`

### Frontend

- [ ] **Task 6: Create `PersonalCabinetView.vue`**
  - File: `frontend/src/views/PersonalCabinetView.vue` (NEW)
  - Minimal cabinet scaffold with:
    - Page title: "My Cabinet"
    - Placeholder section for profile info (story 1-4 fills this in — leave a `<!-- Profile info: added in story 1-4 -->` comment)
    - "Danger Zone" section: red-bordered box, warning text, "Delete Account" red button
    - On button click: set `showDeleteModal = true`
  - Composition API (`<script setup lang="ts">`)
  - Import and render `<DeleteAccountModal v-model="showDeleteModal" />`

- [ ] **Task 7: Create `DeleteAccountModal.vue`**
  - File: `frontend/src/components/DeleteAccountModal.vue` (NEW)
  - Props: `modelValue: boolean` (v-model for show/hide)
  - Emits: `update:modelValue`
  - Content: warning heading + explanation text ("This cannot be undone. Your name and email will be permanently removed.")
  - Buttons: "Cancel" (secondary) + "Delete My Account" (red, destructive)
  - States: idle / loading (disable both buttons + spinner) / error (inline error message, re-enable buttons)
  - On confirm: `accountService.deleteAccount()` → `authStore.clearToken()` → `router.push('/')`
  - On cancel / close: `emit('update:modelValue', false)`

- [ ] **Task 8: Create `accountService.ts`**
  - File: `frontend/src/services/accountService.ts` (NEW)
  - Named export: `async function deleteAccount(): Promise<void>`
  - `DELETE /api/v1/users/me` with CSRF header pattern (see Dev Notes)
  - Throws `Error` with status code on non-2xx response
  - No default export (consistent with `statisticsService.ts`)

- [ ] **Task 9: Update `router/index.ts`**
  - File: `frontend/src/router/index.ts` (UPDATE)
  - Add route: `{ path: '/cabinet', name: 'cabinet', component: PersonalCabinetView, meta: { requiresAuth: true } }`
  - Add global `beforeEach` guard: if `to.meta.requiresAuth && !authStore.isAuthenticated` → `return { name: 'home' }`

- [ ] **Task 10: Update `HomeHub.vue`**
  - File: `frontend/src/views/HomeHub.vue` (UPDATE)
  - In authenticated section: add `<router-link to="/cabinet">` button alongside the existing Sign Out button

- [ ] **Task 11: Write `accountService.spec.ts`**
  - File: `frontend/src/services/__tests__/accountService.spec.ts` (NEW)
  - Vitest + `vi.stubGlobal('fetch', mockFetch)`
  - Tests:
    - `deleteAccount_sendsDELETE_withCsrfHeader()` — verify `method: 'DELETE'` and `X-XSRF-TOKEN` header present
    - `deleteAccount_throwsOnErrorResponse()` — stub 500, expect thrown `Error`

---

## Dev Notes

### Dependency Context
Stories 1-2 (Localization), 1-3 (Profile Generation), and 1-4 (Profile Management) are still in `backlog`. This story creates a minimal `PersonalCabinetView.vue` scaffold. Story 1-4 will extend it — **do NOT add profile editing logic here**. The cabinet view is intentionally thin: only page shell + danger zone.

### Backend: Anonymization Strategy (AD-04)
GDPR pseudonymization — the `users` row is **never deleted**. All FK columns in `matches` (`creator_id`, `team_a_attacker_id`, `team_a_defender_id`, `team_b_attacker_id`, `team_b_defender_id`) reference `users.id`. Physical deletion violates FK constraints and destroys match history.

**Placeholder format (deterministic — no schema change needed):**
```
email      → "ex-player-" + userId.toString() + "@deleted.local"
name       → "ex-player-" + userId.toString()
providerId → null
avatar     → null
language   → null
```

`email` has `UNIQUE NOT NULL` constraint. Using the user's own UUID guarantees natural uniqueness. `@deleted.local` is an intentionally invalid domain preventing re-registration.

**No Flyway migration required** — schema unchanged.

### Backend: Token Revocation (AD-03)
`TokenRevocationService` and `RedisTokenRevocationService` already exist and are fully functional. Call `revoke(token)` from the **controller** (not the service) — token revocation is a security concern, not a business concern. Revocation failure must NOT abort the deletion.

```java
// In AccountController — AFTER accountService.deleteAccount() returns successfully
var token = jwtService.extractToken(request);
if (token != null) {
    try {
        tokenRevocationService.revoke(token);
    } catch (Exception e) {
        log.warn("Token revocation failed during account deletion — session may persist until expiry", e);
    }
}
```

### Backend: Cookie Clearing
Exact same pattern as `AuthController.logout()`. Constants are in `CustomOAuth2SuccessHandler`:

```java
var authCookie = ResponseCookie.from(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, "")
    .httpOnly(true).secure(request.isSecure())
    .path("/").maxAge(0).sameSite("Lax").build();

var sessionCookie = ResponseCookie.from(CustomOAuth2SuccessHandler.SESSION_COOKIE_NAME, "")
    .httpOnly(false).secure(request.isSecure())
    .path("/").maxAge(0).sameSite("Lax").build();

response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());
return ResponseEntity.noContent().build();
```

### Backend: Extracting Authenticated User in Controller
```java
var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
// user.getId() → UUID used to call accountService.deleteAccount(user.getId())
```

The `User` in the security context is a stub built by `JwtAuthenticationFilter` with only `id`, `email`, `name` populated. `AccountService` MUST reload from DB by ID before mutating — do NOT pass the stub to `save()`.

### Backend: EntityNotFoundException Mapping
`GlobalApiExceptionHandler` maps `EntityNotFoundException` → `404`. Throw it from `AccountService`:
```java
userRepository.findById(userId)
    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
```
Controller must NOT null-check — let the exception handler do its job (per coding convention Rule 11).

### Frontend: CSRF Pattern (copy from `authStore.logout()`)
```typescript
import { getCookie } from '@/utils/cookieUtils'

const CSRF_COOKIE  = 'XSRF-TOKEN'
const CSRF_HEADER  = 'X-XSRF-TOKEN'
const ENDPOINT     = '/api/v1/users/me'

export async function deleteAccount(): Promise<void> {
  const csrfToken = getCookie(CSRF_COOKIE)
  const headers: HeadersInit = {}
  if (csrfToken) {
    headers[CSRF_HEADER] = decodeURIComponent(csrfToken)
  }
  const response = await fetch(ENDPOINT, { method: 'DELETE', headers })
  if (!response.ok) {
    throw new Error(`Account deletion failed: ${response.status}`)
  }
}
```

### Frontend: Post-Deletion Flow
```
accountService.deleteAccount()   ← 204 No Content (server clears cookies in response)
  → authStore.clearToken()       ← sets isMaybeAuthenticated = false
  → router.push('/')             ← HomeHub renders unauthenticated state
```
The server clears cookies in the 204 response. Frontend does NOT need to touch cookies manually.

### Frontend: Styling
- Tailwind CSS v4. Custom SCSS prefix: `ch-`
- "Delete Account" trigger: red button (`bg-red-500 hover:bg-red-600 text-white`) inside a danger zone box (`border border-red-300 rounded-lg p-4`)
- Modal: fixed backdrop (`fixed inset-0 bg-black/50`), centered card, warning icon + text
- Loading state: spinner icon + disabled buttons (prevent double-submit)
- Error state: red inline error message below buttons
- All touch targets ≥ 44px (mobile-first)

### File Summary
```
NEW — backend:
  src/main/java/com/tictactore/controller/AccountApi.java
  src/main/java/com/tictactore/controller/AccountController.java
  src/main/java/com/tictactore/service/AccountService.java

NEW — backend tests:
  src/test/java/com/tictactore/controller/AccountControllerTest.java
  src/test/java/com/tictactore/service/AccountServiceTest.java

NEW — frontend:
  frontend/src/views/PersonalCabinetView.vue
  frontend/src/components/DeleteAccountModal.vue
  frontend/src/services/accountService.ts
  frontend/src/services/__tests__/accountService.spec.ts

UPDATED — frontend:
  frontend/src/router/index.ts     (add /cabinet route + beforeEach guard)
  frontend/src/views/HomeHub.vue   (add "My Cabinet" link for authenticated users)
```

### Coding Conventions (`_project-spec/rules/1-write.md`)
- All code text (identifiers, constants, comments) in **English**
- String literals as `private static final String` — no inline magic strings
- Controller: `@Valid` on inputs, delegate to Service, NO repo calls, NO null-checks on service returns
- `@Transactional` on service methods that write to DB
- No source file or test class exceeds **500 lines** — IP-04 enforced by Checkstyle + ESLint at build time
- `GlobalApiExceptionHandler` handles all domain exceptions — controller must not catch or rethrow them

### Testing Conventions (`_project-spec/rules/2-test.md`)
- Unit tests: `@ExtendWith(MockitoExtension.class)`, class suffix `Test`
- Integration tests: class suffix `IT` or `IntegrationTest` (Gradle task separation)
- Every assertion must **fail when production code is broken** — `assertNotNull` alone is insufficient
- Never test Lombok-generated code (`@Getter`, `@Setter`, `@Builder`, etc.)
- AAA structure: blank line separating Arrange / Act / Assert
- Unit test: mock only external dependencies (repos, other services). Never mock the class under test.

### Known Risks
1. **Double-delete race condition**: Two simultaneous DELETE requests for the same user — the second hits the idempotency guard (`email.startsWith("ex-player-")`) and exits cleanly. No `@Version` needed.
2. **Bloom filter false positive**: `RedisTokenRevocationService.isRevoked()` already does a definitive `KEY_PREFIX + token` Redis bucket check after a Bloom hit — no security gap.
3. **Detached User stub in security context**: `JwtAuthenticationFilter` builds `User` with only `id`/`email`/`name`. `AccountService` MUST call `userRepository.findById()` — do NOT mutate the stub and save it.

---

## Dev Agent Record

### Agent Model Used
_[to be filled by dev agent]_

### Debug Log References
_[to be filled by dev agent]_

### Completion Notes List
_[to be filled by dev agent]_

### File List
_[to be filled by dev agent]_
