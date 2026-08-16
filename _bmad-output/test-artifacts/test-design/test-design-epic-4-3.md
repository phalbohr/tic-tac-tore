---
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted: ['step-01-detect-mode', 'step-02-load-context', 'step-03-risk-and-testability', 'step-04-coverage-plan', 'step-05-generate-output']
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-16'
inputDocuments:
  - '_bmad-output/implementation-artifacts/spec-4-3-positional-statistics-attack-vs-defense.md'
  - '_bmad-output/implementation-artifacts/sprint-status.yaml'
  - '_bmad/tea/config.yaml'
  - 'src/main/java/com/tictactore/controller/StatisticsController.java'
  - 'src/main/java/com/tictactore/dto/PlayerStatsResponse.java'
  - 'src/main/java/com/tictactore/service/LeaderboardService.java'
  - 'src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java'
  - 'src/main/java/com/tictactore/config/SecurityConfig.java'
  - 'src/main/java/com/tictactore/security/JwtAuthenticationFilter.java'
  - 'src/main/java/com/tictactore/model/Match.java'
  - 'src/main/java/com/tictactore/model/Game.java'
  - 'src/main/java/com/tictactore/repository/LeaderboardRepository.java'
  - 'src/test/java/com/tictactore/controller/StatisticsControllerTest.java'
  - 'src/test/java/com/tictactore/controller/StatisticsControllerIT.java'
  - 'src/test/java/com/tictactore/service/LeaderboardServiceTest.java'
  - 'frontend/src/services/statisticsService.ts'
  - 'frontend/src/features/stats/stores/useStatsStore.ts'
  - 'frontend/src/features/stats/components/StatsDashboard.vue'
  - 'frontend/src/features/stats/utils/demoDataGenerator.ts'
  - 'frontend/src/services/__tests__/statisticsService.spec.ts'
  - 'frontend/tests/unit/useStatsStore.spec.ts'
  - '_bmad-output/test-artifacts/test-design/test-design-epic-4.md'
---

# Test Design: Epic 4.3 - Positional Statistics (Attack vs. Defense)

**Date:** 2026-08-16
**Author:** Pavel
**Status:** Draft (Post-Implementation Risk Assessment)

---

## Executive Summary

**Scope:** Epic-level (Story 4.3) test design for the `/statistics/me` personal-statistics endpoint and `StatsDashboard.vue` positional breakdown. The implementation is complete and committed (`26413bb`); `./mvnw test` reports 37 backend tests pass (BUILD SUCCESS) and `vue-tsc --noEmit` is clean. This document assesses residual risk in the changed code and produces a risk-based coverage strategy.

**Risk Summary:**

- Total risks identified: 7
- High-priority risks (≥6): 3 (R-001 SEC, R-002 PERF, R-003 DATA)
- Critical categories: SEC, PERF, DATA

**Coverage Summary:**

- P0 scenarios: 9
- P1 scenarios: 6
- P2 scenarios: 6
- P3 scenarios: 3
- **Total:** 24 scenarios (~16–28 hours, ~2–4 days)

**Working-Tree / Committed Changes Assessed (commit `26413bb`):**

- **Backend:** `PlayerStatsResponse.java` (new DTO record + nested `PositionStatsResponse`, `winRate` 0–100), `LeaderboardService.java` (`getPersonalStats(UUID)` added), `LeaderboardServiceImpl.java` (`getPersonalStats` + `increment`/`withRate`), `StatisticsController.java` (`@GetMapping("/me")` with `@AuthenticationPrincipal User` + null-guard 401).
- **Frontend:** `StatsDashboard.vue` (Overall/Attacker/Defender cards + proportional bars, `ch-` prefix, null-coalescing), `useStatsStore.ts` (fetches `getPersonalStats`, demo-data gating at `<5` overall matches), `statisticsService.ts` (`PlayerStats`/`PositionStats` types, `getPersonalStats` sends `period`).
- **Coverage gaps:** zero backend tests for `getPersonalStats` or the `/me` endpoint; zero frontend component tests for `StatsDashboard.vue`; `period`/`myPosition`/`opponentPosition` parameters typed in the frontend but silently ignored by the backend.

---

## Not in Scope

