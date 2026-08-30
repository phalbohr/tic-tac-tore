---
baseline_commit: f90fb27aa66112ab8b49a49c0f57b2105464cfc6
---

# Story 7.1: Achievement System (Badges)

Status: ready-for-dev

<!-- Note: Validation is complete. Story is ready for dev-story execution. -->

## Story

As a player,
I want to earn achievement badges for hitting gameplay milestones and match performance thresholds,
so that I feel rewarded for my progress and can showcase my accomplishments on my profile.

## Acceptance Criteria

1. **Given** an authenticated player completes or confirms a match
   **When** the match transitions to `CONFIRMED` status (either via auto-approval upon creation or explicit opponent confirmation)
   **Then** the system asynchronously evaluates all eligible achievements for all match participants via a Spring ApplicationEvent (`MatchConfirmedEvent`)
   **And** the evaluation does not block or add latency to the match creation/confirmation HTTP response (<200ms backend threshold).
2. **Given** a player meets the criteria for one or more achievements (initial catalog: `FIRST_WIN`, `MATCHES_10`, `CLEAN_SHEET`, `STRIKER_50`, `DEFENSE_WALL`)
   **When** the achievement evaluation engine evaluates the player's updated statistics
   **Then** the system awards the achievement badge by creating a `player_achievement` record with `unlocked_at` timestamp
   **And** enforces an idempotency/uniqueness constraint `(user_id, achievement_id)` so a badge is never awarded more than once.
3. **Given** an authenticated user queries a player's achievements via `GET /api/v1/players/{id}/achievements`
   **When** the request is processed
   **Then** the endpoint returns `200 OK` with a summary DTO containing total counts and the list of achievements with metadata (ID, code, category, name key, description key, icon) and unlock status (`isUnlocked`, `unlockedAt`)
   **And** no private user information (PII/email) is exposed per `AD-04`.
4. **Given** an authenticated user views their profile in the Personal Cabinet (`/cabinet`)
   **When** the Cabinet view renders
   **Then** a dedicated `ProfileBadgesSection.vue` component displays earned and locked badges with Clubhouse Editorial design tokens (`ch-` palette, tactile warmth, no neon/gaming cliches per `UX-DR3`)
   **And** tapping/hovering a badge shows its title, description, and unlock timestamp.
5. **Given** interface localization in English and German (`FR59`)
   **When** viewing badge titles, descriptions, and section headers
   **Then** all strings are fully localized via `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.

## Tasks / Subtasks

- [ ] Task 1: Database Migration & Domain Entities (AC1, AC2)
  - [ ] Create Flyway migration `V16__create_achievements_tables.sql`:
    - Table `achievement` (`id UUID PRIMARY KEY`, `code VARCHAR(50) NOT NULL UNIQUE`, `category VARCHAR(50) NOT NULL`, `name_key VARCHAR(100) NOT NULL`, `description_key VARCHAR(255) NOT NULL`, `icon VARCHAR(100) NOT NULL`, `created_at TIMESTAMP WITH TIME ZONE NOT NULL`).
    - Table `player_achievement` (`id UUID PRIMARY KEY`, `user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`, `achievement_id UUID NOT NULL REFERENCES achievement(id) ON DELETE CASCADE`, `unlocked_at TIMESTAMP WITH TIME ZONE NOT NULL`, `CONSTRAINT uk_player_achievement UNIQUE (user_id, achievement_id)`).
    - Indexes on `player_achievement(user_id)` and `player_achievement(achievement_id)`.
    - Seed initial achievements (`FIRST_WIN`, `MATCHES_10`, `CLEAN_SHEET`, `STRIKER_50`, `DEFENSE_WALL`).
  - [ ] Create JPA entity `com.tictactore.model.Achievement` with `@CreationTimestamp`.
  - [ ] Create JPA entity `com.tictactore.model.PlayerAchievement` with `@CreationTimestamp`.
  - [ ] Create `com.tictactore.repository.AchievementRepository` extending `JpaRepository<Achievement, UUID>`.
  - [ ] Create `com.tictactore.repository.PlayerAchievementRepository` extending `JpaRepository<PlayerAchievement, UUID>`:
    - `List<PlayerAchievement> findByUserIdOrderByUnlockedAtDesc(UUID userId)`
    - `boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId)`
- [ ] Task 2: Domain Events & Asynchronous Achievement Evaluation Engine (AC1, AC2)
  - [ ] Create `com.tictactore.event.MatchConfirmedEvent` record (`UUID matchId`, `List<UUID> participantIds`).
  - [ ] Update `MatchServiceImpl.java` / `MatchOperation.java` to publish `MatchConfirmedEvent` when match status becomes `CONFIRMED`.
  - [ ] Create `com.tictactore.service.achievement.AchievementEvaluator` interface:
    - `String getAchievementCode()`
    - `boolean evaluate(UUID userId, Match match, PlayerStatsContext stats)`
  - [ ] Implement concrete evaluators:
    - `FirstWinEvaluator` (`FIRST_WIN` — player won at least one match)
    - `MatchesPlayedEvaluator` (`MATCHES_10` — player participated in >= 10 matches)
    - `CleanSheetEvaluator` (`CLEAN_SHEET` — player won match conceding 0 goals)
    - `StrikerGoalsEvaluator` (`STRIKER_50` — player scored >= 50 total goals as attacker)
    - `DefenseWallEvaluator` (`DEFENSE_WALL` — player played >= 10 matches as defender)
  - [ ] Create `com.tictactore.listener.AchievementEventListener` with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` and `@Async` evaluation.
  - [ ] Create `com.tictactore.service.AchievementService` and `AchievementServiceImpl`:
    - `PlayerAchievementsSummaryResponse getPlayerAchievements(UUID playerId)`
    - `void evaluateMatchAchievements(UUID matchId, List<UUID> participantIds)`
