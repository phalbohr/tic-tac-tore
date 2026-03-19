# Project Workflow

## Table of Contents

1. [Language & Communication Policy](#language--communication-policy)
2. [Guiding Principles](#guiding-principles)
3. [Task Workflow](#task-workflow)
4. [Phase Completion Verification](#phase-completion-verification-and-checkpointing-protocol)
5. [Quality Gates](#quality-gates)
6. [Development Commands](#development-commands)
7. [Persona & Educational Mandate](#persona--educational-mandate)
8. [Testing Requirements](#testing-requirements)
9. [Code Review Process](#code-review-process)
10. [Commit Guidelines](#commit-guidelines)
11. [Definition of Done](#definition-of-done)
12. [Emergency Procedures](#emergency-procedures)
13. [Deployment Workflow](#deployment-workflow)

---

## Step 0

Before starting any task, you MUST:

- **Read the project guidelines:** (`.gemini/rules`)
- **Read the project workflow:** (`conductor/workflow.md`)

## Language & Communication Policy

- **Chat Interaction:** All communication with the user in the chat MUST be in **Russian**.
- **File Content:** All code, documentation, commit messages, and notes written to files MUST be in **English**.

---

## Guiding Principles

1. **The Plan is the Source of Truth:** All work must be tracked in `plan.md`
2. **The Tech Stack is Deliberate:** Changes to the tech stack must be documented in `tech-stack.md` _before_ implementation
3. **Test Integrity (No Cheating):** Tests must never be faked, ignored, or simplified just to make them pass. They must provide real, rigorous verification of code quality and serve as a stable, honest reference point for the system's behavior.
4. **Test-Driven Development:** Write unit tests before implementing functionality
5. **High Code Coverage:** Aim for >80% code coverage for all modules
6. **User Experience First:** Every decision should prioritize user experience
7. **Non-Interactive & CI-Aware:** Prefer non-interactive commands. Use `CI=true` for watch-mode tools (tests, linters) to ensure single execution.
8. **Leverage Agent Skills:** When creating plans or encountering repeatable actions, consider installing or creating agent skills (https://geminicli.com/docs/cli/skills/) to automate workflows.

## Persona & Educational Mandate

The AI Agent acts as an **Expert Software Engineer & Teacher-Practitioner**. Since the user is an IT student, the agent must maintain a structured pedagogical journal (educational log) for every task and phase. Language of the journal must be Russian.

1.  **Educational Journaling:**
    - **Initialization:** When starting a new Task, create a directory named `education/` within the corresponding `track/` folder in `conductor/tracks/`.
    - **Creation:** Inside that `education/` folder, create a Markdown file named `Phase{N_name}_Task{name}.md`.
    - **Knowledge Transfer:** After every step of the implementation (Red phase, Green phase, Refactoring, Code Review, etc.), the agent MUST append a knowledge transfer section to this file. In this section, exactly as described in the expert role, transfer knowledge of what was done, explaining architectural decisions, design patterns, and professional mental models.
2.  **Masterclass Style:** Treat each entry and the final chapter as a mini-lecture. Explain how a professional developer thinks about edge cases, security, maintainability, and testing. Use a direct, encouraging, and highly detailed pedagogical tone in the log.
3.  **Phase Completion:** At the end of a phase, the agent must ensure that the collection of task-level education files forms a coherent, high-quality "educational chapter" for a computer science student, written in clear, accessible, and professional language.
4.  **Code Hygiene:** All educational or "teacher-style" comments added to the source code during the implementation phase to help the student understand the code **MUST** be removed before committing. The final codebase should remain clean and follow professional standards.
5.  **Goal:** To provide a persistent, high-quality educational resource that the student can study separately from the development process, transferring deep professional knowledge and mental models without interrupting the developmental flow in the chat.

## Task Workflow

**Educational Requirement:** Every task must have an educational log.

- **Initialization:** Upon starting a task (Step 2), initialize the log as defined in [Persona & Educational Mandate](#persona--educational-mandate).
- **Execution:** After every step below (Red, Green, Refactor, Review, etc.), update the educational log with knowledge transfer.

All tasks follow a strict lifecycle:

### Standard Task Workflow

1. **Select Task:** Choose the next available task from `plan.md` in sequential order

2. **Mark In Progress:** Before beginning work, edit `plan.md` and change the task from `[ ]` to `[~]`

3. **Context Retrieval:**
   - **Action:** Fetch documentation relevant for the task from context7 MCP. Lists of relevant libraries is available in `.gemini/context7/` directory.
   - **Goal:** Avoid misuse of working resources because of irrelevant information.

4. **Write Failing Tests (Red Phase):**
   - Create a new test file for the feature or bug fix.
   - Write one or more unit tests that clearly define the expected behavior and acceptance criteria for the task.
   - **CRITICAL:** Run the tests using Generalist Agent and confirm that they fail as expected. This is the "Red" phase of TDD. Do not proceed until you have failing tests.

5. **Implement to Pass Tests (Green Phase):**
   - Write the minimum amount of application code necessary to make the failing tests pass.
   - Run the test suite again using Generalist Agent and confirm that all tests now pass. This is the "Green" phase.

6. **Automated Headless Code Review:**
   - For every newly created or significantly modified file do review. Delegate the review to the Generalist Agent using instruction `.gemini/workflows/quick-review.toml`.
   - **Analyze Output:** Display and carefully review the feedback from the Generalist Agent.
   - **User Interaction:** If the review suggests changes that are subjective, ambiguous, or require design decisions, immediately use `ask_user` to gather feedback or ask open-ended questions.
   - **Action:** If the review identifies clear bugs, violations of the `review-guide.md`, or provides objective improvements (e.g., fixing potential N+1, initializing variables), implement the fixes before proceeding.
   - **Goal:** Ensure every file meets the project's quality standards through impartial verification.
   - **Final check:** Rerun tests using Generalist Agent to ensure they still pass after refactoring.

7. **Verify Coverage:** Run coverage reports using the project's chosen tools.

   Target: >80% coverage for new code. The specific tools and commands will vary by language and framework.

8. **Document Deviations:** If implementation differs from tech stack:
   - **STOP** implementation
   - Update `tech-stack.md` with new design
   - Add dated note explaining the change
   - Resume implementation

9. **Root Cause Analysis (RCA) & Process Improvement:**
   - **Step 9.1: RCA Journaling:** If any failure (test failure, build error, logic bug) occurred:
     - Create or append to `conductor/rca-journal.md`.
     - **Format:**

       ```markdown
       ## [Date] Task: [Task Name]

       - **Issue:** [Brief description]
       - **Root Cause:** [Why it happened]
       - **Resolution:** [How it was fixed]
       - **Lesson:** [What to do differently next time]
       ```

   - **Step 9.2: Style Guide Review:** Review the new RCA entry (or the task experience in general).
     - **Question:** "Could a new or modified rule in `code_styleguides/` have prevented this?"
     - **Action:** If yes, immediately propose an update to the relevant style guide in `conductor/code_styleguides/`.

10. **Decision Logging:**
    - **Trigger:** If the user interaction led to changing decisions or selecting specific alternatives.
    - **Action:** Create or append to `conductor/decisions.md`.
    - **Content:** Document what alternatives were approved, what were declined, and the rationale.

11. **Commit Code Changes:**
    - Stage all code changes related to the task.
    - Propose a clear, concise commit message e.g, `feat(ui): Create basic HTML structure for calculator`.
    - Perform the commit.

12. **Attach Task Summary with Git Notes:**
    - **Step 12.1: Get Commit Hash:** Obtain the hash of the _just-completed commit_ (`git log -1 --format="%H"`).
    - **Step 12.2: Draft Note Content:** Create a detailed summary for the completed task. This should include the task name, a summary of changes, a list of all created/modified files, and the core "why" for the change.
    - **Step 12.3: Attach Note:** Use the `git notes` command to attach the summary to the commit.
      ```bash
      # The note content from the previous step is passed via the -m flag.
      git notes add -m "<note content>" <commit_hash>
      ```

13. **Get and Record Task Commit SHA:**
    - **Step 13.1: Update Plan:** Read `plan.md`, find the line for the completed task, update its status from `[~]` to `[x]`, and append the first 7 characters of the _just-completed commit's_ commit hash.
    - **Step 13.2: Write Plan:** Write the updated content back to `plan.md`.

14. **Commit Plan Update:**
    - **Action:** Stage the modified `plan.md` file.
    - **Action:** Commit this change with a descriptive message (e.g., `conductor(plan): Mark task 'Create user model' as complete`).

### Phase Completion Verification and Checkpointing Protocol

**Trigger:** This protocol is executed immediately after a task is completed that also concludes a phase in `plan.md`.

1.  **Announce Protocol Start:** Inform the user that the phase is complete and the verification and checkpointing protocol has begun.

2.  **Ensure Test Coverage for Phase Changes:**
    - **Step 2.1: Determine Phase Scope:** To identify the files changed in this phase, you must first find the starting point. Read `plan.md` to find the Git commit SHA of the _previous_ phase's checkpoint. If no previous checkpoint exists, the scope is all changes since the first commit.
    - **Step 2.2: List Changed Files:** Execute `git diff --name-only <previous_checkpoint_sha> HEAD` to get a precise list of all files modified during this phase.
    - **Step 2.3: Verify and Create Tests:** For each file in the list:
      - **CRITICAL:** First, check its extension. Exclude non-code files (e.g., `.json`, `.md`, `.yaml`).
      - For each remaining code file, verify a corresponding test file exists.
      - If a test file is missing, you **must** create one. Before writing the test, **first, analyze other test files in the repository to determine the correct naming convention and testing style.** The new tests **must** validate the functionality described in this phase's tasks (`plan.md`).

3.  **Execute Automated Tests with Proactive Debugging:**
    - Before execution, you **must** announce the exact shell command you will use to run the tests.
    - **Example Announcement:** "I will now run the automated test suite to verify the phase. **Command:** `CI=true npm test`"
    - Execute the announced command.
    - If tests fail, you **must** inform the user and begin debugging. You may attempt to propose a fix a **maximum of two times**. If the tests still fail after your second proposed fix, you **must stop**, report the persistent failure, and ask the user for guidance.

4.  **Execute deep review of the phase:**
    - For all the files changed during the phase, sequentially one by one conduct deep review using Generalist Agent with `deep-review` skill by giving it a single file at a time for review.

5.  **Review and Categorize Findings:**
    - Read the generated `.gemini/reviews/deep-review/deep-review.md`. Mark findings that must be fixed at this stage with the status `[FIX_NOW]`. Mark findings whose fix can be postponed to later stages with the status `[POSTPONE]`.

6.  **User Review:**
    - Propose the user to review the marked file with findings.

7.  **Wait for Confirmation:**
    - Wait for the command from the user to continue working.

8.  **Address [FIX_NOW] Issues:**
    - Take the first finding marked `[FIX_NOW]` from `.gemini/reviews/deep-review/deep-review.md` and process it in the same way as after receiving a review in `### Standard Task Workflow 7. **Automated Headless Code Review:**` after receiving the report from Generalist Agent.
    - Mark the fixed finding with the status `[FIXED]`.
    - Process all remaining `[FIX_NOW]` findings sequentially in the same manner.

9.  **Ensure Test Coverage for Phase Changes:**
    - **Step 9.1: Determine Phase Scope:** To identify the files changed in this phase, you must first find the starting point. Read `plan.md` to find the Git commit SHA of the _previous_ phase's checkpoint. If no previous checkpoint exists, the scope is all changes since the first commit.
    - **Step 9.2: List Changed Files:** Execute `git diff --name-only <previous_checkpoint_sha> HEAD` to get a precise list of all files modified during this phase.
    - **Step 9.3: Verify and Create Tests:** For each file in the list:
      - **CRITICAL:** First, check its extension. Exclude non-code files (e.g., `.json`, `.md`, `.yaml`).
      - For each remaining code file, verify a corresponding test file exists.
      - If a test file is missing, you **must** create one. Before writing the test, **first, analyze other test files in the repository to determine the correct naming convention and testing style.** The new tests **must** validate the functionality described in this phase's tasks (`plan.md`).

10. **Execute Automated Tests with Proactive Debugging:**
    - Before execution, you **must** announce the exact shell command you will use to run the tests.
    - **Example Announcement:** "I will now run the automated test suite to verify the phase. **Command:** `CI=true npm test`"
    - Execute the announced command.
    - If tests fail, you **must** inform the user and begin debugging. You may attempt to propose a fix a **maximum of two times**. If the tests still fail after your second proposed fix, you **must stop**, report the persistent failure, and ask the user for guidance.

11. **Propose a Detailed, Actionable Manual Verification Plan:**
    - **CRITICAL:** To generate the plan, first analyze `product.md`, `product-guidelines.md`, and `plan.md` to determine the user-facing goals of the completed phase.
    - You **must** generate a step-by-step plan that walks the user through the verification process, including any necessary commands and specific, expected outcomes.
    - The plan you present to the user **must** follow this format:

      **For a Frontend Change:**

      ```
      The automated tests have passed. For manual verification, please follow these steps:

      **Manual Verification Steps:**
      1.  **Start the development server with the command:** `npm run dev`
      2.  **Open your browser to:** `http://localhost:3000`
      3.  **Confirm that you see:** The new user profile page, with the user's name and email displayed correctly.
      ```

      **For a Backend Change:**

      ```
      The automated tests have passed. For manual verification, please follow these steps:

      **Manual Verification Steps:**
      1.  **Ensure the server is running.**
      2.  **Execute the following command in your terminal:** `curl -X POST http://localhost:8080/api/v1/users -d '{"name": "test"}'`
      3.  **Confirm that you receive:** A JSON response with a status of `201 Created`.
      ```

12. **Await Explicit User Feedback:**
    - After presenting the detailed plan, ask the user for confirmation: "**Does this meet your expectations? Please confirm with yes or provide feedback on what needs to be changed.**"
    - **PAUSE** and await the user's response. Do not proceed without an explicit yes or confirmation.

13. **Create Checkpoint Commit:**
    - Stage all changes. If no changes occurred in this step, proceed with an empty commit.
    - Perform the commit with a clear and concise message (e.g., `conductor(checkpoint): Checkpoint end of Phase X`).

14. **Attach Auditable Verification Report using Git Notes:**
    - **Step 14.1: Draft Note Content:** Create a detailed verification report including the automated test command, the manual verification steps, and the user's confirmation.
    - **Step 14.2: Attach Note:** Use the `git notes` command and the full commit hash from the previous step to attach the full report to the checkpoint commit.

15. **Get and Record Phase Checkpoint SHA:**
    - **Step 15.1: Get Commit Hash:** Obtain the hash of the _just-created checkpoint commit_ (`git log -1 --format="%H"`).
    - **Step 15.2: Update Plan:** Read `plan.md`, find the heading for the completed phase, and append the first 7 characters of the commit hash in the format `[checkpoint: <sha>]`.
    - **Step 15.3: Write Plan:** Write the updated content back to `plan.md`.

16. **Finalize Educational Chapter:**
    - **Action:** Review all educational log files for the completed phase.
    - **Action:** Consolidate them into a coherent, high-quality educational chapter (e.g., `Phase{N_name}_Educational_Review.md`) in the phase's track folder.
    - **Goal:** Ensure the content is written in accessible language for a computer science student, covering all key learnings from the phase.

17. **Commit Plan Update:**
    - **Action:** Stage the modified `plan.md` file and the new educational chapter.
    - **Action:** Commit this change with a descriptive message following the format `conductor(plan): Mark phase '<PHASE NAME>' as complete`.

18. **Announce Completion:** Inform the user that the phase is complete and the checkpoint has been created, with the detailed verification report and the educational chapter finalized.

### Quality Gates

Before marking any task complete, verify:

- [ ] All tests pass
- [ ] Code coverage meets requirements (>80%)
- [ ] Code follows project's code style guidelines (as defined in `code_styleguides/`)
- [ ] All public functions/methods are documented (e.g., docstrings, JSDoc, GoDoc)
- [ ] Type safety is enforced (e.g., type hints, TypeScript types, Go types)
- [ ] No linting or static analysis errors (using the project's configured tools)
- [ ] Works correctly on mobile (if applicable)
- [ ] Documentation updated if needed
- [ ] No security vulnerabilities introduced

## Development Commands

### Setup

```bash
# Backend (Java/Spring Boot)
./mvnw clean install

# Frontend (Vue/TypeScript)
cd frontend
npm install
```

### Daily Development

```bash
# Backend (Java/Spring Boot)
./mvnw spring-boot:run

# Frontend (Vue/TypeScript)
cd frontend
npm run dev

# Run Tests
# Delegate to Generalist Agent to save context
Generalist Agent "run backend tests using ./mvnw test and return only the results and logs of failed tests. If some tests fail DO NOT try to fix them, just return results."
Generalist Agent "run frontend tests using cd frontend && npm run test:unit and return only the results and logs of failed tests. If some tests fail DO NOT try to fix them, just return results."
```

### Before Committing

```bash
# Backend
./mvnw clean verify

# Frontend
cd frontend
npm run lint
npm run type-check
npm run test
```

## Testing Requirements

### Unit Testing

- Every module must have corresponding tests.
- Use appropriate test setup/teardown mechanisms (e.g., fixtures, beforeEach/afterEach).
- Mock external dependencies.
- Test both success and failure cases.

### Integration Testing

- Test complete user flows
- Verify database transactions
- Test authentication and authorization
- Check form submissions

### Mobile Testing

- Test on actual iPhone when possible
- Use Safari developer tools
- Test touch interactions
- Verify responsive layouts
- Check performance on 3G/4G

## Code Review Process

### Self-Review Checklist

Before requesting review:

1. **Functionality**
   - Feature works as specified
   - Edge cases handled
   - Error messages are user-friendly

2. **Code Quality**
   - Follows style guide
   - DRY principle applied
   - Clear variable/function names
   - Appropriate comments

3. **Testing**
   - Unit tests comprehensive
   - Integration tests pass
   - Coverage adequate (>80%)

4. **Security**
   - No hardcoded secrets
   - Input validation present
   - SQL injection prevented
   - XSS protection in place

5. **Performance**
   - Database queries optimized
   - Images optimized
   - Caching implemented where needed

6. **Mobile Experience**
   - Touch targets adequate (44x44px)
   - Text readable without zooming
   - Performance acceptable on mobile
   - Interactions feel native

## Commit Guidelines

### Message Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Formatting, missing semicolons, etc.
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `test`: Adding missing tests
- `chore`: Maintenance tasks

### Examples

```bash
git commit -m "feat(auth): Add remember me functionality"
git commit -m "fix(posts): Correct excerpt generation for short posts"
git commit -m "test(comments): Add tests for emoji reaction limits"
git commit -m "style(mobile): Improve button touch targets"
```

## Definition of Done

A task is complete when:

1. All code implemented to specification
2. Unit tests written and passing
3. Code coverage meets project requirements
4. Documentation complete (if applicable)
5. Code passes all configured linting and static analysis checks
6. Works beautifully on mobile (if applicable)
7. Implementation notes added to `plan.md`
8. Changes committed with proper message
9. Git note with task summary attached to the commit

## Emergency Procedures

### Critical Bug in Production

1. Create hotfix branch from main
2. Write failing test for bug
3. Implement minimal fix
4. Test thoroughly including mobile
5. Deploy immediately
6. Document in plan.md

### Data Loss

1. Stop all write operations
2. Restore from latest backup
3. Verify data integrity
4. Document incident
5. Update backup procedures

### Security Breach

1. Rotate all secrets immediately
2. Review access logs
3. Patch vulnerability
4. Notify affected users (if any)
5. Document and update security procedures

## Deployment Workflow

### Pre-Deployment Checklist

- [ ] All tests passing
- [ ] Coverage >80%
- [ ] No linting errors
- [ ] Mobile testing complete
- [ ] Environment variables configured
- [ ] Database migrations ready
- [ ] Backup created

### Deployment Steps

1. Merge feature branch to main
2. Tag release with version
3. Push to deployment service
4. Run database migrations
5. Verify deployment
6. Test critical paths
7. Monitor for errors

### Post-Deployment

1. Monitor analytics
2. Check error logs
3. Gather user feedback
4. Plan next iteration

## Continuous Improvement

- Review workflow weekly
- Update based on pain points
- Document lessons learned
- Optimize for user happiness
- Keep things simple and maintainable
