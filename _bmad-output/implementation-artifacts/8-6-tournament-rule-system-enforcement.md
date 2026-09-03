---
baseline_commit: fed5a89
status: ready-for-dev
---

# Story 8.6: Tournament Rule System Enforcement

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a system and tournament participant,
I want the tournament's configured rule system and participant roster to be strictly enforced during match entry and creation,
so that all tournament matches remain consistent, compliant with tournament settings, and protected from accidental or malicious overrides.

## Acceptance Criteria

1. **Given** a user initiates match entry for a tournament match from `TournamentsView.vue` or navigates to `/matches/new` with tournament query parameters (`tournamentId`, `tournamentMatchId`, `ruleConfigId`)
   **When** `NewMatchFlow.vue` and `RulePicker.vue` mount
   **Then**:
   - The active rule system selection is automatically locked to the tournament's configured rule set (`ruleConfigId`) (`FR45`).
   - The selected tournament rule chip displays a distinct locked indicator (e.g. lock icon and "Tournament Rule" badge).
   - Other rule chips are non-interactive and disabled (`pointer-events-none`).
   - The "+ Custom Rule" action button is hidden or disabled.
   - The "Set as default" pin button is hidden.
   - An informative notice communicates that the rule system is locked to tournament settings.

2. **Given** a match entry initiated with a `tournamentMatchId`
   **When** `NewMatchFlow.vue` initializes
   **Then**:
   - The match format (`1v1` or `2v2`) is pre-selected according to the tournament mode and locked against modification.
   - The assigned participants from the `TournamentMatch` slot are pre-populated into Team A and Team B roster slots.
   - Arbitrary player replacement is disabled for the tournament match.

3. **Given** an active match draft initiated from a tournament match
   **When** the user completes score entry and submits the match draft
   **Then** `useMatchDraftStore.submitMatchDraft` constructs the match creation payload including:
   - `tournamentMatchId`: the UUID of the tournament match slot.
   - `ruleConfigId`: the UUID of the locked tournament rule configuration.
   - Standard match creation fields (`teamAAttackerId`, `teamADefenderId`, `teamBAttackerId`, `teamBDefenderId`, `games`, `creatorId`, `idempotencyKey`).

4. **Given** `CreateMatchRequest.java` record
   **When** updated to support explicit rule configuration association
   **Then**:
   - The record includes `UUID ruleConfigId`.
   - Overloaded convenience constructors are provided to preserve backward compatibility for all existing callers and tests that omit `ruleConfigId`.

5. **Given** a match creation request submitted to `POST /api/v1/matches`
   **When** the request contains a `tournamentMatchId`
   **Then**:
   - The system retrieves the corresponding `TournamentMatch` entity (or fails with `404 Not Found`).
   - The system verifies the tournament is in `IN_PROGRESS` status.
   - The system verifies that `request.ruleConfigId()` is not null and STRICTLY matches the tournament's `ruleConfiguration.id`.
   - If `ruleConfigId` does not match, the request fails with `409 Conflict` (`TournamentRuleMismatchException` / `TournamentConflictException`).
   - The system verifies that request participants match the assigned participants in `TournamentMatch`. If mismatched, the request fails with `409 Conflict` (`TournamentConflictException`).
   - The persisted `Match` entity has its `rule_config_id` column set to the validated `ruleConfigId`.

6. **Given** a match creation request (tournament or standalone) with a specified `ruleConfigId`
   **When** game count and scores are validated
   **Then**:
   - Game count validation respects the rule configuration's `gameLimit` (rather than a hardcoded maximum of 3 games).
   - If the game scores or count violate rule configuration constraints, the request is rejected with `400 Bad Request` (`InvalidMatchScoreException`).

7. **Given** a tournament match query or start operation (`GET /api/v1/tournaments/{id}/matches` or `POST .../start`)
   **When** returning `TournamentMatchResponse`
   **Then** the response DTO includes `UUID ruleConfigurationId` and `String ruleConfigurationName` from the parent tournament, allowing clients to identify and display the rule set without additional round-trips.

## Tasks / Subtasks

- [ ] Task 1: Backend DTOs & Entity Enhancements (AC4, AC7)
  - [ ] Update `com.tictactore.dto.CreateMatchRequest.java`:
    - Add component `UUID ruleConfigId`.
    - Provide overloaded constructors defaulting `ruleConfigId` to `null` to ensure 100% backward compatibility with existing tests and controllers.
  - [ ] Update `com.tictactore.dto.TournamentMatchResponse.java`:
    - Add `UUID ruleConfigurationId`.
    - Add `String ruleConfigurationName`.
  - [ ] Update `com.tictactore.service.tournament.impl.TournamentMatchServiceImpl.java` and `TournamentMatchQueryServiceImpl.java`:
    - Populate `ruleConfigurationId` and `ruleConfigurationName` from `tournament.getRuleConfiguration()` when mapping `TournamentMatchResponse`.
  - [ ] Unit tests for DTO mapping in `src/test/java/com/tictactore/dto/CreateMatchRequestTest.java` and `TournamentMatchServiceTest.java`.

