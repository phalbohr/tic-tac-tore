# Epic 2 Context: Retrospective Match Entry & Rule Systems

<!-- Generated from planning artifacts. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Provide core functionality allowing players to quickly record 1v1 and 2v2 matches immediately after they are played. Retrospective entry emphasizes speed over statistical precision (sub-10-second entry) to ensure high adoption rates, utilizing a portrait-oriented, kicker-table-inspired mobile UI.

## Stories

- Story 2.1: Rule System Selection & Inline Creation
- Story 2.2: Match Type & Player Selection (Portrait)
- Story 2.3: Score Entry & Automatic Completion
- Story 2.4: Match Submission with Undo Window
- Story 2.5: Position Swapping Between Games

## Requirements & Constraints

- **Match Recording:** Enable retrospective creation of 1v1 and 2v2 matches by selecting players and entering game scores.
- **Rule System Enforcement:** Allow selection of preset (ITSF, DTFB) or custom rule templates. The system must enforce constraints defined by the selected rule system (e.g., game count, score limits, win conditions).
- **Auto-Completion:** Automatically complete a game when the score limit is reached and complete the match when win conditions are met, removing the need for manual end-game actions.
- **Positional Tracking:** Support swapping teammate positions between games. Due to the "speed over precision" trade-off in retrospective mode, goals are recorded without intra-game positional attribution.
- **Performance:** End-to-end match entry must take less than 10 seconds.
- **Platform:** Application must be installable as a Progressive Web App (PWA).

## Technical Decisions

- **Immutable RuleConfigurations:** All rule sets are stored as immutable records. Any changes result in a new configuration ID to preserve historical statistics and ensure data integrity.
- **Rule-Agnostic Aggregation:** Statistics filter and aggregate based on underlying rule parameter combinations, rather than just template labels, ensuring identically configured rules remain comparable.
- **Immutability of Matches:** Confirmed match entities are strictly immutable.
- **PWA Infrastructure:** Rely on Service Workers for necessary progressive capabilities like offline caching and add-to-homescreen.

## UX & Interaction Patterns

- **Orientation & Layout:** Mobile-first, portrait orientation required for retrospective entry to support one-handed, mobile-in-hand posture. The layout uses a top-down "kicker table" view.
- **No-Line Rule:** Use background color shifts and spacing to define UI boundaries; avoid 1px borders.
- **Undo Workflow:** After match submission, display a 15-second undo toast rather than a traditional double-confirmation modal.

## Cross-Story Dependencies

- **Verification Pipeline:** Match submission (Story 2.4) serves as the upstream dependency for the data verification and trust pipeline in Epic 3. MVP release requires both submission (Story 2.4) and position swapping (Story 2.5) to be fully integrated.
