---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-06T21:07:22+02:00'
storyId: '3.5'
storyKey: '3-5-publication-rules-and-24-hour-cooldown'
storyFile: '{project-root}/_bmad-output/implementation-artifacts/spec-3-5-publication-rules-and-24-hour-cooldown.md'
atddChecklistPath: '{test_artifacts}/atdd-checklist-3-5-publication-rules-and-24-hour-cooldown.md'
generatedTestFiles:
  - '{project-root}/src/test/java/com/tictactore/service/MatchCooldownRedPhaseTest.java'
  - '{project-root}/frontend/src/features/match/components/__tests__/CooldownTimer.spec.ts'
inputDocuments:
  - '{project-root}/_bmad-output/implementation-artifacts/spec-3-5-publication-rules-and-24-hour-cooldown.md'
  - '{project-root}/_bmad-output/test-artifacts/test-design-story-3-5.md'
  - '{project-root}/_bmad/tea/config.yaml'
---

# ATDD Checklist: Story 3.5 — Publication Rules & 24-hour Cooldown

## TDD Red Phase (Current)

✅ Red-phase test scaffolds generated

- Backend API Tests: 7 tests (all disabled with `@Disabled`)
- Frontend Component Tests: 4 tests (all skipped with `test.skip()`)

## Acceptance Criteria Coverage

| AC | Description | Backend | Frontend | Status |
|----|-------------|---------|----------|--------|
| AC1 | 2v2 standard first confirm → PARTIALLY_CONFIRMED + cooldownExpiresAt set | ✅ `MatchCooldownRedPhaseTest` | — | Covered |
| AC2 | Second confirm during cooldown → CONFIRMED + cooldown cleared | ✅ `MatchCooldownRedPhaseTest` | — | Covered |
| AC3 | Cooldown expires → CONFIRMED automatically via scheduled job | ✅ `MatchCooldownRedPhaseTest` | — | Covered |
| AC4 | 1v1, 2v2 RANDOM, 2v2 REFEREE → no cooldown set | ✅ `MatchCooldownRedPhaseTest` | — | Covered |
| AC5 | Double confirmation → idempotent, no state change | ✅ `MatchCooldownRedPhaseTest` | — | Covered |
| AC6 | Frontend countdown timer renders remaining hours/minutes | — | ✅ `CooldownTimer.spec.ts` | Covered |

## Generated Test Files

### Backend (JUnit 5 + Mockito + AssertJ)

**File:** `src/test/java/com/tictactore/service/MatchCooldownRedPhaseTest.java`

| Test | Priority | AC | Description |
|------|----------|----|-------------|
| `red_2v2StandardFirstConfirm_sets24hCooldown` | P0 | AC1 | Verifies `cooldownExpiresAt` is set ~24h when 2v2 standard first opponent confirms |
| `red_secondConfirmDuringCooldown_clearsCooldownAndConfirms` | P0 | AC2 | Verifies `cooldownExpiresAt` is cleared on second confirmation before expiry |
| `red_scheduledJob_autoPublishesExpiredCooldown` | P0 | AC3 | Verifies `MatchCooldownService.processExpiredCooldowns()` auto-transitions to CONFIRMED |
| `red_1v1Participant_noCooldownSet` | P0 | AC4 | Verifies 1v1 participant confirms without cooldown |
| `red_2v2Random_noCooldownSet` | P0 | AC4 | Verifies 2v2 RANDOM confirms without cooldown |
| `red_2v2Referee_noCooldownSet` | P0 | AC4 | Verifies 2v2 REFEREE confirms without cooldown |
| `red_doubleConfirm_returnsCurrentStateWithoutError` | P0 | AC5 | Verifies idempotent double confirmation |

### Frontend (Vitest + Vue Test Utils)

**File:** `frontend/src/features/match/components/__tests__/CooldownTimer.spec.ts`

| Test | Priority | AC | Description |
|------|----------|----|-------------|
| `Should render cooldown timer text for PARTIALLY_CONFIRMED match with future expiry` | P0 | AC6 | Verifies countdown text renders for active cooldown |
| `Should render "Auto-publishing soon" when cooldownExpiresAt is in the past` | P0 | AC6 | Verifies expired-cooldown messaging |
| `Should hide cooldown timer when cooldownExpiresAt is absent` | P0 | AC6 | Verifies timer is absent when no cooldown |
| `Should display partial confirmation badge text` | P0 | AC6 | Verifies "X of Y confirmed" badge renders |

## Implementation Checklist

### Backend

