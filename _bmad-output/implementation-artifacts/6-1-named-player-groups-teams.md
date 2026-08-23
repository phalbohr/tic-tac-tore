---
baseline_commit: 66fd32b166a30c09b1a31360f31adef112b7171c
---

# Story 6.1: Named Player Groups ("Teams")

Status: review

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

- [x] Task 1: Backend Database Migration & Domain Entity (AC1, AC2, AC5)
  - [x] Create Flyway migration `V10__create_player_group_tables.sql`:
    - Table `player_group` (`id UUID PRIMARY KEY`, `name VARCHAR(100) NOT NULL`, `creator_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`, `is_favorite BOOLEAN DEFAULT FALSE NOT NULL`, `created_at TIMESTAMP WITH TIME ZONE NOT NULL`, `updated_at TIMESTAMP WITH TIME ZONE`, `version BIGINT NOT NULL`, `CONSTRAINT uk_player_group_creator_name UNIQUE (creator_id, name)`).
    - Table `player_group_member` (`group_id UUID NOT NULL REFERENCES player_group(id) ON DELETE CASCADE`, `user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`, `added_at TIMESTAMP WITH TIME ZONE NOT NULL`, `PRIMARY KEY (group_id, user_id)`).
    - Indexes on `player_group(creator_id)` and `player_group_member(user_id)`.
  - [x] Create `com.tictactore.model.PlayerGroup` entity with `@Version`, `@CreationTimestamp`, `@UpdateTimestamp`, and `@ManyToMany` mapping with `User`.
  - [x] Create `com.tictactore.repository.PlayerGroupRepository` extending `JpaRepository<PlayerGroup, UUID>`:
    - `List<PlayerGroup> findByCreatorIdOrderByCreatedAtAsc(UUID creatorId)`
    - `Optional<PlayerGroup> findByIdAndCreatorId(UUID id, UUID creatorId)`
    - `boolean existsByCreatorIdAndNameIgnoreCase(UUID creatorId, String name)`
    - `boolean existsByCreatorIdAndNameIgnoreCaseAndIdNot(UUID creatorId, String name, UUID id)`
- [x] Task 2: Backend Service, DTOs & REST Controller (AC1, AC2, AC5)
  - [x] Create DTOs:
    - `CreatePlayerGroupRequest` (`@NotBlank @Size(max = 50) String name`, `@NotEmpty List<UUID> memberIds`, `Boolean isFavorite`)
    - `UpdatePlayerGroupRequest` (`@NotBlank @Size(max = 50) String name`, `@NotEmpty List<UUID> memberIds`, `Boolean isFavorite`)
    - `PlayerGroupResponse` (`UUID id`, `String name`, `boolean isFavorite`, `UUID creatorId`, `List<PlayerSummaryDto> members`, `OffsetDateTime createdAt`, `OffsetDateTime updatedAt`)
    - `PlayerSummaryDto` (`UUID id`, `String nickname`, `String avatar`)
  - [x] Create `com.tictactore.service.PlayerGroupService` and `PlayerGroupServiceImpl`:
    - `List<PlayerGroupResponse> getGroups(UUID creatorId)`
    - `PlayerGroupResponse getGroupById(UUID creatorId, UUID groupId)`
    - `PlayerGroupResponse createGroup(UUID creatorId, CreatePlayerGroupRequest request)`
    - `PlayerGroupResponse updateGroup(UUID creatorId, UUID groupId, UpdatePlayerGroupRequest request)`
    - `void deleteGroup(UUID creatorId, UUID groupId)`
    - Validate duplicate group names per creator, member existence, and enforce ownership checks (`403 Forbidden` if group belongs to another user).
    - Return safe player summaries without leaking email or PII (`AD-04`).
  - [x] Create `com.tictactore.controller.PlayerGroupController` mapped to `/api/v1/player-groups`:
    - `GET /api/v1/player-groups` -> `200 OK`
    - `GET /api/v1/player-groups/{id}` -> `200 OK`
    - `POST /api/v1/player-groups` -> `201 Created`
    - `PUT /api/v1/player-groups/{id}` -> `200 OK`
    - `DELETE /api/v1/player-groups/{id}` -> `204 No Content`
    - Extract user from `@AuthenticationPrincipal User principal` (`AD-05`).
- [x] Task 3: Frontend Service & Pinia Store (AC1, AC2, AC3, AC4)
  - [x] Create `frontend/src/services/playerGroupService.ts` for interacting with `/api/v1/player-groups`.
  - [x] Create `frontend/src/features/group/stores/usePlayerGroupStore.ts`:
    - State: `groups: PlayerGroupResponse[]`, `selectedGroupId: string | null`, `loading: boolean`, `error: string | null`.
    - Getters: `favoriteGroup`, `customGroups`, `getGroupById`.
    - Actions: `fetchGroups()`, `createGroup()`, `updateGroup()`, `deleteGroup()`, `selectGroup()`.
- [x] Task 4: Frontend UI Components & Inline UX Integration (AC1, AC3, AC4)
  - [x] Create `frontend/src/features/group/components/PlayerGroupModal.vue` (inline modal/dialog for creating and editing groups, selecting members via `PlayerSearchOverlay.vue` in `customSelect` mode).
  - [x] Integrate group management in `frontend/src/features/profile/Cabinet.vue` via `PlayerGroupSection.vue` (Settings section to list, create, edit, and delete player groups).
  - [x] Update `frontend/src/features/match/components/PlayerSelection.vue` to show player group quick-filter/selection chips (Favorites + custom groups) above the player slots.
  - [x] Update `frontend/src/features/match/components/MatchFilterChips.vue` to support filtering match history by player group (`groupId` filter).
  - [x] Add i18n localization keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.
  - [x] Adhere to Clubhouse "No-Line" rule (`UX-DR3`) with `ch-` classes and 500-line limit (`IP-04`).