- [ ] Task 2: Backend Tournament Validation & Match Service Integration (AC5, AC6)
  - [ ] Create exception `com.tictactore.exception.TournamentRuleMismatchException.java` (extends `TournamentConflictException` -> HTTP 409).
  - [ ] Create validator `com.tictactore.service.tournament.TournamentMatchValidator.java` (interface and implementation):
    - Method `void validateTournamentMatchCreation(TournamentMatch tournamentMatch, CreateMatchRequest request)`:
      - Verify tournament status is `IN_PROGRESS`.
      - Verify `request.ruleConfigId()` equals `tournamentMatch.getTournament().getRuleConfiguration().getId()`.
      - Verify participant IDs match `tournamentMatch` assigned players.
    - *Critical Guardrail:* Extracting this logic prevents `MatchServiceImpl.java` (currently 494 lines) from violating the strict 500-line rule.
  - [ ] Update `com.tictactore.service.impl.MatchServiceImpl.java`:
    - Inject `TournamentMatchValidator`.
    - In `createMatch()`:
      - Set `match.setRuleConfigId(request.ruleConfigId())` on `Match.builder()`.
      - When `request.tournamentMatchId() != null`: invoke `tournamentMatchValidator.validateTournamentMatchCreation(...)` before saving.
      - Validate `request.games().size()` against `ruleConfig.getGameLimit()` when rule configuration exists, replacing the hardcoded 3-game restriction.
  - [ ] Unit tests in `src/test/java/com/tictactore/service/TournamentMatchValidatorTest.java` and `src/test/java/com/tictactore/service/MatchServiceTest.java`.

- [ ] Task 3: Frontend Store & Draft Submission Payload (AC2, AC3)
  - [ ] Update `frontend/src/features/tournament/types/tournament.ts`:
    - Add `ruleConfigurationId?: string` and `ruleConfigurationName?: string` to `TournamentMatchDto`.
  - [ ] Update `frontend/src/features/match/stores/matchDraftStore.ts`:
    - Add computed getter `isTournamentMatch = computed(() => !!tournamentMatchId.value)`.
    - Add action `setTournamentContext(params: { tournamentId: string; tournamentMatchId: string; ruleConfigId?: string; ruleSystemName?: string; matchType?: MatchType; playerIds?: string[] })`.
    - In `submitMatchDraft()`: include `ruleConfigId: ruleConfigurationId.value || undefined` in the submission `payload`.
  - [ ] Unit tests in `frontend/src/features/match/stores/__tests__/matchDraftStore.spec.ts`.

- [ ] Task 4: Frontend RulePicker & Match Entry Flow Locking (AC1, AC2)
  - [ ] Update `frontend/src/features/match/components/RulePicker.vue`:
    - Add optional `isLocked` prop with fallback to `draftStore.isTournamentMatch`.
    - When locked:
      - Disable selection interaction on rule chips (`pointer-events-none` / disabled).
      - Add lock icon (`<span class="material-symbols-outlined text-xs">lock</span>`) and "Tournament Rule" badge to the selected chip.
      - Hide "+ Custom Rule" action button.
      - Hide "Set as default" pin button.
      - Render informative banner: "Rule system is locked to tournament settings (FR45)".
      - Apply Clubhouse Design Tokens (use surface distinctions `bg-surface-container-highest`, no `border-*` Tailwind classes).
  - [ ] Update `frontend/src/features/match/components/NewMatchFlow.vue`:
    - Read `route.query.ruleConfigId` alongside `tournamentId` and `tournamentMatchId`.
    - Pass locked state to `RulePicker` and lock `MatchTypePicker`.
    - If tournament context is present but players are not selected, pre-populate players from tournament match.
  - [ ] Update `frontend/src/features/tournament/views/TournamentsView.vue`:
    - In `handleStartMatch(matchId)`: include `ruleConfigId: activeBracketTournament.value.ruleConfiguration.id` in `router.push` query parameters.
  - [ ] Component tests in `frontend/src/features/match/components/__tests__/RulePicker.spec.ts` and `NewMatchFlow.spec.ts`.

