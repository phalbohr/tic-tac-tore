// RED PHASE — imports will fail until frontend/src/plugins/i18n.ts is created.
// Run: npm run test:unit
// Expected: FAIL with "Cannot find module '@/plugins/i18n'"
// After implementation: all tests must PASS (green phase).

import { describe, it, expect } from 'vitest'
import { i18n } from '@/plugins/i18n'

// ─── AC2: Extensibility — new locale requires no component code changes ────────

describe('i18n extensibility — AC2', () => {
  it('registering a new locale via setLocaleMessage() makes t() return its translations without component changes', () => {
    const testMessages = { home: { title: 'Тест-заголовок' } }

    i18n.global.setLocaleMessage('test-locale', testMessages)
    // @ts-expect-error — 'test-locale' not in SupportedLocale union, intentional for test
    i18n.global.locale.value = 'test-locale'

    expect(i18n.global.t('home.title')).toBe('Тест-заголовок')

    // Restore
    i18n.global.locale.value = 'en'
  })
})

// ─── AC6: Date formatting — MM/DD/YYYY (en) vs DD.MM.YYYY (de) ───────────────

describe('date formatting via d() — AC6', () => {
  // Dec 31, 2025 — unambiguous test date
  const testDate = new Date(2025, 11, 31)

  it('English "short" format uses MM/DD/YYYY pattern', () => {
    const result = i18n.global.d(testDate, 'short', 'en')

    // Must contain 12/31/2025 — month-first
    expect(result).toMatch(/12.31.2025/)
  })

  it('German "short" format uses DD.MM.YYYY pattern', () => {
    const result = i18n.global.d(testDate, 'short', 'de')

    // Must contain 31.12.2025 — day-first
    expect(result).toMatch(/31\.12\.2025/)
  })

  it('both locales have "short" and "long" datetime formats registered', () => {
    // If format is not registered, d() throws or returns raw value
    expect(() => i18n.global.d(testDate, 'short', 'en')).not.toThrow()
    expect(() => i18n.global.d(testDate, 'long', 'en')).not.toThrow()
    expect(() => i18n.global.d(testDate, 'short', 'de')).not.toThrow()
    expect(() => i18n.global.d(testDate, 'long', 'de')).not.toThrow()
  })
})

// ─── AC6: Number formatting — '.' decimal (en) vs ',' decimal (de) ───────────

describe('number formatting via n() — AC6', () => {
  const testNumber = 1234.56

  it('English "decimal" format uses "." as decimal separator', () => {
    const result = i18n.global.n(testNumber, 'decimal', 'en')

    expect(result).toContain('.')
    expect(result).not.toContain(',')
  })

  it('German "decimal" format uses "," as decimal separator', () => {
    const result = i18n.global.n(testNumber, 'decimal', 'de')

    expect(result).toContain(',')
  })

  it('both locales have "decimal" and "percent" number formats registered', () => {
    expect(() => i18n.global.n(testNumber, 'decimal', 'en')).not.toThrow()
    expect(() => i18n.global.n(0.42, 'percent', 'en')).not.toThrow()
    expect(() => i18n.global.n(testNumber, 'decimal', 'de')).not.toThrow()
    expect(() => i18n.global.n(0.42, 'percent', 'de')).not.toThrow()
  })
})
