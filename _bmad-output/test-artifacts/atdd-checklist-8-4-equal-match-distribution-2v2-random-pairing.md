---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-02T16:15:00+02:00'
workflowType: 'testarch-atdd'
storyId: '8.4'
storyKey: '8-4-equal-match-distribution-2v2-random-pairing'
storyFile: '_bmad-output/implementation-artifacts/8-4-equal-match-distribution-2v2-random-pairing.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-4-equal-match-distribution-2v2-random-pairing.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-4/RandomPairingBracketGeneratorTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-4/StubPartnerSelectorTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-4/TournamentAccountDeletionHandlerTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-4/TournamentMatchRepositoryATDDTest.java'
  - 'frontend/e2e/tests/api/tournament-random-pairing.spec.ts'
  - 'frontend/e2e/tournament-random-pairing.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-4/TournamentMatchCard.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-4-equal-match-distribution-2v2-random-pairing.md'
  - '.agent/skills/bmad-testarch-atdd/resources/tea-index.csv'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.4

## Story Context
- **Story Key:** `8-4-equal-match-distribution-2v2-random-pairing`
- **Story ID:** `8.4`
- **Title:** Story 8.4: Equal Match Distribution (2v2 Random Pairing)
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/8-4-equal-match-distribution-2v2-random-pairing.md`

## Acceptance Criteria Summary
1. **AC 1 (Algorithm & Equal Distribution):** In open tournaments with mode `TWO_VS_TWO_RANDOM_PAIRINGS` ($N \ge 4$), the tournament start routine calculates match schedules using `RandomPairingBracketGenerator` (Whist / Social Golfer algorithm) guaranteeing:
   - Each participant plays an exact equal number of matches $M$.
   - Every match is a 2v2 contest referencing individual `TournamentRegistration` entries.
   - Partner repetition is minimized (maximizing unique teammate pairings).
   - Opponent encounters are uniformly distributed.
   - Deterministic and reproducible schedule generation seeded with tournament ID/seed (`FR47`).
2. **AC 2 (Entity & 4-Player Match Persistence):** Persisted `TournamentMatch` entity records all 4 distinct participants (`participant1`, `participant1Partner`, `participant2`, `participant2Partner`), their seeds, and initial `PENDING`/`READY` status (`FR47`).
3. **AC 3 (Stub Partner Selection & Deletion Protocol):** On player deletion or withdrawal during an active 2v2 random pairing tournament:
   - System selects a stub partner from active participants with closest frozen `strength_score` (`FR33`).
   - Deterministic tie-breaking using registration ID.
   - Match slot is updated with stub partner and flagged as `isParticipant1Stub = true` or `isParticipant2Stub = true`.
   - `TournamentStubPartnerAssignedEvent` is published, and push notifications sent to affected teammate and stub partner.
4. **AC 4 (Stub Statistics Isolation & Knockout Safety):** Stub partner's extra substitute match applies to the active team's tournament standings, but does NOT count toward the stub's own individual statistics/standings. In knockout format, the stub cannot be eliminated from their own bracket branch (`FR33`).
5. **AC 5 (Individual Confirmation):** In 2v2 random pairing mode, both opponents confirm individually (`FR14`).
6. **AC 6 (DTO & UI Exposure):** Authenticated bracket, schedule, and match queries return `participant1Partner`, `participant2Partner`, `isParticipant1Stub`, `isParticipant2Stub`, allowing `TournamentMatchCard.vue` to render 4-player rosters and `(Stub)` badges (`FR46`, `FR47`).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, deterministic domain algorithms (Whist / Social Golfer matrix, strength-based stub selection), well-defined REST schemas, and strict architectural alignment with `code-1-guide` and `code-2-test`.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Whist Equal Match Distribution | Unit / Domain | `RandomPairingBracketGeneratorTest.java` | P0 | 1. Equal matches per player for $N=4, 6, 8, 12, 16$<br>2. 4 distinct players per match<br>3. Zero partner repetition for $N=8$<br>4. Deterministic seeding reproducibility<br>5. Exception when $N < 4$ |
| **AC 2** | 4-Player Match Persistence & Queries | Integration (@DataJpaTest) | `TournamentMatchRepositoryATDDTest.java` | P1 | 1. Persist 4 participants and stub flags<br>2. `findByAnyParticipantRegistrationId` matches primary and partner slots |
| **AC 3** | Stub Partner Selection & Deletion Handler | Unit & Integration | `StubPartnerSelectorTest.java`, `TournamentAccountDeletionHandlerTest.java` | P0 | 1. Closest frozen `strengthScore` selection<br>2. Deterministic tie-breaking by UUID<br>3. Exclusion of current match partner<br>4. Replace slot, set stub flag, publish event |
| **AC 4** | Stub Stats Isolation & Knockout Immunity | Unit / Service | `TournamentStandingsServiceTest.java` | P1 | 1. Standings update excludes stub substitute matches from stub's personal record<br>2. Knockout elimination protection |
| **AC 5** | Individual Match Confirmation | Unit / Service | `VerificationRulesTest.java` | P1 | 1. Both opponents confirm individually for 2v2 random mode |
| **AC 6** | REST DTOs & Match Card UI | Slice (WebMvcTest), Component (Vitest) & E2E (Playwright) | `TournamentBracketControllerTest.java`, `TournamentMatchCard.spec.ts`, `tournament-random-pairing.spec.ts` | P0 | 1. GET /matches returns partners and stub flags<br>2. `TournamentMatchCard.vue` renders `${P1} & ${P1Partner} vs ${P2} & ${P2Partner}`<br>3. `TournamentMatchCard.vue` renders `(Stub)` badge |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend Unit Tests (Whist Algorithm):** [`RandomPairingBracketGeneratorTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-4/RandomPairingBracketGeneratorTest.java)
- **Backend Unit Tests (Stub Selection):** [`StubPartnerSelectorTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-4/StubPartnerSelectorTest.java)
- **Backend Service Tests (Deletion Handler):** [`TournamentAccountDeletionHandlerTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-4/TournamentAccountDeletionHandlerTest.java)
- **Backend Repository Slice Tests:** [`TournamentMatchRepositoryATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-4/TournamentMatchRepositoryATDDTest.java)
- **Frontend API Tests (Playwright):** [`frontend/e2e/tests/api/tournament-random-pairing.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/api/tournament-random-pairing.spec.ts) (marked with `test.skip()`)
- **Frontend E2E Tests (Playwright):** [`frontend/e2e/tournament-random-pairing.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tournament-random-pairing.spec.ts) (marked with `test.skip()`)
- **Frontend Component Tests (Vitest):** [`_bmad-output/test-artifacts/atdd-redphase-8-4/TournamentMatchCard.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-4/TournamentMatchCard.spec.ts)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.4 in `bmad-dev-story`:
1. **Task 1: Database Migration & Entity Enhancements (AC1, AC2, AC3)**
   - Run Flyway migration `V21__add_partners_and_stubs_to_tournament_match.sql`.
   - Update `TournamentMatch` entity with `participant1Partner`, `participant2Partner`, `isParticipant1Stub`, `isParticipant2Stub`.
   - Update `TournamentMatchRepository` with `findByAnyParticipantRegistrationId`.
   - Integrate and verify `TournamentMatchRepositoryATDDTest.java`.
