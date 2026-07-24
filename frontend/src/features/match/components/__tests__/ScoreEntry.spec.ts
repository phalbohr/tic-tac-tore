import { mount } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import ScoreEntry from '../ScoreEntry.vue'
import { createTestingPinia } from '@pinia/testing'
import { useMatchDraftStore } from '../../stores/matchDraftStore'
import { setActivePinia, createPinia } from 'pinia'

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
})