| Item | Reasoning | Mitigation |
|---|---|---|
| Database-level (`GROUP BY`) aggregation for `/me` | Per spec, in-memory aggregation reused from the `/leaderboard` pattern (MVP scale) | Documented as R-002; load test planned at P3 |
| H2H head-to-head endpoint (`/statistics/h2h`) | Part of Story 4.5 backlog, not 4.3 | Not present in current implementation |
| `period` filtering on `/me` | Defered per spec; backend takes only `@AuthenticationPrincipal` | Flagged as R-003; contract reconciliation is a future task |
| `@PreAuthorize` defense-in-depth on `/me` | Not required by spec; protected by global `anyRequest().authenticated()` | Flagged as R-001 mitigation option |
| Frontend E2E for stats dashboard | Requires full stack + controlled auth state; exceeds PR-cycle budget | Covered by component tests + store tests; E2E at P3 |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---|---|---|---|---|---|---|---|---|
| R-001 | SEC | `/me` authentication & principal resolution is entirely untested. `StatisticsControllerTest` and `StatisticsControllerIT` only cover `/leaderboard`. The `/me` handler injects `@AuthenticationPrincipal com.tictactore.model.User`, but the project's standard test harness uses `@WithMockUser`, which injects `org.springframework.security.core.userdetails.User` — a **different type** — so `AuthenticationPrincipalArgumentResolver` resolves `principal` to `null` and the null-guard returns 401. Any developer writing a MockMvc `/me` test the conventional way gets a false positive on the 401 path and never validates the 200 happy path. Additionally, no test asserts that unauthenticated `/me` returns 401, so the global `anyRequest().authenticated()` protection is unverified; if `SecurityConfig.PUBLIC_ENDPOINTS` is later extended to `/api/v1/statistics/**`, all users' positional stats would leak. (Verified: `JwtAuthenticationFilter` *does* set a `com.tictactore.model.User` principal for real JWT requests, so the endpoint works today — but nothing proves it stays that way.) | 3 | 3 | 9* | *Re-classified: see note. | Backend | Sprint |

**R-001 scoring note:** `JwtAuthenticationFilter` correctly builds `new UsernamePasswordAuthenticationToken(user, null, [ROLE_USER])` with `user` of type `com.tictactore.model.User` (filter lines 72–85), and `SecurityConfig` protects `/api/v1/statistics/**` via `anyRequest().authenticated()`. So the data-leak impact is currently mitigated by config. The residual risk is **testability + regression fragility**: the endpoint has zero coverage and the standard harness is incompatible with it, so a future refactor that breaks principal wiring or loosens `PUBLIC_ENDPOINTS` sails through CI. Probability 3 × Impact (regression → feature fully 401) 2 = **6**, with the config-leak vector contributing to the "likely + impactful" read. Mitigated to 6 (MITIGATE/CONCERNS). | 3 | 2 | 6 | Add custom `SecurityContext` test helper injecting a `com.tictactore.model.User`; P0 API contract tests (401 without auth + 200 with principal + response shape); verify `PUBLIC_ENDPOINTS` never includes `/api/v1/statistics/**`; consider `@PreAuthorize` defense-in-depth. | Backend | Sprint |

| R-002 | PERF | `LeaderboardServiceImpl.getPersonalStats` calls `leaderboardRepository.findConfirmedMatchesWithFilters(null, null, null, null)` — loading **every confirmed match in the database** — then filters to a single user in-memory (`if (!match.isParticipant(userId)) continue;`). This is the same unbounded pattern as `/leaderboard` (already a 4.2 R-003 concern) but worse here: a per-user singleton endpoint that scales its work with total match volume rather than the requesting user's matches. Response time and heap grow linearly with `match_count × games_per_match`; the spec's own residual-risk note acknowledges the `/me` load pattern. Under a `games` collection joined per match, this is an N+1 load risk too. | 2 | 3 | 6 | Add a repository query scoped to the user (`WHERE team_a_attacker_id = :uid OR …`) so only the user's matches load; p95 latency target <500 ms validated by a k6/perf test with a seeded 10k-match dataset (P3); document current MVP limit. | Backend | Epic 4.6 |
| R-003 | DATA | Front/backend contract mismatch: the frontend `PersonalStatsParams` declares `period`, `myPosition`, `opponentPosition`, `page`, `size` (and `statisticsService.spec.ts:115` asserts `period=MONTHLY` is sent to `/statistics/me`), but the backend `StatisticsController.getPersonalStats` takes **only** `@AuthenticationPrincipal` and `LeaderboardServiceImpl.getPersonalStats(UUID)` ignores `period`. The endpoint always returns all-time stats regardless of the UI-selected period — silently incorrect data shown to the user with no error. Spec Review-Triage lists this as a deferred item. | 3 | 2 | 6 | Make the contract explicit: either add `period`/`position` query params to `/me` (and a date-scoped repository overload) **or** remove the unsupported params from the frontend `PersonalStatsParams`/service call; add an integration test asserting period is honored (or that it is intentionally not sent). | Backend | Backlog |

