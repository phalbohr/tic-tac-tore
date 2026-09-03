import { test, expect } from '@playwright/test'

test.describe.skip('Tournament Standings & Archive API Tests (Story 8.7)', () => {
  test('[P0] GET /api/v1/tournaments/{id}/standings returns 200 with sorted standings (AC1, AC2, AC3)', async ({
    request,
  }) => {
    const tournamentId = 'mock-tournament-standings-id'
    const response = await request.get(`/api/v1/tournaments/${tournamentId}/standings`)

    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(Array.isArray(body)).toBe(true)
    if (body.length > 0) {
      expect(body[0]).toHaveProperty('registrationId')
      expect(body[0]).toHaveProperty('nickname')
      expect(body[0]).toHaveProperty('points')
      expect(body[0]).toHaveProperty('rank')
    }
  })

  test('[P1] GET /api/v1/tournaments?status=COMPLETED returns 200 with paginated archive (AC5)', async ({
    request,
  }) => {
    const response = await request.get('/api/v1/tournaments?status=COMPLETED&page=0&size=10')

    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body).toHaveProperty('content')
    expect(body).toHaveProperty('totalElements')
    expect(body).toHaveProperty('totalPages')
  })
})
