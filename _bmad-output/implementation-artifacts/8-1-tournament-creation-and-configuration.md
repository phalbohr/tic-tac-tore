---
baseline_commit: c889be5256bccf1ad6a66f2d0c4a41a157409e39
---

# Story 8.1: Tournament Creation & Configuration

Status: ready-for-dev

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As an organizer,
I want to configure tournament parameters (format, mode, rule system, participant limits, registration deadlines, round count, and optional playoff),
so that I can set up a structured competition and invite participants.

## Acceptance Criteria

1. **Given** an authenticated user navigating to the Tournaments section (`/tournaments`) or Home Hub
   **When** they tap the "Create Tournament" action button
   **Then** a dedicated `CreateTournamentModal.vue` dialog opens, presenting a creation form with fields for Tournament Name, Format (`CUP` / `CHAMPIONSHIP`), Mode (`ONE_VS_ONE_PERSONAL`, `TWO_VS_TWO_FIXED_TEAMS`, `TWO_VS_TWO_RANDOM_PAIRINGS`), Rule System selection (fetching existing `RuleConfiguration` presets/custom templates), Min/Max Participants, Registration Deadline (future date/time), Round Count (for championship format), and Playoff Option toggle (`FR41`).
2. **Given** an authenticated user configuring a valid tournament
   **When** they submit the creation form via `POST /api/v1/tournaments`
   **Then** the backend persists the new `Tournament` entity with status `REGISTRATION_OPEN`, associates it with the authenticated creator (`creator_id`), links the specified `rule_configuration_id`, and returns `201 Created` with the complete `TournamentResponse` DTO (`FR41`).
3. **Given** a tournament configuration with format and mode parameters
   **When** the user selects format and mode
   **Then** they can choose:
   - Format: Single Elimination Cup (`CUP`) or Round Robin Championship (`CHAMPIONSHIP`).
   - Mode: 1v1 Personal (`ONE_VS_ONE_PERSONAL`), 2v2 Fixed Teams (`TWO_VS_TWO_FIXED_TEAMS`), or 2v2 Random Pairings (`TWO_VS_TWO_RANDOM_PAIRINGS`).
4. **Given** a user attempting to create a tournament with invalid parameters:
   - `name` is blank or outside 3–100 characters
   - `registrationDeadline` is null or in the past (`<= Instant.now()`)
   - `minParticipants < 2` or `maxParticipants < minParticipants`
   - Mode is `TWO_VS_TWO_FIXED_TEAMS` or `TWO_VS_TWO_RANDOM_PAIRINGS` and `minParticipants < 4`
   - `ruleConfigurationId` does not exist in `RuleConfigurationRepository`
   - Unauthenticated request
   **When** the request is received by `POST /api/v1/tournaments`
   **Then** the backend rejects the request with `400 Bad Request` containing field-level validation error messages, or `401 Unauthorized` if unauthenticated.
5. **Given** an authenticated user requesting tournament listings or details
   **When** they query `GET /api/v1/tournaments` (with optional `status` filter) or `GET /api/v1/tournaments/{id}`
   **Then** the backend returns `200 OK` with the list of tournaments or the single tournament details including rule configuration summary, creator metadata, and registration parameters (`FR41`, `FR46`).
6. **Given** a user successfully submits the tournament creation form in the frontend
   **When** `POST /api/v1/tournaments` returns `201 Created`
   **Then** the modal closes, a success toast notification appears confirming tournament creation, the new tournament is recorded in `useTournamentStore`, and the view updates the tournaments list (`/tournaments`).

## Tasks / Subtasks

