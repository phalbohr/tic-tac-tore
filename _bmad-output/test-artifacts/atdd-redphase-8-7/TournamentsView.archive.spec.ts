import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import TournamentsView from '@/features/tournament/views/TournamentsView.vue'
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore'

describe.skip('TournamentsView.vue Archive & Standings Tab (ATDD red phase: Story 8.7)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('allows switching between Active and Archive tabs', async () => {
    const store = useTournamentStore()
    store.archiveTournaments = [
      {
        id: 'arch-1',
        title: 'Historic Tournament 2025',
        status: 'COMPLETED' as any,
        format: 'CHAMPIONSHIP' as any,
        mode: 'ONE_VS_ONE' as any,
        createdAt: '2025-01-01T00:00:00Z',
      } as any,
    ]

    const wrapper = mount(TournamentsView)

    const archiveTab = wrapper.find('[data-testid="tab-archive"]')
    expect(archiveTab.exists()).toBe(true)

    await archiveTab.trigger('click')

    expect(wrapper.text()).toContain('Historic Tournament 2025')
  })
})
