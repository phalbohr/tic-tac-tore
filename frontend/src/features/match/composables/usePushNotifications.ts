import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { getCsrfHeaders } from '../../../utils/cookieUtils'

function urlBase64ToUint8Array(base64String: string): Uint8Array {
  try {
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4)
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/')
    const rawData = typeof window !== 'undefined' && window.atob ? window.atob(base64) : ''
    const outputArray = new Uint8Array(rawData.length)
    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i)
    }
    return outputArray
  } catch {
    return new Uint8Array(0)
  }
}

export function usePushNotifications() {
  const isSupported = ref(false)
  const permissionState = ref<NotificationPermission>('default')
  const isSubscribed = ref(false)

  function checkPermissionState() {
    if (typeof window !== 'undefined' && 'Notification' in window && 'serviceWorker' in navigator) {
      isSupported.value = true
      permissionState.value = Notification.permission
    } else {
      isSupported.value = false
      permissionState.value = 'denied'
    }
  }

  async function requestPermissionAndSubscribe(): Promise<boolean> {
    checkPermissionState()
    if (!isSupported.value) return false

    try {
      const permission = await Notification.requestPermission()
      permissionState.value = permission
      if (permission !== 'granted') return false

      const registration = await navigator.serviceWorker.register('/sw.js')
      await navigator.serviceWorker.ready

      const vapidPublicKey =
        import.meta.env.VITE_VAPID_PUBLIC_KEY || 'dummy_public_key_string_for_testing_purposes'
      const convertedVapidKey = urlBase64ToUint8Array(vapidPublicKey) as unknown as BufferSource

      let subscription = await registration.pushManager.getSubscription()
      if (!subscription) {
        subscription = await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: convertedVapidKey,
        })
      }

      const rawSub = subscription.toJSON()
      const p256dh = rawSub.keys?.p256dh || ''
      const auth = rawSub.keys?.auth || ''

      const res = await fetch('/api/v1/notifications/subscribe', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...getCsrfHeaders() },
        credentials: 'include',
        body: JSON.stringify({
          endpoint: subscription.endpoint,
          p256dh,
          auth,
        }),
      })

      if (!res.ok) {
        throw new Error(`Subscribe failed with status ${res.status}`)
      }

      isSubscribed.value = true
      return true
    } catch (e) {
      console.warn('Failed to subscribe to push notifications:', e)
      return false
    }
  }

  function handleVisibilityChange() {
    if (document.visibilityState === 'visible') {
      checkPermissionState()
    }
  }

  if (getCurrentInstance()) {
    onMounted(() => {
      checkPermissionState()
      if (typeof window !== 'undefined') {
        document.addEventListener('visibilitychange', handleVisibilityChange)
      }
    })

    onUnmounted(() => {
      if (typeof window !== 'undefined') {
        document.removeEventListener('visibilitychange', handleVisibilityChange)
      }
    })
  } else {
    checkPermissionState()
  }

  return {
    isSupported,
    permissionState,
    isSubscribed,
    checkPermissionState,
    requestPermissionAndSubscribe,
  }
}
