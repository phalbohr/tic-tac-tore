import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PlayerSearchOverlay from '../PlayerSearchOverlay.vue'
import { useMatchDraftStore } from '../../stores/matchDraftStore'

describe('PlayerSearchOverlay.vue (ATDD)', () => {
  let store: ReturnType<typeof useMatchDraftStore>

  let testingPinia: ReturnType<typeof createTestingPinia>

  beforeEach(() => {
    testingPinia = createTestingPinia({ createSpy: vi.fn })
    store = useMatchDraftStore()
  })

  afterEach(() => {
    store.closeSearch()
  })

  it('[P0] renders overlay when isOpen is true', () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    expect(wrapper.find('[data-testid="player-search-overlay"]').exists()).toBe(true)
  })

  it('[P0] does not render overlay when isOpen is false', () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: false }
    })

    expect(wrapper.find('[data-testid="player-search-overlay"]').exists()).toBe(false)
  })

  it('[P0] auto-focuses search input when overlay opens', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: false }
    })

    await wrapper.setProps({ isOpen: true })
    await wrapper.vm.$nextTick()

    const input = wrapper.find('[data-testid="player-search-input"]')
    expect(input.exists()).toBe(true)
  })

  it('[P0] emits select event when result row is clicked', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    store.searchResults = [
      { id: 'player-1', nickname: 'Alice', avatar: 'avatar-1' }
    ]

    await wrapper.vm.$nextTick()

    const row = wrapper.find('[data-testid="search-result-row"]')
    await row.trigger('click')

    expect(store.isSearchOpen).toBe(false)
  })

  it('[P0] emits close event when backdrop is clicked', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    const backdrop = wrapper.find('[data-testid="player-search-overlay"]')
    await backdrop.trigger('click')

    expect(store.isSearchOpen).toBe(false)
  })

  it('[P0] emits close event when Escape key is pressed', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    await wrapper.trigger('keydown.escape')

    expect(store.isSearchOpen).toBe(false)
  })

  it('[P1] displays loading state while searching', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    store.searchLoading = true
    store.searchQuery = 'ali'
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Searching...')
  })

  it('[P1] displays error message when search fails', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    store.searchError = 'Search service unavailable. Please try again later.'
    store.searchQuery = 'ali'
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="search-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="search-error"]').text()).toBe('Search service unavailable. Please try again later.')
  })

  it('[P1] displays empty state when no results found', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    store.searchQuery = 'xyznonexistent'
    store.searchResults = []
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="no-results"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="no-results"]').text()).toBe('No players found')
  })

  it('[P1] orders frequent opponents before other results', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    store.frequentOpponents = [
      { id: 'frequent-1', nickname: 'Frank', avatar: 'avatar-f' }
    ]
    store.searchResults = [
      { id: 'other-1', nickname: 'Alice', avatar: 'avatar-a' }
    ]
    store.searchQuery = ''
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-testid="search-result-row"]')
    expect(rows.length).toBe(2)
    expect(rows.at(0).text()).toContain('Frank')
    expect(rows.at(1).text()).toContain('Alice')
  })

  it('[P1] does not add player when all slots are filled', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia]
      },
      props: { isOpen: true }
    })

    store.selectedPlayers = ['player-1', 'player-2']
    store.searchResults = [
      { id: 'player-3', nickname: 'Charlie', avatar: 'avatar-c' }
    ]
    store.searchQuery = ''
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-testid="search-result-row"]')
    expect(rows.length).toBe(1)

    await rows.at(0).trigger('click')

    expect(store.selectedPlayers).toHaveLength(2)
  })
})