- [x] `src/main/java/com/tictactore/TicTacToreApplication.java` — Add `@EnableScheduling`
- [x] `src/main/java/com/tictactore/model/Match.java` — Add `cooldownExpiresAt` Instant field; update `confirmByOpponent()` to set/clear cooldown; add `isInCooldown()`, `isCooldownExpired()`, `publishAfterCooldown()`
- [x] `src/main/java/com/tictactore/rules/VerificationRules.java` — Add `requiresCooldown(Match)` returning true only for 2v2 + STANDARD + PARTICIPANT
- [x] `src/main/java/com/tictactore/service/operation/MatchOperation.java` — No signature changes; delegate to updated `Match.confirmByOpponent()`
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` — No structural changes needed if logic lives in entity; verify `getPendingMatches()` behavior
- [x] `src/main/java/com/tictactore/service/MatchCooldownService.java` — Create `@Scheduled` service with `processExpiredCooldowns()` method
- [x] `src/main/java/com/tictactore/service/PushNotificationService.java` + `impl/PushNotificationServiceImpl.java` — Add `sendCooldownReminderNotification()` (optional, not yet wired)
- [x] `src/main/java/com/tictactore/dto/MatchResponse.java` — Add `cooldownExpiresAt` field
- [x] `src/main/java/com/tictactore/repository/MatchRepository.java` — Add `findByCooldownExpiresAtBeforeAndStatus(Instant, String)` query method
- [x] `src/main/resources/db/migration/V8__add_cooldown_expires_at.sql` — Add `cooldown_expires_at` column with `TIMESTAMP WITH TIME ZONE`

### Backend Tests

- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` — Add cooldown set/clear idempotency tests
- [x] `src/test/java/com/tictactore/rules/VerificationRulesTest.java` — Add `requiresCooldown()` context tests
- [x] `src/test/java/com/tictactore/service/MatchCooldownServiceTest.java` — Unit tests for scheduled transition with fixed clock
- [x] `src/test/java/com/tictactore/service/MatchCooldownRedPhaseTest.java` — ATDD red-phase scaffolds (this run)

### Frontend

- [x] `frontend/src/features/match/composables/usePendingMatches.ts` — Expose `cooldownExpiresAt` and local countdown; update `confirmOpponent()` to handle cooldown in response
- [x] `frontend/src/features/match/components/PendingMatches.vue` — Display formatted countdown timer for PARTIALLY_CONFIRMED matches

### Frontend Tests

- [x] `frontend/src/features/match/composables/__tests__/usePendingMatches.spec.ts` — Cooldown countdown tests
- [x] `frontend/src/features/match/components/__tests__/PendingMatches.spec.ts` — Cooldown timer display tests
- [x] `frontend/src/features/match/components/__tests__/CooldownTimer.spec.ts` — ATDD red-phase scaffolds (this run)

## Next Steps (Task-by-Task Activation)

During implementation of each task:

1. Remove `@Disabled` from the current backend test method or `test.skip()` from the current frontend test
2. Run tests: `./mvnw test` (backend) or `npm run test:unit -- --run` (frontend)
3. Verify the activated test fails first (if implementation absent), then passes after implementation (green phase)
4. If any activated tests still fail unexpectedly:
   - Either fix implementation (feature bug)
   - Or fix test (test bug)
5. Commit passing tests

## Feature Coverage Summary

| Layer | File | Tests | Status |
|-------|------|-------|--------|
| Backend Unit | `MatchCooldownRedPhaseTest.java` | 7 disabled | RED phase scaffolds |
| Frontend Component | `CooldownTimer.spec.ts` | 4 skipped | RED phase scaffolds |
| Backend Active | `MatchCooldownServiceTest.java` | 5 active | GREEN phase |
| Backend Active | `MatchServiceTest.java` | cooldown ACs | GREEN phase |
| Frontend Active | `usePendingMatches.spec.ts` | cooldown tests | GREEN phase |
| Frontend Active | `PendingMatches.spec.ts` | timer tests | GREEN phase |

## Key Risks & Assumptions

- **R-001**: Scheduled job race with manual confirmation — mitigated by `findByCooldownExpiresAtBeforeAndStatus` filter and `publishAfterCooldown()` status guard.
- **R-002**: Client-side countdown clock skew — mitigated by computing remaining from server-provided `cooldownExpiresAt`.
- **Assumption**: 24-hour cooldown is hardcoded (deferred as DW-41). Tests hardcode `plusSeconds(24 * 60 * 60)` to match current behavior.
- **Assumption**: `sendCooldownReminderNotification` is dead code until wired (deferred as DW-40). Not covered in ATDD scaffolds.

## Completion Summary

- **Total Tests Generated**: 11 (7 backend + 4 frontend)
- **All Tests Skipped/Disabled**: Yes (TDD RED PHASE)
- **Acceptance Criteria Covered**: AC1–AC6 (100%)
- **Test Artifacts Directory**: `{test_artifacts}`
- **Story File**: `{story_file}`
- **Next Workflow**: `dev-story` for implementation activation; `automate` after green phase
