/* eslint-env serviceworker */

self.addEventListener('push', (event) => {
  if (!event.data) return

  let payload = {}
  try {
    payload = event.data.json()
  } catch {
    payload = { summary: event.data.text() }
  }

  const matchId = payload.matchId || ''
  const creatorName = payload.creatorName || 'A player'
  const isDuplicate = payload.isDuplicateWarning === true

  const title = isDuplicate
    ? `⚠️ Duplicate Warning: Match from ${creatorName}`
    : `Match Verification Request: ${creatorName}`

  const options = {
    body: payload.summary || 'Tap to review and verify this match outcome.',
    icon: '/favicon.ico',
    badge: '/favicon.ico',
    data: {
      matchId,
      url: matchId ? `/match/${matchId}/review` : '/matches?tab=pending',
    },
    actions: [
      { action: 'review', title: 'Review Match' },
    ],
  }

  event.waitUntil(
    self.registration.showNotification(title, options).catch((e) => {
      console.error('Failed to show push notification:', e)
    }),
  )
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()

  const targetUrl = (event.notification.data && event.notification.data.url) || '/matches?tab=pending'

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if ('focus' in client) {
          if ('navigate' in client) {
            client.navigate(targetUrl)
          }
          return client.focus()
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(targetUrl)
      }
    }),
  )
})
