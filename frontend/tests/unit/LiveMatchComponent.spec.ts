import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import LiveMatch from '@/features/match/LiveMatch.vue'
import { useLiveMatchStore } from '@/stores/liveMatch'
import { useAuthStore } from '@/stores/auth'

const mockRoute = ref({
  query: {} as Record<string, string | string[]>,
})

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute.value,
}))

describe('[Story 5.4] LiveMatch.vue Component - Referee Mode & Auto Detection', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockRoute.value = { query: {} }
  })

  it('[P0] resets matchStore.isRefereeMode to false when unmounted', async () => {
    const store = useLiveMatchStore()
    const wrapper = mount(LiveMatch, {
      props: { refereeMode: true },
    })

    expect(store.isRefereeMode).toBe(true)

    wrapper.unmount()
    expect(store.isRefereeMode).toBe(false)
  })

  it('[P0] activates referee mode when route.query.mode is an array containing referee', async () => {
    mockRoute.value = { query: { mode: ['other', 'referee'] } }
    const store = useLiveMatchStore()

    const wrapper = mount(LiveMatch)
    expect(store.isRefereeMode).toBe(true)
    wrapper.unmount()
  })

  it('[P0] activates referee mode when route.query.referee is an array containing true', async () => {
    mockRoute.value = { query: { referee: ['true', '1'] } }
    const store = useLiveMatchStore()

    const wrapper = mount(LiveMatch)
    expect(store.isRefereeMode).toBe(true)
    wrapper.unmount()
  })

  it('[P0] automatically activates referee mode when authenticated user is not in the 4-player match roster', async () => {
    const authStore = useAuthStore()
    authStore.setAuthenticated(true)
    authStore.profile = {
      id: 'referee-user-123',
      nickname: 'ViktorTheRef',
      avatar: 'ref.png',
    }

    const store = useLiveMatchStore()
    // Standard roster: p1 (Alice), p2 (Bob), p3 (Charlie), p4 (Dave)
    const wrapper = mount(LiveMatch)

    expect(store.isRefereeMode).toBe(true)
    wrapper.unmount()
  })

  it('[P0] does not activate referee mode automatically when authenticated user is one of the players', async () => {
    const authStore = useAuthStore()
    authStore.setAuthenticated(true)
    authStore.profile = {
      id: 'p1',
      nickname: 'Alice',
      avatar: 'alice.png',
    }

    const store = useLiveMatchStore()
    const wrapper = mount(LiveMatch)

    expect(store.isRefereeMode).toBe(false)
    wrapper.unmount()
  })
})
