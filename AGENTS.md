# AGENTS.md

## Project Rules

### Code Standards

1. When writing production code use /Users/ppolukhin/.agents/skills/code-1-guide/SKILL.md
2. When writing test code use /Users/ppolukhin/.agents/skills/code-2-test/SKILL.md

## Agent Execution & Validation Rules

- **Strict Verification & Test Granularity**: Run targeted unit/component tests during active development for the specific area being modified. Run the full local verification script (`./scripts/ci-local.sh`) only ONCE at the end of the task when ~95% confident in the complete solution. NEVER present a bug fix or feature completion to the user without first running `./scripts/ci-local.sh`.
- **Test Environment Parity**: Backend tests must not blindly mock critical startup configuration (e.g., via `application.properties` in `src/test/resources`). If an environment variable is required for production startup, the application must handle its absence gracefully (defaults), or the test must accurately simulate its absence.
- **Boundary Testing**: Features involving frontend-to-backend proxies, static asset bundling, or OAuth redirects cannot be validated by unit tests alone. You must either write an E2E test (Playwright) or manually verify the integration using `curl`/shell scripts before marking the task complete.
- **No Premature Completion**: Task checkboxes in stories or plans MUST ONLY be marked as complete AFTER a corresponding test verifying the functionality has been written and successfully passed.
- **No Leaked Secrets**: NEVER hardcode or commit real private keys, API secrets, tokens, or credentials into repository files or default configuration fallbacks. Always use environment variables (`${VAR:dummy_placeholder}`) and store production secrets exclusively in secure secret managers / GitHub Secrets.
- **Real Service Verification & JWT**: If verifying a hypothesis via an actual request to a real service is more precise and cheaper than analyzing code and theorizing, but fails due to a required JWT, explicitly ask the user for the JWT or retrieve it yourself if simpler, without modifying code.

## BMAD Workflow Rules

### Branches & PRs

- Every story must be processed in a dedicated feature branch (e.g., `story/1-1-...`).
- Merge changes into the `develop` branch exclusively via Pull Requests (PRs).
- Direct commits to `develop` are strictly prohibited.

### Story Status Labels

- `backlog` — Story exists only in the epic file.
- `ready-for-dev` — Story file has been created.
- `in-progress` — Work is currently underway.
- `review` — Ready for code review.
- `done` — Completed.

### GitHub Issue Synchronization

When mirroring a story to GitHub, follow this checklist in order:

1. **Duplicate check** — before creating, run `gh issue list --search "Story X.Y"`. If exists — edit, do not create.
2. **Issue body** — verbatim full content of the story MD file from `_bmad-output/implementation-artifacts/`. Not a summary. If the file lives on a feature branch: `git show <branch>:path/to/file.md`.
3. **Issue fields** — `--assignee phalbohr`, `--label <story-status>`.
4. **Relationships** — add `Depends on #N` / `Related to #N` in body based on story Dev Notes dependencies.

### Creating PRs

1. **Duplicate check** — `gh pr list --search "story/X-Y"` before creating.
2. **Branch** — `story/X-Y-slug` from `develop`.
3. **PR fields** — `--assignee phalbohr`, `--label <story-status>`, `--body "Closes #N"` (only this line, nothing else).
4. **Reviewer** — do NOT add Jules manually — triggered automatically via workflow.
5. **GitHub Project** — add PR to project «Tic-Tac-Tore» (owner: phalbohr, project #2).

### Label sync

On every status transition, update label on **both** issue and PR:

```
gh issue edit <N> --remove-label <old> --add-label <new>
gh pr edit <N> --remove-label <old> --add-label <new>
```

After `bmad-code-review`: `gh issue edit <N> --add-label review` — triggers Jules.

## Knowledge Base (Brain)

When "brain" is mentioned in this project context, refer to `/Users/ppolukhin/Brain/projects/tic-tac-tore` (or root `/Users/ppolukhin/Brain`).
