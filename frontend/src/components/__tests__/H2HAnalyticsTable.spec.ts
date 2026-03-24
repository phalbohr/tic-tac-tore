import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import H2HAnalyticsTable from '../H2HAnalyticsTable.vue'

const mockH2H = [
  {
    opponentId: 'opp-1',
    opponentName: 'Rival 1',
    matches: 10,
    wins: 7,
    losses: 3,
    winRate: 70.0
  },
  {
    opponentId: 'opp-2',
    opponentName: 'Rival 2',
    matches: 5,
    wins: 1,
    losses: 4,
    winRate: 20.0
  }
]

describe('H2HAnalyticsTable', () => {
  it('renders H2H records correctly', () => {
    const wrapper = mount(H2HAnalyticsTable, {
      props: { h2hRecords: mockH2H }
    })

    expect(wrapper.text()).toContain('Rival 1')
    expect(wrapper.text()).toContain('70.0%')
    expect(wrapper.text()).toContain('Rival 2')
    expect(wrapper.text()).toContain('20.0%')
  })

  it('sorts by wins when column header is clicked', async () => {
    const wrapper = mount(H2HAnalyticsTable, {
      props: { h2hRecords: mockH2H }
    })

    // Default order is as passed (Rival 1, Rival 2)
    let rows = wrapper.findAll('tbody tr')
    expect(rows[0].text()).toContain('Rival 1')

    // Click on Wins header to sort (initial click might be asc, click again for desc)
    const winsHeader = wrapper.findAll('th').find(th => th.text().includes('Wins'))
    await winsHeader?.trigger('click')
    
    // Check if sorted (Rival 2 has 1 win, Rival 1 has 7 wins)
    // Assuming first click is ASC: Rival 2, Rival 1
    rows = wrapper.findAll('tbody tr')
    expect(rows[0].text()).toContain('Rival 2')

    await winsHeader?.trigger('click') // DESC: Rival 1, Rival 2
    rows = wrapper.findAll('tbody tr')
    expect(rows[0].text()).toContain('Rival 1')
  })

  it('sorts by matches correctly', async () => {
    const wrapper = mount(H2HAnalyticsTable, {
      props: { h2hRecords: mockH2H }
    })
    const header = wrapper.findAll('th').find(th => th.text().includes('Matches'))
    await header?.trigger('click') // ASC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 2') // 5 matches
    await header?.trigger('click') // DESC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 1') // 10 matches
  })

  it('sorts by losses correctly', async () => {
    const wrapper = mount(H2HAnalyticsTable, {
      props: { h2hRecords: mockH2H }
    })
    const header = wrapper.findAll('th').find(th => th.text().includes('Losses'))
    await header?.trigger('click') // ASC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 1') // 3 losses
    await header?.trigger('click') // DESC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 2') // 4 losses
  })

  it('sorts by winRate correctly', async () => {
    const wrapper = mount(H2HAnalyticsTable, {
      props: { h2hRecords: mockH2H }
    })
    const header = wrapper.findAll('th').find(th => th.text().includes('Win Rate'))
    await header?.trigger('click') // ASC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 2') // 20%
    await header?.trigger('click') // DESC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 1') // 70%
  })

  it('sorts by opponentName correctly', async () => {
    const wrapper = mount(H2HAnalyticsTable, {
      props: { h2hRecords: mockH2H }
    })
    const header = wrapper.findAll('th').find(th => th.text().includes('Opponent'))
    await header?.trigger('click') // ASC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 1')
    await header?.trigger('click') // DESC
    expect(wrapper.findAll('tbody tr')[0].text()).toContain('Rival 2')
  })

  it('shows empty state when no records provided', () => {
    const wrapper = mount(H2HAnalyticsTable, {
      props: { h2hRecords: [] }
    })

    expect(wrapper.text()).toContain('No head-to-head records yet')
  })
})
