# Story 1.4: Profile Management in Personal Cabinet

## Story

As a player,
I want to change my nickname and language in the personal cabinet,
So that I can personalize my experience.

## Status

ready-for-dev

## Context

**Epic:** Epic 1 — Quick Start (Auth & Basic Profile)
**FRs covered:** FR31 (nickname change ≤1/month), FR32 (language EN/DE), FR34 (minimal data storage), FR56 (demo data toggle)

### Dependency Matrix

| Story | Status | What this story needs from it |
|-------|--------|-------------------------------|
| 1-1/1-1a/1-1b | **DONE** | `User.java`, `UserRepository`, `UserService`, `AuthController`, JWT cookie auth, `useAuthStore`, `HomeHub.vue` |
| **1-2** | backlog | `vue-i18n` setup, EN/DE message files. If not done: scaffold minimally in this story (see Task F3) |
| **1-3** | backlog | `nickname`, `lastNicknameChangedAt`, `locale` fields on `User` entity; profile created on first login. **Verify what 1-3 added before starting — avoid duplicating migrations or fields** |

> ⚠️ This story is created ahead of 1-2 and 1-3 for planning purposes. Implement only after 1-3 is merged. If 1-2 is not yet done, scaffold i18n minimally (Task F3).

### Current Codebase State (post 1-1/1-1a/1-1b)

**Backend:**
- Package root: `com.tictactore` (architecture doc says `com.itemis.tictactore` — use what exists)
- Entity: `User.java` in `com.tictactore.model` (NOT `domain/` — follow existing convention)
- `UserRepository`, `UserService` exist
- No Flyway migrations — `ddl-auto: update` in `application.yml`
- Controller pattern: `XxxController implements XxxApi` (see `AuthController implements AuthApi`)
- Exception handling: check if `GlobalApiExceptionHandler` exists; if not, create it

**Frontend:**
- Structure: flat `views/` + `components/` (no `features/` folder yet — this story introduces it)
- `stores/auth.ts` → `useAuthStore` with `isAuthenticated`, `logout()`
- CSRF pattern in `logout()`: read `XSRF-TOKEN` cookie → send as `X-XSRF-TOKEN` header
- Routes: only `/` and `/oauth2/redirect`

---

## Acceptance Criteria

1. **Given** authenticated player is on Home Hub
   **When** they tap/click their avatar in the header (UX-DR5)
   **Then** the Personal Cabinet page opens at route `/cabinet`

2. **Given** player is on Personal Cabinet (`/cabinet`)
   **When** the page loads
   **Then** current nickname, language setting, and avatar placeholder are displayed (avatar is read-only — story 1-6 handles upload)

3. **Given** player last changed nickname more than 30 days ago (or has never changed it)
   **When** they edit the nickname field and click Save
   **Then** nickname is updated in the database and reflected in the UI immediately; `lastNicknameChangedAt` is set to now

4. **Given** player last changed nickname within the past 30 days
   **When** they view the nickname field
   **Then** the field is disabled with explicit lockout banner: "Next change available on {formatted date}" — no vague message

5. **Given** player submits a nickname already taken by another player (race condition at save time)
   **When** the server returns a conflict
   **Then** an error message is shown inline; field remains editable

6. **Given** player is on Personal Cabinet
   **When** they switch language (EN ↔ DE)
   **Then** locale preference is persisted to backend AND the UI immediately re-renders in the new language without page reload (i18n hot-reload)

7. **Given** player clicks "Sign Out" on Personal Cabinet
   **When** the confirmation dialog is confirmed
   **Then** `POST /api/auth/logout` is called (reusing existing endpoint), session cleared, user redirected to `/`

8. **Given** unauthenticated user navigates directly to `/cabinet`
   **When** the route guard evaluates
   **Then** user is redirected to `/` (login)

---

## Tasks / Subtasks

### Backend

- [ ] **Task B1: Extend User entity for profile fields** (AC: 3, 4, 6)
  - [ ] B1.1: Check what fields story 1-3 added to `User.java`. Add any missing: `nickname VARCHAR(30) UNIQUE`, `lastNicknameChangedAt LocalDateTime`, `locale String` (default `"en"`), `showDemoData boolean` (default `false`)
  - [ ] B1.2: If Flyway migrations now exist (check `src/main/resources/db/migration/`): add `V{N}__add_profile_fields.sql`; otherwise verify fields appear via `ddl-auto: update`
  - [ ] B1.3: Verify `@Version private Long version` exists on `User` — add if missing (optimistic locking, mandatory per coding rules)
  - [ ] B1.4: Annotate `nickname` with `@Column(unique = true, length = 30)` and `@Size(min = 3, max = 30)`

