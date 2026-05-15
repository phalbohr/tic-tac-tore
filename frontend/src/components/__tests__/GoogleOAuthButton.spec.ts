import { mount } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createI18n } from 'vue-i18n'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'
import en from '@/locales/en.json'
import de from '@/locales/de.json'

const testI18n = createI18n({ legacy: false, locale: 'en', messages: { en, de } })

describe('GoogleOAuthButton', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  it('renders a sign in with Google button', () => {
    const wrapper = mount(GoogleOAuthButton, {
      global: { plugins: [testI18n] },
    })
    expect(wrapper.text()).toContain('Sign in with Google')
  })

  it('stores intentUrl in sessionStorage before redirect', async () => {
    const wrapper = mount(GoogleOAuthButton, {
      props: { intentUrl: '/match/confirm/abc123' },
      global: { plugins: [testI18n] },
    })

    Object.defineProperty(window, 'location', {
      value: { href: '' },
      writable: true,
    })

    await wrapper.find('button').trigger('click')

    expect(sessionStorage.getItem('intent_url')).toBe('/match/confirm/abc123')
  })

  it('redirects to /oauth2/authorization/google on click', async () => {
    const wrapper = mount(GoogleOAuthButton, {
      global: { plugins: [testI18n] },
    })
    const assignSpy = vi.fn()
    Object.defineProperty(window, 'location', {
      value: { assign: assignSpy, href: '' },
      writable: true,
    })

    await wrapper.find('button').trigger('click')

    expect(window.location.href).toContain('/oauth2/authorization/google')
  })
})
