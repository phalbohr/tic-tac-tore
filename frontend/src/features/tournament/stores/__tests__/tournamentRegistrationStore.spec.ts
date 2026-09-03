import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore'
import * as registrationService from '@/features/tournament/services/tournamentRegistrationService'
import type {
  TournamentRegistrationDto,
  RegisterTournamentPayload,
} from '@/features/tournament/types/tournament'

vi.mock('@/features/tournament/services/tournamentRegistrationService')

describe('Tournament Registration Store Tests — Story 8.2', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should initialize with default empty registration states', () => {
    const store = useTournamentStore()

    expect(store.registrations).toEqual({})
    expect(store.myRegistrations).toEqual({})
    expect(store.pendingInvitations).toEqual([])
    expect(store.isLoading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('should register solo successfully and update registrations and myRegistrations state (AC 1, AC 6)', async () => {
    const store = useTournamentStore()
    const tournamentId = 'tourn-uuid-1'
    const payload: RegisterTournamentPayload = { partnerId: null }
    const mockRegistration: TournamentRegistrationDto = {
      id: 'reg-uuid-1',
      tournamentId,
      tournamentName: 'Autumn Cup 2026',
      playerId: 'user-uuid-1',
      playerNickname: 'Striker',
      playerAvatarUrl: null,
      partnerId: null,
      partnerNickname: null,
      partnerAvatarUrl: null,
      status: 'CONFIRMED',
      createdAt: '2026-09-01T12:00:00Z',
    }

    vi.mocked(registrationService.registerForTournament).mockResolvedValue(mockRegistration)

    const result = await store.register(tournamentId, payload)

    expect(registrationService.registerForTournament).toHaveBeenCalledWith(tournamentId, payload)
    expect(result).toEqual(mockRegistration)
    expect(store.registrations[tournamentId]).toContainEqual(mockRegistration)
    expect(store.myRegistrations[tournamentId]).toEqual({
      isRegistered: true,
      registration: mockRegistration,
      isPendingInvite: false,
    })
  })

  it('should register with partner and set pending confirmation state (AC 2)', async () => {
    const store = useTournamentStore()
    const tournamentId = 'tourn-uuid-2'
    const partnerId = 'user-uuid-2'
    const payload: RegisterTournamentPayload = { partnerId }
    const mockRegistration: TournamentRegistrationDto = {
      id: 'reg-uuid-2',
      tournamentId,
      tournamentName: 'Winter 2v2',
      playerId: 'user-uuid-1',
      playerNickname: 'Striker',
      playerAvatarUrl: null,
      partnerId,
      partnerNickname: 'Defender',
      partnerAvatarUrl: null,
      status: 'PENDING_CONFIRMATION',
      createdAt: '2026-09-01T12:00:00Z',
    }

    vi.mocked(registrationService.registerForTournament).mockResolvedValue(mockRegistration)

    const result = await store.register(tournamentId, payload)

    expect(result.status).toBe('PENDING_CONFIRMATION')
    expect(store.myRegistrations[tournamentId]?.isPendingInvite).toBe(true)
  })

  it('should accept invitation and update registration status to CONFIRMED (AC 3)', async () => {
    const store = useTournamentStore()
    const tournamentId = 'tourn-uuid-2'
    const registrationId = 'reg-uuid-2'
    const confirmedRegistration: TournamentRegistrationDto = {
      id: registrationId,
      tournamentId,
      tournamentName: 'Winter 2v2',
      playerId: 'user-uuid-1',
      playerNickname: 'Striker',
      partnerId: 'user-uuid-2',
      partnerNickname: 'Defender',
      status: 'CONFIRMED',
      createdAt: '2026-09-01T12:00:00Z',
    }

    vi.mocked(registrationService.acceptInvitation).mockResolvedValue(confirmedRegistration)

    const result = await store.acceptInvite(tournamentId, registrationId)

    expect(registrationService.acceptInvitation).toHaveBeenCalledWith(tournamentId, registrationId)
    expect(result.status).toBe('CONFIRMED')
    expect(store.pendingInvitations.filter((i) => i.id === registrationId)).toHaveLength(0)
  })

  it('should decline invitation and remove from pendingInvitations (AC 4)', async () => {
    const store = useTournamentStore()
    const tournamentId = 'tourn-uuid-2'
    const registrationId = 'reg-uuid-2'
    const declinedRegistration: TournamentRegistrationDto = {
      id: registrationId,
      tournamentId,
      tournamentName: 'Winter 2v2',
      playerId: 'user-uuid-1',
      playerNickname: 'Striker',
      partnerId: 'user-uuid-2',
      partnerNickname: 'Defender',
      status: 'DECLINED',
      createdAt: '2026-09-01T12:00:00Z',
    }

    vi.mocked(registrationService.declineInvitation).mockResolvedValue(declinedRegistration)

    await store.declineInvite(tournamentId, registrationId)

    expect(registrationService.declineInvitation).toHaveBeenCalledWith(tournamentId, registrationId)
    expect(store.pendingInvitations.filter((i) => i.id === registrationId)).toHaveLength(0)
  })

  it('should cancel registration and clear myRegistrations state (AC 5)', async () => {
    const store = useTournamentStore()
    const tournamentId = 'tourn-uuid-1'
    const registrationId = 'reg-uuid-1'

    vi.mocked(registrationService.cancelRegistration).mockResolvedValue(undefined)

    await store.cancelRegistration(tournamentId, registrationId)

    expect(registrationService.cancelRegistration).toHaveBeenCalledWith(
      tournamentId,
      registrationId,
    )
    expect(store.myRegistrations[tournamentId]?.isRegistered).toBe(false)
  })

  it('should fetch pending invitations for current user (AC 7)', async () => {
    const store = useTournamentStore()
    const mockInvites: TournamentRegistrationDto[] = [
      {
        id: 'reg-uuid-9',
        tournamentId: 'tourn-uuid-9',
        tournamentName: 'Spring League',
        playerId: 'user-uuid-3',
        playerNickname: 'Captain',
        partnerId: 'user-uuid-current',
        partnerNickname: 'Me',
        status: 'PENDING_CONFIRMATION',
        createdAt: '2026-09-01T12:00:00Z',
      },
    ]

    vi.mocked(registrationService.getPendingInvitations).mockResolvedValue(mockInvites)

    await store.fetchPendingInvitations()

    expect(registrationService.getPendingInvitations).toHaveBeenCalled()
    expect(store.pendingInvitations).toEqual(mockInvites)
  })
})
