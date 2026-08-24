---
baseline_commit: HEAD
---

# Story 6.2: Default Team and Rule Template

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to set default teams and rules in my preferences,
so that future match creation screens automatically pre-populate the selected rule template and filter the player list by the default group, saving me time.

## Acceptance Criteria

1. **Given** an authenticated user managing preferences in Profile Settings (`/cabinet`)
   **When** they view their settings
   **Then** they see dropdowns/selectors for "Default Player Group" and "Default Rule Template"
   **And** the dropdowns list the user's available custom groups/templates, plus built-in system presets (for rules) or "None".
2. **Given** a user selects a default group and/or rule template
   **When** they save their preferences
   **Then** the system persists `defaultGroupId` and `defaultRuleConfigurationId` to the user's profile
   **And** the backend validates that the selected group and template belong to the user (or are system presets for templates).
3. **Given** a user with saved default preferences
   **When** they initiate a new match in the portrait match creation flow (`/matches/new`)
   **Then** the rule template selector is automatically pre-populated with their default rule template
   **And** the player list filter is automatically set to their default player group
   **And** they can still manually override these defaults for the current match without losing the saved defaults in their profile (FR40).
4. **Given** a user deletes a custom group or rule template that was set as a default
   **When** the deletion occurs
   **Then** the corresponding default preference on the user's profile is automatically cleared (set to null) to prevent orphan references.

## Developer Context

This story builds on the foundation established in 6.1 (Named Player Groups) and 6.1b (Create Rule Template). It integrates these two concepts into the user profile.

### Technical Requirements

- **Database:** Create a new Flyway migration (e.g., `V12__add_user_defaults.sql`) to add `default_group_id` (UUID, nullable, FK to `player_group`) and `default_rule_configuration_id` (UUID, nullable, FK to `rule_configuration`) to the `"user"` table. Add `ON DELETE SET NULL` for the FK constraints to naturally handle AC4.
- **Domain:** Update `com.tictactore.model.User` with `defaultGroupId` and `defaultRuleConfigurationId`.
- **Backend APIs:** Update `UserController` and `UserService` (e.g., `/api/v1/users/me/preferences` or via existing update endpoints) to accept and save these defaults. Validate ownership before saving.
- **Frontend State:** Update `useAuthStore` or a dedicated user preferences store to fetch and hold these defaults.
- **Match Creation UI:** In `NewMatchFlow.vue` / `PlayerSelection.vue` / `RulePicker.vue`, read the defaults from the store on initial mount and apply them to the local match draft state.
- **Cabinet UI:** Add selectors to `Cabinet.vue`. Use existing `usePlayerGroupStore` and `useRuleConfigStore` to populate the options.

### Architecture Compliance

- **Validation:** When saving a default, the backend MUST verify `playerGroup.creatorId == currentUserId`. For rule templates, verify `ruleConfig.type == PRESET || ruleConfig.createdBy == currentUserId`.
- **Performance:** Adding FKs to the user table is safe here because it's a 1:1 relationship with the user profile, but ensure lazy loading or DTO mapping doesn't inadvertently trigger N+1 queries.
- **500-Line Rule (IP-04):** Ensure `Cabinet.vue` and `NewMatchFlow.vue` stay under 500 lines. Consider extracting the preferences section into a `UserPreferencesSection.vue` component if necessary.

### Previous Story Intelligence (Learnings from 6.1 and 6.1b)

- **UI Consistency:** Use the Clubhouse "No-Line" styling (`UX-DR3`) for the new preference dropdowns.
- **Error Handling:** Propagate backend validation errors gracefully to the UI as done in `RuleTemplateModal` and `PlayerGroupModal`.
- **Playwright Testing:** Write E2E tests against a real backend instead of mocked routes, following the pattern in `player-groups.spec.ts` and `rule-system-selection.spec.ts`.

### Project Context Reference

- [Architecture Document](_bmad-output/planning-artifacts/architecture.md)
- [PRD](_bmad-output/planning-artifacts/prd.md)
- [UX Design Specification](_bmad-output/planning-artifacts/ux-design-specification.md)
