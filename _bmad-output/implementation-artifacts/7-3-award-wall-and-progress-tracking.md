---
baseline_commit: 67fc7b1f2b9853666f631b78233eb4baa57cde07
---

# Story 7.3: Award Wall and Progress Tracking

Status: ready-for-dev

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a player,
I want to view my complete award wall in my profile and track granular progress toward locked achievements,
so that I can celebrate my collected badges, easily browse different categories, and clearly see how close I am to reaching pending milestones.

## Acceptance Criteria

1. **Given** an authenticated player views their profile in the Personal Cabinet (`/cabinet`)
   **When** the Award Wall (`ProfileBadgesSection.vue`) renders
   **Then** all achievements in the catalog are displayed in an organized gallery (`FR50`)
   **And** interactive category filter tabs allow switching between `All`, `Badges` (Milestones), and `Anti-Achievements`
   **And** summary counters show both total progress (`totalUnlocked / totalAvailable`) and category-specific unlock counts.
2. **Given** an achievement with measurable milestone thresholds (`MATCHES_10`, `STRIKER_50`, `DEFENSE_WALL`, `FIRST_WIN`)
   **When** a player has not yet unlocked the achievement (`isUnlocked = false`)
   **Then** the system dynamically evaluates the player's current numerical progress against the target milestone (`currentProgress`, `targetValue`, `hasProgress = true`) via `AchievementEvaluator.getProgress(userId, stats)` (`FR51`)
   **And** progress calculation executes in-memory during query using aggregated `PlayerStatsContext` without persisting intermediate progress counters to `player_achievement` (zero DB write amplification).
3. **Given** a non-progressive / single-match event achievement (`CLEAN_SHEET`, `GOOSE_EGG`, `GENEROUS_HOST`, `SIEVE_DEFENSE`, `HEARTBREAKER`) or an already unlocked progressive achievement
   **When** querying player achievements
   **Then** non-progressive achievements return `hasProgress = false`, `currentProgress = null`, `targetValue = null`
   **And** unlocked progressive achievements return `isUnlocked = true`, `hasProgress = true`, `currentProgress = targetValue`.
4. **Given** an API request to `GET /api/v1/players/{id}/achievements`
   **When** the endpoint executes
   **Then** it returns `200 OK` with `PlayerAchievementsSummaryResponse` containing enriched `AchievementDto` items with progress metadata (`currentProgress`, `targetValue`, `hasProgress`)
   **And** caller authorization is verified via JWT and no PII/email is leaked (`AD-04`, `AD-05`).
5. **Given** a locked progressive badge rendered on the Award Wall (`BadgeCard.vue`)
   **When** `hasProgress = true` and `isUnlocked = false`
   **Then** the card displays a subtle, tactile progress bar and a numeric progress ratio (e.g. `3 / 10`) styled in Clubhouse Editorial tokens (`bg-ch-surface-highest` track, `bg-ch-primary` fill per `UX-DR3`)
   **And** tapping the badge opens the detail modal displaying full title, description, category, and an expanded progress bar with percentage and remaining count.
6. **Given** interface localization in English and German (`FR59`)
   **When** viewing the Award Wall, category tabs, and progress indicators
   **Then** all strings and labels (including filter tabs, progress labels, and modal counters) are fully localized in `en.json` and `de.json`.

## Tasks / Subtasks

- [ ] Task 1: Backend Domain & Evaluator Extensions (AC2, AC3)
  - [ ] Create `com.tictactore.service.achievement.ProgressInfo` record (`long current`, `long target`, `boolean hasProgress`).
  - [ ] Add default method `ProgressInfo getProgress(UUID userId, PlayerStatsContext stats)` to `AchievementEvaluator` returning `new ProgressInfo(0, 0, false)`.
  - [ ] Update `MatchesPlayedEvaluator`: return `new ProgressInfo(Math.min(stats.totalMatches(), THRESHOLD), THRESHOLD, true)`.
  - [ ] Update `StrikerGoalsEvaluator`: return `new ProgressInfo(Math.min(stats.totalGoalsAsAttacker(), THRESHOLD), THRESHOLD, true)`.
  - [ ] Update `DefenseWallEvaluator`: return `new ProgressInfo(Math.min(stats.totalMatchesAsDefender(), THRESHOLD), THRESHOLD, true)`.
  - [ ] Update `FirstWinEvaluator`: return `new ProgressInfo(Math.min(stats.totalWins(), 1), 1, true)`.
