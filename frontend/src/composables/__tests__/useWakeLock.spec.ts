import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { effectScope } from 'vue'
import { useWakeLock } from '../useWakeLock'

describe('useWakeLock', () => {
  let mockSentinel: {
    released: boolean
    type: string
    release: ReturnType<typeof vi.fn>
    addEventListener: ReturnType<typeof vi.fn>
    removeEventListener: ReturnType<typeof vi.fn>
  }

  let requestMock: ReturnType<typeof vi.fn>

  beforeEach(() => {
    mockSentinel = {
      released: false,
      type: 'screen',
      release: vi.fn().mockImplementation(async () => {
        mockSentinel.released = true
      }),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    }

    requestMock = vi.fn().mockResolvedValue(mockSentinel)

    Object.defineProperty(navigator, 'wakeLock', {
      value: {
        request: requestMock,
      },
      configurable: true,
      writable: true,
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('detects wake lock support correctly', () => {
    const { isSupported } = useWakeLock()

    expect(isSupported.value).toBe(true)
  })

  it('acquires wake lock and sets isActive to true', async () => {
    const { request, isActive, sentinel } = useWakeLock()

    const result = await request()

    expect(result).toBe(true)
    expect(isActive.value).toBe(true)
    expect(sentinel.value).toBe(mockSentinel)
    expect(requestMock).toHaveBeenCalledWith('screen')
  })

  it('releases wake lock and sets isActive to false', async () => {
    const { request, release, isActive, sentinel } = useWakeLock()

    await request()
    await release()

    expect(isActive.value).toBe(false)
    expect(sentinel.value).toBeNull()
    expect(mockSentinel.release).toHaveBeenCalledTimes(1)
  })

  it('keeps lock active when request is called again while an initial request is in-flight after release', async () => {
    let resolveLock!: (val: typeof mockSentinel) => void
    requestMock.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveLock = resolve
        }),
    )

    const { request, release, isActive, sentinel } = useWakeLock()

    const p1 = request()
    expect(isActive.value).toBe(true)

    await release()
    expect(isActive.value).toBe(false)

    const p2 = request()
    expect(isActive.value).toBe(true)

    resolveLock(mockSentinel)

    const [result1, result2] = await Promise.all([p1, p2])

    expect(result1).toBe(true)
    expect(result2).toBe(true)
    expect(isActive.value).toBe(true)
    expect(sentinel.value).toBe(mockSentinel)
    expect(mockSentinel.release).not.toHaveBeenCalled()
  })

  it('releases lock upon resolution if release was called during in-flight request and not requested again', async () => {
    let resolveLock!: (val: typeof mockSentinel) => void
    requestMock.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveLock = resolve
        }),
    )

    const { request, release, isActive, sentinel } = useWakeLock()

    const p1 = request()
    await release()
    expect(isActive.value).toBe(false)

    resolveLock(mockSentinel)
    const result = await p1

    expect(result).toBe(false)
    expect(isActive.value).toBe(false)
    expect(sentinel.value).toBeNull()
    expect(mockSentinel.release).toHaveBeenCalledTimes(1)
  })

  it('cleans up event listeners and releases lock on scope dispose', async () => {
    const scope = effectScope()
    let wakeLockInstance!: ReturnType<typeof useWakeLock>

    scope.run(() => {
      wakeLockInstance = useWakeLock()
    })

    await wakeLockInstance.request()
    expect(wakeLockInstance.isActive.value).toBe(true)

    scope.stop()

    expect(wakeLockInstance.isActive.value).toBe(false)
    expect(mockSentinel.release).toHaveBeenCalledTimes(1)
  })
})
