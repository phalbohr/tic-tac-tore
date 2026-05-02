## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)

## Agent Execution & Validation Rules
- **Strict Verification**: NEVER present a bug fix or feature completion to the user without first running the full local verification script (`./scripts/ci-local.sh`). You must empirically prove your fix works.
- **Test Environment Parity**: Backend tests must not blindly mock critical startup configuration (e.g., via `application.properties` in `src/test/resources`). If an environment variable is required for production startup, the application must handle its absence gracefully (defaults), or the test must accurately simulate its absence.
- **Boundary Testing**: Features involving frontend-to-backend proxies, static asset bundling, or OAuth redirects cannot be validated by unit tests alone. You must either write an E2E test (Playwright) or manually verify the integration using `curl`/shell scripts before marking the task complete.

## BMAD Workflow Rules

### Branches & PRs
- Every story must be processed in a dedicated feature branch (e.g., `story/1-1-...`).
- Merge changes into the `develop` branch exclusively via Pull Requests (PRs).
- Direct commits to `develop` are strictly prohibited.

### Creating PRs (`gh pr create`)
- `--assignee`: `phalbohr`
- `--label`: story status
- `--body`: Must include `Closes #N`
- **Reviewer**: Do NOT add Jules manually — it is triggered automatically via workflow.

### Story Status Labels
- `backlog` — Story exists only in the epic file.
- `ready-for-dev` — Story file has been created.
- `in-progress` — Work is currently underway.
- `review` — Ready for code review.
- `done` — Completed.

### GitHub Issue Synchronization
- Creation/modification/deletion of a story file must be mirrored to the GitHub Issue (`gh issue create` / `gh issue edit`).
- After running `bmad-code-review`, update the issue: `gh issue edit <N> --add-label review` (This acts as a trigger for Jules).