### Medium-Priority Risks (Score 3–5)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
|---|---|---|---|---|---|---|---|
| R-004 | DATA | Inconsistent `winRate` scale across the same API surface. `/me` returns `winRate` on a **0–100** scale (patched per spec, matches frontend `PlayerStats`/`demoDataGenerator`), while `/leaderboard` still returns **0–1** (verified `StatisticsControllerTest:103` asserts `winRate(0.8)` and `LeaderboardView.spec.ts` renders `0.4`/`80.0%`). A single frontend consumer or shared utility could render one endpoint's `0.8` as `0.8%` or the other's `80.0` as `8000%`. | 2 | 2 | 4 | Document the two scales in one place; add a unit test pinning `/me` → 0–100 and `/leaderboard` → 0–1; consider normalizing `winRate` to a single scale in Epic 4.4. | Backend + Frontend |
| R-005 | BUS | `StatsDashboard.vue` rendering is entirely untested — no `StatsDashboard.spec.ts` exists (spec explicitly notes "file did not exist; vue-tsc passes"). The component has four states (loaded with stats, no stats/error → "Unable to load statistics.", loading skeleton, demo-data banner) plus proportional bar widths (`width: Math.min(winRate, 100) + '%'`) and the `?? 0` NaN guard. The patched null-coalescing and the 0-match case (all positions zeroed, `0.0%` bars) are unverified. | 2 | 2 | 4 | Add `StatsDashboard.spec.ts` (P0 + P1) covering all render states, bar-width proportionality, and the 0-stats case. | Frontend |
| R-006 | TECH | `playerName` lookup is decoupled from stats computation and silently degrades. `getPersonalStats` builds the name from `userRepository.findById(userId)` (spec line 190), but loads matches via a separate repository call. If the authenticated principal's `userId` (JWT `sub`) does not map to a row in the `user` table — e.g., the `JwtAuthenticationFilter` name-derivation branch at line 63 produces a UUID from email that was never provisioned — `playerName` becomes `"Unknown"` while positional stats still aggregate. Misleading but not fatal. | 1 | 2 | 2 | Add a unit test for `getPersonalStats` with a non-existent user → `playerName == "Unknown"`, all `PositionStatsResponse.empty()`. | Backend |
| R-007 | OPS | No observability on the new `/me` endpoint. No Micrometer metric, no structured log, no error-rate alert. A regression in `getPersonalStats` (e.g., the NPE class of bug, or the R-002 perf cliff) would manifest only as user complaints. Mirrors the 4.2 `/leaderboard` instrumentation gap. | 2 | 1 | 2 | Add request-count + p95-latency + 5xx meters on `GET /api/v1/statistics/me`; alert on 5xx rate > 1%/5 min. | Backend / Observability |

### Risk Category Legend

- **TECH**: Technical/Architecture (flaws, integration, scalability)
- **SEC**: Security (access controls, auth, data exposure)
- **PERF**: Performance (SLA violations, degradation, resource limits)
- **DATA**: Data Integrity (loss, corruption, inconsistency)
- **BUS**: Business Impact (UX harm, logic errors, revenue)
- **OPS**: Operations (deployment, config, monitoring)

---

## NFR Planning

**Purpose:** Capture Epic-4.3-specific NFR thresholds, planned validation, and evidence for later `nfr-assess`. Not a final evidence audit.

