---
baseline_commit: 99d15aa7f132778f9616726ff0ef2f33efd71cc8
---

# Story 7.2: Humorous Anti-achievements

Status: ready-for-dev

<!-- Note: Validation is complete. Story is ready for dev-story execution. -->

## Story

As a player,
I want to receive humorous, lighthearted anti-achievement badges when experiencing memorable fails or mishaps during foosball matches,
so that losses and comical situations are celebrated with good sportsmanship rather than feeling punishing or shaming.

## Acceptance Criteria

1. **Given** a confirmed match is processed by the asynchronous achievement evaluation engine (`MatchConfirmedEvent`)
   **When** a player's team lost any game within the match with 0 points scored (e.g., 0-10)
   **Then** the system awards the `GOOSE_EGG` anti-achievement badge (`category: ANTI_ACHIEVEMENT`, `icon: egg`)
   **And** records it in `player_achievement` with the unlock timestamp if not previously awarded.
2. **Given** a confirmed match is processed by the asynchronous achievement evaluation engine
   **When** a player's team conceded 10 or more points in a single game of the match
   **Then** the system awards the `GENEROUS_HOST` anti-achievement badge (`category: ANTI_ACHIEVEMENT`, `icon: volunteer_activism`).
3. **Given** a confirmed match is processed by the asynchronous achievement evaluation engine
   **When** a player participated as Defender (`teamADefenderId` or `teamBDefenderId`) and their team conceded a total of 15 or more goals across all games in that match
   **Then** the system awards the `SIEVE_DEFENSE` anti-achievement badge (`category: ANTI_ACHIEVEMENT`, `icon: water_drop`).
4. **Given** a confirmed match is processed by the asynchronous achievement evaluation engine
   **When** a player's team lost the match in the final/deciding game by a difference of exactly 1 goal (e.g., 9-10 or sudden death deuce)
   **Then** the system awards the `HEARTBREAKER` anti-achievement badge (`category: ANTI_ACHIEVEMENT`, `icon: heart_broken`).
5. **Given** anti-achievement definitions and localization files (`en.json`, `de.json`)
   **When** any player views their earned or locked anti-achievement badges in their profile or award wall
   **Then** all titles and descriptions are phrased in a lighthearted, humorous, and celebratory tone without shaming or toxic language (`FR49`)
   **And** strings are fully localized in English and German.
6. **Given** the frontend profile badges section (`ProfileBadgesSection.vue` / `BadgeCard.vue`)
   **When** anti-achievement badges are rendered
   **Then** the badges display their respective Material Symbols icons (`egg`, `volunteer_activism`, `water_drop`, `heart_broken`)
   **And** visual styling seamlessly adheres to Clubhouse Editorial design tokens (`UX-DR3`).

## Tasks / Subtasks

- [ ] Task 1: Database Migration for Anti-Achievements Seed Data (AC1-AC5)
  - [ ] Create Flyway migration `V17__seed_anti_achievements.sql`:
    - Insert 4 initial humorous anti-achievements into `achievement` table:
      - `GOOSE_EGG` (`category: ANTI_ACHIEVEMENT`, `icon: egg`, `name_key: achievements.goose_egg.title`, `description_key: achievements.goose_egg.description`)
      - `GENEROUS_HOST` (`category: ANTI_ACHIEVEMENT`, `icon: volunteer_activism`, `name_key: achievements.generous_host.title`, `description_key: achievements.generous_host.description`)
      - `SIEVE_DEFENSE` (`category: ANTI_ACHIEVEMENT`, `icon: water_drop`, `name_key: achievements.sieve_defense.title`, `description_key: achievements.sieve_defense.description`)
      - `HEARTBREAKER` (`category: ANTI_ACHIEVEMENT`, `icon: heart_broken`, `name_key: achievements.heartbreaker.title`, `description_key: achievements.heartbreaker.description`)
    - Set `created_at = CURRENT_TIMESTAMP`, `version = 0`.
- [ ] Task 2: Concrete Evaluators Implementation (AC1-AC4)
  - [ ] Create `com.tictactore.service.achievement.evaluator.GooseEggEvaluator` implementing `AchievementEvaluator`:
    - Code: `GOOSE_EGG`
    - Evaluates `match.getGames()`: returns `true` if any game has player's team score equal to 0.
  - [ ] Create `com.tictactore.service.achievement.evaluator.GenerousHostEvaluator` implementing `AchievementEvaluator`:
    - Code: `GENEROUS_HOST`
    - Evaluates `match.getGames()`: returns `true` if any game has opponent team score >= 10.
  - [ ] Create `com.tictactore.service.achievement.evaluator.SieveDefenseEvaluator` implementing `AchievementEvaluator`:
    - Code: `SIEVE_DEFENSE`
    - Evaluates defender position: returns `true` if player is defender on team A/B and total conceded goals in match >= 15.
  - [ ] Create `com.tictactore.service.achievement.evaluator.HeartbreakerEvaluator` implementing `AchievementEvaluator`:
    - Code: `HEARTBREAKER`
    - Evaluates match outcome: returns `true` if player lost the deciding game by exactly 1 goal.