- [ ] Task 3: REST Controller & DTOs (AC3)
  - [ ] Create DTOs:
    - `AchievementDto` (`UUID id`, `String code`, `String category`, `String nameKey`, `String descriptionKey`, `String icon`, `boolean isUnlocked`, `OffsetDateTime unlockedAt`)
    - `PlayerAchievementsSummaryResponse` (`UUID playerId`, `int totalUnlocked`, `int totalAvailable`, `List<AchievementDto> achievements`)
  - [ ] Create `com.tictactore.controller.AchievementController` mapped to `/api/v1/players/{id}/achievements`:
    - `GET /api/v1/players/{id}/achievements` -> `200 OK`
    - Extract caller from `@AuthenticationPrincipal User principal` (`AD-05`).
- [ ] Task 4: Frontend Service & Pinia Store (AC3, AC4)
  - [ ] Create `frontend/src/services/achievementService.ts` for calling `/api/v1/players/{id}/achievements`.
  - [ ] Create `frontend/src/features/achievements/stores/useAchievementStore.ts`:
    - State: `achievements: AchievementDto[]`, `totalUnlocked: number`, `totalAvailable: number`, `loading: boolean`, `error: string | null`.
    - Getters: `unlockedList`, `lockedList`.
    - Actions: `fetchPlayerAchievements(playerId: string)`.
- [ ] Task 5: Frontend UI Components & Profile Integration (AC4, AC5)
  - [ ] Create `frontend/src/features/achievements/components/BadgeCard.vue`:
    - Renders icon, localized name, description, and status with tactile Clubhouse styling (`UX-DR3`).
  - [ ] Create `frontend/src/features/achievements/components/ProfileBadgesSection.vue`:
    - Displays grid of badges with progress overview. Keep under 500 lines (`IP-04`).
  - [ ] Mount `ProfileBadgesSection.vue` in `frontend/src/features/profile/Cabinet.vue`.
  - [ ] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.
- [ ] Task 6: Testing & Quality Verification (AC1-AC5)
  - [ ] Backend: Unit tests in `AchievementEvaluatorTest.java` and `AchievementServiceTest.java`.
  - [ ] Backend ATDD: `AchievementControllerATDDTest.java` covering contract, query by ID, and idempotent award.
  - [ ] Frontend: Store and component tests in `useAchievementStore.spec.ts` and `BadgeCard.spec.ts`.
  - [ ] Verification: Run `./scripts/ci-local.sh` and ensure 100% test pass.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contracts & Security (AD-05):**
  - Base path: `/api/v1/players/{id}/achievements`
  - Authentication: All endpoints require valid JWT authentication.
  - PII Protection (`AD-04`): Never leak email addresses or private credentials in achievement responses.
