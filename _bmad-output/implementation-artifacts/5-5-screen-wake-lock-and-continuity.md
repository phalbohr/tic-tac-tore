# Story 5.5: Screen Wake Lock & Continuity

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player or referee,
I want the device screen to stay illuminated continuously during an active live match,
so that the live scoreboard remains immediately visible and responsive throughout play without requiring periodic screen touches.

## Acceptance Criteria

1. **Wake Lock Acquisition on Live Match Start (Landscape & Referee Mode)**:
   - **Given** the live scoring interface is loaded (in standard landscape player mode or portrait referee mode)
   - **When** the user taps "Start Match" (`data-testid="start-match-btn"`)
   - **Then** the application requests a screen wake lock using the Screen Wake Lock API (`navigator.wakeLock.request('screen')`) (FR11, AD-06)
   - **And** prevents the device display from dimming or locking while the match is active.

2. **Continuity Across Visibility Changes (Tab Switch / App Switch / Screen Re-open)**:
   - **Given** an active live match with a wake lock acquired
   - **When** the application visibility state changes to `'hidden'` (e.g. user switches tabs, minimizes browser, or locks screen) and subsequently returns to `'visible'` (`document.visibilityState === 'visible'`)
   - **Then** the application automatically re-requests and restores the screen wake lock if the match is still active (`isMatchStarted === true`)
   - **And** ensures seamless continuity without requiring user re-activation.

3. **Wake Lock Release on Match Finish, Exit, and Component Unmount**:
   - **Given** an active live match with a wake lock acquired
   - **When** the match finishes, the user navigates away, or the `LiveMatch.vue` component is unmounted
   - **Then** the application explicitly releases the wake lock sentinel (`sentinel.release()`), cleans up all `visibilitychange` event listeners, and resets internal wake lock state to avoid resource leaks.

4. **Graceful Degradation on Unsupported Browsers or Permission Errors**:
   - **Given** a browser environment where the Screen Wake Lock API is unsupported (`!('wakeLock' in navigator)`) or where the request is rejected (e.g. system battery saver mode, permission policy restriction)
   - **When** the match starts or visibility changes
   - **Then** the match startup and scoring flow continue uninterrupted without throwing unhandled exceptions or breaking the UI
   - **And** a non-fatal warning is logged to `console.warn`.

## Tasks / Subtasks

- [ ] Task 1: Create Screen Wake Lock Composable (`frontend/src/composables/useWakeLock.ts`) (AC 1, AC 2, AC 3, AC 4)
  - [ ] Implement `useWakeLock` composable with reactive states: `isSupported` (`computed`/`ref`), `isActive` (`ref<boolean>`), and `sentinel` reference
  - [ ] Implement `request(): Promise<boolean>` to acquire lock via `navigator.wakeLock.request('screen')` with try/catch handling for `NotAllowedError` and `AbortError`
  - [ ] Implement `release(): Promise<void>` to release sentinel (`sentinel.value.release()`) and reset `isActive`
  - [ ] Add `visibilitychange` event listener: automatically re-invoke `request()` when `document.visibilityState === 'visible'` and `isActive` was requested/intended
  - [ ] Guard all browser API access with SSR checks (`typeof window !== 'undefined'`, `typeof navigator !== 'undefined' && 'wakeLock' in navigator`)
  - [ ] Provide cleanup function / `onUnmounted` hook within or callable by composable consumers to remove `visibilitychange` listener and release sentinel
- [ ] Task 2: Integrate `useWakeLock` into `LiveMatch.vue` (AC 1, AC 2, AC 3)
  - [ ] In `frontend/src/features/match/LiveMatch.vue`:
    - [ ] Import and initialize `useWakeLock()`
    - [ ] Call `wakeLock.request()` inside `startMatch()` alongside fullscreen and screen orientation requests
    - [ ] Ensure wake lock operates identically in both standard landscape player mode and portrait referee mode (`matchStore.isRefereeMode`)
    - [ ] In `onUnmounted()`, call `wakeLock.release()` to ensure clean teardown when leaving the live match view
- [ ] Task 3: Unit Testing (AC 1, AC 2, AC 3, AC 4)
  - [ ] Create `frontend/tests/unit/useWakeLock.spec.ts`:
    - [ ] Test successful lock acquisition and setting `isActive = true`
    - [ ] Test lock release on `release()` call and setting `isActive = false`
    - [ ] Test automatic re-acquisition on `document.dispatchEvent(new Event('visibilitychange'))` when visible
    - [ ] Test graceful fallback when `navigator.wakeLock` is undefined
    - [ ] Test error handling when `navigator.wakeLock.request` rejects with `NotAllowedError`
  - [ ] Create/update `frontend/tests/unit/LiveMatchComponent.spec.ts`:
    - [ ] Verify `startMatch` triggers wake lock request
    - [ ] Verify component unmount triggers wake lock release
  - [ ] Maintain strict 500-line file limit (IP-04)