- [ ] Task 2: Backend DTO & Service Implementation (AC2, AC3, AC4)
  - [ ] Update `com.tictactore.dto.AchievementDto`:
    - Add fields: `Long currentProgress`, `Long targetValue`, `boolean hasProgress`.
  - [ ] Update `AchievementServiceImpl.getPlayerAchievements(UUID playerId)`:
    - Build `PlayerStatsContext` for the requested `playerId`.
    - Map evaluators by achievement code into a lookup map.
    - Compute `currentProgress`, `targetValue`, and `hasProgress` for each catalog achievement based on unlock status and evaluator `getProgress()`.
    - Ensure unlocked progressive achievements set `currentProgress = targetValue`.
- [ ] Task 3: Backend Unit, ATDD & Integration Tests (AC2, AC3, AC4)
  - [ ] Update `AchievementEvaluatorTest.java`: add unit test cases for `getProgress()` on each evaluator (0 progress, partial progress, threshold met).
  - [ ] Update `AchievementServiceTest.java`: verify `getPlayerAchievements` correctly populates progress fields for both locked and unlocked achievements.
  - [ ] Update `AchievementControllerATDDTest.java` and `AchievementServiceIT.java`: verify REST endpoint contract includes new progress fields and passes ATDD acceptance tests.
- [ ] Task 4: Frontend Types & Store Enhancements (AC1, AC4)
  - [ ] Update `AchievementDto` in `frontend/src/services/achievementService.ts` with `currentProgress: number | null`, `targetValue: number | null`, and `hasProgress: boolean`.
  - [ ] Update `frontend/src/features/achievements/stores/useAchievementStore.ts`:
    - Add computed getters for filtered views if needed (`badgesList`, `antiAchievementsList`).
- [ ] Task 5: Frontend UI: Award Wall & Progress Bars (AC1, AC5)
  - [ ] Update `frontend/src/features/achievements/components/BadgeCard.vue`:
    - Render mini progress bar and numeric counter (`currentProgress / targetValue`) when `badge.hasProgress && !badge.isUnlocked`.
    - Apply Clubhouse Editorial tokens (`bg-ch-surface-highest` track, `bg-ch-primary` fill, `text-ch-text-secondary/70` ratio).
  - [ ] Update `frontend/src/features/achievements/components/ProfileBadgesSection.vue`:
    - Add category filter pill tabs (`All`, `Badges`, `Anti-Achievements`) with active styling.
    - Update badge detail modal to render a full progress bar with percentage and remaining count for progressive achievements.
- [ ] Task 6: Localization in English & German (AC6)
  - [ ] Add translation keys in `frontend/src/locales/en.json`:
    - `achievements.filterAll: "All"`
    - `achievements.filterBadges: "Badges"`
    - `achievements.filterAnti: "Anti-Achievements"`
    - `achievements.progress: "Progress: {current} / {target}"`
    - `achievements.remaining: "{count} remaining"`
  - [ ] Add translation keys in `frontend/src/locales/de.json`:
    - `achievements.filterAll: "Alle"`
    - `achievements.filterBadges: "Erfolge"`
    - `achievements.filterAnti: "Anti-Erfolge"`
    - `achievements.progress: "Fortschritt: {current} / {target}"`
    - `achievements.remaining: "Noch {count}"`
- [ ] Task 7: Frontend Unit & Playwright E2E Tests (AC1, AC5, AC6)
  - [ ] Update `BadgeCard.spec.ts`: test progress bar rendering when `hasProgress = true` and hidden when `hasProgress = false` or `isUnlocked = true`.
  - [ ] Update `useAchievementStore.spec.ts` and add tests for `ProfileBadgesSection.spec.ts` for category filtering.
  - [ ] Update Playwright E2E test `frontend/e2e/achievements-profile.spec.ts`: assert category filtering and progress bars render properly on `/cabinet`.
- [ ] Task 8: Verification & Quality Gate
  - [ ] Execute `./scripts/ci-local.sh` and ensure 100% test pass across backend, frontend unit tests, and Playwright E2E suites.

## Dev Notes

### Architecture & Implementation Guardrails

