---
baseline_commit: 8488830e2ea8a6818274ef9a58fb944eef26f32e
---

# Story 6.3: Create "Want to Play" Pool

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to create a "Want to Play" matchmaking pool with configurable match format, start conditions, and optional skill-level restrictions directly from the Home Hub,
so that I can easily find suitable opponents and organize casual or scheduled foosball matches without social friction.

## Acceptance Criteria

1. **Given** an authenticated user on the Home Hub (`/`)
   **When** they tap the "Want to Play" action button
   **Then** a dedicated `CreatePoolModal.vue` dialog opens, presenting a creation form with options for Match Type (`1v1` / `2v2`), Start Condition (`FILL_BASED` / `SCHEDULED_TIME`), and optional Skill Level filter (`OPEN_FOR_ALL`, `BEGINNER`, `INTERMEDIATE`, `ADVANCED`).
2. **Given** an authenticated user configuring an immediate/fill-based pool (`startCondition == FILL_BASED`)
   **When** they select Match Type (`1v1` requiring 2 total players or `2v2` requiring 4 total players) and submit the form via `POST /api/v1/pools`
   **Then** the backend persists the new pool with status `OPEN`, sets `creator_id` to the authenticated user's ID, automatically registers the creator as the first participant (`HOST`), and returns `201 Created` with the complete `PoolResponse` (FR35).
3. **Given** an authenticated user configuring a scheduled pool (`startCondition == SCHEDULED_TIME`)
   **When** they specify a valid future date/time (between now and +7 days in ISO 8601 UTC) and submit the form via `POST /api/v1/pools`
   **Then** the backend persists the scheduled pool with `scheduled_time`, registers the creator as the initial participant, and returns `201 Created` with the complete `PoolResponse` (FR35).
4. **Given** a user attempting to create a pool with invalid parameters (e.g. `scheduled_time` in the past or missing when `startCondition == SCHEDULED_TIME`, or invalid match format)
   **When** the request is received by `POST /api/v1/pools`
   **Then** the backend rejects the request with `400 Bad Request` containing standard validation error details, or `401 Unauthorized` if unauthenticated.
5. **Given** an authenticated user who already has 3 active open pools (`status == OPEN`)
   **When** they attempt to create another pool
   **Then** the backend rejects the request with `400 Bad Request` and message `"Maximum active pools limit reached (3)"` to prevent pool spam.
6. **Given** a user successfully submits the pool creation form in the frontend
   **When** `POST /api/v1/pools` returns `201 Created`
   **Then** the modal closes, a success toast notification appears confirming pool creation, and the new pool is recorded in `usePoolStore`.

## Tasks / Subtasks

- [x] Task 1: Database Migration & JPA Entities (AC2, AC3, AC5)
  - [x] Create Flyway migration `src/main/resources/db/migration/V13__create_matchmaking_pools.sql`:
    - Create `matchmaking_pool` table: `id UUID PRIMARY KEY`, `creator_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`, `match_type VARCHAR(20) NOT NULL`, `start_condition VARCHAR(20) NOT NULL`, `scheduled_time TIMESTAMP WITH TIME ZONE`, `skill_level VARCHAR(20) DEFAULT 'OPEN_FOR_ALL' NOT NULL`, `status VARCHAR(20) NOT NULL`, `created_at TIMESTAMP WITH TIME ZONE NOT NULL`, `updated_at TIMESTAMP WITH TIME ZONE`, `version BIGINT NOT NULL`.
    - Create `pool_participant` table: `id UUID PRIMARY KEY`, `pool_id UUID NOT NULL REFERENCES matchmaking_pool(id) ON DELETE CASCADE`, `user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`, `role VARCHAR(20) NOT NULL`, `joined_at TIMESTAMP WITH TIME ZONE NOT NULL`, `CONSTRAINT uk_pool_participant UNIQUE (pool_id, user_id)`.
    - Create indexes: `idx_pool_creator_id ON matchmaking_pool(creator_id)`, `idx_pool_status ON matchmaking_pool(status)`, `idx_pool_participant_user_id ON pool_participant(user_id)`.
  - [x] Create Enums in `com.tictactore.model`:
    - `MatchType` (`ONE_VS_ONE`, `TWO_VS_TWO`)
    - `StartCondition` (`FILL_BASED`, `SCHEDULED_TIME`)
    - `PoolStatus` (`OPEN`, `FILLED`, `CANCELLED`, `EXPIRED`)
    - `SkillLevel` (`OPEN_FOR_ALL`, `BEGINNER`, `INTERMEDIATE`, `ADVANCED`)
    - `PoolParticipantRole` (`HOST`, `PLAYER`)
  - [x] Create Entities in `com.tictactore.model`:
    - `MatchmakingPool.java` with `@Version Long version`, `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) List<PoolParticipant> participants`.
    - `PoolParticipant.java` with reference to `MatchmakingPool` and `User`.
  - [x] Create Repositories in `com.tictactore.repository`:
    - `MatchmakingPoolRepository.java` (`long countByCreatorIdAndStatus(UUID creatorId, PoolStatus status)`, `Optional<MatchmakingPool> findByIdAndStatus(UUID id, PoolStatus status)`).
    - `PoolParticipantRepository.java`.
  - [x] Update `MatchmakingPoolRepositoryTest.java` verifying cascade saves, unique participant constraints, and index queries.
