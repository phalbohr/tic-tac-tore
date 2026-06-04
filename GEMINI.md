## Code Analysis: ast-grep + graphify

**Decision tree — run before any file Read or grep:**

| Question type                              | Tool                                                        | Why                                               |
| ------------------------------------------ | ----------------------------------------------------------- | ------------------------------------------------- |
| "How does X relate to Y?"                  | `graphify path "X" "Y"`                                     | traverses EXTRACTED+INFERRED edges, zero API cost |
| "What is X / what depends on it?"          | `graphify explain "X"`                                      | community + neighbor context in one call          |
| "Architecture overview / god nodes"        | read `graphify-out/GRAPH_REPORT.md`                         | pre-built, no scanning                            |
| "Where is X defined / all callers of X"    | ast-grep `pattern` or `kind` rule                           | AST match, no false positives, no API cost        |
| "All classes/methods matching a structure" | ast-grep `scan --inline-rules`                              | structural, handles generics/annotations          |
| "Impact of changing X"                     | ast-grep (callers) + `graphify explain "X"` (semantic deps) | structural + inferred together                    |

### Combined workflow (best quality, fewest tokens)

1. **graphify first** — identify relevant community + god nodes from `GRAPH_REPORT.md`; if `graphify-out/wiki/index.md` exists, navigate it instead of raw files
2. **ast-grep second** — locate exact implementations in those files
3. **Read last** — only files you will actually edit
4. **After editing code** — run `graphify update .` to keep graph current (AST-only, no API cost)

### Never do first

- `grep` for symbol search → use ast-grep (structural, no regex false positives)
- `Read` files to understand architecture → use graphify
- Multiple Bash find/grep/cat chains → replace with one ast-grep scan or `ctx_batch_execute`

### Applies to subagent prompts too

When spawning Explore or Plan agents, the same rules apply. Agent prompts MUST instruct subagents to use ast-grep + graphify — never `grep`/`cat`/`find` for symbol search or architecture analysis. Write the tool instructions explicitly in the agent prompt.

### ast-grep quick reference (Java / Kotlin)

```bash
# Find all callers of a method
ast-grep scan --inline-rules "id: r
language: java
rule:
  pattern: \$OBJ.methodName(\$\$\$)" src/

# Find classes implementing an interface
ast-grep scan --inline-rules "id: r
language: java
rule:
  pattern: class \$NAME implements TargetInterface { \$\$\$ }" src/

# Debug AST node kinds
ast-grep run --pattern 'someCode()' --lang java --debug-query=ast src/
```

## Project Rules

### Code Standards

- **Writing code**: Follow rules in `_project-spec/rules/1-write.md`. Read this file before writing any production code.
- **Writing tests**: Follow rules in `_project-spec/rules/2-test.md`. Read this file before writing any tests.

#### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

#### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

#### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting (unless explicitly asked).
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently. Only suggest changing the style verbally if there are clear reasons (ask for permission before making changes).
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

#### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

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
