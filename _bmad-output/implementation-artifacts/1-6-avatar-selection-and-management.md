# Story 1.6: Avatar Selection & Management

**Status:** ready-for-dev
**Epic:** 1 – Quick Start (Auth & Basic Profile)

---

## Story

As a player, I want to select an avatar from a preset collection, so that I can personalize my profile identity across all app views.

---

## Acceptance Criteria

- **Given** the authenticated player is on Home Hub
- **When** they tap the `AvatarHeaderButton` in the header (UX-DR5)
- **Then** they are navigated to the Personal Cabinet at `/cabinet`
- **And** the `AvatarPicker` component displays a 24-item preset emoji/illustration grid
- **And** the currently active avatar is highlighted in the grid

- **Given** the player selects a different preset in the `AvatarPicker`
- **When** they tap "Save"
- **Then** `PATCH /api/v1/users/me/avatar` is called with `{ "avatarId": "preset-01" }` (kebab-case ID)
- **And** the avatar is persisted in the `users.avatar` column in the database
- **And** the `auth` Pinia store `user.avatar` is updated immediately (optimistic UI, NFR7 <50ms)
- **And** the `AvatarHeaderButton` in the header reflects the new avatar without page reload (FR30)

- **Given** a newly registered player with `user.avatar = null`
- **When** they open the `AvatarPicker`
- **Then** `preset-01` is shown as the default selected preset

- **Given** an unauthenticated request to `PATCH /api/v1/users/me/avatar`
- **When** no valid JWT cookie is present
- **Then** the endpoint returns `401 Unauthorized`

- **Given** the player sends an invalid preset ID (not in the 24 predefined set)
- **When** `PATCH /api/v1/users/me/avatar` is called
- **Then** the backend returns `400 Bad Request`

---

## Tasks / Subtasks

### Backend

- [ ] Task 1: Create `ProfileApi` interface + `ProfileController`
  - [ ] 1.1 Create `src/main/java/com/tictactore/controller/ProfileApi.java` — OpenAPI-annotated interface following the pattern of `AuthApi.java`
  - [ ] 1.2 Create `src/main/java/com/tictactore/controller/ProfileController.java` implementing `ProfileApi`
  - [ ] 1.3 Implement `PATCH /api/v1/users/me/avatar` — requires JWT auth, validates preset, calls `UserService.updateAvatar()`
  - [ ] 1.4 Implement `GET /api/v1/users/me` — returns `UserProfileResponse` for the authenticated user

- [ ] Task 2: Create `AvatarPreset` enum
  - [ ] 2.1 Create `src/main/java/com/tictactore/model/AvatarPreset.java` — 24 constants (`PRESET_01`…`PRESET_24`) each with `getId()` returning kebab-case string (`"preset-01"`…`"preset-24"`)
  - [ ] 2.2 Add static `isValid(String id)` helper for validation

- [ ] Task 3: Add DTOs
  - [ ] 3.1 Create `src/main/java/com/tictactore/dto/UpdateAvatarRequest.java` — record with `@NotBlank String avatarId`
  - [ ] 3.2 Create `src/main/java/com/tictactore/dto/UserProfileResponse.java` — record: `UUID id, String name, String email, String avatar, String language`

- [ ] Task 4: Extend `UserService`
  - [ ] 4.1 Add `updateAvatar(UUID userId, String avatarId)` — validates `AvatarPreset.isValid(avatarId)`, persists, returns updated `User`
  - [ ] 4.2 Add `findByEmail(String email)` returning `User` (needed by controller to resolve current user from JWT principal)
  - [ ] 4.3 Add `getProfile(UUID userId)` returning `UserProfileResponse`

- [ ] Task 5: Update `User.java`
  - [ ] 5.1 Add `@Column(name = "avatar")` annotation to the existing `avatar` field

### Frontend

- [ ] Task 6: Update `auth.ts` Pinia store
  - [ ] 6.1 Add `avatar: string | null` to the `User` interface
  - [ ] 6.2 Add `updateAvatar(avatarId: string)` action — store old value, update `user.avatar` optimistically, call `profileService.updateAvatar()`, revert on error

- [ ] Task 7: Create `profileService.ts`
  - [ ] 7.1 `updateAvatar(avatarId: string): Promise<UserProfileResponse>` — `PATCH /api/v1/users/me/avatar` with `X-XSRF-TOKEN` header from `XSRF-TOKEN` cookie
  - [ ] 7.2 `getProfile(): Promise<UserProfileResponse>` — `GET /api/v1/users/me`

- [ ] Task 8: Create `AvatarPicker.vue`
  - [ ] 8.1 Renders 24 preset items in a responsive grid (Tailwind CSS v4 utilities)
  - [ ] 8.2 Highlights the currently selected preset (use `ch-avatar-picker__item--selected` SCSS class with `ch-` prefix)
  - [ ] 8.3 v-model compatible: emits `update:modelValue` with `avatarId: string` on selection
  - [ ] 8.4 Props: `modelValue: string` (currently selected preset ID)

