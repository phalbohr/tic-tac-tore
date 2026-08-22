---
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04-generate-tests'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-22T22:58:00+02:00'
storyId: '5.5'
storyKey: '5-5-screen-wake-lock-and-continuity'
storyFile: '_bmad-output/implementation-artifacts/5-5-screen-wake-lock-and-continuity.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-5-5-screen-wake-lock-and-continuity.md'
generatedTestFiles:
  - 'frontend/tests/unit/useWakeLock.spec.ts'
  - 'frontend/tests/unit/LiveMatchComponent.spec.ts'
  - 'frontend/e2e/wake-lock-continuity.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/5-5-screen-wake-lock-and-continuity.md'
  - '_bmad/tea/config.yaml'
---

# ATDD Checklist: Story 5.5 — Screen Wake Lock & Continuity

## 🔴 TDD Red Phase Summary

- **TDD Phase**: RED (Test scaffolds generated with `it.skip()` / `test.skip()`)
- **Unit Tests (Vitest)**:
  - `frontend/tests/unit/useWakeLock.spec.ts` (7 red-phase test scaffolds)
  - `frontend/tests/unit/LiveMatchComponent.spec.ts` (2 red-phase test scaffolds)
- **E2E Tests (Playwright)**:
  - `frontend/e2e/wake-lock-continuity.spec.ts` (5 red-phase test scaffolds)
- **Total Tests**: 14 red-phase test scaffolds

---

## 📋 Acceptance Criteria Traceability & Scaffolds

### AC 1: Wake Lock Acquisition on Live Match Start (Landscape & Referee Mode)
- [ ] Application requests a screen wake lock using `navigator.wakeLock.request('screen')` upon match start
- [ ] Operates identically in both standard landscape player mode and portrait referee mode
- [ ] Prevents the device display from dimming or locking during active play
- **Test Scaffolds**:
  - `frontend/tests/unit/useWakeLock.spec.ts` → `[P0] request() acquires wake lock sentinel and sets isActive to true`
  - `frontend/tests/unit/LiveMatchComponent.spec.ts` → `[P0] requests wake lock when starting match in landscape mode`
  - `frontend/e2e/wake-lock-continuity.spec.ts` → `[Story 5.5] [P0] AC1: requests screen wake lock when starting match in landscape player mode`
  - `frontend/e2e/wake-lock-continuity.spec.ts` → `[Story 5.5] [P0] AC1: requests screen wake lock when starting match in portrait referee mode`

### AC 2: Continuity Across Visibility Changes (Tab Switch / App Switch / Screen Re-open)
- [ ] When document visibility changes to `'hidden'` and returns to `'visible'`, lock is automatically re-requested if match is active (`isMatchStarted === true`)
- [ ] Does not re-request lock if match was already finished or lock inactive
- **Test Scaffolds**:
  - `frontend/tests/unit/useWakeLock.spec.ts` → `[P0] re-requests wake lock on document visibilitychange to visible when lock is active`
  - `frontend/tests/unit/useWakeLock.spec.ts` → `[P0] does not re-request wake lock on visibilitychange when isActive is false`
  - `frontend/e2e/wake-lock-continuity.spec.ts` → `[Story 5.5] [P0] AC2: re-requests wake lock when document visibility returns to visible during active match`

### AC 3: Wake Lock Release on Match Finish, Exit, and Component Unmount
- [ ] Calling `release()` explicitly releases the sentinel (`sentinel.release()`) and cleans up event listeners
- [ ] Unmounting `LiveMatch.vue` releases the wake lock and prevents memory leaks
- **Test Scaffolds**:
  - `frontend/tests/unit/useWakeLock.spec.ts` → `[P0] release() releases sentinel and resets isActive to false`
  - `frontend/tests/unit/LiveMatchComponent.spec.ts` → `[P0] releases wake lock when component is unmounted`
  - `frontend/e2e/wake-lock-continuity.spec.ts` → `[Story 5.5] [P0] AC3: releases wake lock sentinel when navigating away from live match`

### AC 4: Graceful Degradation on Unsupported Browsers or Permission Errors
- [ ] Handles environments without `navigator.wakeLock` gracefully without unhandled exceptions
- [ ] Handles permission denials or low battery rejections (`NotAllowedError`, `AbortError`) with `console.warn`
- [ ] Match startup and scoring flow proceed without interruption
- **Test Scaffolds**:
  - `frontend/tests/unit/useWakeLock.spec.ts` → `[P1] handles navigator.wakeLock unsupported environment gracefully`
  - `frontend/tests/unit/useWakeLock.spec.ts` → `[P1] handles request rejection (NotAllowedError / low battery) gracefully without throwing`
  - `frontend/e2e/wake-lock-continuity.spec.ts` → `[Story 5.5] [P1] AC4: match starts and runs normally without errors when wake lock API rejects request`

---

## 🛠️ Implementation Guidance for `bmad-dev-story`

During story implementation:

1. **Create Composable (`frontend/src/composables/useWakeLock.ts`)**:
   - Reactive state: `isSupported`, `isActive`, `sentinel`.
   - `request(): Promise<boolean>` with try/catch wrapping `navigator.wakeLock.request('screen')`.
   - `release(): Promise<void>` releasing sentinel and resetting state.
   - `visibilitychange` listener re-acquiring lock when returning to `visible` while `isActive` is true.
   - SSR guards: `typeof window !== 'undefined'`, `typeof navigator !== 'undefined' && 'wakeLock' in navigator`.
   - Unskip and run unit tests in `frontend/tests/unit/useWakeLock.spec.ts` (`npm run test:unit -- tests/unit/useWakeLock.spec.ts`).
   - Verify tests turn GREEN.

2. **Integrate into `LiveMatch.vue` (`frontend/src/features/match/LiveMatch.vue`)**:
   - Import and instantiate `const wakeLock = useWakeLock()`.
   - In `startMatch()`, call `await wakeLock.request()`.
   - In `onUnmounted()`, call `wakeLock.release()`.
   - Unskip and run unit tests in `frontend/tests/unit/LiveMatchComponent.spec.ts`.
   - Unskip and run E2E tests in `frontend/e2e/wake-lock-continuity.spec.ts`.
   - Verify all tests turn GREEN.

---

## 🚀 Execution Report

- **Execution Mode**: AI Test Architecture & Scaffold Generation (ATDD Red Phase)
- **Status**: Red-phase scaffolds generated and verified
- **Next Workflow**: `bmad-agent-dev` / `bmad-dev-story` (Story Implementation)