- [ ] **Task B2: DTOs** (AC: 2, 3, 5, 6)
  - [ ] B2.1: Create `src/main/java/com/tictactore/controller/dto/ProfileResponse.java` (record): fields `id Long`, `nickname String`, `locale String`, `avatarUrl String` (nullable), `nextNicknameChangeAt LocalDateTime` (nullable — null means change is allowed), `showDemoData boolean`
  - [ ] B2.2: Create `UpdateProfileRequest.java` (record): `nickname String` (nullable, `@Size(min=3,max=30)`, `@Pattern(regexp="[A-Za-z0-9_-]+")`), `locale String` (nullable, `@Pattern(regexp="en|de")`), `showDemoData Boolean` (nullable)

- [ ] **Task B3: ProfileApi interface** (AC: 2, 3, 6)
  - [ ] B3.1: Create `src/main/java/com/tictactore/controller/ProfileApi.java`
  - [ ] B3.2: `GET /api/v1/profile` → `ResponseEntity<ProfileResponse>`
  - [ ] B3.3: `PATCH /api/v1/profile` → `ResponseEntity<ProfileResponse>` (body: `@Valid UpdateProfileRequest`)

- [ ] **Task B4: ProfileService** (AC: 3, 4, 5, 6)
  - [ ] B4.1: Create `src/main/java/com/tictactore/service/ProfileService.java`
  - [ ] B4.2: `@Transactional(readOnly=true) getProfile(String email): ProfileResponse`
  - [ ] B4.3: `@Transactional updateNickname(String email, String nickname): ProfileResponse`
    - If `lastNicknameChangedAt != null && lastNicknameChangedAt.plusDays(30).isAfter(LocalDateTime.now())` → throw `NicknameChangeCooldownException(nextEligibleAt)`
    - If `userRepository.existsByNickname(nickname) && !existing.getEmail().equals(email)` → throw `NicknameAlreadyTakenException`
    - Save via `User saved = userRepository.save(user)` — **use `saved`, discard original**
    - Set `lastNicknameChangedAt = LocalDateTime.now()` on `saved`
  - [ ] B4.4: `@Transactional updateLocale(String email, String locale): ProfileResponse`
    - Validate locale is `"en"` or `"de"` — throw `IllegalArgumentException` otherwise
    - Save and return
  - [ ] B4.5: `@Transactional updateDemoData(String email, boolean show): ProfileResponse`

- [ ] **Task B5: ProfileController** (AC: 2, 3, 5, 6)
  - [ ] B5.1: Create `src/main/java/com/tictactore/controller/ProfileController.java`
  - [ ] B5.2: Extract email from `Authentication`: `((OAuth2User) auth.getPrincipal()).getAttribute("email")`
  - [ ] B5.3: Implement `GET /api/v1/profile` and `PATCH /api/v1/profile` delegating to `ProfileService`
  - [ ] B5.4: In `GlobalApiExceptionHandler` (create if not exists): map `NicknameChangeCooldownException` → HTTP 409 `{ "errorCode": "NICKNAME_COOLDOWN", "nextEligibleAt": "..." }`, `NicknameAlreadyTakenException` → HTTP 409 `{ "errorCode": "NICKNAME_TAKEN" }`

- [ ] **Task B6: Integration Tests** (AC: 2–6, 8)
  - [ ] B6.1: Create `ProfileControllerIT` — suffix `IT`, `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`
  - [ ] B6.2: `GET /api/v1/profile` unauthenticated → 401
  - [ ] B6.3: `GET /api/v1/profile` authenticated → 200 with profile data
  - [ ] B6.4: `PATCH` with valid nickname → 200, nickname updated, `nextNicknameChangeAt` set ~30 days from now
  - [ ] B6.5: `PATCH` within 30-day cooldown → 409 with `errorCode: NICKNAME_COOLDOWN` and `nextEligibleAt`
  - [ ] B6.6: `PATCH` with duplicate nickname → 409 with `errorCode: NICKNAME_TAKEN`

### Frontend

- [ ] **Task F1: Profile API service** (AC: 2, 3, 6)
  - [ ] F1.1: Create `frontend/src/features/profile/services/profileService.ts`
  - [ ] F1.2: `getProfile(): Promise<ProfileResponse>` — `GET /api/v1/profile`
  - [ ] F1.3: `updateProfile(req: Partial<UpdateProfileRequest>): Promise<ProfileResponse>` — `PATCH /api/v1/profile` with CSRF header (reuse pattern from `stores/auth.ts`: `getCookie('XSRF-TOKEN')` → `X-XSRF-TOKEN` header)

