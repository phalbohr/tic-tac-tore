export const meta = {
  name: 'bmad-story-pipeline',
  description: 'End-to-end BMAD story pipeline: create → validate → ATDD → dev/review loop → automate → quality gates, looping until green',
  whenToUse: 'Drive one user story from creation to verified implementation autonomously. Pass the story identifier as args (e.g. "1-5-account-deletion-with-anonymization", "1-5", or "1.5").',
  phases: [
    { title: 'Create' },
    { title: 'Validate' },
    { title: 'ATDD' },
    { title: 'Dev Loop' },
    { title: 'Automate' },
    { title: 'Quality Gates' },
  ],
}

// ---------------------------------------------------------------------------
// BMAD story pipeline
//
// Stages (mirrors the user-requested flow):
//   1. /bmad-create-story  (Create mode)
//   2. /bmad-create-story  (Validate mode)  — auto-fixes issues it finds
//   3. /bmad-testarch-atdd                  — red-phase acceptance scaffolds
//   4. OUTER LOOP (max MAX_OUTER):
//        INNER LOOP (max MAX_INNER):
//          4a. /bmad-dev-story              — implement / address findings
//          4b. /bmad-code-review            — adversarial review, writes findings to story file
//          → repeat until code-review is clean
//        5. /bmad-testarch-automate         — expand coverage
//        6. parallel: /bmad-testarch-test-review + /bmad-testarch-nfr  (read-only)
//        → if either gate is not PASS, carry findings back into stage 4
//
// Why subagents EXECUTE the skill files instead of calling the Skill tool:
//   - The Skill tool is a main-conversation construct; workflow subagents
//     can't rely on having it.
//   - BMAD skills are interactive (<ask>, HALT menus, [C]/[R]/[V]/[E]).
//   Each agent therefore reads `.claude/skills/<skill>/SKILL.md` and runs the
//   workflow itself, NON-INTERACTIVELY, auto-answering every prompt.
//
// Stage hand-off uses the FILE CONTRACT (robust, not text-parsing):
//   - story file:  _bmad-output/implementation-artifacts/<key>.md
//   - statuses:    sprint-status.yaml  (backlog→ready-for-dev→in-progress→review→done)
//   - code-review writes findings into the story's "### Review Findings" section;
//     dev-story's review-continuation reads them back (the inner loop).
//   - test-review/nfr run read-only in parallel (no write race) and their
//     findings are injected into the next dev pass via the prompt (the outer loop).
// ---------------------------------------------------------------------------

const IMPL_DIR = '_bmad-output/implementation-artifacts'
const SPRINT = IMPL_DIR + '/sprint-status.yaml'
const SKILLS = '.claude/skills'

// --- resolve args -----------------------------------------------------------
const STORY = typeof args === 'string'
  ? args.trim()
  : (args && (args.story || args.id) ? String(args.story || args.id).trim() : '')

if (!STORY) {
  throw new Error(
    'bmad-story-pipeline requires a story identifier as args, e.g. ' +
    '"1-5-account-deletion-with-anonymization" or "1-5" or "1.5"'
  )
}

const MAX_INNER = (args && Number(args.maxInner)) || 3 // dev↔review rounds per outer pass
const MAX_OUTER = (args && Number(args.maxOuter)) || 3 // full quality passes (step 6 → step 4)

log(`▶ BMAD pipeline for story "${STORY}" (inner≤${MAX_INNER}, outer≤${MAX_OUTER})`)

// --- schemas ---------------------------------------------------------------
const CREATE_SCHEMA = {
  type: 'object',
  properties: {
    created: { type: 'boolean' },
    storyKey: { type: 'string', description: 'e.g. 1-5-account-deletion-with-anonymization' },
    storyFile: { type: 'string', description: 'absolute or repo-relative path to the created story md' },
    status: { type: 'string', description: 'sprint-status value after this stage' },
    blockers: { type: 'array', items: { type: 'string' } },
    summary: { type: 'string' },
  },
  required: ['created', 'storyFile', 'summary'],
}