| NFR Category | Requirement / Threshold | Risk Link | Planned Validation | Evidence Needed |
|---|---|---|---|---|
| Security | Unauthenticated `GET /api/v1/statistics/me` returns HTTP 401 (global `anyRequest().authenticated()`) | R-001 | `@WithMockUser`-incompatible: seed real `com.tictactore.model.User` principal via custom `SecurityContext` test helper; assert 401 without auth, 200 with auth | JUnit / MockMvc report |
| Security | Authenticated `GET /me` returns only the requesting user's own stats (no other user's data) | R-001 | Integration test: user U1 cannot see U2's stats via `/me` (endpoint takes principal, not a path param) | JUnit report |
| Performance | `/me` responds p95 < 500 ms at MVP scale (≤50 active players, ≤5k matches) | R-002 | k6/perf test with seeded dataset; also JUnit timing assertion for `getPersonalStats` with mocked repo returning 5k matches | k6 report / JUnit timing |
| Reliability | 0-match user → all positions `empty()` (matches=0, wins=0, losses=0, winRate=0.0), 200 OK, no NPE | – | Unit test `getPersonalStats(noMatches)` + component test for 0-stat dashboard | JUnit report + Vitest report |
| Reliability | Tied match → counted as `totalMatches` only, no win/loss increment (matches existing `getLeaderboard` tie semantics) | – | Unit test with fully-tied game | JUnit report |
| Maintainability | `getPersonalStats` + DTO + controller `/me` ≥ 80% line coverage | – | `./mvnw test` coverage report (JaCoCo) | JaCoCo coverage report |
| Operability | `/me` emits request count + error metrics | R-007 | Add Micrometer meters; verify via `/actuator/metrics` | Metrics/scrape report |

**Unknown thresholds:** p95 latency target not specified in spec (500 ms used as planning assumption). k6 infrastructure for the 10k-match load test is not yet provisioned. Rate-limiting threshold for `/me` not defined (defer to platform-wide effort, as 4.2 did).

---

## Entry Criteria

- [x] Story spec with I/O & edge-case matrix + acceptance criteria (`spec-4-3-...md`)
- [x] Implementation committed (`26413bb`) — Java 21 + Maven backend, Vue 3 + Vitest frontend
- [x] Existing test baseline captured (`LeaderboardServiceTest` 12 tests, `StatisticsControllerTest`/`IT` for `/leaderboard`, `useStatsStore.spec.ts`, `statisticsService.spec.ts`)
- [x] `StatsTestDataFactory` available (H2 test DB, confirmed matches) — reused from 4.2
- [x] CI script: `./scripts/ci-local.sh` (`./mvnw clean verify` + `npm ci` / `type-check` / `build` / `test:unit` / `test:e2e`)

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority (≥6) risks unmitigated
- [ ] `getPersonalStats` line coverage ≥ 80%
- [ ] `StatsDashboard.vue` component coverage ≥ 70% (3 render states + bar proportionality)
- [ ] Security: `/me` 401-without-auth + 200-with-auth proven by integration test
- [ ] `./mvnw test` + `npm run test:unit -- --run` green; `npm run type-check` clean

---

## Test Coverage Plan

> **P0/P1/P2/P3 = priority & risk classification, NOT execution timing.** See "Execution Strategy" for timing.

### P0 (Critical) — Blocks core journey + High risk (≥6) + No workaround

| Test ID | Requirement | Test Level | Risk Link | Owner | Notes | Status |
|---|---|---|---|---|---|---|
| 4.3-UNIT-001 | `getPersonalStats` computes correct per-position wins/losses from CONFIRMED matches; winRate 0–100; overall == attacker+defender in 1v1 | Unit | R-003 | DEV | New — mirrors `LeaderboardServiceTest` factory pattern | NEW |
| 4.3-UNIT-002 | 0-match user → all positions `empty()` (0/0/0/0.0), `playerName` resolved from `userRepository` | Unit | R-006 | DEV | Spec AC: NO_MATCHES | NEW |
| 4.3-UNIT-003 | Fully-tied match counts as `totalMatches` only, no win/loss (consistent with `/leaderboard` tie semantics) | Unit | – | DEV | No existing `getPersonalStats` tie test | NEW |
| 4.3-UNIT-004 | `userIsAttacker`/`userIsDefender`/`userOnTeamA` flags correct across 1v1 (attacker) and 2v2 (attacker+defender) | Unit | R-003 | DEV | Branch coverage for position detection | NEW |
| 4.3-API-001 | `GET /me` without JWT → 401 (Spring Security global rule) | API/IT | R-001 | QA | Requires custom principal helper (see R-001 mitigation) | NEW |
| 4.3-API-002 | `GET /me` with `com.tictactore.model.User` principal → 200 + `PlayerStatsResponse` shape (`playerId`, `playerName`, `overall/attacker/defender` each `matches/wins/losses/winRate`) | API/IT | R-001 | QA | The `@WithMockUser` trap — must inject app `User` principal | NEW |
| 4.3-API-003 | `getPersonalStats` excludes PENDING/PARTIALLY_CONFIRMED/REJECTED matches (only CONFIRMED) | API/IT | R-002 | QA | `findConfirmedMatchesWithFilters` already filters status; assert via seeded non-confirmed match | NEW |
| 4.3-COMP-001 | StatsDashboard renders Overall/Attacker/Defender cards with matches, W/L, and proportional win-rate bar for loaded stats | Component | R-005 | DEV | New — no `StatsDashboard.spec.ts` exists | NEW |
| 4.3-COMP-002 | StatsDashboard 0-match state renders zeroed cards, no NaN, bars at 0% | Component | R-005 | DEV | Spec AC: NO_MATCHES + patched null-coalescing | NEW |

