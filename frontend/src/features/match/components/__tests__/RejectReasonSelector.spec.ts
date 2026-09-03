import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RejectReasonSelector from '../RejectReasonSelector.vue'

const mocks = {
  $t: (key: string, fallback?: string) => {
    const translations: Record<string, string> = {
      'match.rejectMatch': 'Reject Match',
      'match.selectReason': 'Select reason for rejection',
      'match.submitRejection': 'Submit Rejection',
      'match.customReasonPlaceholder': 'Optional details (max 200 chars)',
      'match.reasonWrongScore': 'Wrong score',
      'match.reasonWrongPlayers': 'Wrong players',
      'match.reasonDidNotPlay': 'Did not play',
      'match.reasonOther': 'Other',
      'common.cancel': 'Cancel',
    }
    return translations[key] || fallback || key
  },
}

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: mocks.$t,
  }),
}))

describe('RejectReasonSelector.vue', () => {
  it('does not render modal content when isOpen is false', () => {
    const wrapper = mount(RejectReasonSelector, {
      props: { isOpen: false },
      global: { mocks },
    })
    expect(wrapper.find('[data-testid="rejection-dialog-title"]').exists()).toBe(false)
  })

  it('renders modal content when isOpen is true', () => {
    const wrapper = mount(RejectReasonSelector, {
      props: { isOpen: true },
      global: { mocks },
    })
    expect(wrapper.find('[data-testid="rejection-dialog-title"]').text()).toBe('Reject Match')
    expect(wrapper.text()).toContain('Wrong score')
    expect(wrapper.text()).toContain('Wrong players')
    expect(wrapper.text()).toContain('Did not play')
    expect(wrapper.text()).toContain('Other')
  })

  it('disables submit button by default when no reason is selected', () => {
    const wrapper = mount(RejectReasonSelector, {
      props: { isOpen: true },
      global: { mocks },
    })
    const submitBtn = wrapper.find('[data-testid="submit-rejection-btn"]')
    expect(submitBtn.attributes('disabled')).toBeDefined()
  })

  it('enables submit button and emits submit event with selected reason', async () => {
    const wrapper = mount(RejectReasonSelector, {
      props: { isOpen: true },
      global: { mocks },
    })

    const radioOptions = wrapper.findAll('input[type="radio"]')
    await radioOptions[0]!.setValue('Wrong score')

    const submitBtn = wrapper.find('[data-testid="submit-rejection-btn"]')
    expect(submitBtn.attributes('disabled')).toBeUndefined()

    await submitBtn.trigger('click')
    expect(wrapper.emitted('submit')).toBeTruthy()
    expect(wrapper.emitted('submit')![0]).toEqual([{ reason: 'Wrong score', customReason: '' }])
  })

  it('requires custom text when Other is selected', async () => {
    const wrapper = mount(RejectReasonSelector, {
      props: { isOpen: true },
      global: { mocks },
    })

    const radioOptions = wrapper.findAll('input[type="radio"]')
    const otherRadio = radioOptions.find((r) => (r.element as HTMLInputElement).value === 'Other')
    await otherRadio?.setValue('Other')

    const submitBtn = wrapper.find('[data-testid="submit-rejection-btn"]')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    const textarea = wrapper.find('[data-testid="rejection-free-text"]')
    await textarea.setValue('Match was played on different table')

    expect(submitBtn.attributes('disabled')).toBeUndefined()

    await submitBtn.trigger('click')
    expect(wrapper.emitted('submit')![0]).toEqual([
      { reason: 'Other', customReason: 'Match was played on different table' },
    ])
  })

  it('emits cancel event when cancel button is clicked', async () => {
    const wrapper = mount(RejectReasonSelector, {
      props: { isOpen: true },
      global: { mocks },
    })

    const cancelBtn = wrapper.findAll('button').find((b) => b.text() === 'Cancel')
    await cancelBtn?.trigger('click')
    expect(wrapper.emitted('cancel')).toBeTruthy()
  })
})