const VALIDATE_SCHEMA = {
  type: 'object',
  properties: {
    valid: { type: 'boolean' },
    issuesFound: { type: 'array', items: { type: 'string' } },
    issuesFixed: { type: 'array', items: { type: 'string' } },
    unresolved: { type: 'array', items: { type: 'string' } },
    summary: { type: 'string' },
  },
  required: ['valid', 'summary'],
}

const ATDD_SCHEMA = {
  type: 'object',
  properties: {
    created: { type: 'boolean' },
    testFiles: { type: 'array', items: { type: 'string' } },
    redConfirmed: { type: 'boolean', description: 'tests fail as expected (red phase)' },
    summary: { type: 'string' },
  },
  required: ['created', 'summary'],
}

const DEV_SCHEMA = {
  type: 'object',
  properties: {
    implemented: { type: 'boolean' },
    allAcsSatisfied: { type: 'boolean' },
    testsPass: { type: 'boolean', description: 'full regression / ci-local.sh green' },
    filesChanged: { type: 'array', items: { type: 'string' } },
    halted: { type: 'boolean', description: 'true if a HALT condition stopped progress' },
    haltReason: { type: 'string' },
    summary: { type: 'string' },
  },
  required: ['implemented', 'halted', 'summary'],
}

const REVIEW_SCHEMA = {
  type: 'object',
  properties: {
    clean: { type: 'boolean', description: 'true = no blocking findings (decision-needed or patch)' },
    blockingCount: { type: 'integer' },
    findings: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          severity: { type: 'string' },
          kind: { type: 'string', description: 'decision-needed | patch | defer' },
          title: { type: 'string' },
          location: { type: 'string' },
        },
      },
    },
    verdict: { type: 'string' },
    summary: { type: 'string' },
  },
  required: ['clean', 'blockingCount', 'summary'],
}

const AUTOMATE_SCHEMA = {
  type: 'object',
  properties: {
    added: { type: 'boolean' },
    testFiles: { type: 'array', items: { type: 'string' } },
    testsPass: { type: 'boolean' },
    summary: { type: 'string' },
  },
  required: ['added', 'summary'],
}

const GATE_SCHEMA = {
  type: 'object',
  properties: {
    gate: { type: 'string', enum: ['PASS', 'CONCERNS', 'FAIL'] },
    findings: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          severity: { type: 'string' },
          title: { type: 'string' },
          detail: { type: 'string' },
        },
      },
    },
    summary: { type: 'string' },
  },
  required: ['gate', 'summary'],
}

// --- shared prompt fragments ------------------------------------------------
function preamble(skill, mode) {
  return [
    `You are an autonomous BMAD executor running INSIDE an unattended pipeline. There is NO human available to answer questions.`,
    ``,
    `## Task`,
    `Execute the BMAD "${skill}" workflow${mode ? ` in **${mode}** mode` : ''} for story \`${STORY}\`.`,
    ``,
    `## How to run it`,
    `1. Preferred: invoke the \`${skill}\` skill via the Skill tool if that tool is available to you.`,
    `2. Otherwise (the usual case for subagents): read \`${SKILLS}/${skill}/SKILL.md\` and every step/instruction/checklist/template file it references, then carry out that workflow yourself.`,
    ``,
    `## Non-interactive mandate (critical)`,
    `- Run fully autonomously to completion in a single pass.`,
    `- The story identifier is already provided: \`${STORY}\`. Use it directly so any "determine target story" step skips its prompt. Story files live in \`${IMPL_DIR}/\`; sprint tracking is \`${SPRINT}\`.`,
    `- Wherever the instructions say to greet the user, present a mode menu ([C]/[R]/[V]/[E]), \`<ask>\`, or "HALT — waiting for your choice": DO NOT stop. Pick the most sensible option yourself${mode ? ` (this run = ${mode} mode)` : ''} and continue. Record any non-trivial decision in your summary.`,
    `- Only a genuine blocking condition (missing prerequisite artifact, contradictory spec, unfixable failure) may stop you — report it via the structured fields instead of asking.`,
    ``,
    `## Project rules`,
    `- Follow \`_project-spec/rules/1-write.md\` when writing production code and \`_project-spec/rules/2-test.md\` when writing tests. Match existing code style. Surgical changes only.`,
    `- Respond in Русский in any prose, but keep code/identifiers in their original form.`,
    ``,
    `## Output`,
    `Your final message IS the structured result (it is consumed by code, not shown to a human). Fill every schema field accurately and honestly — never report success you did not verify.`,
  ].join('\n')
}