- [ ] Task 5: Testing & Local CI Verification (AC1-AC7)
  - [ ] Backend Unit & Slice Tests:
    - `TournamentMatchValidatorTest.java`: test rule mismatch throws `TournamentRuleMismatchException`, test participant mismatch throws conflict, test valid tournament match passes.
    - `MatchServiceTest.java`: test tournament match creation succeeds with matching ruleConfigId; test `rule_config_id` column correctly stored on `Match`.
    - `MatchControllerTest.java` / WebMvc tests.
  - [ ] Frontend Unit/Component Tests:
    - `RulePicker.spec.ts`: test locked state renders lock indicator, hides custom button, ignores click events.
    - `NewMatchFlow.spec.ts`: test tournament context query params initialize locked rules.
    - `matchDraftStore.spec.ts`: test `submitMatchDraft` emits `ruleConfigId` in payload.
  - [ ] E2E Playwright Tests:
    - Create/update `frontend/e2e/tournament-rule-enforcement.spec.ts` (or extend `tournament-async-execution.spec.ts`):
      - Start tournament match from tournament view.
      - Verify match entry opens with rule system locked to tournament configuration.
      - Verify rule chips cannot be toggled.
      - Complete match entry and verify successful submission.
  - [ ] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **Package Layout & Layering (code-1-guide):**
  - Models & Enums: `com.tictactore.model`
  - Repositories: `com.tictactore.repository`
  - Services: `com.tictactore.service` & `com.tictactore.service.tournament`
  - Validators: `com.tictactore.service.tournament`
  - Controllers: `com.tictactore.controller`
  - DTOs: `com.tictactore.dto`
  - Exceptions: `com.tictactore.exception`
- **500-Line Rule (IP-04):**
  - `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` is currently at **494 lines**.
  - Direct addition of tournament validation logic to `MatchServiceImpl` will violate the 500-line rule.
  - Implementation MUST delegate tournament validation to `TournamentMatchValidator` or `TournamentMatchService`.
- **Backward Compatibility for `CreateMatchRequest`:**
  - Over 30 unit, slice, and integration tests across the repository construct `CreateMatchRequest`.
  - Maintain overloaded constructors so existing tests and non-tournament calls remain unaffected when adding `UUID ruleConfigId`.
- **Immutable Rule Configuration (AD-01):**
  - The tournament's `rule_configuration_id` is immutable once the tournament starts.
  - The match's `rule_config_id` must match the tournament's rule configuration ID exactly.
- **Clubhouse Design Tokens (UX-DR3) & No-Line Rule:**
  - For locked and disabled UI components in `RulePicker.vue`, use tonal elevation differences (`bg-surface-container-high`, `bg-surface-container-highest`, `text-on-surface-variant`).
  - Do NOT use `border-*` Tailwind classes.
- **Testing Standards (code-2-test):**
  - Strict AAA pattern: Arrange, Act, Assert separated by single blank lines.
  - Zero structural section comments (`// Given`, `// When`, `// Then` are strictly forbidden).
  - Test classes must end in `Test.java` (unit), `ATDDTest.java` / `IT.java` (integration).

### ATDD Artifacts

- Checklist: `_bmad-output/test-artifacts/atdd-checklist-8-6-tournament-rule-system-enforcement.md`
- Backend DTO & Service ATDD Scaffolds: `_bmad-output/test-artifacts/atdd-redphase-8-6/`
  - `CreateMatchRequestTest.java`
  - `TournamentMatchResponseTest.java`
  - `TournamentMatchValidatorTest.java`
  - `TournamentMatchQueryServiceTest.java`
- Frontend Component & Store ATDD Scaffolds: `_bmad-output/test-artifacts/atdd-redphase-8-6/`
  - `matchDraftStore.spec.ts`
  - `RulePicker.spec.ts`
  - `NewMatchFlow.spec.ts`
- Playwright Tests:
  - `frontend/e2e/tests/api/tournament-rule-enforcement.spec.ts`
  - `frontend/e2e/tournament-rule-enforcement.spec.ts`

### References

- [Source: _bmad-output/planning-artifacts/prd.md#Functional Requirements] (FR44, FR45)
- [Source: _bmad-output/planning-artifacts/epics.md#Story 8.6] (Epic 8, Story 8.6)
- [Source: _bmad-output/implementation-artifacts/8-5-asynchronous-tournament-match-execution.md] (Story 8.5 Intelligence)
- [Source: src/main/java/com/tictactore/service/impl/MatchServiceImpl.java] (Line count check: 494 lines)

## Dev Agent Record

### Agent Model Used
Auto (Antigravity Assistant)

### Debug Log References
- Story validation against `checklist.md` completed.
- Full context enrichment applied.
