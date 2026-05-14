# Story 1.2: Localization and Translation Architecture

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a user,
I want the application to support my preferred language (English or German),
so that I can interact with the app comfortably without language barriers.

## Acceptance Criteria

1. **AC1 (FR59 — live switch):** Given the application is running, When `useLocaleStore().setLocale('de')` is called programmatically, Then all `t()` translation keys in the UI update reactively — no page reload, no build step required.

2. **AC2 (FR59 — extensibility):** Given a developer adds `src/locales/xx.json` and registers it in the i18n plugin messages map, Then no Vue component code changes are required to render the new language.

3. **AC3 (persistence):** Given a user has previously selected German, When they close and reopen the browser, Then the app restores German from `localStorage` key `ttt_locale`.

4. **AC4 (auto-detection):** Given no `ttt_locale` key exists in `localStorage`, When the app loads, Then the locale defaults to the browser's primary language if it is `en` or `de`; otherwise it falls back to `en`.

5. **AC5 (string externalization):** Given all currently-existing Vue components, When this story is complete, Then every user-facing string uses `t()` and has a matching key in both `en.json` and `de.json` — no hardcoded UI text remains.

6. **AC6 (date/number formatting):** Given a date or number is rendered in a component, When displayed in English, Then it uses `MM/DD/YYYY` and `.` as decimal separator; in German it uses `DD.MM.YYYY` and `,` as decimal separator — implemented via vue-i18n `d()` and `n()`.

7. **AC7 (RTL-neutral CSS):** Given the NFR that CSS must not preclude future RTL support, When new CSS is written in this story, Then it uses Tailwind v4 logical utilities (`ms-*`, `me-*`, `ps-*`, `pe-*`) or logical CSS properties — never physical `margin-left`/`padding-right`.

## Tasks / Subtasks

