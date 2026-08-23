# Story 6.1: Named Player Groups ("Teams")

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to create named player groups ("teams") and manage curated lists of players (including a default "Favorites" group),
so that I can quickly select frequent teammates and opponents during match creation and filter match history and statistics.

## Acceptance Criteria

1. **Given** an authenticated player creating a match (`/matches/new`) or managing preferences in Profile Settings (`/cabinet`)
   **When** they create a new player group with a name and selected player IDs (or update an existing group)
   **Then** the system persists the group associated with the creator (`creatorId`)
   **And** group names must be unique per creator (1–50 characters, trimmed)
   **And** a built-in "Favorites" group is provided and supported for fast access (FR39).
2. **Given** an authenticated user
   **When** they query their player groups via `GET /api/v1/player-groups`
   **Then** the system returns only groups created by the authenticated user, including group ID, name, favorite flag, member count, and member details (safe summary DTOs without email/PII per `AD-04`)
   **And** groups created by other users are strictly isolated and inaccessible.
3. **Given** a player is setting up a new match in the portrait match creation flow (`/matches/new`)
   **When** selecting players for 1v1 or 2v2 slots
   **Then** the player selector offers inline access to player groups ("Favorites" and custom groups) to quickly filter or populate players
   **And** the player can create a new group inline via `PlayerGroupModal.vue` without losing their active match draft state.
4. **Given** a player is viewing the Unified Match History (`/matches` or `/history`)
   **When** they interact with the filter controls (`MatchFilterChips.vue`)
   **Then** player groups are available as filter chips to filter match history to games involving members of the selected group
   **And** selecting a group filter immediately updates the query and match list.
5. **Given** an authenticated user
   **When** they attempt to update (`PUT /api/v1/player-groups/{id}`) or delete (`DELETE /api/v1/player-groups/{id}`) a group created by another user
   **Then** the system rejects the operation with `403 Forbidden`.

## Tasks / Subtasks

- [ ] Task 1: Backend Database Migration & Domain Entity (AC1, AC2, AC5)
  - [ ] Create Flyway migration `V10__create_player_group_tables.sql`:
    - Table `player_group` (`id UUID PRIMARY KEY`, `name VARCHAR(100) NOT NULL`, `creator_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`, `is_favorite BOOLEAN DEFAULT FALSE NOT NULL`, `created_at TIMESTAMP WITH TIME ZONE NOT NULL`, `updated_at TIMESTAMP WITH TIME ZONE`, `version BIGINT NOT NULL`, `CONSTRAINT uk_player_group_creator_name UNIQUE (creator_id, name)`).
    - Table `player_group_member` (`group_id UUID NOT NULL REFERENCES player_group(id) ON DELETE CASCADE`, `user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`, `added_at TIMESTAMP WITH TIME ZONE NOT NULL`, `PRIMARY KEY (group_id, user_id)`).
    - Indexes on `player_group(creator_id)` and `player_group_member(user_id)`.
  - [ ] Create `com.tictactore.model.PlayerGroup` entity with `@Version`, `@CreationTimestamp`, `@UpdateTimestamp`, and `@ManyToMany` mapping with `User`.
  - [ ] Create `com.tictactore.repository.PlayerGroupRepository` extending `JpaRepository<PlayerGroup, UUID>`:
    - `List<PlayerGroup> findByCreatorIdOrderByCreatedAtAsc(UUID creatorId)`
    - `Optional<PlayerGroup> findByIdAndCreatorId(UUID id, UUID creatorId)`
    - `boolean existsByCreatorIdAndNameIgnoreCase(UUID creatorId, String name)`
    - `boolean existsByCreatorIdAndNameIgnoreCaseAndIdNot(UUID creatorId, String name, UUID id)`
- [ ] Task 2: Backend Service, DTOs & REST Controller (AC1, AC2, AC5)
  - [ ] Create DTOs:
    - `CreatePlayerGroupRequest` (`@NotBlank @Size(max = 50) String name`, `@NotEmpty List<UUID> memberIds`, `Boolean isFavorite`)
    - `UpdatePlayerGroupRequest` (`@NotBlank @Size(max = 50) String name`, `@NotEmpty List<UUID> memberIds`, `Boolean isFavorite`)
    - `PlayerGroupResponse` (`UUID id`, `String name`, `boolean isFavorite`, `UUID creatorId`, `List<PlayerSummaryDto> members`, `OffsetDateTime createdAt`, `OffsetDateTime updatedAt`)
    - `PlayerSummaryDto` (`UUID id`, `String nickname`, `String avatar`)
  - [ ] Create `com.tictactore.service.PlayerGroupService` and `PlayerGroupServiceImpl`:
    - `List<PlayerGroupResponse> getGroups(UUID creatorId)`
    - `PlayerGroupResponse getGroupById(UUID creatorId, UUID groupId)`
    - `PlayerGroupResponse createGroup(UUID creatorId, CreatePlayerGroupRequest request)`
    - `PlayerGroupResponse updateGroup(UUID creatorId, UUID groupId, UpdatePlayerGroupRequest request)`
    - `void deleteGroup(UUID creatorId, UUID groupId)`
    - Validate duplicate group names per creator, member existence, and enforce ownership checks (`403 Forbidden` if group belongs to another user).
    - Return safe player summaries without leaking email or PII (`AD-04`).
  - [ ] Create `com.tictactore.controller.PlayerGroupController` mapped to `/api/v1/player-groups`:
    - `GET /api/v1/player-groups` -> `200 OK`
    - `GET /api/v1/player-groups/{id}` -> `200 OK`
    - `POST /api/v1/player-groups` -> `201 Created`
    - `PUT /api/v1/player-groups/{id}` -> `200 OK`
    - `DELETE /api/v1/player-groups/{id}` -> `204 No Content`
    - Extract user from `@AuthenticationPrincipal User principal` (`AD-05`).
