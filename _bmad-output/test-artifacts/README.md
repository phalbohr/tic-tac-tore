# Test Artifacts Organization & Notes

## Flat Artifacts Notice

Several TEA / QA steps in earlier iterations wrote test artifacts directly to flat filenames in this directory without a `story_key` suffix:
- `nfr-assessment.md` (overwritten by the most recently run story's NFR evaluation)
- `test-review.md` (overwritten by the most recently run story's test review)
- `traceability-matrix.md` (overwritten by the most recently run story's traceability matrix)

**Per-Story Artifact Locations:**
To prevent losing historical reports across stories, dedicated per-story copies are preserved with story suffixes:
- NFR assessments: `nfr-assessment-<story-key>.md` or `nfr/`
- Test reviews: `test-reviews/<story-key>-test-review.md`
- Traceability matrices: `traceability/traceability-matrix-<story-key>.md`

## Root JSON Artifacts

The root JSON files:
- `gate-decision.json`
- `e2e-trace-summary.json`

contain legacy data from **Story 2-4** (2026-08-04) and are not updated automatically by all subsequent workflows. For active per-story gate decisions and E2E summaries, consult the `traceability/` directory (e.g. `traceability/gate-decision-<story-key>.json`, `traceability/e2e-trace-summary-<story-key>.json`).