- [x] Task 2: Backend DTOs, Service & Controller (AC1, AC2, AC3, AC4, AC5)
  - [x] Create DTOs in `com.tictactore.dto`:
    - `CreatePoolRequest.java` (`@NotNull MatchType matchType`, `@NotNull StartCondition startCondition`, `Instant scheduledTime`, `SkillLevel skillLevel`).
    - `PoolParticipantDto.java` (`UUID userId`, `String nickname`, `String avatar`, `PoolParticipantRole role`, `Instant joinedAt`).
    - `PoolResponse.java` (`UUID id`, `UUID creatorId`, `String creatorNickname`, `MatchType matchType`, `StartCondition startCondition`, `Instant scheduledTime`, `SkillLevel skillLevel`, `PoolStatus status`, `int requiredPlayers`, `int currentPlayers`, `List<PoolParticipantDto> participants`, `Instant createdAt`).
  - [x] Create `PoolService.java` & `PoolServiceImpl.java` in `com.tictactore.service`:
    - Method `createPool(UUID creatorId, CreatePoolRequest request)`:
      - Fail-fast validation: If `startCondition == SCHEDULED_TIME`, verify `scheduledTime != null` and `scheduledTime` is between `now()` and `now() + 7 days`. If `startCondition == FILL_BASED`, ensure `scheduledTime == null`.
      - Check active pool quota: `countByCreatorIdAndStatus(creatorId, PoolStatus.OPEN) < 3`, throw `IllegalArgumentException` / `400 Bad Request` if exceeded.
      - Instantiate `MatchmakingPool` and attach creator as initial `PoolParticipant` with role `HOST`.
      - Persist entity via repository and map to `PoolResponse`.
    - Method `getPoolById(UUID poolId)` returning `PoolResponse`.
  - [x] Create `PoolController.java` in `com.tictactore.controller`:
    - `POST /api/v1/pools` with `@Valid @RequestBody CreatePoolRequest`, extracting `principal` via `@AuthenticationPrincipal User principal`. Return `201 Created`.
    - `GET /api/v1/pools/{id}` returning `PoolResponse`.
  - [x] Unit & ATDD Tests:
    - `PoolServiceTest.java` (testing 1v1 fill-based, 2v2 scheduled, validation errors for past dates, active pool limit violations).
    - `PoolControllerTest.java` (unauthenticated 401, validation 400, success 201).
    - `PoolControllerATDDTest.java` (full request/response lifecycle).
- [x] Task 3: Frontend Types, Service & Pinia Store (AC1, AC2, AC3, AC6)
  - [x] Create `frontend/src/features/matchmaking/types/pool.ts` with TypeScript interfaces matching backend DTOs and enums.
  - [x] Create `frontend/src/features/matchmaking/services/poolService.ts` calling `/api/v1/pools`.
  - [x] Create `frontend/src/features/matchmaking/stores/poolStore.ts`:
    - State: `activePools`, `currentPool`, `isLoading`, `error`.
    - Actions: `createPool(payload: CreatePoolPayload)`, `fetchPool(id: string)`.
  - [x] Frontend store tests in `frontend/src/features/matchmaking/stores/__tests__/poolStore.spec.ts`.
