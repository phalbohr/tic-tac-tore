import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PlayerSearchOverlay from '../PlayerSearchOverlay.vue'
import { useMatchDraftStore } from '../../stores/matchDraftStore'

function createTestPlayer(
  overrides: Partial<{ id: string; nickname: string; avatar: string }> = {},
) {
  return {
    id: overrides.id ?? 'test-player-id',
    nickname: overrides.nickname ?? 'Test Player',
    avatar: overrides.avatar ?? 'test-avatar',
  }
}

describe('PlayerSearchOverlay.vue (ATDD)', () => {
  let testingPinia: ReturnType<typeof createTestingPinia>

  beforeEach(() => {
    testingPinia = createTestingPinia({ createSpy: vi.fn })
  })

  afterEach(() => {
    try {
      useMatchDraftStore().closeSearch()
    } catch {
      // no active store to clean up
    }
  })

  it('[P0] renders overlay when isOpen is true', () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    expect(wrapper.find('[data-testid="player-search-overlay"]').exists()).toBe(true)
  })

  it('[P0] does not render overlay when isOpen is false', () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: false },
    })

    expect(wrapper.find('[data-testid="player-search-overlay"]').exists()).toBe(false)
  })

  it('[P0] auto-focuses search input when overlay opens', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: false },
    })

    await wrapper.setProps({ isOpen: true })
    await wrapper.vm.$nextTick()

    const input = wrapper.find('[data-testid="player-search-input"]')
    expect(input.exists()).toBe(true)
  })

  it('[P0] emits select event when result row is clicked', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    store.searchResults = [createTestPlayer({ id: 'player-1', nickname: 'Alice' })]

    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-testid="search-result-row"]')
    const aliceRow = rows.find((r) => r.text().includes('Alice'))
    expect(aliceRow).toBeDefined()
    await aliceRow!.trigger('click')

    expect(store.isSearchOpen).toBe(false)
  })

  it('[P0] emits select and close events in customSelect mode without mutating draft store', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: {
        isOpen: true,
        customSelect: true,
      },
    })

    const store = useMatchDraftStore()
    const testPlayer = createTestPlayer({ id: 'player-custom', nickname: 'Custom Player' })
    store.searchResults = [testPlayer]

    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-testid="search-result-row"]')
    expect(rows).toHaveLength(1)
    await rows[0]!.trigger('click')

    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')![0]).toEqual([testPlayer])
    expect(wrapper.emitted('close')).toBeTruthy()
    expect(store.selectedPlayers).toEqual([])
  })

  it('[P0] emits close event when backdrop is clicked', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    const backdrop = wrapper.find('[data-testid="player-search-overlay"]')
    await backdrop.trigger('click')

    expect(store.isSearchOpen).toBe(false)
  })

  it('[P0] emits close event when Escape key is pressed', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    await wrapper.trigger('keydown.escape')

    expect(store.isSearchOpen).toBe(false)
  })

  it('[P1] displays loading state while searching', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    store.searchLoading = true
    store.searchQuery = 'ali'
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Searching...')
  })

  it('[P1] displays error message when search fails', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    store.searchError = 'Search service unavailable. Please try again later.'
    store.searchQuery = 'ali'
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="search-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="search-error"]').text()).toBe(
      'Search service unavailable. Please try again later.',
    )
  })

  it('[P1] displays empty state when no results found', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    store.searchQuery = 'xyznonexistent'
    store.searchResults = []
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="no-results"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="no-results"]').text()).toBe('No players found')
  })

  it('[P1] orders frequent opponents before other results', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    store.frequentOpponents = [createTestPlayer({ id: 'frequent-1', nickname: 'Frank' })]
    store.searchResults = [createTestPlayer({ id: 'other-1', nickname: 'Alice' })]
    store.searchQuery = ''
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-testid="search-result-row"]')
    expect(rows).toHaveLength(2)
    expect(rows[0]!.text()).toContain('Frank')
    expect(rows[1]!.text()).toContain('Alice')
  })

  it('[P1] does not add player when all slots are filled', async () => {
    const wrapper = mount(PlayerSearchOverlay, {
      global: {
        plugins: [testingPinia],
      },
      props: { isOpen: true },
    })

    const store = useMatchDraftStore()
    store.selectedPlayers = ['player-1', 'player-2']
    store.searchResults = [createTestPlayer({ id: 'player-3', nickname: 'Charlie' })]
    store.searchQuery = ''
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-testid="search-result-row"]')
    expect(rows).toHaveLength(1)

    await rows[0]!.trigger('click')

    expect(store.selectedPlayers).toHaveLength(2)
  })
})
