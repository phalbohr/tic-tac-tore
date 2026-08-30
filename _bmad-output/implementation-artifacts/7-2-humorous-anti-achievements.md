# Story 7.2: Humorous Anti-achievements

## Foundation
**Epic:** 7
**Story:** 7.2
**Title:** Humorous Anti-achievements

### User Story
As a player, I want to receive funny anti-achievements, so that the experience is lighthearted.

### Acceptance Criteria
- **Given** a player experiences a memorable fail
- **When** the specific fail condition is met
- **Then** the system awards a lighthearted anti-achievement (FR49)

### Success Criteria
- The system correctly evaluates and awards anti-achievements after a match is confirmed and published.
- At least 3 humorous anti-achievements are implemented.
- Anti-achievement icons are visually distinct and generated using Google Stitch.
- Notifications follow the "The Clubhouse Editorial" theme (no overly gamified UI).

---

## Developer Context

### Architecture & Integration
The system already has an achievement evaluation pipeline via the `AchievementEvaluator` interface and `AchievementService`.
- **Entities**: The `Achievement` entity has `code`, `category`, `nameKey`, `descriptionKey`, `icon`.
- **Category**: Use a distinct category for anti-achievements (e.g., `ANTI_ACHIEVEMENT` or `HUMOR`).
- **Logic**: Implement `AchievementEvaluator` for each new anti-achievement condition. Examples:
  - `THE_GOOSE_EGG`: Player lost a game 0-X.
  - `GENEROUS_HOST`: Player lost 5 matches in a row.
  - `SIEVE_DEFENSE`: Player conceded 10+ goals in a single match while playing defense.
- **Evaluation Trigger**: Ensure these evaluators are registered and executed when a match completes (same as normal achievements).

### UI/UX & Assets (Google Stitch)
**CRITICAL REQUIREMENT:** Use Google Stitch to generate the icons (or a full set of icons) for these anti-achievements.
- The icons should fit the overall design system but have a playful, editorial tone rather than looking like standard gaming badges.
- Example prompts for Stitch: "A broken foosball player figure in a warm dark theme", "A golden goose egg on a foosball table".
- Place the generated assets in the appropriate frontend assets folder and reference them in the `icon` field of the DB migration.
- Add translation keys to the i18n files for `nameKey` and `descriptionKey`.

### Database
- Provide a Flyway migration (e.g., `V7.2__insert_anti_achievements.sql`) to insert the new records into the `achievement` table.

### Testing Guardrails
- **Unit Tests**: Test each new `AchievementEvaluator` for true/false conditions.
- **Integration Tests**: Verify that saving a match triggers the anti-achievements if conditions are met.

## Status
**State:** ready-for-dev
