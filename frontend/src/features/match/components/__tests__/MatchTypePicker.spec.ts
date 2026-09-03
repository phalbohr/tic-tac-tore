import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import MatchTypePicker from '../MatchTypePicker.vue'

describe('MatchTypePicker.vue', () => {
  it('renders two buttons for match types', () => {
    const wrapper = mount(MatchTypePicker, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
      },
    })
    expect(wrapper.findAll('button')).toHaveLength(2)
  })
})