- **The 500-Line Rule (IP-04):**
  - No production file or test class may exceed 500 lines. Split evaluators, stores, and components if necessary.
- **Asynchronous Decoupling & Performance:**
  - Achievement evaluation MUST NOT run synchronously on the match creation/confirmation transaction path. Use Spring `@TransactionalEventListener(phase = AFTER_COMMIT)` with `@Async`.
- **Database & Flyway Migration:**
  - Use Flyway script `V16__create_achievements_tables.sql`.
  - Ensure table names and columns use `snake_case`. Enforce unique constraint `uk_player_achievement` on `(user_id, achievement_id)`.
- **Frontend Design System Compliance (UX-DR3):**
  - Adhere to the "Clubhouse Editorial" aesthetic with `ch-` class tokens (`bg-ch-surface-card`, `text-ch-primary`, `border-ch-border`).
  - Do NOT use garish neon gaming styling or flashy pop-ups. Maintain editorial restraint.

### ATDD Artifacts

- Checklist: `_bmad-output/test-artifacts/atdd-checklist-7-1-achievement-system-badges.md`
- Backend ATDD Controller test: `src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java`
- Frontend E2E test: `frontend/e2e/achievements-profile.spec.ts`
- Frontend Store test: `frontend/src/features/achievements/stores/__tests__/useAchievementStore.spec.ts`
- Frontend Component test: `frontend/src/features/achievements/components/__tests__/BadgeCard.spec.ts`

## Dev Agent Record

### Implementation Plan
- Step 1: Scaffold Flyway migration `V16__create_achievements_tables.sql` and JPA entities `Achievement`, `PlayerAchievement`.
- Step 2: Implement `AchievementEvaluator` strategy pattern and asynchronous `AchievementEventListener`.
- Step 3: Implement `AchievementController` and DTOs with ATDD test coverage.
- Step 4: Implement frontend Pinia store, service, and Vue components (`BadgeCard.vue`, `ProfileBadgesSection.vue`).
- Step 5: Mount in `Cabinet.vue`, add i18n keys for EN/DE, and verify with `./scripts/ci-local.sh`.

### Completion Notes
- *Pending development execution.*

## File List

- `src/main/resources/db/migration/V16__create_achievements_tables.sql`
- `src/main/java/com/tictactore/model/Achievement.java`
- `src/main/java/com/tictactore/model/PlayerAchievement.java`
- `src/main/java/com/tictactore/repository/AchievementRepository.java`
- `src/main/java/com/tictactore/repository/PlayerAchievementRepository.java`
- `src/main/java/com/tictactore/event/MatchConfirmedEvent.java`
- `src/main/java/com/tictactore/listener/AchievementEventListener.java`
- `src/main/java/com/tictactore/service/achievement/AchievementEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/FirstWinEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/MatchesPlayedEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/CleanSheetEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/StrikerGoalsEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/DefenseWallEvaluator.java`
- `src/main/java/com/tictactore/service/AchievementService.java`
- `src/main/java/com/tictactore/service/impl/AchievementServiceImpl.java`
- `src/main/java/com/tictactore/dto/AchievementDto.java`
- `src/main/java/com/tictactore/dto/PlayerAchievementsSummaryResponse.java`
- `src/main/java/com/tictactore/controller/AchievementController.java`
- `frontend/src/services/achievementService.ts`
- `frontend/src/features/achievements/stores/useAchievementStore.ts`
- `frontend/src/features/achievements/components/BadgeCard.vue`
- `frontend/src/features/achievements/components/ProfileBadgesSection.vue`
- `frontend/src/features/profile/Cabinet.vue`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`

## Change Log

- 2026-08-30: Initialized comprehensive Story 7.1 specification, Acceptance Criteria, Tasks, and Dev Notes.
