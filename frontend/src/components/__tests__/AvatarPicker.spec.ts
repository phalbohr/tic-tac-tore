import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { createI18n } from 'vue-i18n'
import AvatarPicker from '@/components/AvatarPicker.vue'
import en from '@/locales/en.json'
import de from '@/locales/de.json'

const testI18n = createI18n({ legacy: false, locale: 'en', messages: { en, de } })

describe('AvatarPicker', () => {
  it('renders all 24 preset avatar buttons', () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
    })

    const avatarButtons = wrapper.findAll('[data-testid^="avatar-option-"]')

    expect(avatarButtons.length).toBe(24)
  })

  it('emits select event when an avatar is clicked', async () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
    })
    const firstAvatarBtn = wrapper.find('[data-testid="avatar-option-ball-classic"]')

    await firstAvatarBtn.trigger('click')

    const emittedSelect = wrapper.emitted('select')
    expect(emittedSelect).toBeTruthy()
    expect(emittedSelect?.[0]?.[0]).toBe('ball-classic')
  })

  it('emits close event when cancel button is clicked', async () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
    })
    const cancelBtn = wrapper.find('[data-testid="cancel-picker-button"]')

    await cancelBtn.trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('emits close event when close icon button is clicked', async () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
    })
    const closeIconBtn = wrapper.find('[data-testid="close-picker-icon-button"]')

    await closeIconBtn.trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('handles Shift+Tab correctly when modal container is focused', async () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
      attachTo: document.body,
    })

    const modalRef = wrapper.find('[data-testid="avatar-picker-backdrop"]')
    const cancelBtn = wrapper.find('[data-testid="cancel-picker-button"]')

    // Focus the modal container
    ;(modalRef.element as HTMLElement).focus()
    expect(document.activeElement).toBe(modalRef.element)

    // Simulate Shift+Tab
    const event = new KeyboardEvent('keydown', {
      key: 'Tab',
      shiftKey: true,
      bubbles: true,
      cancelable: true,
    })

    // dispatch event on the window, since event listener is attached to window
    window.dispatchEvent(event)

    // Focus should be moved to the last focusable element (cancel button)
    expect(document.activeElement).toBe(cancelBtn.element)

    wrapper.unmount()
  })
})
