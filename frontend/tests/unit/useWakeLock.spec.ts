import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { nextTick } from 'vue'
import { useWakeLock } from '@/composables/useWakeLock'

describe('[Story 5.5] useWakeLock Composable (ATDD Red Phase)', () => {
  let mockSentinel: {
    released: boolean
    release: ReturnType<typeof vi.fn>
    addEventListener: ReturnType<typeof vi.fn>
    removeEventListener: ReturnType<typeof vi.fn>
  }

  let originalWakeLock: unknown
  let originalVisibilityState: PropertyDescriptor | undefined
  let instances: ReturnType<typeof useWakeLock>[] = []

  beforeEach(() => {
    instances = []
    mockSentinel = {
      released: false,
      release: vi.fn().mockImplementation(async () => {
        mockSentinel.released = true
      }),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    }

    originalWakeLock = (navigator as unknown as { wakeLock: unknown }).wakeLock
    originalVisibilityState = Object.getOwnPropertyDescriptor(document, 'visibilityState')

    Object.defineProperty(navigator, 'wakeLock', {
      value: {
        request: vi.fn().mockResolvedValue(mockSentinel),
      },
      configurable: true,
      writable: true,
    })

    Object.defineProperty(document, 'visibilityState', {
      value: 'visible',
      configurable: true,
      writable: true,
    })
  })

  afterEach(() => {
    instances.forEach((instance) => instance.cleanup())
    instances = []
    vi.restoreAllMocks()
    if (originalWakeLock !== undefined) {
      Object.defineProperty(navigator, 'wakeLock', {
        value: originalWakeLock,
        configurable: true,
        writable: true,
      })
    } else {
      delete (navigator as unknown as { wakeLock?: unknown }).wakeLock
    }

    if (originalVisibilityState) {
      Object.defineProperty(document, 'visibilityState', originalVisibilityState)
    }
  })

  const createWakeLock = () => {
    const lock = useWakeLock()
    instances.push(lock)
    return lock
  }

  it('[P0] isSupported is true when wakeLock API exists in navigator', () => {
    const { isSupported } = createWakeLock()
    expect(isSupported.value).toBe(true)
  })

  it('[P0] request() acquires wake lock sentinel and sets isActive to true', async () => {
    const { request, isActive, sentinel } = createWakeLock()

    expect(isActive.value).toBe(false)
    expect(sentinel.value).toBeNull()

    const result = await request()

    expect(result).toBe(true)
    expect(navigator.wakeLock.request).toHaveBeenCalledWith('screen')
    expect(isActive.value).toBe(true)
    expect(sentinel.value).toBe(mockSentinel)
  })

  it('[P0] release() releases sentinel and resets isActive to false', async () => {
    const { request, release, isActive, sentinel } = createWakeLock()

    await request()
    expect(isActive.value).toBe(true)

    await release()

    expect(mockSentinel.release).toHaveBeenCalled()
    expect(isActive.value).toBe(false)
    expect(sentinel.value).toBeNull()
  })

  it('[P0] re-requests wake lock on document visibilitychange to visible when lock is active', async () => {
    const { request, isActive } = createWakeLock()

    await request()
    expect(isActive.value).toBe(true)
    expect(navigator.wakeLock.request).toHaveBeenCalledTimes(1)

    // Simulate switching away (document hidden)
    Object.defineProperty(document, 'visibilityState', {
      value: 'hidden',
      configurable: true,
    })
    document.dispatchEvent(new Event('visibilitychange'))
    await nextTick()

    // Simulate switching back (document visible)
    Object.defineProperty(document, 'visibilityState', {
      value: 'visible',
      configurable: true,
    })
    document.dispatchEvent(new Event('visibilitychange'))
    await nextTick()

    expect(navigator.wakeLock.request).toHaveBeenCalledTimes(2)
  })

  it('[P0] does not re-request wake lock on visibilitychange when isActive is false', async () => {
    const { release, isActive } = createWakeLock()

    await release()
    expect(isActive.value).toBe(false)

    Object.defineProperty(document, 'visibilityState', {
      value: 'visible',
      configurable: true,
    })
    document.dispatchEvent(new Event('visibilitychange'))
    await nextTick()

    expect(navigator.wakeLock.request).not.toHaveBeenCalled()
  })

  it('[P1] handles navigator.wakeLock unsupported environment gracefully', async () => {
    delete (navigator as unknown as { wakeLock?: unknown }).wakeLock
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})

    const { isSupported, request, isActive } = createWakeLock()

    expect(isSupported.value).toBe(false)
    const result = await request()

    expect(result).toBe(false)
    expect(isActive.value).toBe(false)
    warnSpy.mockRestore()
  })

  it('[P1] handles request rejection (NotAllowedError / low battery) gracefully without throwing', async () => {
    Object.defineProperty(navigator, 'wakeLock', {
      value: {
        request: vi.fn().mockRejectedValue(new Error('NotAllowedError: permission denied')),
      },
      configurable: true,
      writable: true,
    })
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})

    const { request, isActive } = createWakeLock()

    const result = await request()

    expect(result).toBe(false)
    expect(isActive.value).toBe(false)
    expect(warnSpy).toHaveBeenCalled()
    warnSpy.mockRestore()
  })
})
