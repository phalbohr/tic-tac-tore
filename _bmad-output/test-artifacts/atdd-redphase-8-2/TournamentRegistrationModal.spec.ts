import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import TournamentRegistrationModal from '@/features/tournament/components/TournamentRegistrationModal.vue';
import type { TournamentDto } from '@/features/tournament/types/tournament';

describe('TournamentRegistrationModal.vue Component ATDD Specs — Story 8.2', () => {
    const mockTournament1v1: TournamentDto = {
        id: 'tourn-1v1',
        name: 'Solo Masters',
        format: 'CUP',
        mode: 'ONE_VS_ONE_PERSONAL',
        minParticipants: 4,
        maxParticipants: 16,
        registrationDeadline: '2026-09-10T12:00:00Z',
        roundCount: null,
        hasPlayoff: false,
        status: 'REGISTRATION_OPEN',
        creatorId: 'user-1',
        creatorNickname: 'Master',
        createdAt: '2026-09-01T10:00:00Z',
    };

    const mockTournament2v2: TournamentDto = {
        id: 'tourn-2v2',
        name: 'Duo Masters',
        format: 'CUP',
        mode: 'TWO_VS_TWO_FIXED_TEAMS',
        minParticipants: 4,
        maxParticipants: 16,
        registrationDeadline: '2026-09-10T12:00:00Z',
        roundCount: null,
        hasPlayoff: false,
        status: 'REGISTRATION_OPEN',
        creatorId: 'user-1',
        creatorNickname: 'Master',
        createdAt: '2026-09-01T10:00:00Z',
    };

    it('should render solo registration prompt when tournament mode is 1v1 (AC 1, AC 8)', () => {
        const wrapper = mount(TournamentRegistrationModal, {
            props: {
                isOpen: true,
                tournament: mockTournament1v1,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        expect(wrapper.find('[data-testid="tournament-registration-modal"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="partner-search-input"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="confirm-registration-btn"]').exists()).toBe(true);
    });

    it('should render partner selector when tournament mode is 2v2 fixed teams (AC 2, AC 8)', () => {
        const wrapper = mount(TournamentRegistrationModal, {
            props: {
                isOpen: true,
                tournament: mockTournament2v2,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        expect(wrapper.find('[data-testid="partner-search-input"]').exists()).toBe(true);
    });

    it('should emit register event without partner for 1v1 mode (AC 1)', async () => {
        const wrapper = mount(TournamentRegistrationModal, {
            props: {
                isOpen: true,
                tournament: mockTournament1v1,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        await wrapper.find('[data-testid="confirm-registration-btn"]').trigger('click');

        expect(wrapper.emitted('register')).toBeTruthy();
        expect(wrapper.emitted('register')![0][0]).toEqual({ partnerId: null });
    });

    it('should validate partner selection before submitting 2v2 fixed team registration (AC 6)', async () => {
        const wrapper = mount(TournamentRegistrationModal, {
            props: {
                isOpen: true,
                tournament: mockTournament2v2,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        await wrapper.find('[data-testid="confirm-registration-btn"]').trigger('click');

        expect(wrapper.emitted('register')).toBeFalsy();
        expect(wrapper.find('[data-testid="partner-required-error"]').exists()).toBe(true);
    });

    it('should emit close event when cancel or close button is clicked', async () => {
        const wrapper = mount(TournamentRegistrationModal, {
            props: {
                isOpen: true,
                tournament: mockTournament1v1,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        await wrapper.find('[data-testid="close-registration-modal-btn"]').trigger('click');

        expect(wrapper.emitted('close')).toBeTruthy();
    });
});
