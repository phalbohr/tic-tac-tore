---
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted: ['step-01-detect-mode', 'step-02-load-context', 'step-03-risk-and-testability', 'step-04-coverage-plan', 'step-05-generate-output']
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-16T01:38:52+02:00'
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
  - 'resources/knowledge/risk-governance.md'
  - 'resources/knowledge/probability-impact.md'
  - 'resources/knowledge/test-levels-framework.md'
  - 'resources/knowledge/test-priorities-matrix.md'
  - 'resources/knowledge/nfr-criteria.md'
---

# Step 1: Detect Mode & Prerequisites

**Mode: Epic-Level** (single story, Story 4.3). Confirmed via `sprint-status.yaml` presence (epic-4 active) and the story spec file with acceptance criteria. Prerequisites met: story spec with I/O & edge-case matrix + acceptance criteria, architecture context from implementation code, and existing test coverage baseline (`LeaderboardServiceTest`, `StatisticsControllerTest`, `StatisticsControllerIT`, `LeaderboardView.spec.ts`, `useStatsStore.spec.ts`, `statisticsService.spec.ts`).

# Step 2: Load Context & Knowledge Base

Config: `test_artifacts = _bmad-output/test-artifacts`; `test_design_output = .../test-design`; `risk_threshold = p1`; `tea_use_playwright_utils = true`; `tea_execution_mode = auto`. Stack auto-detected as `fullstack` (pom.xml + package.json + Vue 3 + Playwright). Loaded core knowledge fragments: `risk-governance.md`, `probability-impact.md`, `test-levels-framework.md`, `test-priorities-matrix.md`; extended `nfr-criteria.md` (NFRs in scope: Security, Performance, Reliability, Maintainability).

# Step 3: Testability & Risk Assessment

Testability review (Epic-level): backend aggregation is isolated in `LeaderboardServiceImpl.getPersonalStats` and testable via Mockito (good). However the `/me` controller endpoint is NOT independently testable with the existing `@WithMockUser` harness because it expects `com.tictactore.model.User` principal (the JWT filter sets this type), while `@WithMockUser` injects `org.springframework.security.core.userdetails.User` → principal resolves to null → 401. A custom `SecurityContext` test helper is required. Frontend `StatsDashboard.vue` has no component test at all.

Risk register (P×I): R-001 SEC 6, R-002 PERF 6, R-003 DATA 6, R-004 DATA 4, R-005 BUS 4, R-006 TECH 2, R-007 OPS 2. No score-9 blockers.

# Step 4: Coverage Plan

See final artifact `test-design/test-design-epic-4-3.md`. P0=9, P1=6, P2=6, P3=3 (24 scenarios). Execution: functional tests in every PR (~5-10 min), E2E/perf nightly.

# Step 5: Generate Outputs

**Output:** `_bmad-output/test-artifacts/test-design/test-design-epic-4-3.md`

**Completion Report**
- Mode used: Epic-Level (Phase 4) — Story 4.3 — Positional Statistics (Attack vs Defense)
- Output: `test-design/test-design-epic-4-3.md`
- Key risks: R-001 (SEC, `/me` auth+principal untested; `@WithMockUser` incompatible) · R-002 (PERF, loads all matches) · R-003 (DATA, `period` param silently ignored)
- Gate thresholds: P0=100% pass, P1≥95%, no unmitigated ≥6 risks, backend coverage ≥80% on `getPersonalStats`
- No production code was modified by this workflow.
