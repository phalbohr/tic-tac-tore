import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMatchDraftStore } from '../frontend/src/features/match/stores/matchDraftStore'

describe('matchDraftStore search (ATDD RED PHASE)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it.skip('[P0] searchPlayers debounces API call by 300ms', async () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = []

    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([])
    } as Response)

    store.searchPlayers('ali')

    expect(fetchSpy).not.toHaveBeenCalled()

    vi.advanceTimersByTime(300)

    expect(fetchSpy).toHaveBeenCalledWith('/api/users/me/players/search?q=ali')
  })

  it.skip('[P0] searchPlayers clears results when query is empty', async () => {
    const store = useMatchDraftStore()
    store.searchResults = [{ id: '1', nickname: 'Alice', avatar: 'a' }]
    store.searchError = 'some error'
    store.searchLoading = true

    store.searchPlayers('')

    expect(store.searchResults).toEqual([])
    expect(store.searchError).toBeNull()
    expect(store.searchLoading).toBe(false)
  })

  it.skip('[P0] searchPlayers handles successful API response', async () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = []

    const mockResults = [
      { id: '1', nickname: 'Alice', avatar: 'avatar-1' }
    ]

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockResults)
    } as Response)

    store.searchPlayers('ali')
    vi.advanceTimersByTime(300)
    await vi.runAllTimersAsync()

    expect(store.searchResults).toEqual(mockResults)
    expect(store.searchError).toBeNull()
    expect(store.searchLoading).toBe(false)
  })

  it.skip('[P0] searchPlayers handles API error response', async () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = []

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500
    } as Response)

    store.searchPlayers('ali')
    vi.advanceTimersByTime(300)
    await vi.runAllTimersAsync()

    expect(store.searchError).toBe('Search service unavailable. Please try again later.')
    expect(store.searchResults).toEqual([])
    expect(store.searchLoading).toBe(false)
  })

  it.skip('[P0] searchPlayers handles network error', async () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = []

    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Network failed'))

    store.searchPlayers('ali')
    vi.advanceTimersByTime(300)
    await vi.runAllTimersAsync()

    expect(store.searchError).toBe('Network error. Please check your connection.')
    expect(store.searchResults).toEqual([])
    expect(store.searchLoading).toBe(false)
  })

  it.skip('[P0] closeSearch clears debounce timer', async () => {
    const store = useMatchDraftStore()
    store.frequentOpponents = []

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([])
    } as Response)

    store.searchPlayers('ali')
    store.closeSearch()

    vi.advanceTimersByTime(300)

    expect(store.searchQuery).toBe('')
    expect(store.searchResults).toEqual([])
    expect(store.searchError).toBeNull()
    expect(store.isSearchOpen).toBe(false)
  })

  it.skip('[P1] openSearch resets search state', () => {
    const store = useMatchDraftStore()

    store.searchQuery = 'previous'
    store.searchResults = [{ id: '1', nickname: 'Alice', avatar: 'a' }]
    store.searchError = 'error'
    store.searchLoading = true

    store.openSearch()

    expect(store.searchQuery).toBe('')
    expect(store.searchResults).toEqual([])
    expect(store.searchError).toBeNull()
    expect(store.searchLoading).toBe(false)
    expect(store.isSearchOpen).toBe(true)
  })
})
