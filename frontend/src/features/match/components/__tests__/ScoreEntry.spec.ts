import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createTestingPinia } from '@pinia/testing'
import ScoreEntry from '../ScoreEntry.vue'
import { useMatchDraftStore, MatchType } from '../../stores/matchDraftStore'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, fallback?: string) => fallback || key,
    }),
  }
})

describe('ScoreEntry.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('emits back event when back button is clicked', async () => {
    const wrapper = mount(ScoreEntry, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              matchDraft: {
                matchState: 'score_entry',
                games: [],
                currentGame: { team1Score: 0, team2Score: 0 },
                ruleConfig: { gameLimit: 3 },
                frequentOpponents: [],
                fetchedPlayers: {},
                selectedPlayers: ['p1', 'p2'],
              },
            },
          }),
        ],
      },
    })

    const backButton = wrapper.findAll('button').find((w) => w.text().includes('Back'))
    expect(backButton).toBeDefined()
    await backButton!.trigger('click')

    expect(wrapper.emitted()).toHaveProperty('back')
  })

  it('renders game rows for games', () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = [
      { id: 'p1', nickname: 'Alice', avatar: '' },
      { id: 'p2', nickname: 'Bob', avatar: '' },
    ]
    store.selectedPlayers = ['p1', 'p2']
    store.matchType = MatchType.ONE_VS_ONE

    const wrapper = mount(ScoreEntry)
    expect(wrapper.find('[data-testid="select-game-btn-1"]').exists()).toBe(true)
  })

  it('disables Next Game button when game is incomplete and enables when complete', async () => {
    const store = useMatchDraftStore()
    store.ruleConfig = { scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false }
    store.selectedPlayers = ['p1', 'p2']
    store.matchState = 'score_entry'

    const wrapper = mount(ScoreEntry)
    const actionBtn = wrapper
      .findAll('button')
      .find((w) => w.text().includes('Next Game') || w.text().includes('Complete Match'))
    expect(actionBtn).toBeDefined()
    expect(actionBtn!.attributes('disabled')).toBeDefined()

    store.currentGame.team1Score = 10
    await wrapper.vm.$nextTick()
    expect(actionBtn!.attributes('disabled')).toBeUndefined()
  })

  it('shows Complete Match when completing final game', async () => {
    const store = useMatchDraftStore()
    store.ruleConfig = { scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false }
    store.selectedPlayers = ['p1', 'p2']
    store.games = [
      { team1Score: 10, team2Score: 5 },
      { team1Score: 5, team2Score: 10 },
    ]
    store.activeGameIndex = -1
    store.currentGame = { team1Score: 10, team2Score: 2 }
    store.matchState = 'score_entry'

    const wrapper = mount(ScoreEntry)
    const actionBtn = wrapper.findAll('button').find((w) => w.text().includes('Complete Match'))
    expect(actionBtn).toBeDefined()
  })
})
