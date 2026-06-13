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

    const buttons = wrapper.findAll('button')
    // 24 preset buttons + 1 close button + 1 cancel button = 26 buttons total
    expect(buttons.length).toBe(26)
  })

  it('emits select event when an avatar is clicked', async () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
    })

    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(1)
    const firstAvatarBtn = buttons[1]!
    await firstAvatarBtn.trigger('click')

    const emittedSelect = wrapper.emitted('select')
    expect(emittedSelect).toBeTruthy()
    expect(emittedSelect?.[0]?.[0]).toBe('ball-classic')
  })

  it('emits close event when cancel button is clicked', async () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
    })

    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
    const cancelBtn = buttons[buttons.length - 1]!
    await cancelBtn.trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('emits close event when close icon button is clicked', async () => {
    const wrapper = mount(AvatarPicker, {
      global: { plugins: [testI18n] },
    })

    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
    const closeIconBtn = buttons[0]!
    await closeIconBtn.trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
