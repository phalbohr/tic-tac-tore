## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:

- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)

## Coding Standards

- **Writing code**: Follow rules in `_project-spec/rules/1-write.md`. Read this file before writing any production code.
- **Writing tests**: Follow rules in `_project-spec/rules/2-test.md`. Read this file before writing any tests.

## Agent Execution & Validation Rules

- **Strict Verification**: NEVER present a bug fix or feature completion to the user without first running the full local verification script (`./scripts/ci-local.sh`). You must empirically prove your fix works.
- **Test Environment Parity**: Backend tests must not blindly mock critical startup configuration (e.g., via `application.properties` in `src/test/resources`). If an environment variable is required for production startup, the application must handle its absence gracefully (defaults), or the test must accurately simulate its absence.
- **Boundary Testing**: Features involving frontend-to-backend proxies, static asset bundling, or OAuth redirects cannot be validated by unit tests alone. You must either write an E2E test (Playwright) or manually verify the integration using `curl`/shell scripts before marking the task complete.
- **No Premature Completion**: Task checkboxes in stories or plans MUST ONLY be marked as complete AFTER a corresponding test verifying the functionality has been written and successfully passed.

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
