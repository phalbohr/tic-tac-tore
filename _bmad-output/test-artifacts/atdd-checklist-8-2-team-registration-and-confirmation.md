---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-01T13:42:00+02:00'
workflowType: 'testarch-atdd'
storyId: '8.2'
storyKey: '8-2-team-registration-and-confirmation'
storyFile: '_bmad-output/implementation-artifacts/8-2-team-registration-and-confirmation.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-2-team-registration-and-confirmation.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentRegistrationControllerATDDTest.java'
  - 'frontend/e2e/tournament-registration.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-2/tournamentRegistrationStore.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentRegistrationModal.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentInviteModal.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-2-team-registration-and-confirmation.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/overview.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/api-request.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.2

## Story Context
- **Story Key:** `8-2-team-registration-and-confirmation`
- **Story ID:** `8.2`
- **Title:** Story 8.2: Team Registration & Confirmation
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/8-2-team-registration-and-confirmation.md`

## Acceptance Criteria Summary
1. **AC 1:** Given an open tournament in `REGISTRATION_OPEN` status with mode `ONE_VS_ONE_PERSONAL` or `TWO_VS_TWO_RANDOM_PAIRINGS`, when an authenticated player submits a registration request via `POST /api/v1/tournaments/{tournamentId}/registrations` without a partner, then the backend persists `TournamentRegistration` entity with `partner_id = null` and status `CONFIRMED`, returning `201 Created` (`FR42`).
2. **AC 2:** Given an open tournament in `REGISTRATION_OPEN` status with mode `TWO_VS_TWO_FIXED_TEAMS`, when an authenticated player submits a registration specifying a valid partner (`partnerId`), then the backend creates `TournamentRegistration` with status `PENDING_CONFIRMATION`, emits `TournamentInviteCreatedEvent`, sends a push notification to the partner, and returns `201 Created` (`FR42`, `FR55`).
3. **AC 3:** Given a pending registration (`PENDING_CONFIRMATION`) for a 2v2 fixed teams tournament, when the designated partner accepts via `POST /api/v1/tournaments/{tournamentId}/registrations/{registrationId}/accept`, then status transitions to `CONFIRMED`, `TournamentInviteAcceptedEvent` is emitted, an acceptance push notification is sent to the initiating player, and the team is confirmed (`FR42`, `FR55`).
4. **AC 4:** Given a pending registration (`PENDING_CONFIRMATION`), when the designated partner declines via `POST /api/v1/tournaments/{tournamentId}/registrations/{registrationId}/decline`, then status transitions to `DECLINED`, `TournamentInviteDeclinedEvent` is emitted, a decline push notification is sent, and the slot is freed.
5. **AC 5:** Given an active or pending registration created by the authenticated user, when they withdraw/cancel before registration deadline via `DELETE /api/v1/tournaments/{tournamentId}/registrations/{registrationId}`, then status transitions to `CANCELLED`, slot is freed, and `204 No Content` is returned.
6. **AC 6:** Given registration attempts with invalid constraints (non-existent tournament `404`, tournament not open `409`, deadline passed `400`, max capacity reached `409`, duplicate active registration `409`, invalid/self partner in 2v2 `400`, partner provided for 1v1 `400`, non-partner accepting/declining `403`, unauthorized user cancelling `403`), backend rejects with appropriate HTTP error code and message.
7. **AC 7:** Given an authenticated user querying `GET /api/v1/tournaments/{tournamentId}/registrations`, `GET .../registrations/my`, or `GET /api/v1/tournaments/invitations/pending`, backend returns `200 OK` with registrations roster, user's registration status, or pending invitations list (`FR42`, `FR46`).
8. **AC 8:** Given an authenticated user in frontend opening `TournamentRegistrationModal.vue` or `TournamentInviteModal.vue`, registration/invite flows dispatch requests, toast notifications appear, Pinia store updates, and view refreshes.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, deterministic domain state transitions (`PENDING_CONFIRMATION` -> `CONFIRMED` / `DECLINED` / `CANCELLED`), well-structured REST endpoints, and consistent fullstack conventions in the repository.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Solo Registration (1v1 & 2v2 Random) | API / Slice & E2E | `TournamentRegistrationController` & `tournament-registration.spec.ts` | P0 | 1. `POST /registrations` without partner persists `CONFIRMED`<br>2. Solo registration E2E user journey |
| **AC 2** | 2v2 Fixed Team Invite Dispatch | API / Slice & E2E | `TournamentRegistrationController` & `tournament-registration.spec.ts` | P0 | 1. `POST /registrations` with partner sets `PENDING_CONFIRMATION` and publishes invite event<br>2. Partner search & invite dispatch in modal |
| **AC 3** | Partner Invitation Acceptance | API / Slice & E2E | `TournamentRegistrationController` & `TournamentInviteModal.spec.ts` | P0 | 1. `POST .../accept` by partner transitions to `CONFIRMED`<br>2. Partner receives prompt in UI and accepts team invitation |
| **AC 4** | Partner Invitation Decline | API / Slice & E2E | `TournamentRegistrationController` & `TournamentInviteModal.spec.ts` | P1 | 1. `POST .../decline` transitions to `DECLINED` and frees slot<br>2. Partner declines in UI and inviter can re-register |
| **AC 5** | Withdraw / Cancel Registration | API / Slice & E2E | `TournamentRegistrationController` & `tournament-registration.spec.ts` | P1 | 1. `DELETE .../registrations/{id}` returns 204 and cancels registration<br>2. User cancels from tournament card before deadline |
| **AC 6** | Validation & Capacity / Deadline Guards | API / Integration & E2E | `TournamentRegistrationControllerATDDTest.java` & `tournament-registration.spec.ts` | P0 | 1. 404 on missing tournament<br>2. 400 on passed deadline<br>3. 400 on self-partner in 2v2<br>4. 409 on duplicate active registration<br>5. 403 on non-partner accept/decline<br>6. 403 on unauthorized cancel |
| **AC 7** | Query Registrations & Invitations | API / Slice & Store | `TournamentRegistrationController` & `tournamentRegistrationStore.spec.ts` | P1 | 1. `GET /registrations` returns roster<br>2. `GET /registrations/my` returns status<br>3. `GET /invitations/pending` returns pending invites |
| **AC 8** | Frontend Modals & Real-time Toasts | Component (Vitest) & E2E | `TournamentRegistrationModal.spec.ts`, `TournamentInviteModal.spec.ts`, `tournament-registration.spec.ts` | P0 | 1. 1v1 vs 2v2 modal rendering<br>2. Invite modal accept/decline actions<br>3. Toast notification and store update |
| **UX** | Clubhouse No-Line styling compliance (`UX-DR3`) | E2E (Playwright) | `frontend/e2e/tournament-registration.spec.ts` | P2 | 1. Modal containers use tonal elevation with 0px solid border |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend API Tests:** [`TournamentRegistrationControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentRegistrationControllerATDDTest.java) (11 test scenarios covering solo registration, partner registration, accept, decline, cancel, validations, 403/404/409, queries, security)
- **Frontend E2E Tests:** [`tournament-registration.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tournament-registration.spec.ts) (6 test scenarios marked with `test.skip()`)
- **Frontend Store Tests:** [`tournamentRegistrationStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-2/tournamentRegistrationStore.spec.ts) (6 store test scenarios)
- **Frontend Component Tests:**
  - [`TournamentRegistrationModal.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentRegistrationModal.spec.ts) (5 component test scenarios)
  - [`TournamentInviteModal.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentInviteModal.spec.ts) (3 component test scenarios)