// ===========================================================================
// STAGE 1 — Create story
// ===========================================================================
phase('Create')
const created = await agent(
  [
    preamble('bmad-create-story', 'Create'),
    ``,
    `This creates the story file from the epic + planning artifacts and flips its sprint-status from \`backlog\` to \`ready-for-dev\`.`,
    `Return the EXACT path of the story file you created in \`storyFile\` and its key (e.g. \`1-5-account-deletion-with-anonymization\`) in \`storyKey\` — downstream stages depend on these.`,
  ].join('\n'),
  { label: `create:${STORY}`, phase: 'Create', schema: CREATE_SCHEMA, agentType: 'general-purpose' }
)

if (!created || !created.created) {
  log(`✖ Stage 1 (create-story) failed — aborting.`)
  return {
    story: STORY,
    aborted: true,
    failedStage: 'create-story',
    detail: created ? created.summary : 'agent returned no result',
    blockers: created ? created.blockers : [],
  }
}

const STORY_FILE = created.storyFile
const STORY_KEY = created.storyKey || STORY
log(`✓ Story created: ${STORY_FILE} (key ${STORY_KEY})`)

// shared tail appended to every downstream prompt so they target the right file
const target = `\n\n## Target\nStory key: \`${STORY_KEY}\`\nStory file: \`${STORY_FILE}\`\n`

// ===========================================================================
// STAGE 2 — Validate story (auto-fix)
// ===========================================================================
phase('Validate')
const validated = await agent(
  [
    preamble('bmad-create-story', 'Validate'),
    target,
    `Run the create-story validation checklist (\`${SKILLS}/bmad-create-story/checklist.md\`) against the story file.`,
    `For every issue you find, FIX it directly in the story file (improve clarity, add missing context, tighten ACs). List what you found in \`issuesFound\`, what you fixed in \`issuesFixed\`, and anything you genuinely could not resolve in \`unresolved\`. Set \`valid\` true only if no critical gaps remain.`,
  ].join('\n'),
  { label: `validate:${STORY_KEY}`, phase: 'Validate', schema: VALIDATE_SCHEMA, agentType: 'general-purpose' }
)
if (validated && validated.unresolved && validated.unresolved.length) {
  log(`⚠ Validation left ${validated.unresolved.length} unresolved item(s); continuing anyway.`)
}
log(`✓ Validation: ${validated ? validated.summary : 'no result'}`)

// ===========================================================================
// STAGE 3 — ATDD red-phase acceptance scaffolds
// ===========================================================================
phase('ATDD')
const atdd = await agent(
  [
    preamble('bmad-testarch-atdd', 'Create'),
    target,
    `Generate red-phase acceptance test scaffolds (E2E/API/Component as appropriate) plus fixtures/helpers for this story's acceptance criteria. These tests SHOULD fail now (no implementation yet) — confirm the red phase and set \`redConfirmed\`. List created files in \`testFiles\`.`,
  ].join('\n'),
  { label: `atdd:${STORY_KEY}`, phase: 'ATDD', schema: ATDD_SCHEMA, agentType: 'general-purpose' }
)
log(`✓ ATDD: ${atdd ? atdd.summary : 'no result'}`)