**Total P0:** 9 tests

### P1 (High) — Important features + Medium risk (3–5) + Common workflows

| Test ID | Requirement | Test Level | Risk Link | Owner | Notes | Status |
|---|---|---|---|---|---|---|
| 4.3-API-004 | `GET /me` returns `playerName` from `userRepository` for authenticated user with matches | API/IT | R-006 | QA | Verify name lookup path with seeded user row | NEW |
| 4.3-API-005 | `GET /me` database/repository failure → 500 with generic error (spec BACKEND_ERROR scenario) | API/IT | – | QA | Spec I/O matrix row 4 | NEW |
| 4.3-API-006 | Repository + service filter consistency: `/me` never returns PENDING/REJECTED matches | API/IT | R-002 | QA | Seed a PENDING match for the user; assert excluded | NEW |
| 4.3-COMP-003 | StatsDashboard loading state shows skeleton (`.animate-pulse`), no crash | Component | R-005 | DEV | `v-else-if(isLoading)` branch | NEW |
| 4.3-COMP-004 | StatsDashboard error state ("Unable to load statistics.") when `stats` is null & not loading | Component | R-005 | DEV | `v-else` branch; covers `fetchStats` catch path | NEW |
| 4.3-COMP-005 | Bar width = `min(winRate, 100)%` proportionality + `toFixed(1)` formatting; attacker bar uses `bg-secondary`, defender/overall `bg-primary` | Component | R-004,R-005 | DEV | Verify CSS class + inline width + label text | NEW |

**Total P1:** 6 tests

### P2 (Medium) — Secondary features + Low risk (1–2) + Edge cases

| Test ID | Requirement | Test Level | Risk Link | Owner | Notes | Status |
|---|---|---|---|---|---|---|
| 4.3-UNIT-005 | 2v2 match: user is defender → defender stats increment, attacker stays 0; user is attacker → attacker increments | Unit | R-003 | DEV | `getPlayersForTeam`/position symmetry | NEW |
| 4.3-UNIT-006 | Asymmetric team data (one defender `null`, other set) — participant detection & 1v1 inference unchanged | Unit | – | DEV | `teamADefenderId == null` branch in `getPersonalStats` | NEW |
| 4.3-COMP-006 | Demo-data path renders scaled bars (0–100) in StatsDashboard without NaN | Component | R-004 | DEV | `useStatsStore` demo fallback → `generateDemoData()` 0–100 scale | NEW |
| 4.3-COMP-007 | `useStatsStore.fetchStats` error path sets `stats=null`, `error` set, demo fallback only when demo-enabled | Unit | – | DEV | Carryover from existing store; tighten `statisticsService.spec.ts`-style fetch mock | NEW |
| 4.3-API-007 | `getPersonalStats` user is neither team A nor B attacker/defender but is participant (impossible by `isParticipant` contract) — assert defensive correctness | Unit | – | DEV | Guard against future model changes | NEW |
| 4.3-UNIT-008 | `winRate` rounding: 2/3 → 66.7% (verified by `withRate`); 0 matches → 0.0% not NaN | Unit | R-004 | DEV | Pin 0–100 scale precisely | NEW |

**Total P2:** 6 tests

### P3 (Low) — Nice-to-have + Exploratory + Benchmarks

