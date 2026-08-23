# Story 6.2: Default Team and Rule Template

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to set default teams and rules,
so that I don't have to select them every time.

## Acceptance Criteria

1. **Given** a player modifies their user preferences
2. **When** they set a default group and a default rule template
3. **Then** future match creation screens automatically pre-populate the selected rule template and filter the player list by the default group (FR40)

## Tasks / Subtasks

- [ ] Task 1: Update User Preferences / Profile UI
  - [ ] Add selectors for "Default Group" and "Default Rule Template" in the user settings/profile page.
  - [ ] Fetch available groups and rule templates for the selectors.
- [ ] Task 2: Backend Support for User Preferences
  - [ ] Update user/player profile entity to store `defaultGroupId` and `defaultRuleTemplateId`.
  - [ ] Update relevant API endpoints to allow saving and retrieving these preferences.
- [ ] Task 3: Match Creation Screen Pre-population
  - [ ] Modify Match Creation UI to fetch user preferences on load.
  - [ ] Pre-populate the rule template selector with the default template.
  - [ ] Automatically apply the default group as a filter in the player selection component.
- [ ] Task 4: Testing
  - [ ] Unit tests for the frontend component changes.
  - [ ] Unit/Integration tests for the backend preference saving/retrieving.
  - [ ] End-to-end tests ensuring match creation honors defaults.

## Dev Notes

- Relevant architecture patterns and constraints:
  - Architecture decision AD-01 states that `RuleConfiguration` is immutable. We just store the ID of the template in preferences.
  - Profile features belong in `features/profile/` and Match creation in `features/match/`.
  - Ensure the 500-line rule (IP-04) is respected when adding these settings to user profiles and endpoints.
  - Follow the `ch-` prefix rule for any specific styling added.
- Source tree components to touch:
  - `frontend/src/features/profile/`
  - `frontend/src/features/match/`
  - Backend user/player domain models, DTOs, and controllers (`com.itemis.tictactore.domain.player`, `com.itemis.tictactore.api.player`).
- Testing standards summary:
  - Unit tests colocated with source files (`*.spec.ts` for Vue, Java tests next to Java files). Playwright for E2E.

### Project Structure Notes

- Keep the separation of concerns: User preferences are loaded in the frontend store (Pinia) and used by `MatchCreation` components.

### References

- Cite all technical details with source paths and sections, e.g. [Source: docs/<file>.md#Section]
- [Source: _bmad-output/planning-artifacts/epics.md#Story 6.2: Default Team and Rule Template]

## Dev Agent Record

### Agent Model Used
Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

### File List