- [x] Task 5: Testing & Quality Verification
  - [x] Backend: Unit tests in `PlayerGroupServiceTest.java` and `PlayerGroupRepositoryTest.java`.
  - [x] Backend ATDD: Scaffold `PlayerGroupControllerATDDTest.java` covering CRUD, ownership isolation, validation errors, and PII masking.
  - [x] Frontend: Store tests in `frontend/src/features/group/stores/__tests__/usePlayerGroupStore.spec.ts` and component tests in `PlayerGroupModal.spec.ts`.
  - [x] E2E: Playwright test in `frontend/e2e/player-groups.spec.ts` testing inline group creation during match setup, group management in Settings, and filtering in Match History.
  - [x] Verification: Execute `./scripts/ci-local.sh` and verify 100% test pass.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contracts & Security (AD-05):**
  - Base path: `/api/v1/player-groups`
  - Authentication: All endpoints require authentication. Extract user ID from `@AuthenticationPrincipal User principal`.
  - Ownership enforcement: Only the creator of a group can view, modify, or delete it. Attempts to access/modify another user's group return `403 Forbidden`.
- **Privacy & PII Protection (AD-04):**
  - Member DTOs in group responses NEVER expose user emails or OAuth IDs. Only return `PlayerSummaryDto(UUID id, String nickname, String avatar)`.
- **Database Design & Migrations:**
  - Flyway migration script: `src/main/resources/db/migration/V10__create_player_group_tables.sql`.
  - Primary keys: UUIDs generated by backend/database.
  - Foreign keys: Cascade deletion (`ON DELETE CASCADE`) when a user or group is deleted.
  - Concurrency: `@Version` column on `PlayerGroup` entity to support optimistic locking.
- **UX & Navigation Invariants:**
  - **No Dedicated Admin Screen:** As defined in UX Design Specification, no standalone "Teams & Rules" page created.
  - Group creation and editing occurs **inline** (via modal/sheet in `PlayerSelection.vue`) or within Profile Settings (`Cabinet.vue` via `PlayerGroupSection.vue`).
  - **Match Draft Preservation:** Opening the group creation modal during match setup does not reset or alter the current draft match state.
- **Clubhouse Styling Guidelines (UX-DR3):**
  - Followed Clubhouse "No-Line" rule: surface background tonal shifts and elevation instead of 1px solid border lines between list items.
- **500-Line Rule (IP-04):**
  - All new and updated files strictly under 500 lines.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments.

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR39 (Named player groups / "teams", built-in "Favorites" group)
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - Section "Teams & Rules: no dedicated screen | All management inline during match creation or in Settings", UX-DR3
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-01, AD-04, AD-05, IP-04

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash (High)

### Debug Log References

- Spring Security mock context isolation fixed with `@AfterEach void tearDown() { SecurityContextHolder.clearContext(); }` in `PlayerGroupControllerATDDTest.java`.
- `MatchRepository.findMatchHistory` maintained backward compatibility with 6-arg query while adding `findMatchHistoryWithGroupMembers` for group filtering.
- Scoped Playwright dialog locators to prevent strict mode collisions with global buttons.

### Completion Notes List

- All 5 tasks completed and verified with unit, ATDD, and E2E suites.
- Full verification script `./scripts/ci-local.sh` passed cleanly with 112 passing Playwright tests and 350 passing Maven tests.

### File List

- `src/main/resources/db/migration/V10__create_player_group_tables.sql`
- `src/main/java/com/tictactore/model/PlayerGroup.java`
- `src/main/java/com/tictactore/repository/PlayerGroupRepository.java`
- `src/main/java/com/tictactore/dto/PlayerSummaryDto.java`
- `src/main/java/com/tictactore/dto/CreatePlayerGroupRequest.java`
- `src/main/java/com/tictactore/dto/UpdatePlayerGroupRequest.java`
- `src/main/java/com/tictactore/dto/PlayerGroupResponse.java`
- `src/main/java/com/tictactore/service/PlayerGroupService.java`
- `src/main/java/com/tictactore/service/impl/PlayerGroupServiceImpl.java`
- `src/main/java/com/tictactore/controller/PlayerGroupController.java`
- `src/main/java/com/tictactore/repository/MatchRepository.java`
- `src/main/java/com/tictactore/service/MatchService.java`
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
- `src/main/java/com/tictactore/controller/MatchController.java`
- `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java`
- `src/test/java/com/tictactore/repository/PlayerGroupRepositoryTest.java`
- `src/test/java/com/tictactore/service/PlayerGroupServiceTest.java`
- `src/test/java/com/tictactore/controller/PlayerGroupControllerATDDTest.java`
- `frontend/src/services/playerGroupService.ts`
- `frontend/src/services/matchService.ts`
- `frontend/src/features/group/stores/usePlayerGroupStore.ts`
- `frontend/src/features/group/stores/__tests__/usePlayerGroupStore.spec.ts`
- `frontend/src/features/group/components/PlayerGroupModal.vue`
- `frontend/src/features/group/components/__tests__/PlayerGroupModal.spec.ts`
- `frontend/src/features/profile/components/PlayerGroupSection.vue`
- `frontend/src/features/profile/Cabinet.vue`
- `frontend/src/features/match/components/PlayerSelection.vue`
- `frontend/src/features/match/components/MatchFilterChips.vue`
- `frontend/src/features/match/stores/useMatchHistoryStore.ts`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/e2e/player-groups.spec.ts`
- `_bmad-output/implementation-artifacts/6-1-named-player-groups-teams.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
