# Story 1.2: Localization and Translation Architecture

**Status:** Ready for Dev
**Epic:** 1 — Quick Start (Auth & Basic Profile)
**Story Key:** 1-2-localization-and-translation-architecture

## Story

As a user, I want the app to be localized, so that I can use it in my language.

## Acceptance Criteria

1. **Given** the application is deployed
   **When** the active locale is switched programmatically (e.g., from `'en'` to `'de'`)
   **Then** all UI strings bound via `$t()` / `t()` update immediately — no page reload required (FR59)

2. **Given** any Vue component uses `$t('key')` or `const { t } = useI18n()` / `t('key')`
   **When** the locale is `'en'` or `'de'`
   **Then** the correct translated string is returned for the active locale

3. **Given** a user had previously selected German
   **When** the browser tab is closed and reopened
   **Then** the German locale is restored from `localStorage`

4. **Given** the i18n plugin is installed and configured
   **When** a developer creates a new `frontend/src/locales/xx.json` file and registers it in `i18n.ts`
   **Then** no other code changes are required to support the new language (FR59)

5. **Given** the i18n architecture is in place
   **When** all existing UI components are reviewed
   **Then** no user-facing hardcoded strings remain — all use translation keys

## Tasks / Subtasks

- [ ] **Install vue-i18n**
  - [ ] In `frontend/`, run: `npm install vue-i18n@9`
  - [ ] Verify installed version is `9.x` via `npm list vue-i18n`

- [ ] **Create translation files**
  - [ ] Create `frontend/src/locales/en.json` — seed with all existing UI strings (see inventory table in Dev Notes)
  - [ ] Create `frontend/src/locales/de.json` — German translations for every key in `en.json`

- [ ] **Create i18n plugin**
  - [ ] Create `frontend/src/plugins/i18n.ts` — `createI18n()` with `legacy: false`, locale restored from `localStorage`, `fallbackLocale: 'en'`

- [ ] **Register plugin in app entry point**
  - [ ] Update `frontend/src/main.ts` — add `app.use(i18n)` (preserve existing Pinia + Router registrations)

- [ ] **Externalize all hardcoded strings in existing components**
  - [ ] Update `frontend/src/views/HomeView.vue` — replace the 6 module-level `const` string declarations with `useI18n()` + `t()` calls inside `<script setup>`
  - [ ] Update `frontend/src/components/GoogleOAuthButton.vue` — replace `'Sign in with Google'` button text and `alert()` message with `$t()`
  - [ ] Update `frontend/src/components/OAuthRedirectHandler.vue` — replace `'Completing secure sign-in…'` with `$t()`

- [ ] **Create locale management composable**
  - [ ] Create `frontend/src/composables/useLocale.ts` — exposes `locale`, `setLocale(lang)` (persists to `localStorage`), `supportedLocales`

- [ ] **Update existing component tests to mount with i18n** ← CRITICAL, see Dev Notes
  - [ ] Update `GoogleOAuthButton` tests — provide i18n plugin via `global.plugins` when mounting
  - [ ] Update `OAuthRedirectHandler` tests — same fix

- [ ] **Write unit tests for i18n composable**
  - [ ] Create `frontend/src/composables/__tests__/useLocale.test.ts`
    - [ ] Test: `setLocale('de')` sets `locale.value === 'de'` and persists `'de'` to `localStorage`
    - [ ] Test: locale is initialized from `localStorage` when a prior value exists
    - [ ] Test: `t('home.title')` returns `'Tic-Tac-Tore'` in `'en'` locale
    - [ ] Test: `t('home.title')` returns the German equivalent in `'de'` locale

- [ ] **Run `./scripts/ci-local.sh` and confirm all checks pass**

## Dev Notes

### Technical Stack

| Layer | Technology |
|-------|-----------|
| i18n library | `vue-i18n@9` — Vue 3 compatible (NOT v8 — that is Vue 2 only) |
| Translation format | JSON files, one per locale |
| Supported locales | `en` (primary), `de` (secondary) |
| Locale persistence | `localStorage` key: `app_locale` |
| Unit testing | Vitest + `@vue/test-utils` (already configured) |

### CRITICAL: vue-i18n Version

```bash
npm install vue-i18n@9
```

vue-i18n has two major lines:
- `vue-i18n@8.x` → Vue 2 ONLY — will break at runtime with Vue 3
- `vue-i18n@9.x` → Vue 3 (this project uses Vue `^3.5.27`)

After install, verify: `npm list vue-i18n` must show `9.x.x`.
vue-i18n is NOT in `package.json` — it must be installed fresh.

### New Directory/File Structure

The `plugins/` and `composables/` directories do not yet exist in `frontend/src/` — create them.

