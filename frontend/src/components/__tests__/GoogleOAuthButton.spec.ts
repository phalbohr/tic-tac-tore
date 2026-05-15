import { mount } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'
import { i18n } from '@/plugins/i18n'

describe('GoogleOAuthButton', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  it('renders a sign in with Google button', () => {
    const wrapper = mount(GoogleOAuthButton, {
      global: { plugins: [i18n] },
    })
    expect(wrapper.text()).toContain('Sign in with Google')
  })

  it('stores intentUrl in sessionStorage before redirect', async () => {
    const wrapper = mount(GoogleOAuthButton, {
      props: { intentUrl: '/match/confirm/abc123' },
      global: { plugins: [i18n] },
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
      global: { plugins: [i18n] },
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
