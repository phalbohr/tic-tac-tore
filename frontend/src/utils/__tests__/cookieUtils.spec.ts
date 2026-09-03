import { describe, it, expect, beforeEach } from 'vitest'
import { getCookie, deleteCookie } from '../cookieUtils'

describe('cookieUtils Tests', () => {
  beforeEach(() => {
    document.cookie.split(';').forEach((cookie) => {
      document.cookie = cookie
        .replace(/^ +/, '')
        .replace(/=.*/, `=;expires=${new Date(0).toUTCString()};path=/`)
    })
  })

  it('Condition - should return value when cookie is present', () => {
    document.cookie = 'myCookie=myValue'

    const result = getCookie('myCookie')

    expect(result).toBe('myValue')
  })

  it('Condition - should return undefined when cookie is not present', () => {
    document.cookie = 'otherCookie=123'

    const result = getCookie('myCookie')

    expect(result).toBeUndefined()
  })

  it('Condition - should return correct value when multiple cookies exist', () => {
    document.cookie = 'firstCookie=firstValue'
    document.cookie = 'myCookie=targetValue'
    document.cookie = 'lastCookie=lastValue'

    const result = getCookie('myCookie')

    expect(result).toBe('targetValue')
  })

  it('Condition - should not match when cookie name is a prefix of another cookie', () => {
    document.cookie = 'myCookieName=value'

    const result = getCookie('myCookie')

    expect(result).toBeUndefined()
  })

  it('Condition - should return empty string when cookie has empty value', () => {
    document.cookie = 'myCookie='

    const result = getCookie('myCookie')

    expect(result).toBe('')
  })

  it('Condition - should set cookie to expire in the past', () => {
    document.cookie = 'myCookie=value'

    deleteCookie('myCookie')

    expect(document.cookie).not.toContain('myCookie')
  })
})