| Test ID | Requirement | Test Level | Owner | Notes | Status |
|---|---|---|---|---|---|
| 4.3-PERF-001 | `/me` p95 < 500 ms at 10k-seed-match scale (R-002 perf cliff validation) | Perf/E2E | QA | k6 script; validates DB-scoping mitigation | NEW |
| 4.3-SEC-001 | `SecurityConfig.PUBLIC_ENDPOINTS` never includes `/api/v1/statistics/**` (regression guard for R-001 leak) | Unit/Code | Backend | Assert by inspection + CI grep rule | NEW |
| 4.3-E2E-001 | Authenticated user navigates to stats dashboard; sees Overall/Attacker/Defender cards with real (non-demo) data | E2E | QA | Playwright full-stack; nightly | NEW |

**Total P3:** 3 scenarios

---

## Execution Strategy

**Philosophy:** Run all functional tests in PRs unless there is significant infrastructure overhead. With Maven parallel test execution and Vitest's native parallelism, the full functional suite completes in <10 min locally.

### Every PR — Functional Tests (~5–10 min)

- Backend unit: `./mvnw test -Dtest='LeaderboardServiceTest,StatisticsControllerTest'`
- Backend integration: `./mvnw test -Dtest='StatisticsControllerIT'` (seeded H2; requires custom principal helper)
- Frontend unit/component: `npm run test:unit -- --run --match 'stats'` (StatsDashboard + useStatsStore)
- Full suites: `./mvnw clean verify` (expect 277+ backend tests) + `npm run test:unit -- --run`

### Nightly — E2E + Performance (~30 min)

- `npm run test:e2e` — stats dashboard flow with real auth (P3-E2E-001)
- k6 perf on `/me` at 10k matches (P3-PERF-001)
- Security-context regression grep (P3-SEC-001)

### Weekly — Exploratory

- Redis/match-volume scaling drill (R-002)
- Manual review of `/me` response shape against frontend `PlayerStats` contract

---

## Resource Estimates (Ranges Only)

| Priority | Count | Hours/Test | Total Hours | Notes |
|----------|-------|-----------|-------------|-------|
| P0 | 9 | 1.5–2.5 | ~14–23 hrs | New backend unit + API helper for principal injection + 2 component tests |
| P1 | 6 | 1.0–1.8 | ~6–11 hrs | API error/500 paths, integration seeding, bar-proportionality assertions |
| P2 | 6 | 0.5–1.2 | ~3–7 hrs | Edge cases (2v2, asymmetric, demo path) |
| P3 | 3 | 0.3–0.8 | ~1–2 hrs | Perf + e2e + security grep |
| **Total** | **24** | – | **~24–43 hrs** | **~3–6 days (1 QA + 1 DEV)** |

**Assumptions:** effort includes test design, implementation, debugging, CI wiring; excludes ongoing maintenance (~10%). Test infra (Mockito, H2, Vitest, Playwright) established from Stories 4.1–4.2.

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: R-001, R-002, R-003 must be implemented or have approved waivers (P0 tests prove R-001; R-003 mitigation is contract reconciliation)

### Coverage Targets

- **`LeaderboardServiceImpl.getPersonalStats` + `increment`/`withRate`**: ≥80% line coverage
- **`StatisticsController.getPersonalStats`**: 100% (both branches of null-guard)
- **`StatsDashboard.vue`**: ≥70% (3 render states + bar rendering)
- **Security scenarios**: 100% (401 + 200 + no cross-user data)

### Non-Negotiable Requirements

- [ ] All P0 tests pass (9 tests)
- [ ] `/me` 401-without-auth + 200-with-auth proven by integration test (R-001)
- [ ] `getPersonalStats` aggregation correctness verified (1v1 + 2v2 + ties + 0-match)
- [ ] `StatsDashboard` renders all three states without NaN/crash
- [ ] No high-risk (≥6) items unmitigated without waiver
- [ ] `./mvnw clean verify` + `npm run test:unit -- --run` green; `npm run type-check` clean

---

## Mitigation Plans

### R-001: `/me` auth + principal resolution untested; `@WithMockUser` incompatible (Score: 6)