- [ ] Task 4: E2E Playwright Integration Testing (AC 1, AC 2, AC 3, AC 4)
  - [ ] In `frontend/e2e/wake-lock-continuity.spec.ts` (or `frontend/e2e/real-time-scoring-interface.spec.ts`):
    - [ ] Mock/intercept `navigator.wakeLock.request` in Playwright context
    - [ ] Verify wake lock requested upon clicking "Start Match" in landscape mode
    - [ ] Verify wake lock requested upon clicking "Start Match" in referee mode (`?mode=referee`)
    - [ ] Verify graceful match play when wake lock API is disabled or throws error
  - [ ] Maintain strict 500-line file limit (IP-04)

## Dev Notes

### ATDD Artifacts
- **Unit Tests**: [frontend/tests/unit/useWakeLock.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/tests/unit/useWakeLock.spec.ts), [frontend/tests/unit/LiveMatchComponent.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/tests/unit/LiveMatchComponent.spec.ts)
- **E2E Tests**: [frontend/e2e/wake-lock-continuity.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/wake-lock-continuity.spec.ts)

### Technical Requirements
- **Screen Wake Lock API Lifecycle**:
  - Acquisition: `const sentinel = await navigator.wakeLock.request('screen')`
  - Release listener on sentinel: `sentinel.addEventListener('release', () => { ... })`
  - Browser behavior: The browser automatically releases the wake lock whenever the document loses visibility (`document.visibilityState === 'hidden'`).
  - Continuity requirement: A `visibilitychange` listener on `document` must re-acquire the wake lock when `document.visibilityState === 'visible'` if the match is still active (`isActive.value === true`).
  - Explicit teardown: When the match is finished or component is unmounted, calling `sentinel.release()` releases the lock and the `visibilitychange` listener must be removed (`document.removeEventListener('visibilitychange', ...)`).
- **Error Resilience & SSR Safety**:
  - Always verify `typeof navigator !== 'undefined' && 'wakeLock' in navigator`.
  - Wrap `request()` in `try / catch`. Possible errors include `NotAllowedError` (user/OS denied or low battery mode) and `AbortError`.
  - Errors MUST NOT throw or block match gameplay. Log with `console.warn('WakeLock request failed:', err)`.

### Existing Code Analysis (`LiveMatch.vue`)
- **Current `startMatch` implementation**:
  ```ts
  const startMatch = async () => {
    if (liveMatchContainer.value) {
      try {
        if (liveMatchContainer.value.requestFullscreen) {
          await liveMatchContainer.value.requestFullscreen()
        } else if ((liveMatchContainer.value as any).webkitRequestFullscreen) {
          await (liveMatchContainer.value as any).webkitRequestFullscreen()
        }
        if (typeof screen !== 'undefined' && screen.orientation && (screen.orientation as any).lock) {
          const orientationMode = matchStore.isRefereeMode ? 'portrait' : 'landscape'
          await (screen.orientation as any).lock(orientationMode)
        }
      } catch (err) {
        console.warn('Orientation lock failed', err)
      }
    }
    await wakeLock.request()
    isMatchStarted.value = true
  }
  ```
- **Current `onUnmounted` implementation**:
  ```ts
  onUnmounted(() => {
    matchStore.setRefereeMode(false)
    wakeLock.release()
  })
  ```

### Architecture Compliance
- **Composable Pattern**: Encapsulate wake lock logic inside `frontend/src/composables/useWakeLock.ts` to keep `LiveMatch.vue` lightweight and testable in isolation.
- **500-Line Rule (IP-04)**: Keep `LiveMatch.vue`, `useWakeLock.ts`, and all test files strictly under 500 lines.
- **AD-06 PWA-First Infrastructure**: Use native Screen Wake Lock API for zero-overhead native app feel.

### Previous Story Intelligence (5.1 – 5.4)
- Keep browser API calls (vibration, orientation, wakeLock, fullscreen) safely guarded against SSR and unsupported browser environments.
- Referee Mode (Story 5.4) shares the exact same scoring state and lifecycle in `LiveMatch.vue` — ensure wake lock works equally in referee portrait view.
- In Playwright tests, mock `navigator.wakeLock` using `page.addInitScript()` to verify acquisition calls deterministically.

### References
- **PRD FR11**: "System prevents screen dimming during live match mode (Phase 1.5)"
- **PRD Journey 5**: Phone placed on table as live scoreboard, screen stays on continuously.
- **Architecture AD-06**: "PWA-First Infrastructure: Use of Service Workers for Push API and Screen Wake Lock API (Live Mode)."
- **W3C Screen Wake Lock API**: `https://w3c.github.io/screen-wake-lock/`

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

- Comprehensive context engine analysis completed. All critical gaps, lifecycle handling, continuity requirements, composable architecture, and test plans documented.

### File List

- `frontend/src/composables/useWakeLock.ts` (to be created)
- `frontend/src/features/match/LiveMatch.vue` (to be modified)
- `frontend/tests/unit/useWakeLock.spec.ts` (to be created)
- `frontend/tests/unit/LiveMatchComponent.spec.ts` (to be updated)
- `frontend/e2e/wake-lock-continuity.spec.ts` (to be created)
