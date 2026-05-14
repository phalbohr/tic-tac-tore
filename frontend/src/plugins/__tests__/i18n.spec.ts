// RED PHASE — imports will fail until frontend/src/plugins/i18n.ts is created.
// Run: npm run test:unit
// Expected: all tests FAIL with "Cannot find module '@/plugins/i18n'"
// After implementation: all tests must PASS (green phase).
//
// NOTE FOR DEV AGENT: detectLocale() must be exported from i18n.ts
// so it can be tested independently of the i18n instance.

import { describe, it, expect, afterEach, vi } from 'vitest'
import { detectLocale } from '@/plugins/i18n'

describe('detectLocale() — AC4', () => {
  afterEach(() => vi.unstubAllGlobals())

  // Task 7.1 — AC4: browser language prefix 'de' from 'de-AT'
  it('returns "de" when navigator.language is "de-AT"', () => {
    vi.stubGlobal('localStorage', { getItem: vi.fn().mockReturnValue(null), setItem: vi.fn(), removeItem: vi.fn() })
    vi.stubGlobal('navigator', { language: 'de-AT' })

    expect(detectLocale()).toBe('de')
  })

  // Task 7.2 — AC4: unsupported locale falls back to 'en'
  it('returns "en" for unsupported browser locale "fr-FR"', () => {
    vi.stubGlobal('localStorage', { getItem: vi.fn().mockReturnValue(null), setItem: vi.fn(), removeItem: vi.fn() })
    vi.stubGlobal('navigator', { language: 'fr-FR' })

    expect(detectLocale()).toBe('en')
  })

  // Task 7.3 — AC3/AC4: localStorage value takes precedence over browser language
  it('returns stored locale from localStorage when key ttt_locale is present', () => {
    vi.stubGlobal('localStorage', { getItem: vi.fn().mockReturnValue('de'), setItem: vi.fn(), removeItem: vi.fn() })
    vi.stubGlobal('navigator', { language: 'en-US' })

    expect(detectLocale()).toBe('de')
  })

  // Task 7.7 — fail-closed: localStorage.getItem throws → return 'en'
  it('returns "en" when localStorage.getItem throws (fail-closed / SSR context)', () => {
    vi.stubGlobal('localStorage', {
      getItem: vi.fn().mockImplementation(() => { throw new Error('localStorage blocked') }),
      setItem: vi.fn(),
      removeItem: vi.fn(),
    })
    vi.stubGlobal('navigator', { language: 'de' })

    expect(detectLocale()).toBe('en')
  })

  // Edge case: invalid stored value rejected, falls back to browser language
  it('ignores invalid stored locale and falls back to browser language', () => {
    vi.stubGlobal('localStorage', { getItem: vi.fn().mockReturnValue('zh'), setItem: vi.fn(), removeItem: vi.fn() })
    vi.stubGlobal('navigator', { language: 'de' })

    expect(detectLocale()).toBe('de')
  })
})
