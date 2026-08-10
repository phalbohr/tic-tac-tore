import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { createPinia } from 'pinia'
import PlayerSelection from '../PlayerSelection.vue'

describe('PlayerSelection.vue', () => {
  it('renders player slots based on match type', () => {
    const pinia = createPinia()
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.findAll('.player-slot')).toHaveLength(2)
  })

  it('opens search overlay when search icon is clicked on empty slot', async () => {
    const pinia = createPinia()
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [pinia]
      }
    })

    const searchBtn = wrapper.find('[data-testid="open-search-button"]')
    await searchBtn.trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="player-search-overlay"]').exists()).toBe(true)
  })

  it('adds player via search overlay selection', async () => {
    const pinia = createPinia()
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [pinia]
      }
    })

    const searchBtn = wrapper.find('[data-testid="open-search-button"]')
    await searchBtn.trigger('click')
    await wrapper.vm.$nextTick()

    const overlay = wrapper.find('[data-testid="player-search-overlay"]')
    expect(overlay.exists()).toBe(true)

    const store = (wrapper.vm as any).$.setupState.store
    store.searchResults = [
      { id: 'player-1', nickname: 'Alice', avatar: 'avatar-1' }
    ]
    await wrapper.vm.$nextTick()

    const row = overlay.find('[data-testid="search-result-row"]')
    await row.trigger('click')
    await wrapper.vm.$nextTick()

    expect(store.selectedPlayers).toContain('player-1')
  })
})
