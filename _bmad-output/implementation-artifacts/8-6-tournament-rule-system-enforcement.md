---
baseline_commit: fed5a89
status: ready-for-dev
---

# Story 8.6: Tournament Rule System Enforcement

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story Foundation

As a system, I want to enforce the tournament's rules, so that matches are consistent.

### Acceptance Criteria
- **Given** a player initiates match entry for a tournament match
- **When** the entry screen loads
- **Then** the rule system selection is locked to the tournament's configured rule set and cannot be overridden (FR45)

## Developer Context

This story bridges the match entry flow with tournament constraints. A match entry originating from a tournament should pre-select and lock the rule system to the tournament's configuration.

### Technical Requirements
1. **Frontend**:
   - Needs to accept tournament context (e.g. `tournamentId`, `ruleConfigId`) via route query/params.
   - If tournament context is present, the rule selection dropdown/UI must be disabled/locked.
   - The selected rule system MUST match the tournament's `ruleConfiguration`.
2. **Backend Match Validation**:
   - Validation must ensure that if a match is tied to a tournament (`tournamentMatchId`), the `ruleConfigId` in the request STRICTLY matches the `ruleConfigId` of the tournament.
   - Refuse match creation (HTTP 409 or 400) if the rule config diverges from the tournament's configuration.

### Architecture Compliance
- **Immutable RuleConfiguration (AD-01)**: The `rule_config_id` is the source of truth for the match.
- **Speed as survival metric**: Pre-filling and locking the rule system eliminates user clicks, satisfying the sub-10 second match entry UX requirement.
- **No Line Rule**: Any UI changes in disabled dropdowns must not use `border-*` Tailwind classes, but rely on surface level distinctions (`surface-container-highest` vs `surface`, etc.).
- **500-Line Rule**: Ensure modified Vue and Java files do not cross 500 lines. If they do, refactor cleanly.

### File Structure Requirements
- `frontend/src/features/match/components/NewMatchFlow.vue` or related rule selection components
- `src/main/java/com/tictactore/service/MatchService.java` (Backend validation)
- `src/main/java/com/tictactore/dto/CreateMatchRequest.java` (Ensure tournament correlation ID is passed if needed)

### Testing Requirements
- **Frontend tests**: Verify rule selection is disabled when tournament parameters are passed.
- **Backend tests**: Unit test to assert an exception is thrown when submitting a tournament match with an mismatched `ruleConfigId`.

### Previous Story Intelligence
- Story 8.5 implemented `Asynchronous Tournament Match Execution`. Starting a match transitions it to `IN_PROGRESS`.

### Git Intelligence
- Commit `1bee7bb` shows we just updated match statuses to `READY` by default.
- This story concerns the actual *recording/submission* of the match result for those in progress, or setting up the rules when entering it.

## Completion Status
Ultimate context engine analysis completed - comprehensive developer guide created.
