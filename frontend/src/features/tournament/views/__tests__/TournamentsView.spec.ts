import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import TournamentsView from '@/features/tournament/views/TournamentsView.vue'
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore'
import { useAuthStore } from '@/stores/auth'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
}))

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, params?: Record<string, string | number>) => {
        const translations: Record<string, string> = {
          'tournament.title': 'Tournaments',
          'tournament.subtitle': 'Compete in structured cups and championships',
          'tournament.tabs.active': 'Active & Upcoming',
          'tournament.tabs.archive': 'Archive',
          'tournament.archive.empty': 'No completed tournaments in archive.',
          'tournament.archive.viewDetails': 'View Details & Standings',
          'tournament.standings.bracketTab': 'Bracket & Schedule',
          'tournament.standings.tabTitle': 'Standings',
          'tournament.bracket.completed': 'COMPLETED',
          'tournament.bracket.live': 'LIVE',
          'common.loading': 'Loading...',
        }
        let res = translations[key] || key
        if (params) {
          for (const [k, v] of Object.entries(params)) {
            res = res.replace(`{${k}}`, String(v))
          }
        }
        return res
      },
    }),
  }
})

describe('TournamentsView.vue Archive & Standings Integration', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('allows switching between Active and Archive tabs and renders archive list', async () => {
    const store = useTournamentStore()
    store.archiveTournaments = [
      {
        id: 'arch-1',
        name: 'Historic Tournament 2025',
        status: 'COMPLETED',
        format: 'CHAMPIONSHIP',
        mode: 'ONE_VS_ONE_PERSONAL',
        ruleConfiguration: {
          id: 'rc-1',
          name: 'Default',
          goalLimit: 5,
          gameLimit: 1,
          winByTwo: false,
        },
        minParticipants: 4,
        maxParticipants: 8,
        registrationDeadline: '2025-01-01T00:00:00Z',
        hasPlayoff: false,
        creatorId: 'u-1',
        creatorNickname: 'Host',
        createdAt: '2025-01-01T00:00:00Z',
      },
    ]

    const wrapper = mount(TournamentsView)

    const archiveTab = wrapper.find('[data-testid="tab-archive"]')
    expect(archiveTab.exists()).toBe(true)

    await archiveTab.trigger('click')

    expect(wrapper.text()).toContain('Historic Tournament 2025')
    expect(wrapper.find('[data-testid="tournament-status-badge"]').text()).toBe('COMPLETED')
  })
})