```
frontend/src/
├── locales/                         # NEW directory
│   ├── en.json                      # NEW — English translations
│   └── de.json                      # NEW — German translations
├── plugins/                         # NEW directory
│   └── i18n.ts                      # NEW — createI18n() setup
└── composables/                     # NEW directory
    └── useLocale.ts                 # NEW — locale management composable
```

> **Note on architecture spec vs. existing code:** The architecture document describes a `features/` + `core/` directory structure, but the existing codebase (established by stories 1.1–1.1b) uses a flat structure: `components/`, `views/`, `stores/`, `utils/`, `services/`. Follow the **existing** flat structure. Place new directories at `frontend/src/` root level.

### Files to Update

```
frontend/src/main.ts                              # ADD app.use(i18n)
frontend/src/views/HomeView.vue                   # REPLACE 6 hardcoded constants
frontend/src/components/GoogleOAuthButton.vue     # REPLACE button text + alert text
frontend/src/components/OAuthRedirectHandler.vue  # REPLACE loading text
```

### Implementation: `frontend/src/plugins/i18n.ts`

```typescript
import { createI18n } from 'vue-i18n'
import en from '../locales/en.json'
import de from '../locales/de.json'

const LOCALE_STORAGE_KEY = 'app_locale'
type SupportedLocale = 'en' | 'de'

const savedLocale = localStorage.getItem(LOCALE_STORAGE_KEY) as SupportedLocale | null

export const i18n = createI18n({
  legacy: false,        // REQUIRED: enables Composition API mode (useI18n() composable)
  locale: savedLocale ?? 'en',
  fallbackLocale: 'en',
  messages: { en, de },
})
```

### Implementation: `frontend/src/main.ts` (UPDATE)

```typescript
import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { i18n } from './plugins/i18n'   // ADD

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(i18n)                            // ADD — after router, before mount
app.mount('#app')
```

### Implementation: `frontend/src/composables/useLocale.ts`

```typescript
import { useI18n } from 'vue-i18n'

const LOCALE_STORAGE_KEY = 'app_locale'
export const SUPPORTED_LOCALES = ['en', 'de'] as const
export type SupportedLocale = typeof SUPPORTED_LOCALES[number]

export function useLocale() {
  const { locale } = useI18n()

  function setLocale(lang: SupportedLocale): void {
    locale.value = lang
    localStorage.setItem(LOCALE_STORAGE_KEY, lang)
  }

  return { locale, setLocale, supportedLocales: SUPPORTED_LOCALES }
}
```

### Translation Key Naming Convention

Use dot-notation namespaced by feature area:

```json
{
  "home": {
    "title": "Tic-Tac-Tore",
    "subtitle": "Foosball statistics platform",
    "signInMessage": "Sign in to track your matches",
    "welcomeMessage": "Welcome back! 👋",
    "comingSoon": "Your foosball dashboard is coming soon.",
    "signOut": "Sign Out"
  },
  "auth": {
    "signInWithGoogle": "Sign in with Google",
    "completingSignIn": "Completing secure sign-in…",
    "redirectFailed": "Login redirect failed. Please try again."
  }
}
```

Every key in `en.json` must exist in `de.json`. Missing key → runtime warning + fallback to English.

### Inventory of ALL Hardcoded Strings to Externalize

| File | Current hardcoded value | Translation key |
|------|------------------------|-----------------|
| `views/HomeView.vue` | `'Tic-Tac-Tore'` | `home.title` |
| `views/HomeView.vue` | `'Foosball statistics platform'` | `home.subtitle` |
| `views/HomeView.vue` | `'Sign in to track your matches'` | `home.signInMessage` |
| `views/HomeView.vue` | `'Welcome back! 👋'` | `home.welcomeMessage` |
| `views/HomeView.vue` | `'Your foosball dashboard is coming soon.'` | `home.comingSoon` |
| `views/HomeView.vue` | `'Sign Out'` | `home.signOut` |
| `components/GoogleOAuthButton.vue` | `'Sign in with Google'` (template text) | `auth.signInWithGoogle` |
| `components/GoogleOAuthButton.vue` | `'Login redirect failed. Please try again.'` (alert) | `auth.redirectFailed` |
| `components/OAuthRedirectHandler.vue` | `'Completing secure sign-in…'` | `auth.completingSignIn` |

> `HomeView.vue` currently uses module-level `const` declarations (e.g., `const TITLE = 'Tic-Tac-Tore'`) before `<script setup>`. These are not reactive. Replace the entire block with `const { t } = useI18n()` inside `<script setup>`, then use `t('home.title')` in the template.

### CRITICAL: Existing Tests Will Break After i18n Migration

**`GoogleOAuthButton.spec.ts`** currently asserts:

```typescript
it('renders a sign in with Google button', () => {
  const wrapper = mount(GoogleOAuthButton)
  expect(wrapper.text()).toContain('Sign in with Google')
})
```

After migrating the template to `$t('auth.signInWithGoogle')`, mounting without i18n plugin renders the raw key string, not the translation. **This test will fail.**