- [ ] Task 9: Create `AvatarHeaderButton.vue`
  - [ ] 9.1 Displays current user avatar from `authStore.user?.avatar ?? 'preset-01'`
  - [ ] 9.2 On click: `router.push('/cabinet')`
  - [ ] 9.3 Add `<!-- TODO: UX-DR5 Quick Stats Popover — deferred to future story -->` comment

- [ ] Task 10: Create `PersonalCabinetView.vue` (minimal shell for avatar only)
  - [ ] 10.1 Layout: back navigation, avatar section with `AvatarPicker`, Save button
  - [ ] 10.2 On mount: initialize selection from `authStore.user?.avatar ?? 'preset-01'`
  - [ ] 10.3 Save button: calls `authStore.updateAvatar(selectedId)`, shows loading state, shows error message on failure
  - [ ] 10.4 Add placeholder comment sections: `<!-- TODO: Story 1.2 — Language Switcher -->`, `<!-- TODO: Story 1.4 — Nickname Edit -->`, `<!-- TODO: Story 1.5 — Delete Account -->`

- [ ] Task 11: Update `router/index.ts`
  - [ ] 11.1 Add `/cabinet` route pointing to `PersonalCabinetView`
  - [ ] 11.2 Add `beforeEnter` guard: redirect unauthenticated users to `/`

- [ ] Task 12: Update `HomeHub.vue`
  - [ ] 12.1 Import and render `AvatarHeaderButton` in the authenticated user section header

### Tests

- [ ] Task 13: Extend `UserServiceTest.java` with `updateAvatar` tests
  - [ ] 13.1 Valid preset ID → persists avatar and returns updated `User`
  - [ ] 13.2 Invalid preset ID → throws `IllegalArgumentException`

- [ ] Task 14: Create `ProfileControllerIT.java` (integration test, name MUST end with `IT`)
  - [ ] 14.1 `PATCH /api/v1/users/me/avatar` with valid JWT + valid preset → `200 OK`, `users.avatar` updated in DB
  - [ ] 14.2 `PATCH /api/v1/users/me/avatar` without JWT → `401 Unauthorized`
  - [ ] 14.3 `PATCH /api/v1/users/me/avatar` with invalid preset ID → `400 Bad Request`
  - [ ] 14.4 `GET /api/v1/users/me` with valid JWT → `200 OK` with correct profile fields

- [ ] Task 15: Create `AvatarPicker.spec.ts` (Vitest component test)
  - [ ] 15.1 Renders exactly 24 preset items
  - [ ] 15.2 Item matching `modelValue` prop has selected CSS class
  - [ ] 15.3 Click on an item emits `update:modelValue` with correct preset ID

---

## Dev Notes

### CRITICAL: Read This Before Writing Any Code

**1. `User.avatar` already exists.**
`User.java` already has `private String avatar;` and `private String language;`. Do NOT add these fields again. Only add the `@Column(name = "avatar")` annotation:
```java
@Column(name = "avatar")
private String avatar;
```

**2. Controller pattern: interface + implementation.**
Follow the exact pattern of `AuthApi.java` + `AuthController.java`. Every controller has an API interface with `@Tag`, `@Operation`, `@ApiResponses` and a separate `@RestController` class implementing it. The interface lives in `com.tictactore.controller`.

**3. How to get the authenticated user in a controller.**
JWT is delivered via the `TTT_TOKEN` httpOnly cookie. `JwtAuthenticationFilter` validates it and sets `UsernamePasswordAuthenticationToken` in `SecurityContextHolder` with the user's email as the principal name. Inject `Authentication authentication` as a controller method parameter — Spring auto-populates it:
```java
@PatchMapping("/users/me/avatar")
public ResponseEntity<UserProfileResponse> updateAvatar(
        @RequestBody @Valid UpdateAvatarRequest request,
        Authentication authentication) {
    String email = authentication.getName(); // user's email
    User user = userService.findByEmail(email);
    // ...
}
```
Do NOT call `SecurityContextHolder.getContext()` statically. Do NOT access `UserRepository` from the controller.

**4. CSRF for PATCH requests.**
The project uses `CookieCsrfTokenRepository`. All state-changing requests from the Vue SPA (`PATCH`, `POST`, `DELETE`) must include the `X-XSRF-TOKEN` header. Read it from the `XSRF-TOKEN` cookie in `profileService.ts`:
```ts
const csrfToken = document.cookie.match(/XSRF-TOKEN=([^;]+)/)?.[1] ?? ''
// Pass as header: 'X-XSRF-TOKEN': decodeURIComponent(csrfToken)
```

**5. DTOs go in a new `dto` package.**
No `dto` package exists yet. Create it: `src/main/java/com/tictactore/dto/`. Use Java records.