- **Fixtures:** [`tournament-registration-data.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/fixtures/tournament-registration-data.ts)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.2 in `dev-story`:
1. **Task 1 (Database Migration & JPA Entities):**
   - Create Flyway migration `V19__create_tournament_registration_tables.sql` with partial unique indexes.
   - Implement `RegistrationStatus` enum and `TournamentRegistration` entity with optimistic locking (`@Version private Long version;`).
   - Create `TournamentRegistrationRepository` and test `@DataJpaTest`.
2. **Task 2 (Backend DTOs, Events, Push Notifications, Service & Controller):**
   - Move/activate `TournamentRegistrationControllerATDDTest.java` into `src/test/java/com/tictactore/controller/TournamentRegistrationControllerATDDTest.java`.
   - Implement DTOs (`RegisterTournamentRequest`, `TournamentRegistrationResponse`, `MyRegistrationStatusResponse`), events, `PushNotificationService` updates, `TournamentRegistrationNotificationListener`, `TournamentRegistrationService`, `TournamentRegistrationServiceImpl`, and `TournamentRegistrationController`.
   - Verify unit & ATDD controller tests turn GREEN.
3. **Task 3 (Frontend Types, Service, Store & i18n):**
   - Move/activate `tournamentRegistrationStore.spec.ts` into `frontend/src/features/tournament/stores/__tests__/tournamentRegistrationStore.spec.ts`.
   - Update `types/tournament.ts`, create `tournamentRegistrationService.ts`, update `tournamentStore.ts`, and add i18n keys in `en.json` and `de.json`.
   - Verify store tests turn GREEN.
4. **Task 4 (Frontend UI Components & Views):**
   - Move/activate component tests into `frontend/src/features/tournament/components/__tests__/`.
   - Implement `TournamentRegistrationModal.vue`, `TournamentInviteModal.vue`, `TournamentRoster.vue`, and update `TournamentsView.vue`.
   - Verify component tests turn GREEN.
5. **Task 5 (Testing & Quality Verification):**
   - Unskip `frontend/e2e/tournament-registration.spec.ts` (remove `test.skip()`).
   - Run end-to-end and slice tests, then execute `./scripts/ci-local.sh`.
