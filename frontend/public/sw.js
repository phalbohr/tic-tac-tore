/* eslint-env serviceworker */

self.addEventListener('push', (event) => {
  if (!event.data) return

  let payload = {}
  try {
    payload = event.data.json()
  } catch {
    payload = { summary: event.data.text() }
  }

  const type = payload.type || ''
  const matchId = payload.matchId || ''
  const poolId = payload.poolId || ''
  const creatorName = payload.creatorName || 'A player'
  const isDuplicate = payload.isDuplicateWarning === true

  let title = ''
  let body = payload.summary || ''
  let url = payload.url || '/'
  let actions = []

  if (type === 'POOL_CREATED') {
    title = 'New Matchmaking Pool'
    body = payload.summary || 'A new matchmaking pool is looking for players'
    url = payload.url || '/'
    actions = [{ action: 'open', title: 'Open' }]
  } else if (type === 'POOL_FILLED') {
    title = 'Pool Filled!'
    body = payload.summary || 'Your pool is full — head to the table!'
    url = payload.url || '/'
    actions = [{ action: 'open', title: 'Open' }]
  } else {
    title = isDuplicate
      ? `⚠️ Duplicate Warning: Match from ${creatorName}`
      : `Match Verification Request: ${creatorName}`
    body = payload.summary || 'Tap to review and verify this match outcome.'
    url = matchId ? `/match/${matchId}/review` : '/matches?tab=pending'
    actions = [{ action: 'review', title: 'Review Match' }]
  }

  const options = {
    body,
    icon: '/favicon.ico',
    badge: '/favicon.ico',
    data: {
      matchId,
      poolId,
      url,
    },
    actions,
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