**6. Avatar is a preset ID, not a file path.**
UX Flows 4 and 5 both specify "24 preset emoji/illustration grid". Store as kebab-case string: `"preset-01"` through `"preset-24"`. There is NO file upload, NO multipart form data, NO S3/storage service. The epic text mentioning "upload" is superseded by the UX specification.

**7. `user.avatar` is null for all existing users.**
`UserService.findOrCreate()` does not set a default avatar. Frontend must handle null: display `preset-01` visually, but do NOT call the backend until user explicitly saves.

**8. Optimistic UI (NFR7 <50ms).**
In `auth.ts` `updateAvatar` action:
```ts
async updateAvatar(avatarId: string) {
  const previous = this.user?.avatar ?? null
  if (this.user) this.user.avatar = avatarId  // optimistic
  try {
    await profileService.updateAvatar(avatarId)
  } catch {
    if (this.user) this.user.avatar = previous  // revert
    throw
  }
}
```

**9. `PersonalCabinetView` is a minimal shell.**
Stories 1.2, 1.3, 1.4, 1.5 are all still in backlog and will extend this component. Only implement avatar section. Add clearly marked `<!-- TODO: Story X.Y — ... -->` placeholder comments so future dev agents know where to insert.

**10. `SecurityConfig` does NOT need modification.**
`anyRequest().authenticated()` already covers all `/api/v1/**` endpoints. The new `PATCH /api/v1/users/me/avatar` and `GET /api/v1/users/me` endpoints are protected automatically.

**11. `AvatarPreset` enum validation.**
Backend must reject unknown preset IDs. In `AvatarPreset.java`:
```java
public static boolean isValid(String id) {
    return Arrays.stream(values()).anyMatch(p -> p.getId().equals(id));
}
```
In `UserService.updateAvatar()`: throw `IllegalArgumentException` if `!AvatarPreset.isValid(avatarId)`. The controller handles this as `400 Bad Request` via `@ExceptionHandler` or `@ResponseStatus`.

### Project Structure Notes

**Backend — new/modified files:**
```
src/main/java/com/tictactore/
├── controller/
│   ├── ProfileApi.java           NEW
│   └── ProfileController.java    NEW
├── dto/
│   ├── UpdateAvatarRequest.java  NEW
│   └── UserProfileResponse.java  NEW
├── model/
│   ├── AvatarPreset.java         NEW
│   └── User.java                 UPDATE (add @Column to avatar)
└── service/
    └── UserService.java          UPDATE (add updateAvatar, findByEmail, getProfile)

src/test/java/com/tictactore/
├── controller/
│   └── ProfileControllerIT.java  NEW
└── service/
    └── UserServiceTest.java      UPDATE
```

**Frontend — new/modified files:**
```
frontend/src/
├── components/
│   ├── AvatarHeaderButton.vue    NEW
│   ├── AvatarPicker.vue          NEW
│   └── __tests__/
│       └── AvatarPicker.spec.ts  NEW
├── services/
│   └── profileService.ts         NEW
├── stores/
│   └── auth.ts                   UPDATE (avatar field + updateAvatar action)
├── views/
│   ├── HomeHub.vue               UPDATE (add AvatarHeaderButton)
│   └── PersonalCabinetView.vue   NEW
└── router/
    └── index.ts                  UPDATE (/cabinet route + auth guard)
```

### References

| Resource | Purpose |
|---|---|
| `src/main/java/com/tictactore/controller/AuthApi.java` | Controller interface pattern to replicate |
| `src/main/java/com/tictactore/controller/AuthController.java` | Controller implementation pattern |
| `src/main/java/com/tictactore/model/User.java` | Entity — `avatar` field already exists |
| `src/main/java/com/tictactore/service/UserService.java` | Service to extend |
| `src/main/java/com/tictactore/security/JwtAuthenticationFilter.java` | How JWT principal name (email) is set in SecurityContext |
| `src/main/java/com/tictactore/config/SecurityConfig.java` | PUBLIC_ENDPOINTS list — no changes needed |
| `frontend/src/stores/auth.ts` | Pinia store to extend |
| `frontend/src/views/HomeHub.vue` | View to add AvatarHeaderButton |
| `frontend/src/router/index.ts` | Router to extend |
| `_project-spec/rules/1-write.md` | Code conventions (strict layering, naming, no direct repo access from controllers) |
| `_project-spec/rules/2-test.md` | Test conventions (IT suffix for integration tests, AAA, no Lombok tests) |
| UX doc: Flow 4 | AvatarPicker in onboarding — 24 preset grid, component hint |
| UX doc: Flow 5 | AvatarPicker in cabinet — same component reused from Flow 4 |

---

## Dev Agent Record

**Status:** ready-for-dev

### Agent Model Used
_[to be filled by dev agent]_

### Debug Log References
_[to be filled by dev agent]_

### Completion Notes List
- Ultimate context engine analysis completed - comprehensive developer guide created

### File List
_[to be filled by dev agent]_