**Fix: provide a fresh i18n instance per test via `global.plugins`:**

```typescript
import { createI18n } from 'vue-i18n'
import en from '@/locales/en.json'
import de from '@/locales/de.json'

function createTestI18n(locale = 'en') {
  return createI18n({ legacy: false, locale, messages: { en, de } })
}

it('renders a sign in with Google button', () => {
  const wrapper = mount(GoogleOAuthButton, {
    global: { plugins: [createTestI18n()] },
  })
  expect(wrapper.text()).toContain('Sign in with Google')
})
```

Apply the same fix to all `OAuthRedirectHandler` tests that assert on rendered text.

**Do NOT use the singleton `i18n` from `plugins/i18n.ts` in tests** — it reads `localStorage` at module init time and carries state between tests. Always create a fresh instance via `createI18n()` per test.

### Scope Boundaries

**IN SCOPE — Story 1.2 (architecture foundation):**
- `vue-i18n@9` install and plugin configuration
- Translation JSON files (`en.json`, `de.json`) with all current UI strings
- `useLocale` composable (locale switching + `localStorage` persistence)
- `main.ts` plugin registration
- Externalize all existing hardcoded strings from existing components
- Fix existing tests broken by i18n migration
- Unit tests for `useLocale` composable

**OUT OF SCOPE → Story 1.4 (Profile Management):**
- Language selector UI in profile/personal cabinet
- Backend API for persisting language preference to user profile
- Loading server-stored language preference after login

### Architecture Compliance

- **Rule 13 (Backend vs Frontend Boundary):** i18n is FRONTEND concern only. Backend API responses use raw data. Do NOT add language parameters to any backend API calls.
- **Rule 1 (Technical Text Language):** Code comments, log messages, exception text stay in English. Only user-facing UI strings go in translation files.
- **500-line rule:** No single file exceeds 500 lines. The plugin and composable are small — no risk. Keep translation JSON files organized.

### Testing Requirements

Framework: **Vitest** + `@vue/test-utils`. Pattern: Arrange-Act-Assert (AAA).

Test file location: `frontend/src/composables/__tests__/useLocale.test.ts`

Key test structure:

```typescript
import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { createI18n } from 'vue-i18n'
import { mount } from '@vue/test-utils'
import { defineComponent } from 'vue'
import en from '@/locales/en.json'
import de from '@/locales/de.json'
import { useLocale } from '../useLocale'

function createTestI18n(locale = 'en') {
  return createI18n({ legacy: false, locale, messages: { en, de } })
}

describe('useLocale', () => {
  beforeEach(() => localStorage.clear())
  afterEach(() => localStorage.clear())

  it('setLocale updates locale and persists to localStorage', () => {
    // Arrange
    const TestComponent = defineComponent({
      setup() { return useLocale() },
      template: '<div />',
    })
    const wrapper = mount(TestComponent, { global: { plugins: [createTestI18n()] } })
    // Act
    wrapper.vm.setLocale('de')
    // Assert
    expect(wrapper.vm.locale).toBe('de')
    expect(localStorage.getItem('app_locale')).toBe('de')
  })
})
```

No backend tests required — this story is frontend-only.

### Previous Story Intelligence (from 1-1b)

- E2E tests: `frontend/e2e/` (Playwright)
- Unit tests: co-located in `__tests__/` subdirectories alongside source files
- `@vue/test-utils`, Vitest, `@faker-js/faker` already in `devDependencies` — no additional test tooling needed

### What Must Be Preserved

1. **`main.ts`:** Existing Pinia and Router registrations must not be removed. Only ADD `app.use(i18n)`.
2. **`App.vue`:** No changes needed — it only has `<RouterView />`, no user-facing strings.
3. **`GoogleOAuthButton.vue` redirect logic:** Do NOT modify the `signInWithGoogle()` function behavior. Only externalize the string literals (`'Sign in with Google'` and the `alert()` message).
4. **Playwright E2E tests:** After migration, rendered English text is identical to the current hardcoded text. E2E tests should pass without modification — verify with `npm run test:e2e`.

### Verification

Run mandatory local CI before marking complete:

```bash
./scripts/ci-local.sh
```

This runs: `mvn clean verify`, frontend `type-check`, `build`, `test:unit`, `test:e2e`.

Story is **frontend-only** — no backend changes. Verify:

- [ ] `npm run type-check` passes — TypeScript resolves vue-i18n types correctly
- [ ] `npm run build` passes — no broken imports from locales JSON
- [ ] `npm run test:unit -- --run` passes — composable tests + updated component tests
- [ ] `npm run test:e2e` passes — Playwright: rendered English text unchanged

## Dev Agent Record

### Agent Model Used

_TBD_

### Debug Log References

_TBD_

### Completion Notes List

_TBD_

### File List

_TBD_