- [x] Task 4: Frontend UI Components & Home Hub Integration (AC1, AC6)
  - [x] Create `frontend/src/features/matchmaking/components/CreatePoolModal.vue`:
    - Clubhouse design token styling (`bg-surface-container-low`, rounded-2xl, elevation, no 1px solid borders per `UX-DR3`).
    - Segmented selector for Match Type (1v1 vs 2v2).
    - Segmented selector for Start Condition (Immediate / Fill-based vs Scheduled Time).
    - DateTime picker for scheduled start (visible when `SCHEDULED_TIME` is selected).
    - Skill Level dropdown/selector (`OPEN_FOR_ALL`, `BEGINNER`, `INTERMEDIATE`, `ADVANCED`).
    - Submit ("Create Pool") and Cancel actions with loading state.
  - [x] Update Home Hub (`frontend/src/views/HomeView.vue`):
    - Add "Want to Play" primary action button alongside "New Match".
    - Manage `isCreatePoolOpen` state to launch `CreatePoolModal.vue`.
    - Ensure `HomeView.vue` stays modular and does not expand unnecessarily to adhere to 500-Line Rule (`IP-04`).
  - [x] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json` for all modal labels, options, errors, and toast messages.
  - [x] Component unit tests in `frontend/src/features/matchmaking/components/__tests__/CreatePoolModal.spec.ts`.
- [x] Task 5: Testing & Quality Verification
  - [x] Backend Unit & ATDD Tests:
    - `PoolServiceTest.java` (strict AAA without section comments).
    - `PoolControllerTest.java` & `PoolControllerATDDTest.java`.
    - `MatchmakingPoolRepositoryTest.java`.
  - [x] Frontend Unit/Component Tests:
    - `poolStore.spec.ts`.
    - `CreatePoolModal.spec.ts`.
  - [x] E2E Playwright Tests:
    - Create `frontend/e2e/want-to-play-pool.spec.ts`:
      - Test 1: Authenticated user opens Home Hub -> clicks "Want to Play" -> selects 1v1 fill-based -> submits form -> modal closes and pool created.
      - Test 2: User creates scheduled 2v2 pool with future timestamp -> submits -> pool created with scheduled time.
      - Test 3: Validation feedback when scheduled time is invalid or missing.
  - [x] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contracts & Security (AD-04, AD-05):**
  - Base path: `/api/v1/pools`.
  - Endpoints:
    - `POST /api/v1/pools`: Create a new pool.
    - `GET /api/v1/pools/{id}`: Retrieve pool details by ID.
  - Authentication: All endpoints require authentication. User principal extracted via `@AuthenticationPrincipal User principal`.
  - Validation rules:
    - `matchType`: Required (`ONE_VS_ONE` or `TWO_VS_TWO`). Required player count is 2 for 1v1, 4 for 2v2.
    - `startCondition`: Required (`FILL_BASED` or `SCHEDULED_TIME`).
    - `scheduledTime`: Required if `startCondition == SCHEDULED_TIME`, must be in the future (within 7 days). Must be `null` if `startCondition == FILL_BASED`.
    - `skillLevel`: Defaults to `OPEN_FOR_ALL` if omitted.
    - Maximum 3 active (`OPEN`) pools per creator.
- **Database Design & Migrations:**
  - Flyway migration script: `src/main/resources/db/migration/V13__create_matchmaking_pools.sql`.
  - Tables: `matchmaking_pool` and `pool_participant`.
  - Foreign keys:
    - `creator_id REFERENCES "user"(id) ON DELETE CASCADE`
    - `pool_id REFERENCES matchmaking_pool(id) ON DELETE CASCADE`
    - `user_id REFERENCES "user"(id) ON DELETE CASCADE`
  - Concurrency: `@Version` column on `MatchmakingPool` entity for optimistic locking.
  - Unique constraint: `(pool_id, user_id)` on `pool_participant`.
- **UX & Navigation Invariants:**
  - **Home Hub Evolution:** "Want to Play" button is positioned on Home Hub (`HomeView.vue`) as the Phase 2 evolution CTA.
  - **Modal Flow:** Opens `CreatePoolModal.vue` without full page navigation. On success, closes modal and displays confirmation toast.
  - **Non-blocking match flow:** Creating a pool does not alter existing match draft state in `useMatchDraftStore`.
- **Clubhouse Styling Guidelines (UX-DR3):**
  - Strictly adhere to Clubhouse "No-Line" rule: tonal shifts (`bg-surface-container-low`, `bg-surface-container-high`) and elevation/shadows instead of 1px solid border lines.
- **500-Line Rule (IP-04):**
  - All new files strictly under 500 lines. `CreatePoolModal.vue` and `poolStore.ts` encapsulate all matchmaking creation logic to keep `HomeView.vue` clean.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are forbidden).

### File Boundaries

| File | Status | Description |
|---|---|---|
| `src/main/resources/db/migration/V13__create_matchmaking_pools.sql` | NEW | Flyway migration for `matchmaking_pool` and `pool_participant` |
| `src/main/java/com/tictactore/model/MatchType.java` | NEW | Enum for 1v1 / 2v2 match formats |
| `src/main/java/com/tictactore/model/StartCondition.java` | NEW | Enum for fill-based vs scheduled start |
| `src/main/java/com/tictactore/model/PoolStatus.java` | NEW | Enum for pool lifecycle status |
| `src/main/java/com/tictactore/model/SkillLevel.java` | NEW | Enum for optional skill restriction |
| `src/main/java/com/tictactore/model/PoolParticipantRole.java` | NEW | Enum for host / participant role |
| `src/main/java/com/tictactore/model/MatchmakingPool.java` | NEW | JPA Entity for matchmaking pool |
| `src/main/java/com/tictactore/model/PoolParticipant.java` | NEW | JPA Entity for pool participants |
| `src/main/java/com/tictactore/repository/MatchmakingPoolRepository.java` | NEW | Spring Data repository for pools |
| `src/main/java/com/tictactore/repository/PoolParticipantRepository.java` | NEW | Spring Data repository for pool participants |
| `src/main/java/com/tictactore/dto/CreatePoolRequest.java` | NEW | Request payload DTO with validation |
| `src/main/java/com/tictactore/dto/PoolResponse.java` | NEW | Response DTO for pool representation |
| `src/main/java/com/tictactore/dto/PoolParticipantDto.java` | NEW | Participant DTO |
| `src/main/java/com/tictactore/service/PoolService.java` | NEW | Service interface for pool operations |
| `src/main/java/com/tictactore/service/PoolServiceImpl.java` | NEW | Service implementation with business validations |
| `src/main/java/com/tictactore/controller/PoolController.java` | NEW | REST Controller for `/api/v1/pools` |
| `frontend/src/features/matchmaking/types/pool.ts` | NEW | TypeScript definitions for pools |
| `frontend/src/features/matchmaking/services/poolService.ts` | NEW | Frontend API client for pool endpoints |
| `frontend/src/features/matchmaking/stores/poolStore.ts` | NEW | Pinia store for matchmaking pool state |
| `frontend/src/features/matchmaking/components/CreatePoolModal.vue` | NEW | Clubhouse modal for pool creation |
| `frontend/src/views/HomeView.vue` | UPDATE | Add "Want to Play" trigger and modal mount |
| `frontend/src/locales/en.json` | UPDATE | English i18n keys for pool creation |
| `frontend/src/locales/de.json` | UPDATE | German i18n keys for pool creation |
| `frontend/e2e/want-to-play-pool.spec.ts` | NEW | Playwright E2E test suite |

### Previous Story Intelligence (Learnings from 6.1, 6.1b, 6.2)

- **Ownership & Authentication:** Always extract user ID from `@AuthenticationPrincipal User principal` on the backend. Never allow clients to pass arbitrary creator IDs.
- **Transactional Atomicity:** When creating a pool, the creator must be attached as a participant within the same database transaction (`@Transactional`).
- **Database Cascades:** Use `ON DELETE CASCADE` between `matchmaking_pool` and `pool_participant` to ensure clean deletion if pools are removed.
- **Frontend Architecture:** Keep matchmaking components inside `src/features/matchmaking/` isolated from match entry and profile logic.

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-6-3-create-want-to-play-pool.md`
- **Controller ATDD Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-3/PoolControllerATDDTest.java`
- **Service ATDD Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-3/PoolServiceTest.java`
- **Frontend Pinia Store Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-3/poolStore.spec.ts`
- **Frontend Component Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-3/CreatePoolModal.spec.ts`
- **Frontend E2E Test Suite (Skipped for Red Phase):** `frontend/e2e/want-to-play-pool.spec.ts`

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR35 ("Want to Play" pools)
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - Section "Home Hub: predictable, focused, evolving. Phase 2 adds Want to Play", UX Spec #7 "Skill-level matchmaking"
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-04, AD-05, IP-04 (500-Line Rule)

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash

