import { describe, it, expect } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

// Read raw JSON from disk to bypass Vite's VueI18nPlugin transform (which compiles
// values to message functions, making Object.entries traversal incorrect).
const en = JSON.parse(readFileSync(resolve(__dirname, '../en.json'), 'utf-8'))
const de = JSON.parse(readFileSync(resolve(__dirname, '../de.json'), 'utf-8'))

type JsonNode = Record<string, unknown>

function collectLeafKeys(obj: JsonNode, prefix = ''): string[] {
  return Object.entries(obj).flatMap(([key, value]) => {
    const path = prefix ? `${prefix}.${key}` : key
    return typeof value === 'object' && value !== null && !Array.isArray(value)
      ? collectLeafKeys(value as JsonNode, path)
      : [path]
  })
}

describe('Locale file parity — AC5 (Task 7.6)', () => {
  const enKeys = collectLeafKeys(en as JsonNode)
  const deKeys = collectLeafKeys(de as JsonNode)
  const enKeySet = new Set(enKeys)
  const deKeySet = new Set(deKeys)

  // Every English key must exist in German
  it('[P1]all keys in en.json have a corresponding key in de.json', () => {
    const missing = enKeys.filter(k => !deKeySet.has(k))
    expect(missing).toEqual([])
  })

  // Every German key must exist in English (no orphaned translations)
  it('[P1]all keys in de.json have a corresponding key in en.json (no orphans)', () => {
    const orphans = deKeys.filter(k => !enKeySet.has(k))
    expect(orphans).toEqual([])
  })

  // Both files must have at least the required namespaces
  it('[P1]en.json contains required top-level namespaces: home, auth, common', () => {
    const topLevel = Object.keys(en as JsonNode)
    expect(topLevel).toContain('home')
    expect(topLevel).toContain('auth')
    expect(topLevel).toContain('common')
  })

  // Spot-check: required keys from AC5 components are present in both
  it('[P1]en.json contains all keys required by existing components', () => {
    expect(enKeySet.has('home.title')).toBe(true)
    expect(enKeySet.has('home.subtitle')).toBe(true)
    expect(enKeySet.has('home.signInMessage')).toBe(true)
    expect(enKeySet.has('home.welcomeBack')).toBe(true)
    expect(enKeySet.has('home.comingSoon')).toBe(true)
    expect(enKeySet.has('auth.signOut')).toBe(true)
    expect(enKeySet.has('auth.signInWithGoogle')).toBe(true)
    expect(enKeySet.has('auth.completingSignIn')).toBe(true)
    expect(enKeySet.has('auth.redirectFailed')).toBe(true)
  })
})