- [ ] **Task F2: Profile Pinia store** (AC: 2, 3, 6)
  - [ ] F2.1: Create `frontend/src/features/profile/stores/profile.ts` (composition API style — match `useAuthStore` pattern from `stores/auth.ts`)
  - [ ] F2.2: State: `nickname ref<string>`, `locale ref<string>`, `avatarUrl ref<string|null>`, `nextNicknameChangeAt ref<string|null>`, `showDemoData ref<boolean>`
  - [ ] F2.3: Actions: `fetchProfile()`, `updateNickname(nickname: string)`, `updateLocale(locale: string)`, `updateDemoData(show: boolean)`
  - [ ] F2.4: After `updateLocale` success: set `i18n.global.locale.value = newLocale` (hot-reload, AC: 6)

- [ ] **Task F3: i18n scaffolding** (AC: 6) — skip if story 1-2 already done
  - [ ] F3.1: `npm install vue-i18n@9` (if not installed)
  - [ ] F3.2: Create `frontend/src/i18n/index.ts`: `createI18n({ legacy: false, locale: 'en', messages: { en: { ... }, de: { ... } } })`
  - [ ] F3.3: Minimum translation keys: `cabinet.title`, `cabinet.nickname.label`, `cabinet.nickname.lockout`, `cabinet.locale.label`, `cabinet.signout.label`, `cabinet.save`
  - [ ] F3.4: Register in `main.ts`: `app.use(i18n)` before `app.mount()`
  - [ ] F3.5: On app load: read `profileStore.locale` (after fetch) and apply to `i18n.global.locale.value`

- [ ] **Task F4: PersonalCabinetView** (AC: 2–7)
  - [ ] F4.1: Create `frontend/src/features/profile/views/PersonalCabinetView.vue`
  - [ ] F4.2: `onMounted`: call `profileStore.fetchProfile()`
  - [ ] F4.3: Nickname section: text input; computed `isNicknameLocked = nextNicknameChangeAt && new Date(nextNicknameChangeAt) > new Date()`; when locked: disable input and show banner with exact date (e.g., "Next change available on 14 Jun 2026"); when unlocked: show Save button
  - [ ] F4.4: Language section: EN / DE button toggle; on change call `profileStore.updateLocale()`
  - [ ] F4.5: Avatar area: circular placeholder showing first letter of nickname (read-only; "change avatar" note deferred to story 1-6)
  - [ ] F4.6: Sign Out button: show `SignOutConfirmDialog`; on confirm: `authStore.logout()` then `router.push('/')`
  - [ ] F4.7: Demo Data toggle: checkbox/switch bound to `showDemoData`; call `profileStore.updateDemoData()` on change
  - [ ] F4.8: Styling — "The Clubhouse Editorial" theme (UX-DR7): warm dark tones, Space Grotesk headings, Manrope body; mobile-first, content card max-width 480px on desktop (UX-DR8); SCSS classes prefixed `ch-`

- [ ] **Task F5: SignOutConfirmDialog component** (AC: 7)
  - [ ] F5.1: Create `frontend/src/features/profile/components/SignOutConfirmDialog.vue`
  - [ ] F5.2: Modal with "Sign Out" confirm and "Cancel" buttons
  - [ ] F5.3: Emits `@confirm` and `@cancel`

- [ ] **Task F6: AvatarHeaderButton component** (AC: 1)
  - [ ] F6.1: Create `frontend/src/features/profile/components/AvatarHeaderButton.vue`
  - [ ] F6.2: Circular button: shows avatar image or initials fallback (first letter of nickname)
  - [ ] F6.3: `@click` → `router.push('/cabinet')`
  - [ ] F6.4: Only render when `authStore.isAuthenticated`

- [ ] **Task F7: Update HomeHub.vue** (AC: 1)
  - [ ] F7.1: Add header section with `AvatarHeaderButton` positioned top-right (UX-DR5: avatar in hub header → cabinet)
  - [ ] F7.2: **Preserve** existing auth-conditional content (Google login button, welcome message)
  - [ ] F7.3: Remove the inline "Sign Out" button from HomeHub ONLY after `/cabinet` route is confirmed working end-to-end

- [ ] **Task F8: Router — new route + auth guard** (AC: 1, 8)
  - [ ] F8.1: Add `/cabinet` route in `router/index.ts`: lazy-loaded `() => import('@/features/profile/views/PersonalCabinetView.vue')`, `meta: { requiresAuth: true }`
  - [ ] F8.2: Add `router.beforeEach` guard: if `to.meta.requiresAuth && !authStore.isAuthenticated` → redirect to `/`
  - [ ] F8.3: Verify `/oauth2/redirect` still works — it must NOT have `requiresAuth`

- [ ] **Task F9: Frontend unit tests** (AC: 3, 4, 8)
  - [ ] F9.1: Create `frontend/src/features/profile/views/__tests__/PersonalCabinetView.spec.ts` (Vitest)
  - [ ] F9.2: Nickname field disabled when `nextNicknameChangeAt` is future date; lockout banner visible with correct date
  - [ ] F9.3: Nickname field enabled when `nextNicknameChangeAt` is null
  - [ ] F9.4: Locale change calls `profileStore.updateLocale`
  - [ ] F9.5: Router guard redirects unauthenticated user to `/`

