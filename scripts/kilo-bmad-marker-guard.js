// bmad-loop completion-marker guard for the Kilo CLI.
//
// WHY THIS EXISTS
// bmad-loop injects plugin-workflow sessions (the TEA stages) with one hard
// contract: when the workflow ends, write
//
//     _bmad-output/implementation-artifacts/bmad-dev-auto-result-<task>.md
//
// whose YAML frontmatter carries a terminal `status:`. That marker is the
// orchestrator's ONLY completion signal for the session — `devcontract`
// matches the file by its `bmad-{dev,build}-auto-result-` name prefix, then
// reads `status:` out of the frontmatter.
//
// On 2026-08-06 (run 20260806-132225-eee7, story 3.4) the model wrote the
// marker correctly and then clobbered it with a prose Definition-of-Done
// document, losing the frontmatter. From that point the file existed but read
// as non-terminal, so every turn-end harvested empty; the model answered each
// wake nudge by `ls`-ing the file (present! => "work is done") and going idle
// again. The livelock only ended on the stall cap — ~1h and ~4.5M weighted
// tokens after the work itself had finished.
//
// The guard closes that hole deterministically, so marker validity no longer
// depends on the model's instruction-following:
//   * `tool.execute.after` — the primary path. Any write/edit/patch whose
//     target is a marker file is repaired the instant it lands, long before
//     the turn ends.
//   * `event` on `session.idle` — a backstop sweep for writes the tool hook
//     cannot see (a `bash` heredoc, a subagent, a compacted turn).
//
// STATUS CHOICE (deliberate, and the one judgement call here)
// A marker with no frontmatter tells us nothing about the outcome, so the
// guard cannot know whether the workflow finished or gave up. It stamps
// `status: done` by default because the loop's own downstream gates — the
// deterministic `verify.commands`, the review pass, the TEA blocking gates —
// are what actually validate the work; the marker only asserts "the turn is
// over". Every repair is auditable: the injected frontmatter carries
// `bmad_marker_guard: repaired` and a line is appended to
// `.bmad-loop/marker-guard.log`. Override with the `status` option if you
// would rather have a violated contract surface as `blocked`.
//
// Idempotent by construction: a marker that already carries a terminal status
// is left byte-for-byte alone, so double registration (see the dual export at
// the bottom) or a repeated sweep is harmless.