- [ ] Task 3: Localization in English & German (AC5)
  - [ ] Add translation entries in `frontend/src/locales/en.json`:
    - `achievements.goose_egg.title` / `description`
    - `achievements.generous_host.title` / `description`
    - `achievements.sieve_defense.title` / `description`
    - `achievements.heartbreaker.title` / `description`
  - [ ] Add translation entries in `frontend/src/locales/de.json`:
    - `achievements.goose_egg.title` / `description`
    - `achievements.generous_host.title` / `description`
    - `achievements.sieve_defense.title` / `description`
    - `achievements.heartbreaker.title` / `description`
- [ ] Task 4: Frontend Badge Card & Icon Mapping (AC6)
  - [ ] Update `frontend/src/features/achievements/components/BadgeCard.vue`:
    - Map icons: `egg` -> `egg`, `volunteer_activism` -> `volunteer_activism`, `water_drop` -> `water_drop`, `heart_broken` -> `heart_broken`.
    - Support visual styling for `category === 'ANTI_ACHIEVEMENT'` in Clubhouse Editorial design tokens.
  - [ ] Update `frontend/src/features/achievements/components/ProfileBadgesSection.vue`:
    - Ensure modal icon mapping includes new anti-achievement icons.
- [ ] Task 5: Testing & Quality Verification (AC1-AC6)
  - [ ] Backend Unit Tests: Add unit tests in `src/test/java/com/tictactore/service/achievement/AchievementEvaluatorTest.java` for `GooseEggEvaluator`, `GenerousHostEvaluator`, `SieveDefenseEvaluator`, and `HeartbreakerEvaluator`.
  - [ ] Backend ATDD & Integration Tests: Verify anti-achievement evaluations triggered by `MatchConfirmedEvent` in `AchievementServiceTest.java` and `AchievementControllerATDDTest.java`.
  - [ ] Frontend Unit Tests: Update `BadgeCard.spec.ts` to verify icon and styling mapping for anti-achievements.
  - [ ] Full Verification: Run `./scripts/ci-local.sh` and ensure 100% test pass.

## Dev Notes

### Architecture & Implementation Guardrails

- **Flyway Migration Sequencing:**
  - Next migration is `V17__seed_anti_achievements.sql`. Do not use dot-notation (e.g. `V7.2`).
- **Modular Evaluator Registration:**
  - Spring automatically collects all beans implementing `AchievementEvaluator` into `List<AchievementEvaluator> evaluators` in `AchievementServiceImpl`. Adding a `@Component` evaluator requires zero changes to core service classes.
- **Tone & Culture Policy (FR49):**
  - All copy must celebrate effort and memorable mishaps with humor and warmth. Derogatory or insulting phrasing is strictly prohibited.
- **The 500-Line Rule (IP-04):**
  - Keep evaluators, tests, and components modular and well below the 500-line threshold.
- **Clubhouse Editorial UI Theme (UX-DR3):**
  - Use `ch-` palette tokens (`bg-ch-surface-card`, `text-ch-primary`, `border-ch-border`) and Material Symbols icons. Do not introduce raster assets or gaming neon visuals.

## Dev Agent Record

### Implementation Plan
- Step 1: Create Flyway migration `V17__seed_anti_achievements.sql` seeding the 4 initial anti-achievements.
- Step 2: Implement 4 concrete evaluators in `com.tictactore.service.achievement.evaluator`.
- Step 3: Add localized strings in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.
- Step 4: Update icon mapping and category styling in `BadgeCard.vue` and `ProfileBadgesSection.vue`.
- Step 5: Add backend unit/integration tests and frontend component tests, then verify with `./scripts/ci-local.sh`.

### Completion Notes
- Awaiting implementation.

## File List

- `src/main/resources/db/migration/V17__seed_anti_achievements.sql`
- `src/main/java/com/tictactore/service/achievement/evaluator/GooseEggEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/GenerousHostEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/SieveDefenseEvaluator.java`
- `src/main/java/com/tictactore/service/achievement/evaluator/HeartbreakerEvaluator.java`
- `src/test/java/com/tictactore/service/achievement/AchievementEvaluatorTest.java`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/src/features/achievements/components/BadgeCard.vue`
- `frontend/src/features/achievements/components/ProfileBadgesSection.vue`
- `frontend/src/features/achievements/components/__tests__/BadgeCard.spec.ts`

## Change Log

- 2026-08-30: Initialized comprehensive Story 7.2 specification, Acceptance Criteria, Tasks, and Dev Notes following validation review.
