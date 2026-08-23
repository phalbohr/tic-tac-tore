---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-23T15:15:37+02:00'
storyId: '6.1'
storyKey: '6-1-named-player-groups-teams'
storyFile: '_bmad-output/implementation-artifacts/6-1-named-player-groups-teams.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-6-1-named-player-groups-teams.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-6-1/PlayerGroupControllerATDDTest.java'
  - 'frontend/e2e/player-groups.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-1/usePlayerGroupStore.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-1/PlayerGroupModal.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/6-1-named-player-groups-teams.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist: Story 6.1

## Story Context
- **Story Key:** `6-1-named-player-groups-teams`
- **Story ID:** `6.1`
- **Title:** Story 6.1: Named Player Groups ("Teams")
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/6-1-named-player-groups-teams.md`

## Acceptance Criteria Summary
1. **AC 1:** Given an authenticated player creating a match (`/matches/new`) or managing preferences in Profile Settings (`/cabinet`), when they create a new player group with a name and selected player IDs (or update an existing group), then the system persists the group associated with the creator (`creatorId`), names are unique per creator (1–50 characters, trimmed), and a built-in "Favorites" group is provided and supported (FR39).
2. **AC 2:** Given an authenticated user, when querying their player groups via `GET /api/v1/player-groups`, then the system returns only groups created by the authenticated user with safe member summaries without email/PII (per `AD-04`), strictly isolated between users.
3. **AC 3:** Given a player setting up a match in portrait mode (`/matches/new`), when selecting players for 1v1 or 2v2 slots, then the player selector offers inline access to player groups ("Favorites" and custom groups) to quickly filter or populate players, and allows creating a new group inline via `PlayerGroupModal.vue` without losing active match draft state.
4. **AC 4:** Given a player viewing Unified Match History (`/matches` or `/history`), when interacting with filter controls (`MatchFilterChips.vue`), then player groups are available as filter chips to filter match history to games involving members of the group.
5. **AC 5:** Given an authenticated user, when attempting to update (`PUT /api/v1/player-groups/{id}`) or delete (`DELETE /api/v1/player-groups/{id}`) a group created by another user, then the system rejects the operation with `403 Forbidden`.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, well-defined REST contracts, and established component and testing patterns across Epics 1–5.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Persist group with unique name (1-50 chars), members, favorite flag | API / Integration | `PlayerGroupController` & `PlayerGroupService` | P0 | 1. `POST /api/v1/player-groups` returns 201 Created with persisted entity<br>2. Validation: reject blank name or name >50 chars with 400 Bad Request<br>3. Duplicate name check per creator |
| **AC 1** | Manage player groups in Profile Settings (`/cabinet`) | E2E (Playwright) | `frontend/e2e/player-groups.spec.ts` & `Cabinet.vue` | P0 | 1. Open `/cabinet`, list existing player groups<br>2. Click create group, submit modal, verify group added to list |
| **AC 2** | User-isolated group queries & PII masking (`AD-04`) | API / Integration | `PlayerGroupController` & `PlayerGroupService` | P0 | 1. `GET /api/v1/player-groups` returns only groups created by user<br>2. Members returned as `PlayerSummaryDto` (id, nickname, avatar) without email/oauth IDs |
| **AC 2** | Pinia store state management for groups | Unit (Vitest) | `usePlayerGroupStore.ts` | P0 | 1. `fetchGroups()` populates state, `favoriteGroup` & `customGroups` getters<br>2. `createGroup()`, `updateGroup()`, `deleteGroup()` mutate store state |
| **AC 3** | Inline player group selection & creation in `/matches/new` | E2E (Playwright) | `PlayerSelection.vue` & `PlayerGroupModal.vue` | P0 / P1 | 1. Display Favorites and custom group chips above player slots<br>2. Selecting group filters available players<br>3. Inline modal creation preserves active match draft state |
| **AC 4** | Filter Unified Match History by player group chips | E2E (Playwright) / Unit | `MatchFilterChips.vue` & `useMatchHistoryStore.ts` | P1 | 1. Player groups rendered as filter chips on `/matches`<br>2. Clicking group chip updates query with `groupId` and refetches |
| **AC 5** | Ownership isolation (`403 Forbidden` on foreign groups) | API / Integration | `PlayerGroupController` & `PlayerGroupService` | P0 | 1. `PUT /api/v1/player-groups/{id}` on another user's group returns 403<br>2. `DELETE /api/v1/player-groups/{id}` on another user's group returns 403 |
| **UX** | Clubhouse No-Line styling compliance (`UX-DR3`) | E2E (Playwright) | `frontend/e2e/player-groups.spec.ts` | P2 | 1. Verify list container has 0px hard border dividers |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend API Tests:** [`PlayerGroupControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-1/PlayerGroupControllerATDDTest.java) (8 test cases across CRUD, isolation, validation)
- **Frontend E2E Tests:** [`player-groups.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/player-groups.spec.ts) (5 test scenarios marked with `test.skip()`)
- **Frontend Store Tests:** [`usePlayerGroupStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-1/usePlayerGroupStore.spec.ts) (6 store test cases)
- **Frontend Component Tests:** [`PlayerGroupModal.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-1/PlayerGroupModal.spec.ts) (4 modal component test cases)

## Next Steps (Task-by-Task Activation)

During implementation of Story 6.1 in `dev-story`:
1. **Task 1 (DB & Entity):** Implement Flyway migration `V10__create_player_group_tables.sql`, `PlayerGroup` entity, and `PlayerGroupRepository`.
2. **Task 2 (Backend Service & Controller):** Activate `PlayerGroupControllerATDDTest.java` and implement `PlayerGroupService` and `PlayerGroupController` with ownership isolation and PII masking.
3. **Task 3 (Frontend Store & Service):** Move and activate `usePlayerGroupStore.spec.ts` into `frontend/src/features/group/stores/__tests__/` and implement `playerGroupService.ts` and `usePlayerGroupStore.ts`.
4. **Task 4 (Frontend UI & Inline UX):** Move and activate `PlayerGroupModal.spec.ts` into `frontend/src/features/group/components/__tests__/`, implement `PlayerGroupModal.vue`, and update `Cabinet.vue`, `PlayerSelection.vue`, and `MatchFilterChips.vue`.
5. **Task 5 (E2E & Verification):** Activate `frontend/e2e/player-groups.spec.ts` (remove `test.skip()`), verify 100% pass, and run `./scripts/ci-local.sh`.