- [ ] Task 1: Install dependencies (AC: #1, #2, #6)
  - [ ] 1.1 In `frontend/`: run `npm install vue-i18n@10`
  - [ ] 1.2 In `frontend/`: run `npm install -D @intlify/unplugin-vue-i18n`
  - [ ] 1.3 Add `@intlify/unplugin-vue-i18n` Vite plugin to `frontend/vite.config.ts` (see Dev Notes for exact config)

- [ ] Task 2: Create locale files (AC: #1, #2, #5)
  - [ ] 2.1 Create `frontend/src/locales/en.json` — English strings (audit ALL components/views first)
  - [ ] 2.2 Create `frontend/src/locales/de.json` — German strings (keys must be 1:1 with en.json)
  - [ ] 2.3 Follow dot-notation namespace convention: `nav.*`, `common.*`, `match.*`, `auth.*`, `error.*`, `leaderboard.*`

- [ ] Task 3: Create i18n plugin (AC: #1, #2, #4, #6)
  - [ ] 3.1 Create `frontend/src/plugins/i18n.ts`
  - [ ] 3.2 Use `legacy: false` (Composition API mode — mandatory for Vue 3)
  - [ ] 3.3 Implement `detectLocale()`: check `localStorage` → browser language → fallback `en`
  - [ ] 3.4 Register `datetimeFormats` and `numberFormats` for `en` and `de` (see Dev Notes)
  - [ ] 3.5 Export the `i18n` instance (needed by `locale.ts` store to set `i18n.global.locale.value`)

- [ ] Task 4: Create Pinia locale store (AC: #3, #4)
  - [ ] 4.1 Create `frontend/src/stores/locale.ts` using `defineStore` Setup syntax (same pattern as `stores/auth.ts`)
  - [ ] 4.2 Expose reactive `locale` ref and `setLocale(locale: SupportedLocale)` action
  - [ ] 4.3 `setLocale` must write to `localStorage` AND update `i18n.global.locale.value` atomically

- [ ] Task 5: Register plugin in app entry (AC: #1)
  - [ ] 5.1 Update `frontend/src/main.ts`: add `app.use(i18n)` — order: pinia → i18n → router → mount

- [ ] Task 6: Migrate hardcoded strings in existing components (AC: #5)
  <!-- IMPORTANT: As of Story 1.2, these are the ONLY Vue files that exist in frontend/src.
       Future components (MatchRecordingForm, Leaderboard, etc.) will be i18n-ready from creation. -->
  - [ ] 6.1 Migrate `src/views/HomeHub.vue` — 6 hardcoded constants: TITLE ("Tic-Tac-Tore"), SUBTITLE ("Foosball statistics platform"), SIGN_IN_MESSAGE ("Sign in to track your matches"), WELCOME_MESSAGE ("Welcome back! 👋"), COMING_SOON_MESSAGE ("Your foosball dashboard is coming soon."), SIGN_OUT_LABEL ("Sign Out") → keys: `home.title`, `home.subtitle`, `home.signInMessage`, `home.welcomeBack`, `home.comingSoon`, `auth.signOut`
  - [ ] 6.2 Migrate `src/components/GoogleOAuthButton.vue` — template string "Sign in with Google" → `auth.signInWithGoogle`; the `alert('Login redirect failed...')` call CANNOT use `t()` (called outside component setup) — replace with a reactive `errorMessage` ref bound to `t('auth.redirectFailed')` displayed in the template
  - [ ] 6.3 Migrate `src/components/OAuthRedirectHandler.vue` — "Completing secure sign-in…" → `auth.completingSignIn`
  - [ ] 6.4 `src/App.vue` — no user-facing strings, skip
  - [ ] 6.5 Verify every translation key exists in BOTH `en.json` and `de.json`

- [ ] Task 7: Write unit tests (AC: #1, #3, #4)
  - [ ] 7.1 Test: `detectLocale()` returns `'de'` when `navigator.language = 'de-AT'`
  - [ ] 7.2 Test: `detectLocale()` returns `'en'` for unsupported locale (e.g., `'fr'`)
  - [ ] 7.3 Test: `detectLocale()` returns stored value from `localStorage` when present
  - [ ] 7.4 Test: `useLocaleStore().setLocale('de')` persists `'de'` to `localStorage`
  - [ ] 7.5 Test: `useLocaleStore().setLocale('de')` updates `i18n.global.locale.value` to `'de'`
  - [ ] 7.6 Test (required for AC5): all keys in `en.json` have corresponding keys in `de.json` — no missing translations in either direction
  - [ ] 7.7 Test: `detectLocale()` returns `'en'` when `localStorage.getItem` throws (fail-closed pattern — SSR / restricted browser context)

## Dev Notes

### Architecture Guardrails — Read Before Writing Any Code

**i18n is frontend-only in this story.**
Per `_project-spec/rules/1-write.md` §13: "Frontend = UI, formatting (dates/i18n), presentation." Spring Boot `MessageSource` is NOT needed here. Backend push notification localization is deferred to Story 3.1 (Confirmation Requests & Push Notifications).

**vue-i18n MUST use `legacy: false`.**
`legacy: true` enables Options API (`this.$t()`), which conflicts with Vue 3 Composition API patterns used throughout this codebase. Always `legacy: false`.

**Actual runtime versions (from `package.json` — architecture doc had aspirational numbers):**
- Vue: 3.5.27
- TypeScript: ~5.9.3
- Vite: ^7.3.1 (NOT Vite 8 as architecture doc states)
- Pinia: ^3.0.4

### Implementation Reference

**`frontend/vite.config.ts` update — add imports and update plugins array:**
```typescript
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import { resolve } from 'node:path'

// plugins array — ORDER MATTERS: VueI18nPlugin must come after vue()
plugins: [
  vue(),
  VueI18nPlugin({
    include: resolve(__dirname, './src/locales/**'),
  }),
  vueDevTools(),
  tailwindcss(),
],
```
This pre-compiles `.json` locale files at build time — eliminates runtime JIT compilation, reduces bundle size. `VueI18nPlugin` after `vue()` is required; placing it before causes build errors.

**`frontend/src/plugins/i18n.ts`:**
```typescript
import { createI18n } from 'vue-i18n'
import en from '@/locales/en.json'
import de from '@/locales/de.json'

export type SupportedLocale = 'en' | 'de'
export const SUPPORTED_LOCALES: SupportedLocale[] = ['en', 'de']
const LOCALE_KEY = 'ttt_locale'

function detectLocale(): SupportedLocale {
  try {
    const stored = localStorage.getItem(LOCALE_KEY) as SupportedLocale | null
    if (stored && SUPPORTED_LOCALES.includes(stored)) return stored
    const browser = navigator.language.split('-')[0] as SupportedLocale
    return SUPPORTED_LOCALES.includes(browser) ? browser : 'en'
  } catch {
    return 'en'
  }
}

export const i18n = createI18n({
  legacy: false,
  locale: detectLocale(),
  fallbackLocale: 'en',
  messages: { en, de },
  datetimeFormats: {
    en: {
      short: { year: 'numeric', month: '2-digit', day: '2-digit' },     // 12/31/2025
      long:  { year: 'numeric', month: 'long',    day: 'numeric' },      // December 31, 2025
    },
    de: {
      short: { year: 'numeric', month: '2-digit', day: '2-digit' },     // 31.12.2025
      long:  { year: 'numeric', month: 'long',    day: 'numeric' },      // 31. Dezember 2025
    },
  },
  numberFormats: {
    en: {
      decimal: { style: 'decimal', minimumFractionDigits: 0, maximumFractionDigits: 2 },
      percent: { style: 'percent', minimumFractionDigits: 0 },
    },
    de: {
      decimal: { style: 'decimal', minimumFractionDigits: 0, maximumFractionDigits: 2 },
      percent: { style: 'percent', minimumFractionDigits: 0 },
    },
  },
})
```

**`frontend/src/stores/locale.ts`:**
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { i18n, type SupportedLocale, SUPPORTED_LOCALES } from '@/plugins/i18n'

const LOCALE_KEY = 'ttt_locale'

export const useLocaleStore = defineStore('locale', () => {
  const locale = ref<SupportedLocale>(i18n.global.locale.value as SupportedLocale)

  function setLocale(newLocale: SupportedLocale) {
    if (!SUPPORTED_LOCALES.includes(newLocale)) return
    locale.value = newLocale
    ;(i18n.global.locale as { value: SupportedLocale }).value = newLocale
    localStorage.setItem(LOCALE_KEY, newLocale)
  }

  return { locale, setLocale }
})
```

**`frontend/src/main.ts` update:**
```typescript
import { i18n } from './plugins/i18n'
// ...
app.use(createPinia())
app.use(i18n)        // ← add after pinia, before router
app.use(router)
app.mount('#app')
```

**Translation key convention — use semantic dot-notation:**
```json
{
  "home": {
    "title": "Tic-Tac-Tore",
    "subtitle": "Foosball statistics platform",
    "signInMessage": "Sign in to track your matches",
    "welcomeBack": "Welcome back!",
    "comingSoon": "Your foosball dashboard is coming soon."
  },
  "auth": {
    "signInWithGoogle": "Sign in with Google",
    "signOut": "Sign Out",
    "completingSignIn": "Completing secure sign-in…",
    "redirectFailed": "Login redirect failed. Please try again."
  },
  "common": {
    "save": "Save",
    "cancel": "Cancel",
    "loading": "Loading...",
    "error": "Something went wrong"
  },
  "match": {
    "submit": "Submit Match",
    "pending": "Pending Confirmation",
    "approved": "Approved",
    "rejected": "Rejected"
  },
  "leaderboard": {
    "title": "Leaderboard",
    "rank": "Rank",
    "player": "Player",
    "wins": "Wins",
    "losses": "Losses"
  },
  "stats": {
    "winRate": "Win Rate",
    "matchesPlayed": "Matches Played"
  }
}
```
German `de.json` must have 1:1 matching keys.

**Vitest mock patterns for tests 7.1–7.3:**
```typescript
// Mock navigator.language
vi.stubGlobal('navigator', { language: 'de-AT' })

// Mock localStorage
const localStorageMock = { getItem: vi.fn(), setItem: vi.fn(), removeItem: vi.fn() }
vi.stubGlobal('localStorage', localStorageMock)
localStorageMock.getItem.mockReturnValue('de')  // simulate stored value

// Restore after each test
afterEach(() => vi.unstubAllGlobals())
```

**Using translations in components (Vue SFC):**
```typescript
// script setup
import { useI18n } from 'vue-i18n'
const { t, d, n } = useI18n()

// In template
// {{ t('nav.home') }}
// {{ d(someDate, 'short') }}
// {{ n(someNumber, 'decimal') }}
```

**RTL-neutral Tailwind v4 logical utilities:**
| Physical (forbidden in new code) | Logical (use instead) |
|---|---|
| `ml-*` / `mr-*` | `ms-*` / `me-*` |
| `pl-*` / `pr-*` | `ps-*` / `pe-*` |
| `border-l-*` | `border-s-*` |
| `rounded-l-*` | `rounded-s-*` |

### Project Structure Notes

**Files to CREATE:**
```
frontend/src/
  locales/
    en.json          ← all English UI strings
    de.json          ← all German UI strings (keys 1:1 with en.json)
  plugins/
    i18n.ts          ← createI18n() config + detectLocale()
  stores/
    locale.ts        ← Pinia store: reactive locale + setLocale()
```

**Files to UPDATE:**
```
frontend/
  package.json              ← vue-i18n@10, @intlify/unplugin-vue-i18n (via npm install)
  vite.config.ts            ← add VueI18nPlugin (after vue(), before vueDevTools)
  src/main.ts               ← app.use(i18n)
  src/views/
    HomeHub.vue             ← 6 hardcoded strings → t() keys
  src/components/
    GoogleOAuthButton.vue   ← "Sign in with Google" + alert → t() + reactive error
    OAuthRedirectHandler.vue ← "Completing secure sign-in…" → t()
```

**NOTE:** `HelloWorld.vue`, `MatchRecordingForm.vue`, `MatchScoring.vue`, etc. do NOT exist yet — they are planned future components. They will be created i18n-ready in their respective stories.

**Existing store pattern to follow:** `src/stores/auth.ts` — uses `defineStore` with Setup syntax (arrow function with `ref`/`computed`/functions returned). Locale store must follow the same pattern.

### Previous Story Intelligence (1-1a)

- Package namespace for backend: `com.tictactore` — not relevant (this story is frontend-only)
- Vitest is the test runner for frontend unit tests (`npm run test:unit`)
- No Playwright E2E needed — locale switching has no async backend calls; Vitest unit tests sufficient
- The `stores/auth.ts` already exists and works — do NOT modify it in this story
- Fail-closed pattern from 1-1a: if `detectLocale()` throws (e.g., `localStorage` unavailable in SSR context), it must return `'en'` as fallback — wrap in try/catch

### Story Context: What This Story Does NOT Cover

- **Language switcher UI**: The UI toggle in the user cabinet is Story 1.4. This story only provides the programmatic API (`useLocaleStore().setLocale()`).
- **Backend notification localization**: Deferred to Story 3.1.
- **RTL layout implementation**: NFR says "no RTL implementation required now" — only CSS directionality-neutrality is required here.
- **User preference persistence to server**: Language preference is stored in `localStorage` only in this story. Server-side sync is Story 1.4 (Profile Management).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` §Story 1.2]
- [Source: `_bmad-output/planning-artifacts/prd.md` §Non-Functional Requirements > Internationalization]
- [Source: `_bmad-output/planning-artifacts/prd.md` §MVP Feature Set > FR59]
- [Source: `_bmad-output/planning-artifacts/architecture.md` §Selected Starter]
- [Source: `_project-spec/rules/1-write.md` §13. Backend vs Frontend Boundary]
- [Source: `_project-spec/rules/2-test.md`]
- [Dependency: Story 1.4 — Profile Management will add the UI language switcher that calls `useLocaleStore().setLocale()`]
- [Dependency: Story 3.1 — Push notification localization will need backend `MessageSource`]

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

_None_

### Completion Notes List

_Not started_

### File List

_Not started_
