# PR/MR Blocking Rules

## What to review

Focus ONLY on lines changed in this diff. Evaluate for:

- **Hitting the target**: PR solves the stated problem, all acceptance criteria are met, both positive and negative scenarios are covered.
- **Correctness**: logic errors, null/undefined handling, race conditions, off-by-ones, broken APIs, edge cases, deadlocks.
- **Performance**: N+1 query problems, memory leaks, load degradation.
- **Security**: injection risks (SQLi/command/XSS), hardcoded secrets, insecure crypto, auth/authz flaws, sensitive/personal data leaks (logs, URLs etc.), validate inputs, verify authentication, CSRF protection, file uploads validation (type, size, content), passwords hashed, sessions managed securely, PII, insecure dependencies.
- **Reliability**: missing error handling where it matters, unhandled promise rejections, resource leaks, transaction integrity(rollbacks).
- **Tests**: new non-trivial logic without any test, or tests that assert nothing meaningful, edge cases covered, tests must fail on broken code.

## What NOT to flag (false-positive filter)

Skip these — they add noise and erode trust:

- Pre-existing issues in lines this PR did NOT modify.
- Things a linter, typechecker, formatter, or compiler would catch (imports, type errors, style, trailing whitespace).
- Pedantic nitpicks a senior engineer wouldn't raise.
- Missing test coverage for trivial changes, missing docs, refactor suggestions beyond the diff's scope.
- Stylistic preferences not codified in project rules.
- Changes clearly intentional to the PR's goal even if they look unusual.
- Hypothetical issues ("what if a future caller…") — only flag concrete problems.
