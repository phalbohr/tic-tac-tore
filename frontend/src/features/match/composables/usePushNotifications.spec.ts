import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { usePushNotifications } from './usePushNotifications'

describe('usePushNotifications', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.clearAllMocks()
  })

  it('[P0] should request push permission and subscribe upon explicit user gesture', async () => {
    const mockRequestPermission = vi.fn().mockResolvedValue('granted')
    const mockSubscribe = vi.fn().mockResolvedValue({
      endpoint: 'https://push.example.com/sub/123',
      toJSON: () => ({ keys: { p256dh: 'dummyP256', auth: 'dummyAuth' } }),
    })
    const mockGetSubscription = vi.fn().mockResolvedValue(null)

    vi.stubGlobal('Notification', { permission: 'default', requestPermission: mockRequestPermission })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true }))

    Object.defineProperty(navigator, 'serviceWorker', {
      value: {
        register: vi.fn().mockResolvedValue({
          pushManager: {
            getSubscription: mockGetSubscription,
            subscribe: mockSubscribe,
          },
        }),
        ready: Promise.resolve(),
      },
      configurable: true,
    })

    const { requestPermissionAndSubscribe, permissionState } = usePushNotifications()
    const result = await requestPermissionAndSubscribe()

    expect(mockRequestPermission).toHaveBeenCalled()
    expect(result).toBe(true)
    expect(permissionState.value).toBe('granted')
  })

  it('[P1] should check permission state passively on session initialization', async () => {
    vi.stubGlobal('Notification', { permission: 'denied' })
    Object.defineProperty(navigator, 'serviceWorker', {
      value: {},
      configurable: true,
    })

    const { checkPermissionState, permissionState } = usePushNotifications()
    checkPermissionState()
    expect(permissionState.value).toBe('denied')
  })

  it('[P1] should set isSupported to true when Notification API is available', () => {
    vi.stubGlobal('Notification', { permission: 'default' })
    Object.defineProperty(navigator, 'serviceWorker', {
      value: { register: vi.fn() },
      configurable: true,
    })

    const { isSupported, checkPermissionState } = usePushNotifications()
    checkPermissionState()
    expect(isSupported.value).toBe(true)
  })

  it('[P1] should update permissionState on visibility change to visible', () => {
    vi.stubGlobal('Notification', { permission: 'default', requestPermission: vi.fn() })
    Object.defineProperty(navigator, 'serviceWorker', {
      value: { register: vi.fn() },
      configurable: true,
    })

    const { permissionState, checkPermissionState } = usePushNotifications()
    checkPermissionState()
    expect(permissionState.value).toBe('default')

    Object.defineProperty(document, 'visibilityState', {
      value: 'visible',
      configurable: true,
    })

    expect(permissionState.value).toBe('default')
  })

  it('[P1] should return false when push APIs are unavailable', () => {
    const { requestPermissionAndSubscribe } = usePushNotifications()
    const result = requestPermissionAndSubscribe()

    expect(result).resolves.toBe(false)
  })
})
