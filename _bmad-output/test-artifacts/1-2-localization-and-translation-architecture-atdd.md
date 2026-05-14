---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-ac-analysis', 'step-03-scaffold-generation']
lastStep: 'step-03-scaffold-generation'
lastSaved: '2026-05-14'
storyId: '1.2'
storyKey: '1-2-localization-and-translation-architecture'
storyFile: '_bmad-output/implementation-artifacts/1-2-localization-and-translation-architecture.md'
atddChecklistPath: '_bmad-output/test-artifacts/1-2-localization-and-translation-architecture-atdd.md'
generatedTestFiles:
  - frontend/src/plugins/__tests__/i18n.spec.ts
  - frontend/src/stores/__tests__/locale.spec.ts
  - frontend/src/locales/__tests__/locale-parity.spec.ts
  - frontend/src/plugins/__tests__/i18n-formatting.spec.ts
  - frontend/src/__tests__/rtl-css.spec.ts
inputDocuments:
  - _bmad-output/implementation-artifacts/1-2-localization-and-translation-architecture.md
  - _project-spec/rules/1-write.md
  - _project-spec/rules/2-test.md
detectedStack: fullstack
testFocus: frontend-only (story 1.2 has no backend changes)
---

# ATDD Scaffold — Story 1.2: Localization and Translation Architecture

## AC → Test Coverage Map

| AC | Description | Test file | Tests |
|----|------------|-----------|-------|
| AC1 | setLocale() → reactive UI update | `stores/__tests__/locale.spec.ts` | `setLocale("de") updates i18n.global.locale.value` |
| AC2 | Add locale file → no component changes | `plugins/__tests__/i18n-formatting.spec.ts` | `setLocaleMessage() makes t() return translations without component changes` |
| AC3 | localStorage persistence | `stores/__tests__/locale.spec.ts` | `setLocale("de") persists to localStorage` |
| AC4 | Browser language auto-detection | `plugins/__tests__/i18n.spec.ts` | 5 tests for detectLocale() |
| AC5 | No hardcoded strings | `locales/__tests__/locale-parity.spec.ts` | key parity + spot-check required keys |
| AC6 | Date/number formatting | `plugins/__tests__/i18n-formatting.spec.ts` | 7 tests — d() EN vs DE dates, n() EN vs DE numbers |
| AC7 | RTL-neutral CSS | `src/__tests__/rtl-css.spec.ts` | 5 tests — static analysis of all story-modified files |

## Red Phase Verification

Run before implementation (all must FAIL or be in guard state):
```bash
cd frontend && npm run test:unit -- src/plugins/__tests__/i18n.spec.ts
cd frontend && npm run test:unit -- src/stores/__tests__/locale.spec.ts
cd frontend && npm run test:unit -- src/locales/__tests__/locale-parity.spec.ts
cd frontend && npm run test:unit -- src/plugins/__tests__/i18n-formatting.spec.ts
cd frontend && npm run test:unit -- src/__tests__/rtl-css.spec.ts
```

Expected failures by category:
- `i18n.spec.ts` → `Cannot find module '@/plugins/i18n'`
- `locale.spec.ts` → `Cannot find module '@/stores/locale'` + `Cannot find module '@/plugins/i18n'`
- `locale-parity.spec.ts` → `Cannot find module '@/locales/en.json'`
- `i18n-formatting.spec.ts` → `Cannot find module '@/plugins/i18n'`
- `rtl-css.spec.ts` → PASS (guard mode: skips missing files, fails only if physical direction CSS is introduced)

## Green Phase Verification

Run after implementation (all must PASS):
```bash
cd frontend && npm run test:unit
```

Required: `./scripts/ci-local.sh` must pass before marking story done.

## Dev Agent Notes

### detectLocale() must be exported
The story's Implementation Reference shows `function detectLocale()` (unexported). For `i18n.spec.ts` to work, the function must be exported:
```typescript
export function detectLocale(): SupportedLocale { ... }
```

### AC2 is architectural — tested via runtime extensibility
AC2 (add `locales/xx.json` → no component changes) is guaranteed by the i18n plugin structure. The test in `i18n-formatting.spec.ts` verifies this by calling `i18n.global.setLocaleMessage()` at runtime and confirming `t()` returns the new locale's translations without any component code change.

### AC6 — d() and n() require datetimeFormats / numberFormats in i18n.ts
Both EN and DE locales must register `short` + `long` datetime formats and `decimal` + `percent` number formats. The `i18n-formatting.spec.ts` tests verify concrete output:
- EN `short` date: matches `/12.31.2025/` (month-first)
- DE `short` date: matches `/31\.12\.2025/` (day-first)
- EN `decimal`: contains `.`, not `,`
- DE `decimal`: contains `,`

### AC7 — guard mode, not import-failure red
`rtl-css.spec.ts` reads source files via Node `fs`. Missing files are skipped (new files not yet created). The test fails if any story-modified file contains physical Tailwind direction utilities (`ml-*`, `mr-*`, `pl-*`, `pr-*`, etc.) instead of logical utilities (`ms-*`, `me-*`, `ps-*`, `pe-*`). This is a regression guard: it passes on clean code and fails when a developer introduces physical direction CSS.

## Test Files Summary

| File | AC coverage | Tests |
|------|------------|-------|
| `plugins/__tests__/i18n.spec.ts` | AC4 | 5 (detectLocale variants + fail-closed) |
| `stores/__tests__/locale.spec.ts` | AC1, AC3 | 5 (setLocale + locale ref + guard) |
| `locales/__tests__/locale-parity.spec.ts` | AC5 | 4 (parity + namespaces + spot-check) |
| `plugins/__tests__/i18n-formatting.spec.ts` | AC2, AC6 | 9 (extensibility + date EN/DE + number EN/DE) |
| `src/__tests__/rtl-css.spec.ts` | AC7 | 5 (one per story-modified file) |
| **Total** | **AC1–AC7** | **28 tests** |