- **Zero DB Write Amplification:**
  - `player_achievement` stores ONLY unlocked badges with `unlocked_at`.
  - Progress counters are computed on-the-fly in `AchievementServiceImpl` using single-pass `PlayerStatsContext`. Never create a table or column to track incremental progress per match.
- **Contract & DTO Compatibility:**
  - `AchievementDto` record signature:
    ```java
    public record AchievementDto(
        UUID id,
        String code,
        String category,
        String nameKey,
        String descriptionKey,
        String icon,
        boolean isUnlocked,
        OffsetDateTime unlockedAt,
        Long currentProgress,
        Long targetValue,
        boolean hasProgress
    ) {}
    ```
- **The 500-Line Rule (IP-04):**
  - Keep `AchievementServiceImpl.java`, `BadgeCard.vue`, and `ProfileBadgesSection.vue` modular and well below 500 lines.
- **Zero Comments Policy:**
  - Add documentation only when required by project rules (OpenAPI annotations). No redundant inline comments or fluff.
- **Clubhouse Editorial UI Aesthetics (UX-DR3):**
  - Use `ch-` class tokens exclusively (`bg-ch-surface-card`, `bg-ch-surface-highest`, `bg-ch-primary`, `text-ch-primary`, `border-ch-border`).
  - Progress bars must blend smoothly into the card without loud or garish neon gaming visuals.

### Files to Modify (Audit)

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/tictactore/service/achievement/ProgressInfo.java` | NEW | Record holding `(long current, long target, boolean hasProgress)` |
| `src/main/java/com/tictactore/service/achievement/AchievementEvaluator.java` | UPDATE | Add default method `getProgress` |
| `src/main/java/com/tictactore/service/achievement/evaluator/MatchesPlayedEvaluator.java` | UPDATE | Implement `getProgress` for `MATCHES_10` |
| `src/main/java/com/tictactore/service/achievement/evaluator/StrikerGoalsEvaluator.java` | UPDATE | Implement `getProgress` for `STRIKER_50` |
| `src/main/java/com/tictactore/service/achievement/evaluator/DefenseWallEvaluator.java` | UPDATE | Implement `getProgress` for `DEFENSE_WALL` |
| `src/main/java/com/tictactore/service/achievement/evaluator/FirstWinEvaluator.java` | UPDATE | Implement `getProgress` for `FIRST_WIN` |
| `src/main/java/com/tictactore/dto/AchievementDto.java` | UPDATE | Add `currentProgress`, `targetValue`, `hasProgress` |
| `src/main/java/com/tictactore/service/impl/AchievementServiceImpl.java` | UPDATE | Populate progress info in `mapToDto` |
| `src/test/java/com/tictactore/service/achievement/AchievementEvaluatorTest.java` | UPDATE | Add unit tests for `getProgress()` |
| `src/test/java/com/tictactore/service/AchievementServiceTest.java` | UPDATE | Verify progress mapping in service tests |
| `src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java` | UPDATE | Update test fixtures for expanded DTO |
| `frontend/src/services/achievementService.ts` | UPDATE | Update TS interface `AchievementDto` |
| `frontend/src/features/achievements/stores/useAchievementStore.ts` | UPDATE | Add category filter getters |
| `frontend/src/features/achievements/components/BadgeCard.vue` | UPDATE | Render progress bar and numeric counter for locked progressive badges |
| `frontend/src/features/achievements/components/ProfileBadgesSection.vue` | UPDATE | Add filter tabs and modal progress bar |
| `frontend/src/locales/en.json` | UPDATE | Add localization keys for filters & progress |
| `frontend/src/locales/de.json` | UPDATE | Add German translations for filters & progress |
| `frontend/src/features/achievements/components/__tests__/BadgeCard.spec.ts` | UPDATE | Test progress bar visibility |
| `frontend/src/features/achievements/stores/__tests__/useAchievementStore.spec.ts` | UPDATE | Test store getters and state |
| `frontend/e2e/achievements-profile.spec.ts` | UPDATE | E2E verification of progress bars and filter tabs |

### ATDD Artifacts

- Checklist: `_bmad-output/test-artifacts/atdd-checklist-7-3-award-wall-and-progress-tracking.md`
- Backend ATDD Controller test: `src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java`
- Frontend E2E test: `frontend/e2e/achievements-profile.spec.ts`
- Frontend Component tests: `frontend/src/features/achievements/components/__tests__/BadgeCard.spec.ts`