- [ ] Task 1: Database Migration & JPA Entities (AC1, AC2, AC3, AC4)
  - [ ] Create Flyway migration `src/main/resources/db/migration/V18__create_tournament_tables.sql`:
    - Create `tournament` table:
      - `id UUID PRIMARY KEY`
      - `name VARCHAR(100) NOT NULL`
      - `format VARCHAR(30) NOT NULL`
      - `mode VARCHAR(30) NOT NULL`
      - `rule_configuration_id UUID NOT NULL REFERENCES rule_configuration(id)`
      - `min_participants INT NOT NULL`
      - `max_participants INT NOT NULL`
      - `registration_deadline TIMESTAMP WITH TIME ZONE NOT NULL`
      - `round_count INT`
      - `has_playoff BOOLEAN NOT NULL DEFAULT FALSE`
      - `status VARCHAR(30) NOT NULL`
      - `creator_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`
      - `created_at TIMESTAMP WITH TIME ZONE NOT NULL`
      - `updated_at TIMESTAMP WITH TIME ZONE`
      - `version BIGINT NOT NULL`
    - Create indexes:
      - `idx_tournament_status ON tournament(status)`
      - `idx_tournament_creator_id ON tournament(creator_id)`
      - `idx_tournament_rule_config_id ON tournament(rule_configuration_id)`
  - [ ] Create Enums in `com.tictactore.model`:
    - `TournamentFormat.java` (`CUP`, `CHAMPIONSHIP`)
    - `TournamentMode.java` (`ONE_VS_ONE_PERSONAL`, `TWO_VS_TWO_FIXED_TEAMS`, `TWO_VS_TWO_RANDOM_PAIRINGS`)
    - `TournamentStatus.java` (`REGISTRATION_OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)
  - [ ] Create Entity `com.tictactore.model.Tournament.java`:
    - Annotations: `@Entity`, `@Table(name = "tournament")`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor(access = AccessLevel.PRIVATE)`
    - Fields: `UUID id`, `String name`, `TournamentFormat format`, `TournamentMode mode`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "rule_configuration_id", nullable = false) RuleConfiguration ruleConfiguration`, `int minParticipants`, `int maxParticipants`, `Instant registrationDeadline`, `Integer roundCount`, `boolean hasPlayoff`, `TournamentStatus status`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "creator_id", nullable = false) User creator`, `@CreationTimestamp OffsetDateTime createdAt`, `@UpdateTimestamp OffsetDateTime updatedAt`, `@Version private Long version;` (no `@Column`, code-1-guide rule 2).
  - [ ] Create Repository `com.tictactore.repository.TournamentRepository.java`:
    - `List<Tournament> findByStatus(TournamentStatus status)`
    - `List<Tournament> findByCreatorId(UUID creatorId)`
    - `List<Tournament> findAllByOrderByCreatedAtDesc()`
  - [ ] Repository test `src/test/java/com/tictactore/repository/TournamentRepositoryTest.java` (`@DataJpaTest`).

