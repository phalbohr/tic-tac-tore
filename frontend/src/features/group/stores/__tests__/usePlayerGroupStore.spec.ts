import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, afterEach, vi, type Mock } from 'vitest'
import { usePlayerGroupStore } from '@/features/group/stores/usePlayerGroupStore'

/**
 * ATDD Red-Phase Scaffolds for usePlayerGroupStore.
 * Story 6.1: Named Player Groups ("Teams")
 *
 * AC 1: Persists group associated with creator; supports built-in "Favorites" group (FR39)
 * AC 2: Fetches user-isolated player groups with safe summary DTOs
 * AC 3: Select group / filter members in portrait match creation
 * AC 5: Handles group updates and deletions
 */
describe('usePlayerGroupStore (ATDD Red Phase)', () => {
  let fetchMock: Mock
  const originalFetch = globalThis.fetch

  beforeEach(() => {
    setActivePinia(createPinia())
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })

  afterEach(() => {
    globalThis.fetch = originalFetch
    vi.restoreAllMocks()
  })

  it('initializes with default empty state', () => {
    const store = usePlayerGroupStore()

    expect(store.groups).toEqual([])
    expect(store.selectedGroupId).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.favoriteGroup).toBeUndefined()
    expect(store.customGroups).toEqual([])
  })

  it('fetchGroups() populates groups and resolves favoriteGroup & customGroups getters', async () => {
    const mockGroups = [
      {
        id: 'group-fav-1',
        name: 'Favorites',
        isFavorite: true,
        creatorId: 'user-1',
        members: [{ id: 'user-2', nickname: 'Alice', avatar: 'avatar-1' }],
        createdAt: '2026-08-23T10:00:00Z',
      },
      {
        id: 'group-custom-1',
        name: 'Tuesday Squad',
        isFavorite: false,
        creatorId: 'user-1',
        members: [{ id: 'user-3', nickname: 'Bob', avatar: 'avatar-2' }],
        createdAt: '2026-08-23T11:00:00Z',
      },
    ]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => mockGroups,
    })

    const store = usePlayerGroupStore()
    await store.fetchGroups()

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/player-groups', expect.anything())
    expect(store.groups).toHaveLength(2)
    expect(store.favoriteGroup?.name).toBe('Favorites')
    expect(store.customGroups).toHaveLength(1)
    expect(store.customGroups[0]?.name).toBe('Tuesday Squad')
  })

  it('createGroup() sends POST request and appends newly created group', async () => {
    const newGroup = {
      id: 'group-new-1',
      name: 'Weekend Warriors',
      isFavorite: false,
      creatorId: 'user-1',
      members: [{ id: 'user-4', nickname: 'Charlie', avatar: 'avatar-3' }],
      createdAt: '2026-08-23T12:00:00Z',
    }

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => newGroup,
    })

    const store = usePlayerGroupStore()
    const result = await store.createGroup({
      name: 'Weekend Warriors',
      memberIds: ['user-4'],
      isFavorite: false,
    })

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/player-groups', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: 'Weekend Warriors',
        memberIds: ['user-4'],
        isFavorite: false,
      }),
    })
    expect(result.id).toBe('group-new-1')
    expect(store.groups).toContainEqual(newGroup)
  })

  it('updateGroup() sends PUT request and updates group in state', async () => {
    const existingGroup = {
      id: 'group-1',
      name: 'Old Name',
      isFavorite: false,
      creatorId: 'user-1',
      members: [],
      createdAt: '2026-08-23T10:00:00Z',
    }

    const updatedGroup = {
      ...existingGroup,
      name: 'New Name',
      isFavorite: true,
    }

    const store = usePlayerGroupStore()
    store.groups = [existingGroup]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => updatedGroup,
    })

    await store.updateGroup('group-1', {
      name: 'New Name',
      memberIds: [],
      isFavorite: true,
    })

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/player-groups/group-1', expect.objectContaining({
      method: 'PUT',
    }))
    expect(store.getGroupById('group-1')?.name).toBe('New Name')
    expect(store.getGroupById('group-1')?.isFavorite).toBe(true)
  })

  it('deleteGroup() sends DELETE request and removes group from state', async () => {
    const store = usePlayerGroupStore()
    store.groups = [
      {
        id: 'group-to-delete',
        name: 'To Delete',
        isFavorite: false,
        creatorId: 'user-1',
        members: [],
        createdAt: '2026-08-23T10:00:00Z',
      },
    ]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 204,
    })

    await store.deleteGroup('group-to-delete')

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/player-groups/group-to-delete', expect.objectContaining({
      method: 'DELETE',
    }))
    expect(store.groups).toHaveLength(0)
  })

  it('selectGroup() updates selectedGroupId', () => {
    const store = usePlayerGroupStore()
    store.selectGroup('group-123')

    expect(store.selectedGroupId).toBe('group-123')

    store.selectGroup(null)
    expect(store.selectedGroupId).toBeNull()
  })
})
