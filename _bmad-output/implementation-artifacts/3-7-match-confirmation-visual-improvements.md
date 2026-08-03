# Story 3.7: Match Confirmation Visual Improvements

Status: in-progress

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to be able to collapse match confirmation and rejection notifications,
so that my screen is not cluttered, while still knowing I have pending actions via a badge on my avatar.

## Acceptance Criteria

1. **Collapsing Confirmations**: 
   - **Given** a user is viewing pending matches in the `PendingMatches.vue` component
   - **When** they view an incoming confirmation request
   - **Then** the notification must display three buttons of equal size and consistent styling: `Reject`, `Confirm`, and `Close`.
   - **And** clicking `Close` must collapse the notification.

2. **Collapsing Rejections**: 
   - **Given** a match was rejected and returned to the creator
   - **When** the creator views the rejected match notification
   - **Then** the notification must display three buttons of equal size and consistent styling: `Edit match`, `Delete match`, and `Close`.
   - **And** clicking `Close` must collapse the notification.

3. **Avatar Badge Indicator**: 
   - **Given** a user has collapsed notifications (confirmations or rejections)
   - **When** viewing the `HomeView.vue` central profile avatar (`w-24 h-24` avatar block, not the sticky header avatar)
   - **Then** a red circular badge with the count of total pending/collapsed notifications is displayed on the avatar.
   - **And** the badge must be keyboard accessible (`role="button"`, `tabindex="0"`, `aria-label`).
   - **And** the badge should lightly pulse or animate when new notifications arrive.

4. **Restoring Notifications**: 
   - **Given** notifications are collapsed
   - **When** the user clicks the badge on the central profile avatar (or presses Enter/Space)
   - **Then** it expands/restores all collapsed notifications back to the screen with a smooth visual transition.

5. **Button Styling Consistency**: 
   - **Given** the 3-button layout
   - **Then** the buttons must be identical in width and height, using `grid grid-cols-3 gap-2` or `flex-1`.
   - **And** each button must have a minimum touch target height of `48px` (`min-h-[48px]`).
   - **And** each button must have distinct visual hover feedback (`:hover` states).

## Developer Context & Architecture Guardrails

### 1. File Structure & Component Contracts
- **`PendingMatches.vue`**: 
  - Define new emit: `(e: 'close', matchId: string): void`.
  - Introduce explicit rendering logic to ensure only expanded matches are visible or passed from `HomeView.vue` based on the collapse state.
- **`HomeView.vue`**:
  - The central profile avatar section (currently around line 306 with `<AvatarBase>`) must handle the toggle state. DO NOT attach toggle behavior to the sticky header avatar (which links to `/cabinet`).
  - Introduce Vue `<TransitionGroup>` or `<Transition>` for the `PendingMatches` list to ensure smooth expanding/collapsing.

### 2. State Management (CRITICAL)
- **Problem**: Notifications update every 5 seconds via `fetchPendingMatches()`.
- **Solution**: The collapsed state MUST be maintained outside of the raw API response so it isn't reset on every poll. 
- You may store the `collapsedMatchIds` array or a global `isCollapsed` toggle in the local reactive state of `HomeView.vue` or within the `usePendingMatches.ts` composable. 

### 3. Localization (i18n)
- Update `en.json` and `ru.json` to ensure the following keys exist:
  - `match.close` ("Close" / "Закрыть")
  - `match.editMatch` ("Edit Match" / "Редактировать")
  - `match.deleteMatch` ("Delete Match" / "Удалить")

### 4. Accessibility & UI Polish
- Action buttons in `PendingMatches.vue` must have `min-h-[48px]` for touch targets.
- The avatar badge should use proper ARIA attributes (`aria-label="Pending notifications"`, `role="button"`) and keyboard event listeners (`@keydown.enter.prevent`, `@keydown.space.prevent`).
- Use Tailwind CSS classes for animations, e.g., `animate-pulse` or similar for new notifications.

## Testing Requirements
- **Vitest Component Tests**: 
  - Verify `PendingMatches.vue` renders three equal-sized buttons.
  - Verify `close` emit is fired when the close button is clicked.
  - Verify hover states are applied properly.

## Dev Agent Record

### Agent Model Used
Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

### File List

### Review Findings
- [x] [Review][Patch] Unbounded Stale State Leak in collapsedMatchIds [frontend/src/features/match/composables/usePendingMatches.ts:119]
- [x] [Review][Patch] Impure Timer and Missing Cleanup in watch(pendingCount) [frontend/src/views/HomeView.vue:44]
- [x] [Review][Patch] Missing Unit Test Assertions for Button Hover States [frontend/src/features/match/components/__tests__/PendingMatches.spec.ts:200]
- [x] [Review][Patch] Missing <Transition> Wrapper for PendingMatches Container in HomeView.vue [frontend/src/views/HomeView.vue:353]
- [x] [Review][Patch] Inconsistent Button Text Sizes & Fallback Case Mismatch [frontend/src/features/match/components/PendingMatches.vue:143]