- [ ] Task 2: Backend DTOs, Service & Controller (AC2, AC3, AC4, AC5)
  - [ ] Create DTOs in `com.tictactore.dto`:
    - `CreateTournamentRequest.java` record:
      - `@NotBlank @Size(min = 3, max = 100) String name`
      - `@NotNull TournamentFormat format`
      - `@NotNull TournamentMode mode`
      - `@NotNull UUID ruleConfigurationId`
      - `@NotNull @Min(2) Integer minParticipants`
      - `@NotNull @Min(2) Integer maxParticipants`
      - `@NotNull @Future Instant registrationDeadline`
      - `Integer roundCount`
      - `Boolean hasPlayoff`
    - `TournamentResponse.java` record:
      - `UUID id`, `String name`, `TournamentFormat format`, `TournamentMode mode`, `RuleConfigurationResponse ruleConfiguration`, `int minParticipants`, `int maxParticipants`, `Instant registrationDeadline`, `Integer roundCount`, `boolean hasPlayoff`, `TournamentStatus status`, `UUID creatorId`, `String creatorNickname`, `OffsetDateTime createdAt`
  - [ ] Create Service interface and implementation:
    - `com.tictactore.service.TournamentService.java`
    - `com.tictactore.service.TournamentServiceImpl.java`
    - Implement `TournamentResponse createTournament(UUID creatorId, CreateTournamentRequest request)`:
      - Fail-fast parameter validation:
        - `request.minParticipants() <= request.maxParticipants()`
        - If mode is 2v2 (`TWO_VS_TWO_FIXED_TEAMS` or `TWO_VS_TWO_RANDOM_PAIRINGS`), require `request.minParticipants() >= 4`
        - Lookup `User creator` from `UserRepository` (throw `EntityNotFoundException` if missing)
        - Lookup `RuleConfiguration ruleConfig` from `RuleConfigurationRepository` (throw `EntityNotFoundException` if missing)
      - Instantiate and save `Tournament` entity with status `REGISTRATION_OPEN` (capture returned saved instance)
      - Return mapped `TournamentResponse`
    - Implement `TournamentResponse getTournamentById(UUID id)` (`@Transactional(readOnly = true)`)
    - Implement `List<TournamentResponse> listTournaments(TournamentStatus status)` (`@Transactional(readOnly = true)`)
  - [ ] Create Controller `com.tictactore.controller.TournamentController.java`:
    - `@RestController @RequestMapping("/api/v1/tournaments")`
    - `POST /api/v1/tournaments`: `@Valid @RequestBody CreateTournamentRequest request`, `@AuthenticationPrincipal User principal`, returns `201 Created` with `TournamentResponse`.
    - `GET /api/v1/tournaments`: `@RequestParam(required = false) TournamentStatus status`, returns `200 OK` with `List<TournamentResponse>`.
    - `GET /api/v1/tournaments/{id}`: `@PathVariable UUID id`, returns `200 OK` with `TournamentResponse`.
  - [ ] Backend Unit & Controller Tests:
    - `src/test/java/com/tictactore/service/TournamentServiceTest.java` (unit tests using strict AAA pattern, no section comments).
    - `src/test/java/com/tictactore/controller/TournamentControllerTest.java` (WebMvcTest).
    - `src/test/java/com/tictactore/controller/TournamentControllerATDDTest.java` (full request lifecycle).

