import { describe, it, expect } from 'vitest'
import { getInitials } from '../avatar'

describe('getInitials', () => {
  it('returns empty string for null, undefined or empty values', () => {
    expect(getInitials(null)).toBe('')
    expect(getInitials(undefined)).toBe('')
    expect(getInitials('')).toBe('')
    expect(getInitials('   ')).toBe('')
  })

  it('returns single uppercase character for single word name', () => {
    expect(getInitials('alice')).toBe('A')
    expect(getInitials('  bob  ')).toBe('B')
  })

  it('returns max 2 uppercase initials for multi-word names', () => {
    expect(getInitials('John Doe')).toBe('JD')
    expect(getInitials('john doe')).toBe('JD')
    expect(getInitials('Alice Bob Charlie')).toBe('AB')
  })
})