**Mitigation Strategy:**
1. Author a custom `SecurityContext` / `WithMockAppUser` test helper that injects a `com.tictactore.model.User` as the `Authentication` principal (so `@AuthenticationPrincipal User` resolves non-null). *This is the key non-obvious step — the standard `@WithMockUser` cannot do it.*
2. Add `StatisticsControllerTest` → `/me` specs: 401 without principal; 200 with app-`User` principal delegating to mocked `LeaderboardService.getPersonalStats`; verify response shape via `jsonPath`.
3. Add `StatisticsControllerIT` → `/me` spec: authenticated principal + seeded CONFIRMED matches → full `playerId/playerName/overall/attacker/defender` JSON; 401 without auth.
4. Add a CI grep rule (or static check) asserting `SecurityConfig.PUBLIC_ENDPOINTS` never contains `/api/v1/statistics`.
5. (Defense-in-depth) Consider `@PreAuthorize("isAuthenticated()")` on `getPersonalStats` so the contract is explicit at the method level.

**Owner:** Backend
**Timeline:** Sprint
**Status:** Planned
**Verification:** `./mvnw test -Dtest='StatisticsController*Test*,StatisticsControllerIT'` passes; security grep rule green.

### R-002: `getPersonalStats` loads all confirmed matches, filters in-memory (Score: 6)

**Mitigation Strategy:**
1. Add a repository query scoped to the user, e.g. `findConfirmedMatchesForPlayer(UUID userId, …)` with `WHERE m.status='CONFIRMED' AND (team_a_attacker_id=:uid OR team_a_defender_id=:uid OR team_b_attacker_id=:uid OR team_b_defender_id=:uid)`, eliminating the unbounded full-table scan for the `/me` path.
2. k6/perf test with 10k seeded matches → assert p95 < 500 ms (P3-PERF-001).
3. Document the current MVP limit and the migration path in `LeaderboardServiceImpl` javadoc.

**Owner:** Backend
**Timeline:** Epic 4.6
**Status:** Planned
**Verification:** k6 report shows p95 < 500 ms at 10k matches; profiling confirms only the requesting user's matches load.

### R-003: `period`/`myPosition` frontend params silently ignored by `/me` (Score: 6)

**Mitigation Strategy:**
1. Reconcile the contract: either (a) extend `getPersonalStats(UUID, Instant startDate, Instant endDate, String type)` with date-scoping + a repository overload and `@RequestParam` on the controller, **or** (b) remove the unused `period`/`myPosition`/`opponentPosition` from the frontend `PersonalStatsParams`/service call so the client doesn't advertise unsupported filters.
2. Integration test asserting whichever contract is chosen (period honored, or period not sent).

**Owner:** Backend + Frontend
**Timeline:** Backlog
**Status:** Planned
**Verification:** `statisticsService.spec.ts` + `StatisticsControllerIT` assert consistent contract behavior.

---

## Assumptions and Dependencies

### Assumptions

1. `JwtAuthenticationFilter` reliably sets `com.tictactore.model.User` as the authentication principal (verified filter lines 72–85); the `/me` happy path works for real JWT requests today.
2. `StatsTestDataFactory` (from 4.2) supports seeding CONFIRMED matches with attacker/defender IDs and a mix of 1v1/2v2 structures — sufficient for 4.3 `/me` integration tests.
3. Test-database seeding via `MatchRepository`/`UserRepository` supports the positional ID fields (`teamAAttackerId` etc.) used by `isParticipant`.
4. Vitest + Vue Test Utils harness used for `LeaderboardView.spec.ts` (4.2) is reusable for a `StatsDashboard.spec.ts` without framework changes.
5. `./mvnw test` target `StatisticsControllerIT` can inject a custom principal (assumes helper from R-001 mitigation is adopted into the test infra).

### Dependencies

1. **Story 4.1 (empty state + demo data)** — provides `useStatsStore` demo gating and `StatsDashboard` base layout relied on by 4.3.
2. **Story 4.2 (global leaderboard)** — provides `LeaderboardServiceImpl` aggregation patterns (`isPlayerInPosition`, `PlayerStats` private class, `findConfirmedMatchesWithFilters`), `StatsTestDataFactory`, and the `StatisticsController` route scaffolding.
3. **Story 3.x (match lifecycle)** — guarantees only `CONFIRMED` matches are returned by `findConfirmedMatchesWithFilters`, so `/me` cannot expose pending/rejected data by construction.
4. TEA framework infra (Mockito, H2, Vitest, Playwright) — established in Stories 4.1–4.2.

### Risks to Plan

