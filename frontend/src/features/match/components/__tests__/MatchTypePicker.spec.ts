import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import MatchTypePicker from '../MatchTypePicker.vue'

describe('MatchTypePicker.vue', () => {
  it('renders two buttons for match types', () => {
    const wrapper = mount(MatchTypePicker, {
      global: {
        plugins: [createTestingPinia()]
      }
    })
    expect(wrapper.findAll('button')).toHaveLength(2)
  })
})
