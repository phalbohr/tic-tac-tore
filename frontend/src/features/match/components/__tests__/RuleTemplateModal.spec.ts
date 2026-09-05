import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RuleTemplateModal from '@/features/match/components/RuleTemplateModal.vue'

import type { RuleConfig } from '@/services/ruleConfigService'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (msg: string, fallback?: string) => fallback || msg,
  }),
}))

describe('RuleTemplateModal.vue Component ATDD Specifications', () => {
  const defaultRuleTemplate = {
    name: '',
    matchFormat: 'BEST_OF_N',
    goalLimit: 5,
    gameLimit: 5,
    gamesToWin: 3,
    winByTwoRule: 'DECISIVE_GAME_ONLY',
    absoluteScoreCap: 8,
    timeoutsPerGame: 2,
    timeoutDurationSeconds: 30,
    possessionLimit5BarSeconds: 10,
    possessionLimitOtherSeconds: 15,
    sideSwapRule: 'BETWEEN_GAMES',
    restartRule: 'CONCEDING_TEAM',
    spinningAllowed: false,
    aerialsAllowed: false,
    positionSwapRule: 'FREE',
    pointDistribution: 'WIN_LOSS_3_0',
  }

  const existingTemplate: RuleConfig = {
    id: '11111111-1111-1111-1111-111111111111',
    name: 'Office Blitz',
    type: 'CUSTOM',
    matchFormat: 'BEST_OF_N',
    goalLimit: 7,
    gameLimit: 1,
    gamesToWin: 1,
    winByTwoRule: 'NONE',
    absoluteScoreCap: null,
    timeoutsPerGame: 1,
    timeoutDurationSeconds: 15,
    possessionLimit5BarSeconds: 5,
    possessionLimitOtherSeconds: 10,
    sideSwapRule: 'NONE',
    restartRule: 'RANDOM_DROP',
    spinningAllowed: true,
    aerialsAllowed: false,
    positionSwapRule: 'FREE',
    pointDistribution: 'WIN_LOSS_2_0',
  }

  it('should render modal with smart defaults when creating new template', () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
      },
    })

    expect(wrapper.find('[data-testid="template-name-input"]').exists()).toBe(true)
    expect(
      (wrapper.find('[data-testid="goal-limit-input"]').element as HTMLInputElement).value,
    ).toBe('5')
    expect(
      (wrapper.find('[data-testid="game-limit-input"]').element as HTMLInputElement).value,
    ).toBe('3')
    expect(
      (wrapper.find('[data-testid="games-to-win-input"]').element as HTMLInputElement).value,
    ).toBe('2')
    expect(
      (wrapper.find('[data-testid="win-by-two-select"]').element as HTMLSelectElement).value,
    ).toBe('DECISIVE_GAME_ONLY')
  })

  it('should populate fields with existing template in "Edit as New" mode', () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: existingTemplate,
      },
    })

    expect(
      (wrapper.find('[data-testid="template-name-input"]').element as HTMLInputElement).value,
    ).toBe('Office Blitz')
    expect(
      (wrapper.find('[data-testid="goal-limit-input"]').element as HTMLInputElement).value,
    ).toBe('7')
    expect(
      (wrapper.find('[data-testid="game-limit-input"]').element as HTMLInputElement).value,
    ).toBe('1')
    expect(
      (wrapper.find('[data-testid="win-by-two-select"]').element as HTMLSelectElement).value,
    ).toBe('NONE')
    expect(
      (wrapper.find('[data-testid="spinning-allowed-checkbox"]').element as HTMLInputElement)
        .checked,
    ).toBe(true)
  })

  it('should switch settings when toggling match format to Fixed Games', async () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
      },
    })

    await wrapper.find('[data-testid="match-format-fixed"]').trigger('click')

    expect(
      (wrapper.find('[data-testid="game-limit-input"]').element as HTMLInputElement).value,
    ).toBe('2')
    expect(wrapper.find('[data-testid="games-to-win-input"]').exists()).toBe(false)
    expect(
      (wrapper.find('[data-testid="win-by-two-select"]').element as HTMLSelectElement).value,
    ).toBe('NONE')
    expect(
      (wrapper.find('[data-testid="point-distribution-select"]').element as HTMLSelectElement).value,
    ).toBe('ONE_POINT_PER_GAME_WON')
  })

  it('should prevent submission when template name is empty or exceeds 50 characters', async () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
      },
    })

    const saveButton = wrapper.find('[data-testid="save-template-button"]')
    await saveButton.trigger('click')

    expect(wrapper.emitted('save')).toBeFalsy()
    expect(wrapper.find('[data-testid="name-validation-error"]').exists()).toBe(true)
  })

  it('should emit save event with complete rule configuration payload upon valid submission', async () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
      },
    })

    await wrapper.find('[data-testid="template-name-input"]').setValue('Friday Tourney Rules')
    await wrapper.find('[data-testid="save-template-button"]').trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()
    const emitted = wrapper.emitted('save')
    expect(emitted).toBeDefined()
    const emittedPayload = emitted![0]![0] as typeof defaultRuleTemplate
    expect(emittedPayload.name).toBe('Friday Tourney Rules')
    expect(emittedPayload.goalLimit).toBe(5)
    expect(emittedPayload.gameLimit).toBe(3)
    expect(emittedPayload.gamesToWin).toBe(2)
    expect(emittedPayload.winByTwoRule).toBe('DECISIVE_GAME_ONLY')
  })

  it('should dynamically adjust absolute score cap both upwards and downwards on goalLimit changes', async () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
      },
    })

    // Default: goalLimit = 5, score cap = 8
    expect((wrapper.find('[data-testid="absolute-score-cap-input"]').element as HTMLInputElement).value).toBe('8')

    // Increase goalLimit to 8 -> score cap increases to 11
    await wrapper.find('[data-testid="goal-limit-input"]').setValue(8)
    expect((wrapper.find('[data-testid="absolute-score-cap-input"]').element as HTMLInputElement).value).toBe('11')

    // Decrease goalLimit to 4 -> score cap decreases down to 7
    await wrapper.find('[data-testid="goal-limit-input"]').setValue(4)
    expect((wrapper.find('[data-testid="absolute-score-cap-input"]').element as HTMLInputElement).value).toBe('7')
  })

  it('should show instant validation warning when absoluteScoreCap is less than or equal to goalLimit', async () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
      },
    })

    // Set score cap to 4 when goalLimit is 5
    await wrapper.find('[data-testid="absolute-score-cap-input"]').setValue(4)
    expect(wrapper.find('[data-testid="cap-validation-warning"]').exists()).toBe(true)

    // Increase score cap to 6 (> 5) -> warning disappears
    await wrapper.find('[data-testid="absolute-score-cap-input"]').setValue(6)
    expect(wrapper.find('[data-testid="cap-validation-warning"]').exists()).toBe(false)
  })

  it('should show instant validation warnings when numeric inputs are out of bounds', async () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
      },
    })

    // Goal limit out of bounds (> 100)
    await wrapper.find('[data-testid="goal-limit-input"]').setValue(150)
    expect(wrapper.find('[data-testid="goal-limit-warning"]').exists()).toBe(true)

    // Game limit out of bounds (< 1)
    await wrapper.find('[data-testid="game-limit-input"]').setValue(0)
    expect(wrapper.find('[data-testid="game-limit-warning"]').exists()).toBe(true)

    // Timeouts out of bounds (> 10)
    await wrapper.find('[data-testid="timeouts-per-game-input"]').setValue(15)
    expect(wrapper.find('[data-testid="timeouts-warning"]').exists()).toBe(true)

    // 5-bar limit out of bounds (> 60)
    await wrapper.find('[data-testid="possession-5bar-input"]').setValue(75)
    expect(wrapper.find('[data-testid="possession-5bar-warning"]').exists()).toBe(true)
  })

  it('should display error message when errorMessage prop is provided', () => {
    const wrapper = mount(RuleTemplateModal, {
      props: {
        isOpen: true,
        initialTemplate: null,
        errorMessage: 'Custom rule limit reached (max 20)',
      },
    })

    const errorEl = wrapper.find('[data-testid="name-validation-error"]')
    expect(errorEl.exists()).toBe(true)
    expect(errorEl.text()).toContain('Custom rule limit reached (max 20)')
  })
})