- **Risk:** Custom principal-injection test helper (R-001 mitigation) becomes a shared infra dependency that rots if the JWT filter's principal type changes.
  - **Impact:** `/me` tests silently pass against the wrong principal shape.
  - **Contingency:** Add a `getClass().isAssignableFrom(com.tictactore.model.User.class)` assertion inside the helper; re-evaluate on each filter change.
- **Risk:** k6/perf infra for the 10k-match load test (R-002, P3) not yet provisioned.
  - **Impact:** R-002 stays "documented but unvalidated" at the gate.
  - **Contingency:** Substitute a JUnit timing assertion (load 10k mocked `Match` objects, assert <500 ms) as a fall-back until k6 is ready; flag in `nfr-assess` output.

---

## Interworking & Regression

| Service/Component | Impact | Regression Scope | Validation Steps |
|---|---|---|---|
| `StatisticsController` | New `/me` endpoint added; `/leaderboard` unchanged | Existing `StatisticsControllerTest` (18 tests) + `StatisticsControllerIT` (6 tests) must still pass | `./mvnw test -Dtest='StatisticsControllerTest,StatisticsControllerIT'` |
| `LeaderboardService`/`LeaderboardServiceImpl` | New `getPersonalStats(UUID)`; existing `getLeaderboard` untouched; private `PlayerStats` retained | Existing `LeaderboardServiceTest` (12 tests) must pass | `./mvnw test -Dtest='LeaderboardServiceTest'` |
| `PlayerStatsResponse` (new DTO) | New public record; no existing DTO modified | Jackson serialization covered by new `/me` tests | New + existing `./mvnw test` |
| `SecurityConfig` | Unchanged; `/me` inherits `anyRequest().authenticated()` | Existing security/auth tests must pass | `./mvnw test` (full suite) |
| `StatsDashboard.vue` | Existing component reworked (3 cards vs 2); `ch-` classes added | No existing `StatsDashboard.spec.ts`; ensure no import/side-effect regression in `LeaderboardView.spec.ts` | `npm run test:unit -- --run --match 'stats'` |
| `useStatsStore.ts` | Unchanged (consumes `PlayerStats` shape); demo gating unchanged | Existing `useStatsStore.spec.ts` (5 tests) must pass | `npm run test:unit -- --run` |
| `statisticsService.ts` | `getPersonalStats`/`PlayerStats` unchanged in shape; no new params added | Existing `statisticsService.spec.ts` must pass | `npm run test:unit -- --run` |

**Regression strategy:** full backend (`./mvnw clean verify`) + full frontend (`npm run test:unit -- --run` + `npm run type-check`) must remain green; no production code was modified by this test-design workflow.

---

## Appendix

### Knowledge Base References

- `risk-governance.md` — scoring (≥6 MITIGATE/CONCERNS, =9 BLOCK), ownership & gate rules
- `probability-impact.md` — P 1–3 × I 1–3 = Score 1–9; threshold mapping (6–8→P0/P1, 4–5→P1/P2, 1–3→P2/P3)
- `test-levels-framework.md` — unit (logic) → integration (service/DB) → E2E (critical paths) pyramid
- `test-priorities-matrix.md` — P0 blocks core+high risk+no workaround; P1/P2/P3 ordering
- `nfr-criteria.md` — security/perf/reliability/maintainability validation approach

### Related Documents

- Story Spec: `_bmad-output/implementation-artifacts/spec-4-3-positional-statistics-attack-vs-defense.md`
- Epic Context: `_bmad-output/implementation-artifacts/sprint-status.yaml` (epic-4)
- TEA Config: `_bmad/tea/config.yaml`
- Sibling (4.2) test design: `_bmad-output/test-artifacts/test-design/test-design-epic-4.md`
- Production: `StatisticsController.java`, `PlayerStatsResponse.java`, `LeaderboardServiceImpl.java` (`getPersonalStats` + `increment`/`withRate`)
- Security: `SecurityConfig.java`, `JwtAuthenticationFilter.java`
- Domain: `Match.java` (`isParticipant`), `Game.java` (score fields)
- Existing Tests: `StatisticsControllerTest.java`, `StatisticsControllerIT.java`, `LeaderboardServiceTest.java`, `useStatsStore.spec.ts`, `statisticsService.spec.ts`, `LeaderboardView.spec.ts`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `bmad-testarch-test-design`
**Version**: 5.0 (Step-File Architecture)
