// RED PHASE — imports will fail until the following files are created:
//   frontend/src/stores/locale.ts
//   frontend/src/plugins/i18n.ts
// Run: npm run test:unit
// Expected: all tests FAIL with module-not-found errors.
// After implementation: all tests must PASS (green phase).

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useLocaleStore } from '@/stores/locale'
import { i18n } from '@/plugins/i18n'

describe('useLocaleStore — AC1, AC3', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    // Reset i18n locale to 'en' before each test
    ;(i18n.global.locale as { value: string }).value = 'en'
  })

  afterEach(() => vi.unstubAllGlobals())

  // Task 7.4 — AC3: setLocale writes to localStorage
  it('setLocale("de") persists "de" to localStorage key ttt_locale', () => {
    const store = useLocaleStore()

    store.setLocale('de')

    expect(localStorage.getItem('ttt_locale')).toBe('de')
  })

  // Task 7.5 — AC1: setLocale updates i18n.global.locale.value reactively
  it('setLocale("de") updates i18n.global.locale.value to "de"', () => {
    const store = useLocaleStore()

    store.setLocale('de')

    expect(i18n.global.locale.value).toBe('de')
  })

  // AC1: locale ref on store reflects the current language
  it('store.locale ref reflects the locale set via setLocale()', () => {
    const store = useLocaleStore()

    store.setLocale('de')

    expect(store.locale).toBe('de')
  })

  // AC1: switch back to 'en'
  it('setLocale("en") switches locale back to "en" and persists it', () => {
    const store = useLocaleStore()
    store.setLocale('de')

    store.setLocale('en')

    expect(store.locale).toBe('en')
    expect(localStorage.getItem('ttt_locale')).toBe('en')
    expect(i18n.global.locale.value).toBe('en')
  })

  // Guard: unsupported locale is ignored — no store mutation
  it('setLocale() with unsupported locale is a no-op', () => {
    const store = useLocaleStore()

    // @ts-expect-error — intentional invalid value for test
    store.setLocale('zh')

    expect(store.locale).toBe('en')
    expect(localStorage.getItem('ttt_locale')).toBeNull()
  })
})
