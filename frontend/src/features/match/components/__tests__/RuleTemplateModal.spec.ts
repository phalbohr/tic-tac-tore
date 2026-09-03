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
    goalLimit: 5,
    gameLimit: 3,
    winByTwo: true,
    absoluteScoreCap: 8,
    timeoutsPerGame: 2,
    timeoutDurationSeconds: 30,
    possessionLimit5BarSeconds: 10,
    possessionLimitOtherSeconds: 15,
    sideSwapRule: 'BETWEEN_GAMES',
    restartRule: 'CONCEDING_TEAM',
    spinningAllowed: false,
    aerialsAllowed: false,
    positionSwapRule: 'BETWEEN_GAMES',
    pointDistribution: 'WIN_LOSS_3_0',
  }

  const existingTemplate: RuleConfig = {
    id: '11111111-1111-1111-1111-111111111111',
    name: 'Office Blitz',
    type: 'CUSTOM',
    goalLimit: 7,
    gameLimit: 1,
    winByTwo: false,
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
      (wrapper.find('[data-testid="win-by-two-checkbox"]').element as HTMLInputElement).checked,
    ).toBe(true)
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
      (wrapper.find('[data-testid="win-by-two-checkbox"]').element as HTMLInputElement).checked,
    ).toBe(false)
    expect(
      (wrapper.find('[data-testid="spinning-allowed-checkbox"]').element as HTMLInputElement)
        .checked,
    ).toBe(true)
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
