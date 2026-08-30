---
baseline_commit: 4256a1fd829f7af36de3c8c6509dcf045e9ce313
---

# Story 7.3: Award Wall and Progress Tracking

Status: ready-for-dev

<!-- Note: Ultimate context engine analysis completed - comprehensive developer guide created. Story is ready for dev-story execution. -->

## Story

As a player,
I want to view all my achievements,
so that I can see what I have collected and what is pending.

## Acceptance Criteria

1. **Given** the player views their profile
   **When** they navigate to the award wall
   **Then** they can see all collected achievements (FR50)
   **And** view progress toward locked achievements (FR51)

## Developer Context & Architecture Guardrails

**Architectural & Technical Requirements:**
- **Dynamic Progress Calculation:** The `player_achievement` table only stores *unlocked* achievements. Progress for locked achievements should NOT be persisted in the DB as continuous updates (which would cause database write amplification). Instead, progress should be evaluated dynamically by extending the `AchievementEvaluator` interface with a tracking method (e.g., returning a `ProgressInfo` record with `current` and `target`) and injecting `PlayerStatsContext`.
- **AchievementDTO Update:** Enhance `AchievementDto` (both backend and frontend) with `currentProgress` (int), `targetValue` (int), and optionally a boolean `hasProgress` flag to differentiate boolean achievements (like FIRST_WIN) from progressive ones (like MATCHES_10).
- **Award Wall UI Component:** Create a dedicated view/modal or expand the existing `ProfileBadgesSection.vue` to show all achievements, with visible tracking for locked progressive badges.
- **Frontend Design System Compliance (UX-DR3):** Adhere to the "Clubhouse Editorial" aesthetic with `ch-` class tokens (`bg-ch-surface-card`, `text-ch-primary`, `border-ch-border`). Use progress bars styled neutrally (e.g., using `bg-ch-primary` for fill and `bg-ch-surface-highest` for background).
- **Translation:** Ensure new strings for the Award Wall and progress text are added to `en.json` and `de.json`.
- **Testing:** Update/write ATDD controller tests, Vue component tests, and E2E tests for the new progress visibility.

## Tasks / Subtasks

- [ ] Task 1: Update Backend DTOs and Interfaces
  - Extend `AchievementEvaluator` to return progress information.
  - Update concrete evaluators (`MatchesPlayedEvaluator`, `StrikerGoalsEvaluator`, etc.) to provide current vs. target progress using `PlayerStatsContext`.
  - Update `AchievementDto` to include `currentProgress` and `targetValue`.
  - Modify `AchievementServiceImpl` to populate progress for locked achievements when queried.
- [ ] Task 2: Update Backend Tests
  - Update `AchievementServiceTest` and `AchievementControllerATDDTest` to verify progress values are returned correctly for locked achievements.
- [ ] Task 3: Update Frontend Store and Types
  - Update `AchievementDto` in `achievementService.ts`.
  - Ensure `useAchievementStore` can correctly expose and type the progress data.
- [ ] Task 4: Enhance Frontend UI (Award Wall & Progress)
  - Modify `BadgeCard.vue` and `ProfileBadgesSection.vue` to render a visual progress bar for locked progressive achievements.
  - Apply `ch-` tokens for the progress bar (e.g. `bg-ch-surface-highest` for track, `bg-ch-primary` for progress fill).
- [ ] Task 5: Localization
  - Add required translation keys to `en.json` and `de.json`.
- [ ] Task 6: Update Frontend Tests
  - Update `BadgeCard.spec.ts` and Playwright E2E tests (`achievements-profile.spec.ts`) to assert progress bars render correctly.