- [ ] Task 3: Frontend Types, Service, Store & i18n (AC1, AC5, AC6)
  - [ ] Create TypeScript types `frontend/src/features/tournament/types/tournament.ts`:
    - `type TournamentFormat = 'CUP' | 'CHAMPIONSHIP'`
    - `type TournamentMode = 'ONE_VS_ONE_PERSONAL' | 'TWO_VS_TWO_FIXED_TEAMS' | 'TWO_VS_TWO_RANDOM_PAIRINGS'`
    - `type TournamentStatus = 'REGISTRATION_OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'`
    - `interface CreateTournamentPayload`
    - `interface TournamentDto`
  - [ ] Create API service `frontend/src/features/tournament/services/tournamentService.ts`:
    - `createTournament(payload: CreateTournamentPayload): Promise<TournamentDto>`
    - `getTournaments(status?: TournamentStatus): Promise<TournamentDto[]>`
    - `getTournamentById(id: string): Promise<TournamentDto>`
  - [ ] Create Pinia store `frontend/src/features/tournament/stores/tournamentStore.ts`:
    - State: `tournaments: TournamentDto[]`, `currentTournament: TournamentDto | null`, `isLoading: boolean`, `error: string | null`
    - Actions: `createTournament(payload)`, `fetchTournaments(status?)`, `fetchTournamentById(id)`
  - [ ] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json` under `tournament.*` namespace (labels, mode/format options, error messages, toast confirmations).
  - [ ] Frontend store tests in `frontend/src/features/tournament/stores/__tests__/tournamentStore.spec.ts`.

- [ ] Task 4: Frontend UI Components & Home Hub Integration (AC1, AC5, AC6)
  - [ ] Create `frontend/src/features/tournament/components/CreateTournamentModal.vue`:
    - Clubhouse design token styling (`bg-surface-container-low`, `rounded-2xl`, elevation, no 1px solid borders per `UX-DR3`).
    - Input for Tournament Name.
    - Segmented selector for Tournament Format (`CUP` vs `CHAMPIONSHIP`).
    - Segmented selector for Tournament Mode (`1v1 Personal`, `2v2 Fixed Teams`, `2v2 Random Pairings`).
    - Rule System dropdown/selector fetching available presets from `ruleConfigurationService`.
    - Min and Max Participants numeric inputs/steppers with validation feedback.
    - Registration Deadline datetime-local picker (must be future).
    - Round count input and playoff toggle (displayed when format is `CHAMPIONSHIP`).
    - Submit ("Create Tournament") and Cancel buttons with loading state.
  - [ ] Create `frontend/src/features/tournament/views/TournamentsView.vue`:
    - Header with "Tournaments" title and "Create Tournament" action button.
    - Filter tabs (All, Open, In Progress, Completed).
    - List of tournament cards displaying format, mode, participant count, registration deadline, and status badge.
  - [ ] Update Router `frontend/src/router/index.ts`:
    - Add route `{ path: '/tournaments', name: 'tournaments', component: () => import('@/features/tournament/views/TournamentsView.vue') }`.
  - [ ] Update Home Hub `frontend/src/views/HomeView.vue`:
    - Add "Tournaments" button in primary navigation actions to evolve Home Hub for Phase 3 (`UX-DR6`).
    - Keep `HomeView.vue` modular and strictly under 500 lines (`IP-04`).
  - [ ] Component unit tests in `frontend/src/features/tournament/components/__tests__/CreateTournamentModal.spec.ts`.

- [ ] Task 5: Testing & Quality Verification
  - [ ] Backend Unit & Slice Tests:
    - `TournamentServiceTest.java` (strict AAA without section comments).
    - `TournamentControllerTest.java` & `TournamentControllerATDDTest.java`.
    - `TournamentRepositoryTest.java`.
  - [ ] Frontend Unit/Component Tests:
    - `tournamentStore.spec.ts`.
    - `CreateTournamentModal.spec.ts`.
  - [ ] E2E Playwright Tests:
    - Create `frontend/e2e/tournament-creation.spec.ts`:
      - Test 1: Authenticated user navigates to Tournaments -> clicks "Create Tournament" -> fills valid parameters -> submits form -> modal closes, success toast appears, and tournament is listed.
      - Test 2: Form validation prevents submission with blank name or past deadline.
      - Test 3: 2v2 modes require minimum 4 participants.
  - [ ] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **Package Layout & Layering (code-1-guide):**
  - Model: `com.tictactore.model`
  - Repository: `com.tictactore.repository`
  - Service: `com.tictactore.service`
  - Controller: `com.tictactore.controller`
  - DTO: `com.tictactore.dto`
- **Database & Concurrency Invariants:**
  - Flyway migration script: `src/main/resources/db/migration/V18__create_tournament_tables.sql`.
  - `@Version private Long version;` on `Tournament` entity for optimistic locking (without `@Column`, as per `code-1-guide` rule 2).
  - Foreign keys:
    - `rule_configuration_id REFERENCES rule_configuration(id)`
    - `creator_id REFERENCES "user"(id) ON DELETE CASCADE`
- **API Contracts & Security:**
  - Base path: `/api/v1/tournaments`
  - Security: Authenticated requests only. `@AuthenticationPrincipal User principal` for creator identity.
  - Validation:
    - `name`: 3–100 characters, non-blank.
    - `registrationDeadline`: must be strictly in the future (`Instant.now()`).
    - `minParticipants`: >= 2 for 1v1, >= 4 for 2v2 modes.
    - `maxParticipants`: >= `minParticipants`.
    - `roundCount`: required or optional based on `CHAMPIONSHIP` format.
    - `hasPlayoff`: default `false`.
- **UX & Design Invariants:**
  - **Clubhouse Design Tokens (UX-DR3):** Strictly adhere to Clubhouse "No-Line" rule: tonal shifts (`bg-surface-container-low`, `bg-surface-container-high`) and elevation/shadows instead of 1px solid border lines.
  - **Phase 3 Home Hub Evolution:** Add "Tournaments" navigation CTA on `HomeView.vue`.
  - **500-Line Rule (IP-04):** All new files must stay strictly under 500 lines.
- **Testing Standards (code-2-test):**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are strictly forbidden).
  - Integration tests end with `IT` or `ATDDTest` / `Test` for unit tests.

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-8-1-tournament-creation-and-configuration.md`
- **Backend API Scaffolds:** `_bmad-output/test-artifacts/atdd-redphase-8-1/TournamentControllerATDDTest.java`
- **Frontend E2E Scaffolds:** `frontend/e2e/tournament-creation.spec.ts`
- **Frontend Store Scaffolds:** `_bmad-output/test-artifacts/atdd-redphase-8-1/useTournamentStore.spec.ts`
- **Frontend Component Scaffolds:** `_bmad-output/test-artifacts/atdd-redphase-8-1/CreateTournamentModal.spec.ts`
- **Fixtures:** `frontend/e2e/fixtures/tournament-data.ts`

