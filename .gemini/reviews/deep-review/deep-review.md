# Deep Review Report - Phase 3 (Match Recording UI)

**Date:** 2026-03-19
**Files reviewed:** 
- frontend/src/components/MatchRecordingForm.vue
- frontend/src/components/MatchScoring.vue

## MatchRecordingForm.vue

### Findings

- **`[FIX_NOW]`** — The `selectedIds` computed property should explicitly check if `authStore.user` is present to ensure the creator is always filtered out from the teammate/opponent selection.
- **`[FIX_NOW]`** — Use a single `reactive` object for the form state instead of multiple individual `ref`s for `teammateId`, `opponent1Id`, etc. This aligns better with standard Vue 3 practices for forms.
- **`[FIX_NOW]`** — Simplify the `selectedIds` filter: `.filter((id): id is number => id != null)`.
- **`[POSTPONE]`** — Refactor to accept `currentUser` as a prop. While better for reusability, it's not strictly necessary for the current architectural phase.
- **`[POSTPONE]`** — Optimize `getAvailableFor` calls by pre-calculating filtered lists. Not a performance issue with small user lists.
- **`[POSTPONE]`** — Add debug logging on submission.

## MatchScoring.vue

### Findings

- **`[FIX_NOW] (CRITICAL)`** — `needsGame3` should be more robust and ensure G1 and G2 are completed (scores entered).
- **`[FIX_NOW] (CRITICAL)`** — `getPlayerByRole` logic for Game 2 potentially uses inconsistent position data if Game 1 positions are changed after navigating to Game 2.
- **`[FIX_NOW]`** — Refactor `getPlayerByRole` to use a more declarative approach (e.g., a mapping object) to reduce complexity and nested `if` statements.
- **`[FIX_NOW]`** — Use constants or an enum for position strings (`'creator-teammate'`, etc.) to prevent typos and improve maintainability.
- **`[POSTPONE]`** — Add upper limits for scores.
- **`[POSTPONE]`** — Document the "Mandatory Swap" rule in code comments.
