import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import RulePicker from '@/features/match/components/RulePicker.vue'
import { useRuleConfigStore } from '@/stores/useRuleConfigStore'
import { useAuthStore, type UserProfile } from '@/stores/auth'
import { useMatchDraftStore } from '@/features/match/stores/matchDraftStore'
import type { RuleConfig } from '@/services/ruleConfigService'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => {
        const translations: Record<string, string> = {
          'rules.pickerTitle': 'Rule System',
          'rules.createCustom': '+ Custom Rule',
          'rules.setAsDefault': 'Set as default',
          'rules.lockedTournamentNotice': 'Rule system is locked to tournament settings (FR45)',
          'rules.tournamentBadge': 'Tournament Rule',
          'common.default': 'Default',
          'common.error': 'An error occurred',
        }
        return translations[key] || defaultVal || key
      },
    }),
  }
})

describe('RulePicker ATDD Component Tests (Tournament Rule Enforcement)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.profile = {
      id: 'user-1',
      defaultRuleConfigurationId: null,
    } as unknown as UserProfile

    const ruleStore = useRuleConfigStore()
    ruleStore.presets = [
      {
        id: 'rule-official',
        name: 'Official 3-Game Standard',
        type: 'PRESET',
        scoreLimit: 10,
        gameLimit: 3,
        winsNeeded: 2,
        winByTwo: false,
      } as unknown as RuleConfig,
    ]
    ruleStore.customRules = [
      {
        id: 'rule-custom',
        name: 'Custom 5-Game',
        type: 'CUSTOM',
        scoreLimit: 10,
        gameLimit: 5,
        winsNeeded: 3,
        winByTwo: true,
      } as unknown as RuleConfig,
    ]
  })

  it('should render locked badge and disabled interaction when isLocked is true', async () => {
    const draftStore = useMatchDraftStore()
    draftStore.ruleSystem = 'Official 3-Game Standard'

    const wrapper = mount(RulePicker, {
      props: {
        isLocked: true,
      },
    })

    // Notice banner
    expect(wrapper.text()).toContain('Rule system is locked to tournament settings')

    // "+ Custom Rule" action button hidden
    expect(wrapper.find('[data-testid="create-custom-rule-inline-btn"]').exists()).toBe(false)

    // "Set as default" pin button hidden
    expect(wrapper.find('[data-test="set-as-default-rule-btn"]').exists()).toBe(false)

    // Lock icon / badge on active chip
    const activeChip = wrapper.find('[data-testid="rule-chip-rule-official"]')
    expect(activeChip.exists()).toBe(true)
    expect(activeChip.text()).toContain('lock')
    expect(activeChip.text()).toContain('Tournament Rule')

    // Other chips disabled
    const otherChip = wrapper.find('[data-testid="rule-chip-rule-custom"]')
    expect(otherChip.classes()).toContain('pointer-events-none')

    // Attempting to click other chip does not change selection
    await otherChip.trigger('click')
    expect(draftStore.ruleSystem).toBe('Official 3-Game Standard')
  })
})