### Debug Log References

- Verified all JPA cascade operations and constraints with `MatchmakingPoolRepositoryTest`.
- Tested service and controller business rules with `PoolServiceTest` and `PoolControllerATDDTest`.
- Verified Pinia store and Vue modal component with Vitest in `poolStore.spec.ts` and `CreatePoolModal.spec.ts`.
- Validated end-to-end user flows with Playwright in `want-to-play-pool.spec.ts`.
- Verified entire system with `./scripts/ci-local.sh` (backend unit/integration tests, frontend type check, frontend build, frontend unit tests, Playwright E2E tests).

### Completion Notes List

- Added Flyway migration `V13__create_matchmaking_pools.sql` creating `matchmaking_pool` and `pool_participant` tables with appropriate constraints, indexes, and cascades.
- Implemented backend domain enums (`MatchType`, `StartCondition`, `PoolStatus`, `SkillLevel`, `PoolParticipantRole`) and JPA entities (`MatchmakingPool`, `PoolParticipant`) with optimistic locking.
- Created Spring Data repositories (`MatchmakingPoolRepository`, `PoolParticipantRepository`).
- Implemented DTOs (`CreatePoolRequest`, `PoolResponse`, `PoolParticipantDto`), service layer (`PoolService`, `PoolServiceImpl`), and REST controller (`PoolController` under `/api/v1/pools`).
- Created frontend matchmaking module under `src/features/matchmaking/` with TypeScript types, API service client, and Pinia store (`usePoolStore`).
- Implemented `CreatePoolModal.vue` following Clubhouse styling without 1px solid borders, integrated "Want to Play" CTA on Home Hub (`HomeView.vue`), added `SuccessToast.vue`, and added bilingual i18n keys (`en.json`, `de.json`).
- Added comprehensive unit, ATDD, and E2E Playwright test suites. Full `./scripts/ci-local.sh` passed with 100% success.