// ===========================================================================
// STAGE 4–6 — outer quality loop
// ===========================================================================
function devPrompt(outer, inner, carryover) {
  const lines = [
    preamble('bmad-dev-story'),
    target,
    `Implement the story end-to-end following the red-green-refactor cycle and the story's Tasks/Subtasks. Make the ATDD acceptance tests pass. Modify ONLY the permitted story-file sections (Tasks/Subtasks checkboxes, Dev Agent Record, File List, Change Log, Status).`,
    `When done: run the full regression suite AND \`./scripts/ci-local.sh\` — do not report success unless it is green. Move the story status to \`review\` only when every AC is satisfied and all tasks are checked.`,
    `If the story already has a "### Review Findings" section (from a prior code-review), this is a continuation: prioritize and resolve those [Review][Patch]/[Review][Decision] items first.`,
  ]
  if (carryover) {
    lines.push(
      ``,
      `## Quality-gate findings to address this pass (from test-review / NFR)`,
      carryover,
      `Treat the above as required fixes for this implementation pass.`
    )
  }
  lines.push(
    ``,
    `Set \`halted\` true only if a real HALT condition blocks you (e.g. AC cannot be satisfied, unfixable regression) and explain in \`haltReason\`. Otherwise drive to completion.`,
    `(outer pass ${outer}, inner round ${inner})`
  )
  return lines.join('\n')
}

function reviewPrompt(outer, inner) {
  return [
    preamble('bmad-code-review'),
    target,
    `Review the changes for this story adversarially (correctness, edge cases, acceptance-criteria coverage). Triage findings into decision-needed / patch / defer / dismiss.`,
    `Write the actionable findings into the story file's "### Review Findings" section using the standard format (\`- [ ] [Review][Patch] <Title> [file:line]\`, \`- [ ] [Review][Decision] <Title> — <Detail>\`) so the next dev pass can resolve them.`,
    `\`clean\` = true ONLY if there are zero unresolved decision-needed or patch findings. \`blockingCount\` = count of those. (outer pass ${outer}, inner round ${inner})`,
  ].join('\n')
}

function automatePrompt(outer) {
  return [
    preamble('bmad-testarch-automate', 'Create'),
    target,
    `Expand automated test coverage for the implemented story: add prioritized tests at the right level (E2E/API/Component/Unit) with fixtures/helpers, covering gaps the ATDD scaffolds did not. Run them and report \`testsPass\`. List added files in \`testFiles\`. (outer pass ${outer})`,
  ].join('\n')
}

function gatePrompt(skill, focus, outer) {
  return [
    preamble(skill, 'Create'),
    target,
    `READ-ONLY assessment — do NOT modify the story file or source (another assessment runs in parallel; avoid write conflicts). ${focus}`,
    `Produce a deterministic gate: PASS (no issues), CONCERNS (non-blocking remarks), or FAIL (blocking). Return concrete, actionable findings in \`findings\` so they can be fed to the next dev pass. (outer pass ${outer})`,
  ].join('\n')
}

function buildCarryover(testReview, nfr) {
  const parts = []
  const fmt = (src, f) => `- [${src}] (${f.severity || 'n/a'}) ${f.title || ''}${f.detail ? ' — ' + f.detail : ''}`
  if (testReview && testReview.gate !== 'PASS' && testReview.findings) {
    parts.push(...testReview.findings.map((f) => fmt('test-review', f)))
  }
  if (nfr && nfr.gate !== 'PASS' && nfr.findings) {
    parts.push(...nfr.findings.map((f) => fmt('nfr', f)))
  }
  return parts.join('\n')
}

const history = []
let carryover = ''
let halted = false
let haltDetail = null
let qualityPass = false
let lastReview = null
let lastGates = null
let outer = 0