2. **Task 2: 2v2 Random Pairing Algorithm & Bracket Generator (AC1, AC2, AC5)**
   - Implement `RandomPairingBracketGenerator.java` (Whist scheduling matrix).
   - Wire routing in `TournamentLifecycleServiceImpl.java`.
   - Move and activate `RandomPairingBracketGeneratorTest.java` into `src/test/java/com/tictactore/service/tournament/`.
   - Verify unit tests turn GREEN.
3. **Task 3: Stub Partner Selection & Account Deletion Protocol (AC3, AC4)**
   - Implement `StubPartnerSelectorImpl.java` and `TournamentAccountDeletionHandlerImpl.java`.
   - Create `TournamentStubPartnerAssignedEvent.java`.
   - Wire `TournamentNotificationListener.java`.
   - Move and activate `StubPartnerSelectorTest.java` and `TournamentAccountDeletionHandlerTest.java` into `src/test/java/com/tictactore/service/tournament/`.
   - Verify unit tests turn GREEN.
4. **Task 4: DTOs, Query Service & Controller Updates (AC2, AC6)**
   - Update `TournamentMatchResponse.java` and `TournamentMatchQueryServiceImpl.java`.
   - Verify controller slice tests turn GREEN.
5. **Task 5: Frontend Types, Components & i18n (AC6)**
   - Update `frontend/src/features/tournament/types/tournament.ts`.
   - Update `TournamentMatchCard.vue` with 4-player display and stub badges.
   - Activate `TournamentMatchCard.spec.ts` in Vitest.
   - Remove `test.skip()` in `frontend/e2e/tests/api/tournament-random-pairing.spec.ts` and `frontend/e2e/tournament-random-pairing.spec.ts`.
6. **Task 6: Verification & CI**
   - Execute `./scripts/ci-local.sh` and ensure 100% pass rate.