### File List

- `src/main/resources/db/migration/V13__create_matchmaking_pools.sql` (NEW)
- `src/main/java/com/tictactore/model/MatchType.java` (NEW)
- `src/main/java/com/tictactore/model/StartCondition.java` (NEW)
- `src/main/java/com/tictactore/model/PoolStatus.java` (NEW)
- `src/main/java/com/tictactore/model/SkillLevel.java` (NEW)
- `src/main/java/com/tictactore/model/PoolParticipantRole.java` (NEW)
- `src/main/java/com/tictactore/model/MatchmakingPool.java` (NEW)
- `src/main/java/com/tictactore/model/PoolParticipant.java` (NEW)
- `src/main/java/com/tictactore/repository/MatchmakingPoolRepository.java` (NEW)
- `src/main/java/com/tictactore/repository/PoolParticipantRepository.java` (NEW)
- `src/main/java/com/tictactore/dto/CreatePoolRequest.java` (NEW)
- `src/main/java/com/tictactore/dto/PoolResponse.java` (NEW)
- `src/main/java/com/tictactore/dto/PoolParticipantDto.java` (NEW)
- `src/main/java/com/tictactore/service/PoolService.java` (NEW)
- `src/main/java/com/tictactore/service/PoolServiceImpl.java` (NEW)
- `src/main/java/com/tictactore/controller/PoolController.java` (NEW)
- `src/test/java/com/tictactore/repository/MatchmakingPoolRepositoryTest.java` (NEW)
- `src/test/java/com/tictactore/service/PoolServiceTest.java` (NEW)
- `src/test/java/com/tictactore/controller/PoolControllerATDDTest.java` (NEW)
- `frontend/src/features/matchmaking/types/pool.ts` (NEW)
- `frontend/src/features/matchmaking/services/poolService.ts` (NEW)
- `frontend/src/features/matchmaking/stores/poolStore.ts` (NEW)
- `frontend/src/features/matchmaking/stores/__tests__/poolStore.spec.ts` (NEW)
- `frontend/src/features/matchmaking/components/CreatePoolModal.vue` (NEW)
- `frontend/src/features/matchmaking/components/__tests__/CreatePoolModal.spec.ts` (NEW)
- `frontend/src/core/components/SuccessToast.vue` (NEW)
- `frontend/src/views/HomeView.vue` (MODIFIED)
- `frontend/src/locales/en.json` (MODIFIED)
- `frontend/src/locales/de.json` (MODIFIED)
- `frontend/src/test-setup.ts` (NEW)
- `frontend/vitest.config.ts` (MODIFIED)
- `frontend/e2e/want-to-play-pool.spec.ts` (MODIFIED)
- `_bmad-output/implementation-artifacts/sprint-status.yaml` (MODIFIED)
- `_bmad-output/implementation-artifacts/6-3-create-want-to-play-pool.md` (MODIFIED)

## Change Log

- **2026-08-28**: Implemented Story 6.3 "Create Want to Play Pool" backend schema, models, services, controller, frontend store, UI modal, Home Hub CTA, and ATDD/E2E test suites. Ready for review.

