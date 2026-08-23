# Story 6.1b: Create Rule Template

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player or organizer,
I want to create custom rule templates with specific parameters and a unique name,
so that I can save and reuse my group's specific house rules for future matches.

## Acceptance Criteria

1. **Given** I am in the rule selection view (e.g., when creating a match or setting default user preferences)
2. **When** I choose to create a custom rule template
3. **Then** I am presented with a form to configure the rule parameters:
   - Win condition (e.g., Best of 1, Best of 3, Best of 5)
   - Score limit per game (e.g., 5, 7, 8)
   - Tie-break rules (e.g., Win by 2 goals in final set: Yes/No, Absolute score cap: 8)
   - Time-outs per game (e.g., 2) and duration (e.g., 30s)
   - Ball possession time limits (e.g., 10s on 5-bar, 15s on others, or disabled)
   - Table side swap rules (e.g., change sides after each game)
   - Restart after goal (Options: Conceding team / Random drop-in)
   - Illegal moves toggles (e.g., Spinning / Aerials allowed: Yes/No)
   - Point distribution
   - Position swap rules (e.g., between games)
4. **And** I can assign a custom name to this template.
5. **When** I save the template
6. **Then** the system persists an immutable `RuleConfiguration` record (AD-01).
7. **And** the template becomes available in the rule selection list.
8. **Given** the system database is initialized
9. **Then** two default, built-in system presets must be seeded: "International Official Rules" (ITSF) and "Bundesliga Official Rules" (DTFB).
10. **Given** an existing custom template
11. **When** I edit its parameters and save
12. **Then** the system creates a *new* immutable `RuleConfiguration` record rather than modifying the existing one, preserving statistical integrity for past matches.

## Tasks / Subtasks

- [ ] Task 1: Create backend endpoint for rule template creation
  - [ ] Implement `POST /api/v1/rule-configurations`
  - [ ] Expand `RuleConfiguration` entity mapping (immutable) with new fields (timeouts, time limits, side swaps, restart rules, illegal moves)
  - [ ] Ensure validation of all rule parameters
  - [ ] Create database migration/seeding for the two built-in presets: "International Official Rules" (ITSF) and "Bundesliga Official Rules" (DTFB)
  - [ ] Add unit tests for template creation and immutability logic
- [ ] Task 2: Create frontend UI for custom rule form
  - [ ] Build a `ch-` prefixed Vue component for rule configuration forms
  - [ ] Integrate with Pinia store to handle saving rule templates
  - [ ] Ensure the form handles parameter combinations properly
- [ ] Task 3: Integrate with rule selection and editing
  - [ ] Add the "Create Custom Template" action to the rule selection list
  - [ ] Implement the "Edit as New" logic when modifying an existing template

## Dev Notes

- **Architecture Compliance**:
  - **AD-01**: `RuleConfiguration` MUST be immutable. Do not provide a `PUT` endpoint that modifies existing records. Updates must create a new record.
  - **AD-02**: Statistics rely on consistent rule parameters. Custom templates are architecturally identical to ITSF/DTFB presets, differing only by the custom name and parameter mix.
- **Source tree components to touch**:
  - Backend: `src/main/java/.../domain/RuleConfiguration.java`, `RuleConfigurationService.java`, `RuleConfigurationController.java`
  - Frontend: `src/features/match/components/RuleSelection.vue`, `src/features/match/components/RuleForm.vue`

### Project Structure Notes

- Follow the 500-Line Rule (IP-04). Keep form components modular.
- Use `ch-` prefixes for all custom SCSS styles in the UI form.
- Ensure backend entities remain in the `domain` package and controllers in the `api` package.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#Rule System Consistency] - Rule templates are immutable. Modifying settings creates a new template.
- [Source: _bmad-output/planning-artifacts/prd.md#FR3] - Player can select a rule system (ITSF, DTFB, or Custom template).
- [Source: _bmad-output/planning-artifacts/architecture.md#AD-01] - Immutable RuleConfiguration.

## Dev Agent Record

### Agent Model Used

Antigravity 

### Debug Log References

N/A

### Completion Notes List

N/A

### File List

- `_bmad-output/implementation-artifacts/6-1b-create-rule-template.md`
