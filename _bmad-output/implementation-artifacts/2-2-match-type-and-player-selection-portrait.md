# Story 2.2: Match Type & Player Selection (Portrait)

## 📖 Story Foundation
**User Story:** As a player, I want to select format and participants, So that I can record match result.
**Epic:** Epic 2: Retrospective Match Entry & Rule Systems
**Status:** done

**Acceptance Criteria:**
- **Given** "New Match" tapped on Home Hub
- **When** creation screen opens in portrait (UX-DR1)
- **Then** 2 or 4 slots available
- **And** UI follows "No-Line" rule (UX-DR3)

## 🎯 Developer Context & Guardrails
- **Goal:** Implement the first step of retrospective match entry (Match Type & Player Selection).
- **Trigger:** "New Match" action from Home Hub.
- **Performance:** End-to-end flow <10s; FCP < 1.5s.
- **UX:** Optimized for one-handed mobile use (portrait orientation only, no horizontal scrolling).

### 🏗️ Technical Requirements
- **Data Fetching:** Fetch frequent opponents and last used rule system via API endpoints (e.g., `/api/v1/players/frequent`, `/api/v1/users/preferences`). Do not hardcode or use persistent local storage without backend sync.
- **Smart Defaults:** Auto-select last used rule system and match type (1v1 or 2v2).
- **Player Selection:** Display frequent opponents at the top. Do NOT pre-fill/pre-confirm players (anti-pattern).
- **State Management:** Use `MatchDraft` Pinia store strictly for the ephemeral state of the currently drafted match. Keep it fully isolated from persistent user data.
- **500-Line Rule (IP-04):** No single Vue component or test file may exceed 500 lines. ESLint will fail the build if violated. Split UI into granular sub-components.

### 🏛️ Architecture Compliance
- **Frontend Stack:** Vue 3 `<script setup>` (Composition API), Pinia, SCSS + Tailwind v4.
- **SCSS Prefixing:** All custom SCSS styles MUST use the `ch-` prefix to prevent Tailwind utility class conflicts.
- **Component Reuse:** Utilize existing UI primitives from `src/core/components/` (buttons, lists, toggles) instead of reinventing them in the feature folder.
- **Mobile-First:** Ensure touch targets are sized for mobile devices (min `h-12`).
- **No-Line Rule (UX-DR3):** Use background color shifts for boundaries, absolutely no 1px borders (`border`, `divide-y`, etc. are forbidden).

### 🎨 Tailwind UI Design Specifications (Extracted Context)
To adhere strictly to UX-DR1 and UX-DR3, apply the following Tailwind classes when creating the components:
- **Backgrounds:** The main page uses `bg-background` (#171211).
- **Cards/Containers:** Use `bg-surface-container-low` (#1f1b19) for main list items or groups, and `bg-surface-container-highest` (#393431) for secondary/inner interactive elements (like the 1v1 / 2v2 toggle pill).
- **Typography:**
  - Headlines: `font-headline text-on-surface text-xl md:text-2xl font-bold`
  - Body text: `font-body text-on-surface`
  - Secondary text/subtitles: `text-on-surface-variant`
- **Buttons / Actions:**
  - Primary button: `bg-primary text-background font-bold h-14 rounded-full w-full flex items-center justify-center`
  - Selection slots (players): `h-16 flex items-center px-4 bg-surface-container-highest rounded-xl gap-4 mb-2`
- **No-Line Separation:** Instead of borders between player slots, rely on the `mb-2` gap and the `bg-surface-container-highest` pill shape.

### 📚 Library & Framework Requirements
- Pinia stores must be modular, fully typed with TypeScript, and use the Composition API syntax (`defineStore`).

### 📂 File Structure Requirements
- **Entry Point:** Add "New Match" trigger in `src/views/HomeView.vue`.
- **Components:** Create new components strictly in `src/features/match/components/` (e.g., `MatchTypePicker.vue`, `PlayerSelection.vue`). Use `PascalCase`.
- **Store:** Create or update `src/features/match/stores/matchDraftStore.ts`.

### 🧪 Testing Requirements
- **Unit/Component Tests:** Use **Vitest** for Pinia store logic (pre-filling defaults, toggles) and Vue component mounting.
- **E2E Tests:** Use **Playwright** (NOT Cypress) to test the flow: Home Hub -> New Match -> Type/Player Selection.
- **Mobile Emulation:** Configure Playwright to use a mobile device profile (e.g., iPhone) to verify portrait constraints and responsive layout.
- **Coverage:** Ensure tests run with JaCoCo/coverage tools enabled as per architecture.

## 🔗 Project Context Reference
- **PRD:** Retrospective match entry requirements.
- **UX Design:** Home Hub -> New Match flow, Smart Defaults, Match Type Picker, No-Line rule.
- **Architecture:** `MatchDraft` state in Pinia, feature-based directory structure (`features/match/`), 500-Line Rule.

### ATDD Artifacts
- **Checklist:** `/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-2-2-match-type-and-player-selection-portrait.md`
- **API Tests:** `/Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/api/new-match.spec.ts`
- **E2E Tests:** `/Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/e2e/new-match-creation.spec.ts`

### 🎨 Design Artifacts
- **Design System Spec:** `/Users/ppolukhin/Projects/tic-tac-tore/.stitch/DESIGN.md`
- **Generated UI Layout (HTML):** `/Users/ppolukhin/Projects/tic-tac-tore/.stitch/designs/match-type-and-player-selection.html`

## 🏁 Story Completion Status
- [x] Ultimate context engine analysis completed - comprehensive developer guide created
- [x] Developer implementation completed
- [x] Code review passed
- [x] Ready for testing

### Review Findings
- [x] [Review][Patch] Missing Frontend Data Fetching & Smart Defaults [frontend/src/features/match/stores/matchDraftStore.ts]
- [x] [Review][Patch] Missing Frequent Opponents UI [frontend/src/features/match/components/PlayerSelection.vue]
- [x] [Review][Patch] Hardcoded Backend Data in UserMatchController [src/main/java/com/tictactore/controller/UserMatchController.java]
- [x] [Review][Patch] Missing Unit/Component Tests [frontend/src/features/match]
- [x] [Review][Patch] Reinventing UI Primitives [frontend/src/views/HomeHub.vue]
- [x] [Review][Patch] Incorrect Entry Point File [frontend/src/views/HomeHub.vue]
- [x] [Review][Patch] Use of `force: true` in Playwright E2E tests [frontend/e2e/tests/e2e/new-match-creation.spec.ts]
- [x] [Review][Patch] Race condition in new-match.spec.ts [frontend/e2e/tests/api/new-match.spec.ts]
- [x] [Review][Patch] HomeHub.vue is used as a dumping ground [frontend/src/views/HomeHub.vue]
- [x] [Review][Patch] Weak orientation constraint test [frontend/e2e/tests/e2e/new-match-creation.spec.ts]
- [x] [Review][Patch] Brittle regex cookie scraping in E2E tests [frontend/e2e/tests/e2e/new-match-creation.spec.ts]
- [x] [Review][Patch] Magic string literals in Pinia store [frontend/src/features/match/stores/matchDraftStore.ts]
- [x] [Review][Patch] Empty string allowed as playerId [frontend/src/features/match/stores/matchDraftStore.ts:12-17]
- [x] [Review][Patch] Ephemeral draft state leaks on cancel [frontend/src/views/HomeHub.vue:81]
- [x] [Review][Patch] Start Match button action is silent [frontend/src/views/HomeHub.vue:91-93]
