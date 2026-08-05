# Story 2.6: Player Display Visual Improvements

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want the match interfaces to clearly display player avatars by their positions (Defender/Attacker),
so that I can easily identify who played in which position during score entry and match confirmation.

## Acceptance Criteria

1. **Avatar Initials Generation**:
   - **Given** a user's display name or username
   - **When** an avatar is displayed without a custom image
   - **Then** the avatar must show the first letter of each word in the name (maximum 2 letters) inside a circle, uppercased.
   - **And** it must handle edge cases gracefully (empty name, single word, multi-word, undefined).

2. **Retrospective Score Entry Screen - Game Rows**:
   - **Given** the user is viewing the game rows in the retrospective score entry screen
   - **When** a game row is displayed
   - **Then** the row must show:
     - Team A: 2 avatars (Defender first on left, then Attacker)
     - The text "VS" centered
     - Team B: 2 avatars (Defender first on left, then Attacker)

3. **Retrospective Score Entry Screen - Score Display Layout**:
   - **Given** the user is viewing the score for a specific game
   - **When** the score is displayed
   - **Then** the labels "Team A Scores" and "Team B" must be on a single line, positioned below the score, on the left and right respectively.
   - **And** there must be two avatar circles with initials for Team A (Defender on left, Attacker on right).
   - **And** there must be two avatar circles with initials for Team B (Defender on left, Attacker on right).

4. **Confirm Positions Window (During Score Entry)**:
   - **Given** the user opens the "Confirm Positions" window
   - **When** the team position layout is displayed
   - **Then** for each team it must show:
     - Top row label (e.g., "Team 1")
     - Column labels: left "Defender", right "Attacker"
     - Below labels: player avatars
     - Below avatars: full player names
     - Below names: a "Swap Team [1/2]" button.

5. **Match Confirmation and Rejection Notifications**:
   - **Given** the user receives a match confirmation or match rejection notification
   - **When** the notification details are displayed
   - **Then** they must look identical in layout to the score entry screen logic (Game rows with avatars Defender/Attacker, labels on a single line below left/right of score, and corresponding team avatars).

## Tasks / Subtasks

- [ ] Task 1 (AC: 1) Update `AvatarBase.vue` component to support initials logic when image is missing.
- [ ] Task 2 (AC: 2, 3, 5) Extract a shared game row display component (e.g. `MatchGameRow.vue`) to unify game row layout.
- [ ] Task 3 (AC: 2, 3) Update the retrospective `ScoreEntry.vue` game row component layout using the shared component.
- [ ] Task 4 (AC: 4) Update the `PositionSwapDialog.vue` (Confirm Positions modal) layout.
- [ ] Task 5 (AC: 5) Update `PendingMatches.vue` notification cards to use the shared game row component.

## Developer Context & Guardrails

### Technical Requirements

- **Avatar Initials Generation Helper**: Implement a robust `getInitials(name: string): string` utility or composable to handle empty strings, single words, and multi-word names (e.g., "John Doe" -> "JD", "Alice" -> "A"). Max 2 characters, capitalized.
- **Shared UI Component Strategy**: To satisfy AC 5 without code duplication, abstract the game row rendering logic into a reusable sub-component (e.g., `MatchGameRow.vue`) that can be imported by both `ScoreEntry.vue` (Epic 2) and `PendingMatches.vue` (Epic 3). Do NOT duplicate the complex flex/grid layout for the avatars and labels.
- **i18n Localization**: Ensure all new textual labels ("VS", "Team A Scores", "Team B", "Defender", "Attacker", "Swap Team 1", "Swap Team 2") use Vue i18n `$t()` or `t()` methods and are properly added to `src/locales/en.json`.
- **Responsive Layout**: Use Tailwind CSS v4 to ensure the mobile-first layouts (particularly displaying 4 avatars and the "VS" text inline) scale gracefully and do not wrap incorrectly on narrow screens (e.g., 360px width).

### Architecture Compliance

- **Styling**: Use Tailwind CSS v4 with the established `ch-` prefix isolation.
- **State Management**: Access match state from `useMatchStore` or props passed from parents. No direct modification of global state inside purely visual display components.

### File Structure Requirements

The following files are explicitly targeted for updates:

- `frontend/src/components/AvatarBase.vue` (Target for AC 1 initials logic)
- `frontend/src/features/match/components/ScoreEntry.vue` (Target for AC 2 & 3 layout updates)
- `frontend/src/features/match/components/PositionSwapDialog.vue` (Target for AC 4 layout updates)
- `frontend/src/features/match/components/PendingMatches.vue` (Target for AC 5 layout updates)
- `frontend/src/features/match/components/MatchGameRow.vue` (Recommended NEW component for sharing game row layout)
- `frontend/src/locales/en.json` (Target for i18n keys)

### Testing Requirements

- **Unit Tests for Initials Logic**: Write Vitest unit tests for the `getInitials` helper. Test cases MUST cover: empty string, null/undefined (if applicable), single word, two words, three+ words, and leading/trailing whitespace.
- **Component Tests**: Ensure Vue Test Utils are used to verify that the `AvatarBase.vue` component correctly renders the initials circle when no image URL is provided.
- **Visual/Layout Validation**: Visually verify mobile layout responsiveness.

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Debug Log References

- Original story validated and optimized via bmad-create-story:validate checklist.

### Completion Notes List

- Comprehensive developer context injected to prevent duplication between Epic 2 and Epic 3 notification interfaces.
- Exact file paths and testing guardrails provided.

### File List

- frontend/src/components/AvatarBase.vue
- frontend/src/features/match/components/ScoreEntry.vue
- frontend/src/features/match/components/PositionSwapDialog.vue
- frontend/src/features/match/components/PendingMatches.vue
