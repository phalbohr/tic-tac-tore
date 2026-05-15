// GUARD PHASE — static analysis, no module imports needed.
// Fails if any story-modified file introduces physical CSS direction utilities.
// Run: npm run test:unit
//
// AC7: RTL-neutral CSS — all new/modified files must use Tailwind v4 logical
// utilities (ms-*, me-*, ps-*, pe-*, border-s-*, border-e-*, start-*, end-*)
// instead of physical direction utilities (ml-*, mr-*, pl-*, pr-*, etc.)

import { describe, it, expect } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve, join } from 'node:path'

// Files created or modified by story 1.2
const STORY_FILES = [
  'src/views/HomeHub.vue',
  'src/components/GoogleOAuthButton.vue',
  'src/components/OAuthRedirectHandler.vue',
  'src/plugins/i18n.ts',
  'src/stores/locale.ts',
]

// Physical direction utilities forbidden per story 1.2 Dev Notes (Tailwind v4 RTL table)
const FORBIDDEN: Array<{ pattern: RegExp; label: string }> = [
  { pattern: /\bml-[\w]/, label: 'ml-* (use ms-*)' },
  { pattern: /\bmr-[\w]/, label: 'mr-* (use me-*)' },
  { pattern: /\bpl-[\w]/, label: 'pl-* (use ps-*)' },
  { pattern: /\bpr-[\w]/, label: 'pr-* (use pe-*)' },
  { pattern: /\bborder-l[-\w]/, label: 'border-l-* (use border-s-*)' },
  { pattern: /\bborder-r[-\w]/, label: 'border-r-* (use border-e-*)' },
  { pattern: /\btext-left\b/, label: 'text-left (use text-start)' },
  { pattern: /\btext-right\b/, label: 'text-right (use text-end)' },
  { pattern: /\bleft-[\w]/, label: 'left-* (use start-*)' },
  { pattern: /\bright-[\w]/, label: 'right-* (use end-*)' },
  { pattern: /margin-left\s*:/, label: 'margin-left CSS property' },
  { pattern: /margin-right\s*:/, label: 'margin-right CSS property' },
  { pattern: /padding-left\s*:/, label: 'padding-left CSS property' },
  { pattern: /padding-right\s*:/, label: 'padding-right CSS property' },
]

describe('RTL-neutral CSS — AC7', () => {
  const frontendRoot = resolve(__dirname, '..')

  it.each(STORY_FILES)('[P1] %s uses no physical CSS direction utilities', (relativePath) => {
    let content: string
    try {
      content = readFileSync(join(frontendRoot, relativePath), 'utf-8')
    } catch {
      // File does not exist yet — skip (will be caught once created)
      return
    }

    const violations: string[] = []
    content.split('\n').forEach((line, idx) => {
      for (const { pattern, label } of FORBIDDEN) {
        if (pattern.test(line)) {
          violations.push(`  line ${idx + 1} [${label}]: ${line.trim()}`)
          break
        }
      }
    })

    expect(violations, `Physical direction CSS found in ${relativePath}:\n${violations.join('\n')}`).toEqual([])
  })
})
