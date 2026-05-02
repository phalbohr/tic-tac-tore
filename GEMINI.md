## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)

## BMAD Workflow Rules
- Process every new BMAD user story in a dedicated feature branch.
- Merge changes into the `develop` branch exclusively via Pull Requests (PRs). Do not commit directly to `develop`.
- **GitHub Synchronization**: Any operation on a User Story (creation, modification, or deletion) must be automatically mirrored to its corresponding GitHub Issue.
- **Jules Integration**: Upon completion of a local `bmad-code-review`, the Story's GitHub Issue must be updated with the `review` label to trigger further verification by Google Jules.
