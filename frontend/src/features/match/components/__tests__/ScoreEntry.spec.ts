import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createTestingPinia } from '@pinia/testing'
import ScoreEntry from '../ScoreEntry.vue'
import { useMatchDraftStore, MatchType } from '../../stores/matchDraftStore'

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
                selectedPlayers: ['p1', 'p2']
              }
            }
          })
        ]
      }
    })

    const backButton = wrapper.findAll('button').find(w => w.text().includes('Back'))
    expect(backButton).toBeDefined()
    await backButton!.trigger('click')

    expect(wrapper.emitted()).toHaveProperty('back')
  })

  it('renders correctly for 1v1', () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = [
      { id: 'p1', nickname: 'Alice', avatar: '' },
      { id: 'p2', nickname: 'Bob', avatar: '' }
    ]
    store.selectedPlayers = ['p1', 'p2']
    store.matchType = MatchType.ONE_VS_ONE

    const wrapper = mount(ScoreEntry)
    const headings = wrapper.findAll('h3')

    expect(headings.length).toBe(2)
    expect(headings[0]!.text()).toBe('Alice')
    expect(headings[1]!.text()).toBe('Bob')
  })

  it('renders correctly for 2v2', () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = [
      { id: 'p1', nickname: 'Alice', avatar: '' },
      { id: 'p2', nickname: 'Bob', avatar: '' },
      { id: 'p3', nickname: 'Charlie', avatar: '' },
      { id: 'p4', nickname: 'Dave', avatar: '' }
    ]

    store.selectedPlayers = ['p1', 'p2', 'p3', 'p4']

    store.matchType = MatchType.TWO_VS_TWO

    const wrapper = mount(ScoreEntry)
    const headings = wrapper.findAll('h3')

    expect(headings[0]!.text()).toBe('Alice & Bob')
    expect(headings[1]!.text()).toBe('Charlie & Dave')
  })

  it('renders correctly for 3v3 (6 players)', () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = [
      { id: 'p1', nickname: 'P1', avatar: '' },
      { id: 'p2', nickname: 'P2', avatar: '' },
      { id: 'p3', nickname: 'P3', avatar: '' },
      { id: 'p4', nickname: 'P4', avatar: '' },
      { id: 'p5', nickname: 'P5', avatar: '' },
      { id: 'p6', nickname: 'P6', avatar: '' }
    ]
    store.selectedPlayers = ['p1', 'p2', 'p3', 'p4', 'p5', 'p6']

    const wrapper = mount(ScoreEntry)
    const headings = wrapper.findAll('h3')

    expect(headings[0]!.text()).toBe('P1 & P2 & P3')
    expect(headings[1]!.text()).toBe('P4 & P5 & P6')
  })
})
