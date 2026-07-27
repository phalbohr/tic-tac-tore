/* eslint-env serviceworker */

self.addEventListener('push', (event) => {
  if (!event.data) return

  let payload = {}
  try {
    payload = event.data.json()
  } catch (e) {
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

  event.waitUntil(self.registration.showNotification(title, options))
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()

  const targetUrl = (event.notification.data && event.notification.data.url) || '/matches?tab=pending'

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if (client.url.includes(targetUrl) && 'focus' in client) {
          return client.focus()
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(targetUrl)
      }
    }),
  )
})