- [ ] Task 3: Frontend Service & Pinia Store (AC1, AC2, AC3, AC4)
  - [ ] Create `frontend/src/services/playerGroupService.ts` for interacting with `/api/v1/player-groups`.
  - [ ] Create `frontend/src/features/group/stores/usePlayerGroupStore.ts`:
    - State: `groups: PlayerGroupResponse[]`, `selectedGroupId: string | null`, `loading: boolean`, `error: string | null`.
    - Getters: `favoriteGroup`, `customGroups`, `getGroupById`.
    - Actions: `fetchGroups()`, `createGroup()`, `updateGroup()`, `deleteGroup()`, `selectGroup()`.
- [ ] Task 4: Frontend UI Components & Inline UX Integration (AC1, AC3, AC4)
  - [ ] Create `frontend/src/features/group/components/PlayerGroupModal.vue` (inline modal/dialog for creating and editing groups, selecting members via `PlayerSearchOverlay.vue` in `customSelect` mode).
  - [ ] Integrate group management in `frontend/src/features/profile/Cabinet.vue` (Settings section to list, create, edit, and delete player groups).
  - [ ] Update `frontend/src/features/match/components/PlayerSelection.vue` to show player group quick-filter/selection chips (Favorites + custom groups) above the player slots.
  - [ ] Update `frontend/src/features/match/components/MatchFilterChips.vue` to support filtering match history by player group (`groupId` filter).
  - [ ] Add i18n localization keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.
  - [ ] Adhere to Clubhouse "No-Line" rule (`UX-DR3`) with `ch-` classes and 500-line limit (`IP-04`).
- [ ] Task 5: Testing & Quality Verification
  - [ ] Backend: Unit tests in `PlayerGroupServiceTest.java` and `PlayerGroupRepositoryTest.java`.
  - [ ] Backend ATDD: Scaffold `PlayerGroupControllerATDDTest.java` covering CRUD, ownership isolation, validation errors, and PII masking.
  - [ ] Frontend: Store tests in `frontend/src/features/group/stores/__tests__/usePlayerGroupStore.spec.ts` and component tests in `PlayerGroupModal.spec.ts`.
  - [ ] E2E: Playwright test in `frontend/e2e/player-groups.spec.ts` testing inline group creation during match setup, group management in Settings, and filtering in Match History.
  - [ ] Verification: Execute `./scripts/ci-local.sh` and verify 100% test pass.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contracts & Security (AD-05):**
  - Base path: `/api/v1/player-groups`
  - Authentication: All endpoints require authentication. Extract user ID from `@AuthenticationPrincipal User principal`.
  - Ownership enforcement: Only the creator of a group can view, modify, or delete it. Attempts to access/modify another user's group must return `403 Forbidden` (or `404 Not Found`).
- **Privacy & PII Protection (AD-04):**
  - Member DTOs in group responses must NEVER expose user emails or OAuth IDs. Only return `PlayerSummaryDto(UUID id, String nickname, String avatar)`.
- **Database Design & Migrations:**
  - Flyway migration script: `src/main/resources/db/migration/V10__create_player_group_tables.sql`.
  - Primary keys: UUIDs generated by backend/database.
  - Foreign keys: Cascade deletion (`ON DELETE CASCADE`) when a user or group is deleted.
  - Concurrency: `@Version` column on `PlayerGroup` entity to support optimistic locking.
- **UX & Navigation Invariants:**
  - **No Dedicated Admin Screen:** As defined in UX Design Specification, do NOT create a separate standalone "Teams & Rules" page.
  - Group creation and editing must occur **inline** (via modal/sheet in `PlayerSelection.vue`) or within Profile Settings (`Cabinet.vue`).
  - **Match Draft Preservation:** Opening the group creation modal during match setup must not reset or alter the current draft match state.
- **Clubhouse Styling Guidelines (UX-DR3):**
  - Follow the Clubhouse "No-Line" rule: use surface background tonal shifts and elevation instead of 1px solid border lines between list items.
  - Use `ch-` prefixed SCSS tokens and Tailwind utility classes conforming to theme tokens.
- **500-Line Rule (IP-04):**
  - Keep all new and updated files strictly under 500 lines. Split UI components (`PlayerGroupModal.vue`, `PlayerGroupList.vue`) and service operations if necessary.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods MUST adhere to Arrange-Act-Assert separated by a single blank line, with **zero structural comments** (no `// Arrange`, `// Act`, `// Assert`).
  - Unit, ATDD, and E2E tests are mandatory before marking the story done.

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-6-1-named-player-groups-teams.md`
- **Backend API tests:** `_bmad-output/test-artifacts/atdd-redphase-6-1/PlayerGroupControllerATDDTest.java`
- **Frontend E2E tests:** `frontend/e2e/player-groups.spec.ts`
- **Frontend Store tests:** `_bmad-output/test-artifacts/atdd-redphase-6-1/usePlayerGroupStore.spec.ts`
- **Frontend Component tests:** `_bmad-output/test-artifacts/atdd-redphase-6-1/PlayerGroupModal.spec.ts`

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR39 (Named player groups / "teams", built-in "Favorites" group)
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - Section "Teams & Rules: no dedicated screen | All management inline during match creation or in Settings", UX-DR3
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-01, AD-04, AD-05, IP-04

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash (High)

### Debug Log References

### Completion Notes List

### File List

- `_bmad-output/implementation-artifacts/6-1-named-player-groups-teams.md`
