import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import MyMatchesView from '../../views/MyMatchesView.vue'
import { useMatchHistoryStore } from '../../stores/useMatchHistoryStore'

const mockPush = vi.fn()
const mockReplace = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({
    query: { tab: 'confirmed' },
  }),
  useRouter: () => ({
    push: mockPush,
    replace: mockReplace,
  }),
  RouterLink: {
    template: '<a :href="to"><slot /></a>',
    props: ['to'],
  },
}))

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => {
        const translations: Record<string, string> = {
          'history.title': 'My Matches',
          'history.tabs.confirmed': 'Confirmed',
          'history.tabs.pending': 'Pending',
          'history.filters.all': 'All',
          'history.filters.player': 'Filter by Player',
          'match.submit': 'Submit Match',
        }
        return translations[key] || defaultVal || key
      },
    }),
  }
})

describe('MyMatchesView.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders title, confirmed tab and pending tab', () => {
    const wrapper = mount(MyMatchesView, {
      global: {
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    expect(wrapper.find('[data-testid="history-title"]').text()).toBe('My Matches')
    expect(wrapper.find('[data-testid="tab-confirmed"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="tab-pending"]').exists()).toBe(true)
  })

  it('switches between confirmed and pending tabs on click', async () => {
    const store = useMatchHistoryStore()
    const wrapper = mount(MyMatchesView, {
      global: {
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    await wrapper.find('[data-testid="tab-pending"]').trigger('click')
    expect(store.activeTab).toBe('pending')

    await wrapper.find('[data-testid="tab-confirmed"]').trigger('click')
    expect(store.activeTab).toBe('confirmed')
  })
})