---

## Dev Notes

### Backend Architecture Compliance (MANDATORY)

Follow strict layering — no exceptions:

```
Controller (controller/) → Service (service/) → Repository (repository/)
                            ↑ only place for business logic
```

- Controller NEVER receives `User` entity — always `ProfileResponse` DTO
- All reads and writes go through `ProfileService` — never call `UserRepository` directly from controller
- `@Transactional` only on service methods, never on controller methods

**Correct JPA save pattern (mandatory):**
```java
// ALWAYS use the returned instance — discard original
User saved = userRepository.save(user);
// NEVER reference `user` after this line — use `saved` only
```

**Optimistic locking — verify User entity has:**
```java
@Version
private Long version; // wrapper Long — no explicit @Column
```

### Extracting Authenticated User in Controller

```java
@GetMapping
public ResponseEntity<ProfileResponse> getProfile(Authentication auth) {
    String email = ((OAuth2User) auth.getPrincipal()).getAttribute("email");
    return ResponseEntity.ok(profileService.getProfile(email));
}
```

All `/api/v1/**` endpoints are already protected by `JwtAuthenticationFilter` in `SecurityConfig` — no additional security config needed for this story.

### Nickname 30-Day Cooldown Logic

```java
// In ProfileService
private boolean canChangeNickname(User user) {
    if (user.getLastNicknameChangedAt() == null) return true;
    return user.getLastNicknameChangedAt().plusDays(30).isBefore(LocalDateTime.now());
}
```

HTTP 409 responses:
- Cooldown: `{ "errorCode": "NICKNAME_COOLDOWN", "nextEligibleAt": "2026-06-14T10:30:00" }`
- Duplicate: `{ "errorCode": "NICKNAME_TAKEN" }`

### Frontend Directory Structure (introduces `features/` pattern)

```
frontend/src/
├── features/
│   └── profile/                   ← first features/ module in the project
│       ├── components/
│       │   ├── AvatarHeaderButton.vue
│       │   └── SignOutConfirmDialog.vue
│       ├── services/
│       │   └── profileService.ts
│       ├── stores/
│       │   └── profile.ts
│       └── views/
│           ├── PersonalCabinetView.vue
│           └── __tests__/
│               └── PersonalCabinetView.spec.ts
├── i18n/
│   └── index.ts                   ← new (if story 1-2 not yet done)
```

### CSRF Pattern (reuse existing — do NOT reinvent)

```typescript
import { getCookie } from '@/utils/cookieUtils'  // already exists

const csrfToken = getCookie('XSRF-TOKEN')
const headers: HeadersInit = {
  'Content-Type': 'application/json',
  ...(csrfToken ? { 'X-XSRF-TOKEN': decodeURIComponent(csrfToken) } : {})
}
```

### i18n Hot-Reload on Locale Switch

```typescript
// In useProfileStore after successful updateLocale()
import { useI18n } from 'vue-i18n'
const { locale } = useI18n()
locale.value = newLocale  // triggers immediate re-render, no page reload
```

### UX Requirements (Non-Negotiable)

- **UX-DR5:** Avatar in HomeHub header ONLY routes to `/cabinet`. Avatar elsewhere → Quick Stats Popover (out of scope here)
- **UX-DR6:** Fonts: Space Grotesk for display/headings, Manrope for body — verify in global CSS
- **UX-DR7:** "The Clubhouse Editorial" — warm dark backgrounds, no pure white text on dark surfaces
- **UX-DR8:** Mobile-first; content cards max-width 480px on lg+ screens
- Lockout banner shows **exact date** — never a vague "you must wait" message

### What Is NOT In Scope for This Story

- Avatar upload/change → Story 1-6
- Account deletion → Story 1-5
- Quick Stats Popover on avatar tap outside HomeHub → later
- Notification preferences → not Epic 1
- Auto-clearing demo data at 50 matches → Epic 4 (just store the `showDemoData` boolean here)

### Regression Checklist

- [ ] `HomeHub.vue` modification: ADD avatar header, preserve existing auth logic — do not break Google login
- [ ] Remove inline Sign Out button from `HomeHub.vue` ONLY after `/cabinet` end-to-end confirmed working
- [ ] `/oauth2/redirect` route must NOT get `requiresAuth: true` meta
- [ ] `useAuthStore.logout()` stays unchanged — `SignOutConfirmDialog` calls it, does not duplicate it
- [ ] `statisticsService.ts` (existing) must not be touched

---

## Dev Agent Record

### Agent Model Used

_to be filled by dev agent_

### Debug Log References

_none_

### Completion Notes List

_to be filled_

### File List

_to be filled_