for (outer = 1; outer <= MAX_OUTER; outer++) {
  log(`── Outer pass ${outer}/${MAX_OUTER} ──`)

  // ----- Stage 4: inner dev ↔ review loop -----
  phase('Dev Loop')
  let cleanReview = false
  let inner = 0
  for (inner = 1; inner <= MAX_INNER; inner++) {
    const dev = await agent(
      devPrompt(outer, inner, inner === 1 ? carryover : ''),
      { label: `dev:o${outer}r${inner}`, phase: 'Dev Loop', schema: DEV_SCHEMA, agentType: 'general-purpose' }
    )
    carryover = '' // consumed by the first inner round of this outer pass
    if (!dev) { halted = true; haltDetail = 'dev-story returned no result'; break }
    if (dev.halted) {
      halted = true
      haltDetail = dev.haltReason || dev.summary
      log(`■ dev-story HALTED (o${outer}r${inner}): ${haltDetail}`)
      break
    }

    const review = await agent(
      reviewPrompt(outer, inner),
      { label: `review:o${outer}r${inner}`, phase: 'Dev Loop', schema: REVIEW_SCHEMA, agentType: 'general-purpose' }
    )
    lastReview = review
    history.push({ outer, inner, dev: dev.summary, review: review ? review.summary : 'no result', clean: !!(review && review.clean) })

    if (review && review.clean) {
      cleanReview = true
      log(`✓ Code-review clean at o${outer}r${inner}`)
      break
    }
    log(`↻ o${outer}r${inner}: ${review ? review.blockingCount : '?'} blocking finding(s) — re-running dev`)
  }

  if (halted) break
  if (!cleanReview) log(`⚠ Inner loop hit cap (${MAX_INNER}) on outer pass ${outer} without a clean review`)

  // ----- Stage 5: expand coverage -----
  phase('Automate')
  const auto = await agent(
    automatePrompt(outer),
    { label: `automate:o${outer}`, phase: 'Automate', schema: AUTOMATE_SCHEMA, agentType: 'general-purpose' }
  )
  log(`✓ Automate (o${outer}): ${auto ? auto.summary : 'no result'}`)

  // ----- Stage 6: parallel quality gates (read-only) -----
  phase('Quality Gates')
  const [testReview, nfr] = await parallel([
    () => agent(
      gatePrompt('bmad-testarch-test-review', 'Audit test quality: determinism, coverage adequacy, assertion strength, anti-patterns, alignment with the story ACs.', outer),
      { label: `test-review:o${outer}`, phase: 'Quality Gates', schema: GATE_SCHEMA, agentType: 'general-purpose' }
    ),
    () => agent(
      gatePrompt('bmad-testarch-nfr', 'Assess non-functional requirements: performance, security, reliability, maintainability — evidence-based.', outer),
      { label: `nfr:o${outer}`, phase: 'Quality Gates', schema: GATE_SCHEMA, agentType: 'general-purpose' }
    ),
  ])
  lastGates = { testReview, nfr }
  const trGate = testReview ? testReview.gate : 'FAIL'
  const nfrGate = nfr ? nfr.gate : 'FAIL'
  log(`Gates (o${outer}): test-review=${trGate}, nfr=${nfrGate}`)

  if (trGate === 'PASS' && nfrGate === 'PASS') {
    qualityPass = true
    break
  }

  // remarks present → loop back to stage 4, carrying findings into the next dev pass
  carryover = buildCarryover(testReview, nfr)
  log(`↩ Quality gates not green — returning to stage 4 (carrying ${carryover ? carryover.split('\n').length : 0} finding(s))`)
}

if (!qualityPass && !halted && outer > MAX_OUTER) {
  log(`⚠ Outer loop hit cap (${MAX_OUTER}); quality gates still not all PASS`)
}

// --- final report (returned to the main thread) ----------------------------
return {
  story: STORY,
  storyKey: STORY_KEY,
  storyFile: STORY_FILE,
  outcome: halted ? 'halted' : qualityPass ? 'passed' : 'capped',
  qualityGatesPassed: qualityPass,
  haltDetail,
  outerPassesRun: Math.min(outer, MAX_OUTER),
  lastReview: lastReview ? { clean: lastReview.clean, blockingCount: lastReview.blockingCount, verdict: lastReview.verdict } : null,
  lastGates: lastGates
    ? { testReview: lastGates.testReview && lastGates.testReview.gate, nfr: lastGates.nfr && lastGates.nfr.gate }
    : null,
  iterations: history,
  caps: { maxInner: MAX_INNER, maxOuter: MAX_OUTER },
}
