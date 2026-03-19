import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MatchRecordingForm from '../MatchRecordingForm.vue'
import { createPinia, setActivePinia } from 'pinia'

// Mock users for testing
const mockUsers = [
  { id: 1, name: 'Alice', email: 'alice@example.com' },
  { id: 2, name: 'Bob', email: 'bob@example.com' },
  { id: 3, name: 'Charlie', email: 'charlie@example.com' },
  { id: 4, name: 'David', email: 'david@example.com' }
]

describe('MatchRecordingForm', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders correctly with player selection fields', () => {
    const wrapper = mount(MatchRecordingForm)
    
    expect(wrapper.find('h2').text()).toContain('Record New Match')
    expect(wrapper.findAll('select').length).toBe(3) // Teammate, Opponent 1, Opponent 2
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('disables submit button if players are not selected', () => {
    const wrapper = mount(MatchRecordingForm)
    const submitBtn = wrapper.find('button[type="submit"]')
    
    expect(submitBtn.attributes('disabled')).toBeDefined()
  })

  it('allows selecting players from the list', async () => {
    // We'll need a way to provide users to the component. 
    // Usually this would be via an API call or a store.
    // For now, let's assume it accepts users as a prop or fetches them.
    const wrapper = mount(MatchRecordingForm, {
      props: {
        availableUsers: mockUsers
      }
    })

    const selects = wrapper.findAll('select')
    
    // Select Teammate
    await selects[0].setValue(mockUsers[1].id)
    // Select Opponent 1
    await selects[1].setValue(mockUsers[2].id)
    // Select Opponent 2
    await selects[2].setValue(mockUsers[3].id)

    const submitBtn = wrapper.find('button[type="submit"]')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('emits submit event with selected player IDs', async () => {
    const wrapper = mount(MatchRecordingForm, {
      props: {
        availableUsers: mockUsers
      }
    })

    const selects = wrapper.findAll('select')
    await selects[0].setValue(mockUsers[1].id)
    await selects[1].setValue(mockUsers[2].id)
    await selects[2].setValue(mockUsers[3].id)

    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toBeTruthy()
    expect(wrapper.emitted('submit')?.[0][0]).toEqual({
      teammateId: mockUsers[1].id,
      opponent1Id: mockUsers[2].id,
      opponent2Id: mockUsers[3].id
    })
  })

  it('prevents selecting the same player multiple times', async () => {
    const wrapper = mount(MatchRecordingForm, {
      props: {
        availableUsers: mockUsers
      }
    })

    const selects = wrapper.findAll('select')
    
    // Select Alice as Teammate
    await selects[0].setValue(mockUsers[0].id)
    
    // Try to select Alice as Opponent 1
    // The component should probably filter out already selected users from other selects
    // or show an error. Let's assume it filters them out.
    const opponent1Options = selects[1].findAll('option')
    const aliceOption = opponent1Options.find(opt => opt.attributes('value') === mockUsers[0].id.toString())
    
    expect(aliceOption).toBeUndefined()
  })
})
