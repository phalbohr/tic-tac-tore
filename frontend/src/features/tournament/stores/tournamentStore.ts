import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  TournamentDto,
  CreateTournamentPayload,
  TournamentStatus,
  TournamentRegistrationDto,
  RegisterTournamentPayload,
  MyRegistrationStatusDto,
  RegistrationStatus,
  TournamentBracketDto,
  TournamentMatchDto,
  TournamentStandingDto,
} from '@/features/tournament/types/tournament'
import * as tournamentService from '@/features/tournament/services/tournamentService'
import * as registrationService from '@/features/tournament/services/tournamentRegistrationService'
import * as bracketService from '@/features/tournament/services/tournamentBracketService'

export const useTournamentStore = defineStore('tournament', () => {
  const tournaments = ref<TournamentDto[]>([])
  const currentTournament = ref<TournamentDto | null>(null)
  const registrations = ref<Record<string, TournamentRegistrationDto[]>>({})
  const myRegistrations = ref<Record<string, MyRegistrationStatusDto>>({})
  const pendingInvitations = ref<TournamentRegistrationDto[]>([])
  const brackets = ref<Record<string, TournamentBracketDto>>({})
  const matches = ref<Record<string, TournamentMatchDto[]>>({})
  const standings = ref<Record<string, TournamentStandingDto[]>>({})
  const archiveTournaments = ref<TournamentDto[]>([])
  const archivePage = ref<number>(0)
  const archiveTotalPages = ref<number>(0)
  const archiveTotalElements = ref<number>(0)
  const isArchiveLoading = ref<boolean>(false)
  const isLoading = ref<boolean>(false)
  const error = ref<string | null>(null)

  async function createTournament(payload: CreateTournamentPayload): Promise<TournamentDto> {
    isLoading.value = true
    error.value = null
    try {
      const item = await tournamentService.createTournament(payload)
      tournaments.value.unshift(item)
      return item
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to create tournament'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchTournaments(status?: TournamentStatus): Promise<TournamentDto[]> {
    isLoading.value = true
    error.value = null
    try {
      const items = await tournamentService.getTournaments(status)
      tournaments.value = items
      return items
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournaments'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchArchive(page: number = 0, size: number = 10): Promise<void> {
    isArchiveLoading.value = true
    error.value = null
    try {
      const res = await tournamentService.getTournamentsPaginated('COMPLETED', page, size)
      archiveTournaments.value = res.content
      archivePage.value = res.number
      archiveTotalPages.value = res.totalPages
      archiveTotalElements.value = res.totalElements
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournament archive'
      throw err
    } finally {
      isArchiveLoading.value = false
    }
  }

  async function fetchStandings(tournamentId: string): Promise<TournamentStandingDto[]> {
    isLoading.value = true
    error.value = null
    try {
      const items = await tournamentService.getTournamentStandings(tournamentId)
      standings.value[tournamentId] = items
      return items
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournament standings'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchTournamentById(id: string): Promise<TournamentDto> {
    isLoading.value = true
    error.value = null
    try {
      const item = await tournamentService.getTournamentById(id)
      currentTournament.value = item
      return item
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournament'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function register(
    tournamentId: string,
    payload: RegisterTournamentPayload,
  ): Promise<TournamentRegistrationDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await registrationService.registerForTournament(tournamentId, payload)
      if (!registrations.value[tournamentId]) {
        registrations.value[tournamentId] = []
      }
      registrations.value[tournamentId].push(result)
      myRegistrations.value[tournamentId] = {
        isRegistered: true,
        registration: result,
        isPendingInvite: result.status === 'PENDING_CONFIRMATION',
      }
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to register for tournament'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function acceptInvite(
    tournamentId: string,
    registrationId: string,
  ): Promise<TournamentRegistrationDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await registrationService.acceptInvitation(tournamentId, registrationId)
      if (registrations.value[tournamentId]) {
        const idx = registrations.value[tournamentId].findIndex((r) => r.id === registrationId)
        if (idx !== -1) {
          registrations.value[tournamentId][idx] = result
        }
      }
      pendingInvitations.value = pendingInvitations.value.filter((i) => i.id !== registrationId)
      myRegistrations.value[tournamentId] = {
        isRegistered: true,
        registration: result,
        isPendingInvite: false,
      }
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to accept invitation'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function declineInvite(
    tournamentId: string,
    registrationId: string,
  ): Promise<TournamentRegistrationDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await registrationService.declineInvitation(tournamentId, registrationId)
      if (registrations.value[tournamentId]) {
        registrations.value[tournamentId] = registrations.value[tournamentId].filter(
          (r) => r.id !== registrationId,
        )
      }
      pendingInvitations.value = pendingInvitations.value.filter((i) => i.id !== registrationId)
      myRegistrations.value[tournamentId] = {
        isRegistered: false,
        registration: result,
        isPendingInvite: false,
      }
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to decline invitation'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function cancelRegistration(tournamentId: string, registrationId: string): Promise<void> {
    isLoading.value = true
    error.value = null
    try {
      await registrationService.cancelRegistration(tournamentId, registrationId)
      if (registrations.value[tournamentId]) {
        registrations.value[tournamentId] = registrations.value[tournamentId].filter(
          (r) => r.id !== registrationId,
        )
      }
      myRegistrations.value[tournamentId] = {
        isRegistered: false,
        registration: null,
        isPendingInvite: false,
      }
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to cancel registration'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchRegistrations(
    tournamentId: string,
    status?: RegistrationStatus,
  ): Promise<TournamentRegistrationDto[]> {
    isLoading.value = true
    error.value = null
    try {
      const items = await registrationService.getTournamentRegistrations(tournamentId, status)
      registrations.value[tournamentId] = items
      return items
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch registrations'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchMyRegistration(tournamentId: string): Promise<MyRegistrationStatusDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await registrationService.getMyRegistration(tournamentId)
      myRegistrations.value[tournamentId] = result
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch my registration'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchPendingInvitations(): Promise<TournamentRegistrationDto[]> {
    isLoading.value = true
    error.value = null
    try {
      const items = await registrationService.getPendingInvitations()
      pendingInvitations.value = items
      return items
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch pending invitations'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchBracket(tournamentId: string): Promise<TournamentBracketDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await bracketService.getTournamentBracket(tournamentId)
      brackets.value[tournamentId] = result
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournament bracket'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchMatches(tournamentId: string, round?: number): Promise<TournamentMatchDto[]> {
    isLoading.value = true
    error.value = null
    try {
      const items = await bracketService.getTournamentMatches(tournamentId, round)
      matches.value[tournamentId] = items
      return items
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournament matches'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function startTournament(tournamentId: string): Promise<TournamentDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await bracketService.startTournament(tournamentId)
      const idx = tournaments.value.findIndex((t) => t.id === tournamentId)
      if (idx !== -1) {
        tournaments.value[idx] = result
      }
      if (currentTournament.value?.id === tournamentId) {
        currentTournament.value = result
      }
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to start tournament'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function startMatch(tournamentId: string, matchId: string): Promise<TournamentMatchDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await bracketService.startTournamentMatch(tournamentId, matchId)
      updateMatchInStore(tournamentId, result)
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to start match'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function cancelMatch(tournamentId: string, matchId: string): Promise<TournamentMatchDto> {
    isLoading.value = true
    error.value = null
    try {
      const result = await bracketService.cancelTournamentMatch(tournamentId, matchId)
      updateMatchInStore(tournamentId, result)
      return result
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to cancel match'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  function updateMatchInStore(tournamentId: string, updatedMatch: TournamentMatchDto): void {
    if (matches.value[tournamentId]) {
      const idx = matches.value[tournamentId].findIndex((m) => m.id === updatedMatch.id)
      if (idx !== -1) {
        matches.value[tournamentId][idx] = updatedMatch
      }
    }
    if (brackets.value[tournamentId]?.rounds) {
      for (const round of brackets.value[tournamentId].rounds) {
        const idx = round.matches.findIndex((m) => m.id === updatedMatch.id)
        if (idx !== -1) {
          round.matches[idx] = updatedMatch
        }
      }
    }
  }

  return {
    tournaments,
    currentTournament,
    registrations,
    myRegistrations,
    pendingInvitations,
    brackets,
    matches,
    standings,
    archiveTournaments,
    archivePage,
    archiveTotalPages,
    archiveTotalElements,
    isArchiveLoading,
    isLoading,
    error,
    createTournament,
    fetchTournaments,
    fetchArchive,
    fetchStandings,
    fetchTournamentById,
    register,
    acceptInvite,
    declineInvite,
    cancelRegistration,
    fetchRegistrations,
    fetchMyRegistration,
    fetchPendingInvitations,
    fetchBracket,
    fetchMatches,
    startTournament,
    startMatch,
    cancelMatch,
  }
})
