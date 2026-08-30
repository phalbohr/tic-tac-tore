# Story 7-1: Achievement System (Badges)

## Story Foundation
**User Story:** As a player, I want to earn achievements, so that I feel rewarded for my progress.

**Acceptance Criteria:**
- **Given** a player performs actions in the app
- **When** they meet specific milestones or statistical thresholds
- **Then** the system awards them an achievement badge (FR48)
- **And** it is visible in their profile

**Business Context & Value:**
Provides gamification and personalization to keep players engaged over time. Belongs to Epic 7 (Engagement & Polish).

## Developer Context

### Technical Requirements
- **Frontend:** Vue 3, Vite 8, Tailwind CSS/Stitch design tokens. Implement a badge notification/popup and a profile badge list component. Keep it within the "Clubhouse Editorial" aesthetic (warm, tactile, not a generic neon gaming app).
- **Backend:** Spring Boot (Java). Implement an `Achievement` entity/table and a linking table for `player_achievements`.
- **Event-Driven / Async:** Achievements should be evaluated asynchronously (e.g., via Spring ApplicationEvents) when a match is confirmed, to avoid slowing down the critical 10-second match recording path.

### Architecture Compliance
- **The 500-Line Rule (IP-04):** No file or test class may exceed 500 lines.
- **REST API:** Endpoints must be `kebab-case` and plural (e.g., `/api/v1/players/{id}/achievements`).
- **Database:** Flyway migration required. Tables `achievements` and `player_achievements` in `snake_case`. Columns in `snake_case`.
- **Feature-based structure:** Create frontend code in `frontend/src/features/profile/` or a new `frontend/src/features/achievements/`. Backend code should follow feature-packaged structure.
- **Data Formats:** All JSON fields must use `camelCase` for seamless JS/TS integration.

### File Structure Requirements
- Create new Flyway migration script in `src/main/resources/db/migration/`.
- Create backend entities, repositories, and services in the appropriate feature package.
- Update `frontend/src/features/profile/` components (e.g., Profile view/cabinet) to show awarded badges.
- Create reusable Vue components for Badge icons. *Note: Individual images or icon sets can be generated using google stitch `generate_image` as requested by the PM.*

### Testing Requirements
- **Backend:** Co-located unit/integration tests (`<500 lines` rule). Test achievement evaluation logic thoroughly.
- **Frontend:** Vitest/Vue Test Utils for the badge display components. Co-locate tests with components.

### Project Context Reference
- System must support English and German (FR59). Add new translation keys for achievements.
- Push notifications should eventually support achievements (FR55).

## Story Completion Status
**Status:** ready-for-dev
**Last Updated:** 2026-08-30
