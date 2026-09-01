import { defineStore } from 'pinia';
import { ref } from 'vue';
import type {
  TournamentDto,
  CreateTournamentPayload,
  TournamentStatus,
  TournamentRegistrationDto,
  RegisterTournamentPayload,
  MyRegistrationStatusDto,
  RegistrationStatus,
} from '@/features/tournament/types/tournament';
import * as tournamentService from '@/features/tournament/services/tournamentService';
import * as registrationService from '@/features/tournament/services/tournamentRegistrationService';

export const useTournamentStore = defineStore('tournament', () => {
  const tournaments = ref<TournamentDto[]>([]);
  const currentTournament = ref<TournamentDto | null>(null);
  const registrations = ref<Record<string, TournamentRegistrationDto[]>>({});
  const myRegistrations = ref<Record<string, MyRegistrationStatusDto>>({});
  const pendingInvitations = ref<TournamentRegistrationDto[]>([]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  async function createTournament(payload: CreateTournamentPayload): Promise<TournamentDto> {
    isLoading.value = true;
    error.value = null;
    try {
      const item = await tournamentService.createTournament(payload);
      tournaments.value.unshift(item);
      return item;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to create tournament';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchTournaments(status?: TournamentStatus): Promise<TournamentDto[]> {
    isLoading.value = true;
    error.value = null;
    try {
      const items = await tournamentService.getTournaments(status);
      tournaments.value = items;
      return items;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournaments';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchTournamentById(id: string): Promise<TournamentDto> {
    isLoading.value = true;
    error.value = null;
    try {
      const item = await tournamentService.getTournamentById(id);
      currentTournament.value = item;
      return item;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch tournament';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function register(
    tournamentId: string,
    payload: RegisterTournamentPayload
  ): Promise<TournamentRegistrationDto> {
    isLoading.value = true;
    error.value = null;
    try {
      const result = await registrationService.registerForTournament(tournamentId, payload);
      if (!registrations.value[tournamentId]) {
        registrations.value[tournamentId] = [];
      }
      registrations.value[tournamentId].push(result);
      myRegistrations.value[tournamentId] = {
        isRegistered: true,
        registration: result,
        isPendingInvite: result.status === 'PENDING_CONFIRMATION',
      };
      return result;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to register for tournament';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function acceptInvite(
    tournamentId: string,
    registrationId: string
  ): Promise<TournamentRegistrationDto> {
    isLoading.value = true;
    error.value = null;
    try {
      const result = await registrationService.acceptInvitation(tournamentId, registrationId);
      if (registrations.value[tournamentId]) {
        const idx = registrations.value[tournamentId].findIndex(r => r.id === registrationId);
        if (idx !== -1) {
          registrations.value[tournamentId][idx] = result;
        }
      }
      pendingInvitations.value = pendingInvitations.value.filter(i => i.id !== registrationId);
      myRegistrations.value[tournamentId] = {
        isRegistered: true,
        registration: result,
        isPendingInvite: false,
      };
      return result;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to accept invitation';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function declineInvite(
    tournamentId: string,
    registrationId: string
  ): Promise<TournamentRegistrationDto> {
    isLoading.value = true;
    error.value = null;
    try {
      const result = await registrationService.declineInvitation(tournamentId, registrationId);
      if (registrations.value[tournamentId]) {
        registrations.value[tournamentId] = registrations.value[tournamentId].filter(
          r => r.id !== registrationId
        );
      }
      pendingInvitations.value = pendingInvitations.value.filter(i => i.id !== registrationId);
      myRegistrations.value[tournamentId] = {
        isRegistered: false,
        registration: result,
        isPendingInvite: false,
      };
      return result;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to decline invitation';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function cancelRegistration(
    tournamentId: string,
    registrationId: string
  ): Promise<void> {
    isLoading.value = true;
    error.value = null;
    try {
      await registrationService.cancelRegistration(tournamentId, registrationId);
      if (registrations.value[tournamentId]) {
        registrations.value[tournamentId] = registrations.value[tournamentId].filter(
          r => r.id !== registrationId
        );
      }
      myRegistrations.value[tournamentId] = {
        isRegistered: false,
        registration: null,
        isPendingInvite: false,
      };
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to cancel registration';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchRegistrations(
    tournamentId: string,
    status?: RegistrationStatus
  ): Promise<TournamentRegistrationDto[]> {
    isLoading.value = true;
    error.value = null;
    try {
      const items = await registrationService.getTournamentRegistrations(tournamentId, status);
      registrations.value[tournamentId] = items;
      return items;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch registrations';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchMyRegistration(
    tournamentId: string
  ): Promise<MyRegistrationStatusDto> {
    isLoading.value = true;
    error.value = null;
    try {
      const result = await registrationService.getMyRegistration(tournamentId);
      myRegistrations.value[tournamentId] = result;
      return result;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch my registration';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchPendingInvitations(): Promise<TournamentRegistrationDto[]> {
    isLoading.value = true;
    error.value = null;
    try {
      const items = await registrationService.getPendingInvitations();
      pendingInvitations.value = items;
      return items;
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch pending invitations';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    tournaments,
    currentTournament,
    registrations,
    myRegistrations,
    pendingInvitations,
    isLoading,
    error,
    createTournament,
    fetchTournaments,
    fetchTournamentById,
    register,
    acceptInvite,
    declineInvite,
    cancelRegistration,
    fetchRegistrations,
    fetchMyRegistration,
    fetchPendingInvitations,
  };
});