### References

- [Source: _bmad-output/planning-artifacts/prd.md#FR41]
- [Source: _bmad-output/planning-artifacts/epics.md#Epic-8]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md#Tournaments]
- [Source: /Users/ppolukhin/.agents/skills/code-1-guide/SKILL.md]
- [Source: /Users/ppolukhin/.agents/skills/code-2-test/SKILL.md]

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash

### Debug Log References

### Completion Notes List

- Comprehensive story context validated and optimized for dev-story execution.
- Configured exact Flyway migration version `V18__create_tournament_tables.sql`.
- Specified backend model package `com.tictactore.model`, service layer, DTO records, controller endpoints, and frontend Vue/Pinia feature architecture.
- Added strict AAA testing requirements, Playwright E2E test plan, and `./scripts/ci-local.sh` verification gate.

### File List

- `src/main/resources/db/migration/V18__create_tournament_tables.sql` (NEW)
- `src/main/java/com/tictactore/model/TournamentFormat.java` (NEW)
- `src/main/java/com/tictactore/model/TournamentMode.java` (NEW)
- `src/main/java/com/tictactore/model/TournamentStatus.java` (NEW)
- `src/main/java/com/tictactore/model/Tournament.java` (NEW)
- `src/main/java/com/tictactore/repository/TournamentRepository.java` (NEW)
- `src/main/java/com/tictactore/dto/CreateTournamentRequest.java` (NEW)
- `src/main/java/com/tictactore/dto/TournamentResponse.java` (NEW)
- `src/main/java/com/tictactore/service/TournamentService.java` (NEW)
- `src/main/java/com/tictactore/service/TournamentServiceImpl.java` (NEW)
- `src/main/java/com/tictactore/controller/TournamentController.java` (NEW)
- `frontend/src/features/tournament/types/tournament.ts` (NEW)
- `frontend/src/features/tournament/services/tournamentService.ts` (NEW)
- `frontend/src/features/tournament/stores/tournamentStore.ts` (NEW)
- `frontend/src/features/tournament/components/CreateTournamentModal.vue` (NEW)
- `frontend/src/features/tournament/views/TournamentsView.vue` (NEW)
- `frontend/src/router/index.ts` (UPDATE)
- `frontend/src/views/HomeView.vue` (UPDATE)
- `frontend/src/locales/en.json` (UPDATE)
- `frontend/src/locales/de.json` (UPDATE)
- `src/test/java/com/tictactore/service/TournamentServiceTest.java` (NEW)
- `src/test/java/com/tictactore/controller/TournamentControllerTest.java` (NEW)
- `src/test/java/com/tictactore/controller/TournamentControllerATDDTest.java` (NEW)
- `src/test/java/com/tictactore/repository/TournamentRepositoryTest.java` (NEW)
- `frontend/src/features/tournament/stores/__tests__/tournamentStore.spec.ts` (NEW)
- `frontend/src/features/tournament/components/__tests__/CreateTournamentModal.spec.ts` (NEW)
- `frontend/e2e/tournament-creation.spec.ts` (NEW)
