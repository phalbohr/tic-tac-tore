# Story 1.3: Automatic Profile Generation & First Entry

Status: ready-for-dev

## Story

As a new player,
I want my profile to be created automatically,
So that I can start recording matches immediately.

## Acceptance Criteria

- **Given** first-time authentication
- **When** profile is created
- **Then** nickname is generated from email prefix (alphanumeric only)
- **And** nickname uniqueness is guaranteed via collision handling
- **And** deterministic default placeholder avatar is assigned
- **And** no PII (e.g., real name) is extracted or stored from the provider

## Developer Context

This story focuses on backend logic during the OAuth2 user creation flow in `UserService.findOrCreate`, and frontend verification in `HomeHub`. Ensure that when a *new* user is created, their `nickname` is derived from their email prefix and a deterministic default placeholder avatar is set.

### Architecture Guardrails

- **Data Privacy (GDPR/FR34):** DO NOT extract or store the user's full name from the OAuth2 provider. Only store auth credentials and the generated profile data (`nickname`, `avatar`). If a `name` field exists on the `User` entity, ignore it or remove it from the extraction logic.
- **Nickname Uniqueness Resolution:** `nickname` must be unique. If a collision occurs (e.g., `userRepository.existsByNickname(nickname)`), append a random 4-digit number in a `while` loop until unique. **Add a safeguard of max 10 iterations** to prevent infinite loops.
- **Email Prefix Parsing:** Strip non-alphanumeric characters from the email prefix using the regex `[^a-zA-Z0-9]` (e.g., `john.doe@gmail.com` -> `johndoe`) to ensure display-safe nicknames.
- **Deterministic Avatar:** Strictly use the Dicebear v7 API (`https://api.dicebear.com/7.x/identicon/svg?seed=[email_hash]`). The seed must be the **SHA-256 hash** of the user's email to ensure consistency.
- **Data Flow:** The generated `nickname` and `avatar` must be returned to the frontend either inside the stateless JWT claims or via a `/api/v1/profile/me` endpoint. Do not invent new, redundant REST endpoints.
- **The 500-Line Rule (IP-04):** No source file or test class should exceed 500 lines. This applies to all modifications in this story.

### Technical Requirements & File Structure

**Files to modify:**
- `src/main/java/com/tictactore/service/UserService.java`: 
  - Update `findOrCreate` to extract the alphanumeric email prefix.
  - Implement the uniqueness `while` loop with a max 10 iteration safeguard and UUID fallback.
  - Set the initial deterministic `avatar` URL using standard `MessageDigest`.
- `src/test/java/com/tictactore/service/UserServiceTest.java`: 
  - Add tests for email prefix extraction, non-alphanumeric stripping, and uniqueness collision handling.
  - Add tests verifying the SHA-256 avatar seed.
- `frontend/src/features/profile/HomeHub.vue` (or equivalent auth state handler within the correct architectural boundary):
  - Verify that the newly generated `nickname` and `avatar` are properly fetched and displayed upon first login without requiring manual page reloads.

### Previous Story Intelligence & Learnings

- **From Story 1.1:** The `UserService.findOrCreate()` method handles both new and returning users. Updating the nickname logic MUST ONLY apply to *new* users so custom modifications by returning users aren't overwritten.
- **From Story 1.1:** `providerId` matching is enforced to prevent email collision account takeovers. Ensure these changes preserve that security fix.

### Testing Requirements

- Unit tests must verify that `email` prefix extraction correctly forms the `nickname`, stripping special characters.
- Unit tests must mock `userRepository.existsByNickname` to return `true` initially and verify that the 4-digit random suffix fallback works properly.
- Test that returning users do not have their custom avatars or nicknames overwritten upon subsequent logins.
- **Testing Co-location Rules:** Frontend unit tests must be co-located with their components (e.g., `HomeHub.spec.ts` next to `HomeHub.vue`), or E2E tests placed in the `frontend/e2e/` directory, as mandated by the architecture.

## Completion Status
Ultimate context engine analysis completed - comprehensive developer guide createdvue`), or E2E tests placed in the `frontend/e2e/` directory, as mandated by the architecture.

## Completion Status
Ultimate context engine analysis completed - comprehensive developer guide created
## Dev Notes
### ATDD Artifacts
- Checklist: _bmad-output/test-artifacts/atdd-checklist-1-3-automatic-profile-generation-and-first-entry.md
- API tests: src/test/java/com/tictactore/service/UserServiceTest.java
- E2E tests: frontend/e2e/profile-generation.spec.ts
