import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import LoginView from '../LoginView.vue'

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders login button with correct oauth2 link', () => {
    const wrapper = mount(LoginView)
    
    const loginButton = wrapper.find('a')
    expect(loginButton.exists()).toBe(true)
    expect(loginButton.text()).toContain('Login with Google')
    expect(loginButton.attributes('href')).toBe('http://localhost:8080/oauth2/authorization/google')
  })
})