import { existsSync, appendFileSync, readFileSync, readdirSync, writeFileSync } from "node:fs";
import { isAbsolute, join, basename, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const MARKER_RE = /^bmad-(?:dev|build)-auto-result-.+\.md$/;
// Exactly bmad_loop.devcontract's terminal set for a workflow marker. Anything
// else (`in-progress`, `ready-for-dev`, a missing key) reads as non-terminal to
// the orchestrator, so it must read as non-terminal here too.
const TERMINAL = new Set(["done", "blocked", "awaiting-operator"]);
const DEFAULT_DIRS = ["_bmad-output/implementation-artifacts"];
const WRITE_TOOLS = new Set(["write", "edit", "patch", "multiedit", "apply_patch"]);

// Mirrors bmad_loop.frontmatter._split_frontmatter: the opening and closing
// `---` count only as standalone delimiter lines, so a `---` inside a scalar
// is never mistaken for the boundary.
function splitFrontmatter(text) {
  const lines = text.split(/(?<=\n)/);
  if (!lines.length || lines[0].trimEnd() !== "---") return null;
  for (let i = 1; i < lines.length; i++) {
    if (lines[i].trimEnd() === "---") return { open: lines[0], block: lines.slice(1, i), rest: lines.slice(i) };
  }
  return null; // an unclosed `---` is not a frontmatter block
}

// Indices of every `status:` line in the block, in order.
function statusLines(block) {
  return block.reduce((acc, line, i) => (/^status:/.test(line.trim()) ? [...acc, i] : acc), []);
}

// The block's terminal status, "" when the block carries none, null when there
// is no frontmatter block at all.
//
// Reads the LAST `status:` line, not the first: YAML resolves a duplicate key to
// its last occurrence, so `status: done` followed by `status: in-progress` is
// `in-progress` to bmad-loop's parser. Judging by the first would let the guard
// approve a marker the orchestrator reads as non-terminal — the very livelock
// this file exists to prevent.
function terminalStatusOf(split) {
  if (split === null) return null;
  const at = statusLines(split.block);
  if (!at.length) return "";
  const m = /^status:\s*(.*)$/.exec(split.block[at[at.length - 1]].trim());
  const value = (m?.[1] ?? "").trim().replace(/^['"]|['"]$/g, "").toLowerCase();
  return TERMINAL.has(value) ? value : "";
}

// Candidate project roots, most trustworthy first. Kilo hands the plugin a
// `directory`/`worktree` of "/" for a session it classifies as global, so those
// fields cannot be the only source: `/` would make the sweep look at
// `/_bmad-output/...` and silently find nothing. The server's cwd is the project
// (bmad-loop spawns it there) and this file lives at <project>/scripts/, so both
// are stronger signals. Only the sweep and the log location depend on this — the
// tool-hook repair works off the written path and needs no root at all.
function candidateRoots(input) {
  // fileURLToPath, not `new URL(...).pathname`: the raw pathname keeps spaces
  // percent-encoded (a repo cloned under "My Projects" would never resolve) and
  // on Windows keeps a leading slash before the drive letter.
  const fromSelf = dirname(dirname(fileURLToPath(import.meta.url)));
  const seen = new Set();
  return [process.cwd(), fromSelf, input?.worktree, input?.directory].filter(
    (r) => typeof r === "string" && r.length > 1 && !seen.has(r) && (seen.add(r), true),
  );
}

export const BmadMarkerGuard = async (input, options = {}) => {
  const roots = candidateRoots(input);
  const status = typeof options.status === "string" ? options.status : "done";
  const relDirs = Array.isArray(options.dirs) && options.dirs.length ? options.dirs : DEFAULT_DIRS;
  // Exactly ONE root wins — the first candidate that actually has an artifacts
  // dir. Sweeping every candidate would, under `scm.isolation = "worktree"`,
  // reach both the unit's worktree (cwd) and the main checkout (this file's
  // repo) and could stamp a marker belonging to a different unit. cwd comes
  // first precisely because it is the checkout this session works in.
  const resolve = (root) =>
    relDirs.map((d) => (isAbsolute(d) ? d : join(root, d))).filter((d) => existsSync(d));
  const root = roots.find((r) => resolve(r).length) ?? roots[0];
  const dirs = resolve(root);
  const logRoot = existsSync(join(root, ".bmad-loop")) ? root : undefined;

  const log = (message) => {
    try {
      if (!logRoot) return; // the chosen root has no .bmad-loop => not a loop run
      appendFileSync(join(logRoot, ".bmad-loop", "marker-guard.log"), `${new Date().toISOString()} ${message}\n`);
    } catch {
      /* observability must never break a session */
    }
  };

  // Proof the factory ran and which directories it will sweep — without it, a
  // guard that never loaded is indistinguishable from one that found nothing.
  // Goes to stderr (the run's `*.server.out`) as well as the guard log, because
  // the log itself is suppressed when `root` has no `.bmad-loop`, which is
  // exactly the misconfiguration this line has to be able to report.
  console.error(`[bmad-marker-guard] active dirs=${dirs.join(",") || "(none found)"} status=${status}`);
  log(`guard active: dirs=${dirs.join(",") || "(none found)"} status=${status}`);

  const repair = (path, why) => {
    if (!path || !MARKER_RE.test(basename(path))) return;
    let text;
    try {
      text = readFileSync(path, "utf8");
    } catch {
      return; // not written yet, or torn mid-write: a later hook pass sees it
    }
    const split = splitFrontmatter(text);
    const found = terminalStatusOf(split);
    if (found) return; // already valid — leave the bytes untouched
    let repaired;
    if (split === null) {
      // No frontmatter at all: prepend a block.
      repaired = `---\nstatus: ${status}\nbmad_marker_guard: repaired\n---\n\n${text}`;
    } else {
      // A block exists but its status is missing or non-terminal. Drop EVERY
      // existing `status:` line and write exactly one: YAML lets the last
      // duplicate key win, so leaving any behind risks the parser resolving to
      // a line we did not write. The replacement sits where the first one was,
      // preserving the author's field order.
      const block = [...split.block];
      const at = statusLines(block);
      const injected = `status: ${status}\nbmad_marker_guard: repaired\n`;
      for (const i of [...at].reverse()) block.splice(i, 1);
      block.splice(at.length ? at[0] : 0, 0, injected);
      repaired = split.open + block.join("") + split.rest.join("");
    }
    try {
      writeFileSync(path, repaired);
    } catch (err) {
      log(`FAILED ${path} (${why}): ${err?.message ?? err}`);
      return;
    }
    log(`repaired ${path} -> status: ${status} (${why}, had ${found === null ? "no frontmatter" : "no terminal status"})`);
  };

  const sweep = (why) => {
    for (const dir of dirs) {
      let names;
      try {
        names = readdirSync(dir);
      } catch {
        continue;
      }
      for (const name of names) if (MARKER_RE.test(name)) repair(join(dir, name), why);
    }
  };

  return {
    // Primary path: repair at write time.
    "tool.execute.after": async (hook) => {
      if (!WRITE_TOOLS.has(String(hook?.tool ?? "").toLowerCase())) return;
      const args = hook.args ?? {};
      repair(args.filePath ?? args.path ?? args.file, `tool ${hook.tool}`);
    },
    // Backstop: catch markers written by routes the tool hook cannot see.
    event: async ({ event }) => {
      if (event?.type === "session.idle") sweep("session.idle sweep");
    },
  };
};

// Kilo's newer `PluginModule` shape ({ server }) and the inherited OpenCode
// convention (any exported plugin factory) both resolve to the same hooks.
// Registering twice is safe: every repair is idempotent.
export const server = BmadMarkerGuard;
