# Story 1.7: Onboarding Tutorial

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a new user, I want an onboarding tutorial, so that I can learn how to use the app.

## Acceptance Criteria

1. **Given** a newly registered user logs in for the first time  
   **When** the Home Hub renders  
   **Then** the system triggers a dismissible onboarding tutorial overlay explaining core features (FR56)

2. **Given** the onboarding tutorial overlay is displayed  
   **When** the user taps/clicks "Skip" or completes all slides ("Done" on the last slide)  
   **Then** the overlay closes and does not reappear on subsequent logins or page reloads

3. **Given** a returning user who has previously dismissed the tutorial  
   **When** the Home Hub renders  
   **Then** the onboarding tutorial overlay is NOT shown

## Tasks / Subtasks

- [ ] Task 1: Create `onboarding` Pinia store (AC: #2, #3)
  - [ ] Create `frontend/src/stores/onboarding.ts` with `hasSeen` ref initialized from `localStorage.getItem('ttt_onboarding_seen')`
  - [ ] Implement `markSeen()` action: sets `hasSeen = true` and calls `localStorage.setItem('ttt_onboarding_seen', '1')`
  - [ ] Export `useOnboardingStore`

- [ ] Task 2: Create `OnboardingTutorial.vue` component (AC: #1, #2)
  - [ ] Place at `frontend/src/features/profile/OnboardingTutorial.vue` (first file establishing `features/profile/` directory)
  - [ ] Implement full-screen overlay with semi-transparent backdrop (Tailwind: `fixed inset-0 bg-black/50 z-50`)
  - [ ] Implement 3-slide carousel covering: (1) Record a match, (2) Confirm together, (3) Track your stats
  - [ ] Add slide navigation: dot indicators + Prev/Next buttons
  - [ ] "Skip" button always visible (top-right); "Done" button replaces "Next" on last slide
  - [ ] Emit `close` event on Skip click and on Done click
  - [ ] Keep each slide to ≤3 lines of text + one icon/emoji — no forms, no required input (UX anti-pattern: "complex onboarding before value delivery")
  - [ ] Mobile-first layout, portrait orientation, thumb-zone CTA placement

- [ ] Task 3: Integrate tutorial into `HomeHub.vue` (AC: #1, #3)
  - [ ] Import `useOnboardingStore` and `OnboardingTutorial`
  - [ ] Add `<OnboardingTutorial v-if="authStore.isAuthenticated && !onboardingStore.hasSeen" @close="onboardingStore.markSeen()" />`
  - [ ] Do NOT alter existing HomeHub layout or auth logic — overlay renders on top (fixed positioning)

- [ ] Task 4: Vitest unit tests — `onboarding` store (AC: #2, #3)
  - [ ] Create `frontend/src/stores/__tests__/onboarding.spec.ts`
  - [ ] Test: `hasSeen` is `false` initially when `localStorage` is empty
  - [ ] Test: `markSeen()` sets `hasSeen` to `true` and writes `'ttt_onboarding_seen'` to `localStorage`
  - [ ] Test: store initializes with `hasSeen: true` when `localStorage` key already exists at mount time
  - [ ] Mock `localStorage` via `vi.stubGlobal` or `vi.spyOn(Storage.prototype, ...)`

- [ ] Task 5: Vitest component tests — `OnboardingTutorial.vue` (AC: #1, #2)
  - [ ] Create `frontend/src/features/profile/__tests__/OnboardingTutorial.spec.ts`
  - [ ] Test: component renders and shows slide 1 content on mount
  - [ ] Test: "Skip" button click emits `close`
  - [ ] Test: "Next" advances to slide 2; "Done" appears on slide 3 and emits `close` on click
  - [ ] Test: dot indicators reflect current slide index
  - [ ] Use `@vue/test-utils` `mount()`, assert behavior and emits (NOT Tailwind class names)

- [ ] Task 6: Playwright E2E test (AC: #1, #2, #3)
  - [ ] Create `frontend/e2e/onboarding.spec.ts`
  - [ ] Before each test: clear `localStorage` to simulate first-time user; authenticate via existing OAuth mock pattern (see `1-1b-e2e-test-for-oauth2-login-flow.md`)
  - [ ] Test: authenticated first-time user sees onboarding overlay on Home Hub
  - [ ] Test: overlay closes on "Skip" click; page reload does not show it again
  - [ ] Test: returning user (localStorage key present) does NOT see overlay on Home Hub load

## Dev Notes

### Critical Design Decision: localStorage for First-Time Detection

**Why localStorage, not backend:** Stories 1-3 (Automatic Profile Generation) and 1-6 (Avatar Management) are both `backlog` — there is no backend `Player` profile endpoint returning an `isNewUser` or `onboardingCompleted` flag yet. Using `localStorage` key `'ttt_onboarding_seen'` is intentional for this story. When Story 1-3 ships and a real profile API exists, the store can be enhanced to sync with backend preference without breaking existing behavior.

**Key**: `'ttt_onboarding_seen'` (value `'1'`). Presence of key = tutorial already shown.

### State Management: New `onboarding` Pinia Store

Do NOT extend `frontend/src/stores/auth.ts` — auth store tracks only authentication status. Adding tutorial state there violates SRP (rule #8 in `1-write.md`). Create a separate `frontend/src/stores/onboarding.ts`:

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'ttt_onboarding_seen'

export const useOnboardingStore = defineStore('onboarding', () => {
  const hasSeen = ref(!!localStorage.getItem(STORAGE_KEY))

  function markSeen() {
    localStorage.setItem(STORAGE_KEY, '1')
    hasSeen.value = true
  }

  return { hasSeen, markSeen }
})
```

### Component Location — Establishing `features/profile/`

Architecture mandates feature-based layout. This story is the **first** to create the `features/profile/` directory. Place files:

```
frontend/src/
├── features/
│   └── profile/                               ← CREATE this directory
│       ├── OnboardingTutorial.vue             ← CREATE
│       └── __tests__/
│           └── OnboardingTutorial.spec.ts     ← CREATE
├── stores/
│   ├── onboarding.ts                          ← CREATE
│   └── __tests__/
│       └── onboarding.spec.ts                 ← CREATE
├── views/
│   └── HomeHub.vue                            ← UPDATE (add overlay)
└── (project root)
    └── frontend/e2e/
        └── onboarding.spec.ts                 ← CREATE (Playwright)
```

### HomeHub.vue Integration Pattern

HomeHub.vue currently shows authenticated content via `v-else` block. Add overlay **inside** `<main>` before its closing tag — it uses `fixed inset-0` so existing layout is unaffected:

```vue
<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useOnboardingStore } from '@/stores/onboarding'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'
import OnboardingTutorial from '@/features/profile/OnboardingTutorial.vue'

// ... existing string constants unchanged ...

const authStore = useAuthStore()
const onboardingStore = useOnboardingStore()
</script>

<template>
  <main class="flex flex-col items-center justify-center min-h-screen gap-8 p-6">
    <!-- existing content unchanged -->
    <OnboardingTutorial
      v-if="authStore.isAuthenticated && !onboardingStore.hasSeen"
      @close="onboardingStore.markSeen()"
    />
  </main>
</template>
```

### Tutorial Slide Content

Since match recording, stats, and confirmation (Stories 1-2 to 1-6) are not yet implemented, slides show **intent/preview** with static text — no interactive flows:

| Slide | Title | Body (≤3 lines) | Icon |
|-------|-------|-----------------|------|
| 1 | Record a match | After every game, tap "New Match" to log the result. It takes under 10 seconds. | ⚽ |
| 2 | Confirm together | Your opponent gets a notification to confirm. One tap — done. | ✅ |
| 3 | Track your stats | Discover your attack/defense strengths and rivalry records. | 📊 |

### Styling Conventions

- **Tailwind utilities**: Layout, spacing, colours, backdrop (`fixed inset-0 bg-black/50 z-50`)
- **`ch-` prefixed SCSS**: Custom animations (overlay fade-in, slide transition). Add to `frontend/src/assets/main.css` or a new `frontend/src/assets/_onboarding.scss` imported from `main.css`
- **Do NOT** create a Tailwind component class or modify `tailwind.config.js` for this story
- Mobile-first: CTA buttons in thumb zone (bottom of overlay card), not top

Recommended overlay DOM structure:
```vue
<div class="fixed inset-0 bg-black/50 z-50 flex items-end justify-center">
  <div class="bg-white rounded-t-2xl w-full max-w-lg p-6 ch-onboarding-card">
    <!-- Skip button: absolute top-right -->
    <!-- Slide content: icon + title + body -->
    <!-- Dot indicators: centered row -->
    <!-- Prev / Next / Done buttons: bottom row -->
  </div>
</div>
```

### Testing Standards Summary

**Vitest unit tests** (AAA pattern, per `1-write.md` + `2-test.md`):
- Mock `localStorage` — do NOT test real browser storage in Vitest (jsdom environment)
- Assert behavior and emitted events; do NOT assert Tailwind class names
- Each test must fail if the production logic is deleted/inverted (Core Validity Contract)

**Playwright E2E**:
- Follow OAuth mock pattern established in Story 1-1b (`frontend/e2e/`)
- Use `page.evaluate(() => localStorage.clear())` before first-time user tests
- Use `page.evaluate(() => localStorage.setItem('ttt_onboarding_seen', '1'))` for returning-user scenario

**500-line rule**: With 3 slides of static content, `OnboardingTutorial.vue` poses no risk. Monitor if slide count grows beyond 5.

### Previous Story Context

Stories 1-2 through 1-6 are all `backlog` — no prior implementation patterns to reference beyond Story 1-1. Patterns established in 1-1 that apply here:
- `<script setup lang="ts">` with `defineStore` Pinia composition pattern (see `stores/auth.ts`)
- Named string constants at top of `<script setup>` block (e.g., `const TITLE = 'Tic-Tac-Tore'`)
- Tailwind-only for utility styling; no inline `style` attributes

### References

- `_bmad-output/planning-artifacts/epics.md` — Story 1.7 AC, FR56
- `_bmad-output/planning-artifacts/architecture.md` — Feature-based layout, Pinia, Tailwind+SCSS, `ch-` prefix, 500-line rule (IP-04), naming conventions
- `_bmad-output/planning-artifacts/ux-design-specification.md` — Newcomer archetype "tutorial slides", anti-pattern "Complex onboarding before value delivery", experience principle "Predictability is speed"
- `_project-spec/rules/1-write.md` — SRP (#8), Strict Layering (#6), Tell Don't Ask (#5), Single Level of Abstraction (#10)
- `_project-spec/rules/2-test.md` — Core Validity Contract (#1), AAA (#3), Testing through public contract (#2)
- `frontend/src/views/HomeHub.vue` — Existing structure to UPDATE — do not change existing auth/layout logic
- `frontend/src/stores/auth.ts` — Auth store — do NOT add onboarding state here

## Dev Agent Record

### Agent Model Used

_[To be filled by dev agent]_

### Debug Log References

_[To be filled by dev agent]_

### Completion Notes List

_[To be filled by dev agent]_

### File List

_[To be filled by dev agent]_

## Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-05-14 | Story created | SM Agent |
