import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PendingApprovals from '../PendingApprovals.vue'

const mockPendingMatches = [
  {
    id: 'match-1',
    creatorName: 'Alice',
    teamAAttackerName: 'Alice',
    teamADefenderName: 'Bob',
    teamBAttackerName: 'Charlie',
    teamBDefenderName: 'David',
    status: 'PENDING_APPROVAL',
    createdAt: '2026-03-20T10:00:00Z',
    games: [
      { gameNumber: 1, teamAScore: 10, teamBScore: 8 },
      { gameNumber: 2, teamAScore: 5, teamBScore: 10 },
      { gameNumber: 3, teamAScore: 10, teamBScore: 7 }
    ]
  }
]

describe('PendingApprovals', () => {
  it('renders "No pending approvals" when list is empty', () => {
    const wrapper = mount(PendingApprovals, {
      props: {
        matches: []
      }
    })
    expect(wrapper.text()).toContain('No pending approvals')
  })

  it('renders match details correctly', () => {
    const wrapper = mount(PendingApprovals, {
      props: {
        matches: mockPendingMatches
      }
    })

    expect(wrapper.text()).toContain('Created by Alice')
    expect(wrapper.text()).toContain('Charlie & David')
    expect(wrapper.text()).toContain('Alice & Bob')
    expect(wrapper.text()).toContain('10 - 8')
    expect(wrapper.text()).toContain('5 - 10')
    expect(wrapper.text()).toContain('10 - 7')
  })

  it('emits approve event when approve button is clicked', async () => {
    const wrapper = mount(PendingApprovals, {
      props: {
        matches: mockPendingMatches
      }
    })

    const approveBtn = wrapper.find('button.approve-btn')
    await approveBtn.trigger('click')

    expect(wrapper.emitted('approve')).toBeTruthy()
    expect(wrapper.emitted('approve')?.[0]?.[0]).toBe('match-1')
  })

  it('emits reject event when reject button is clicked', async () => {
    const wrapper = mount(PendingApprovals, {
      props: {
        matches: mockPendingMatches
      }
    })

    const rejectBtn = wrapper.find('button.reject-btn')
    await rejectBtn.trigger('click')

    expect(wrapper.emitted('reject')).toBeTruthy()
    expect(wrapper.emitted('reject')?.[0]?.[0]).toBe('match-1')
  })
})
