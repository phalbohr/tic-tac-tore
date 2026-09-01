import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import CreateTournamentModal from '@/features/tournament/components/CreateTournamentModal.vue';

describe('CreateTournamentModal.vue Component ATDD Specifications — Story 8.1', () => {
    const mockRulePresets = [
        {
            id: '00000000-0000-0000-0000-000000000001',
            name: 'ITSF Standard Matchplay',
            goalLimit: 5,
            gameLimit: 3,
            winByTwo: true,
        },
    ];

    it('should render modal with all form controls and default selections (AC 1, AC 3)', () => {
        const wrapper = mount(CreateTournamentModal, {
            props: {
                isOpen: true,
                rulePresets: mockRulePresets,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        expect(wrapper.find('[data-testid="tournament-name-input"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="format-selector"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="mode-selector"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="rule-config-select"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="min-participants-input"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="max-participants-input"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="registration-deadline-input"]').exists()).toBe(true);
    });

    it('should show round count and playoff toggle when format is CHAMPIONSHIP (AC 1, AC 3)', async () => {
        const wrapper = mount(CreateTournamentModal, {
            props: {
                isOpen: true,
                rulePresets: mockRulePresets,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        // Default is CUP -> round count input not visible
        expect(wrapper.find('[data-testid="round-count-input"]').exists()).toBe(false);

        // Select CHAMPIONSHIP format
        const championshipOption = wrapper.find('[data-testid="format-option-championship"]');
        if (championshipOption.exists()) {
            await championshipOption.trigger('click');
        } else {
            await wrapper.find('[data-testid="format-selector"]').setValue('CHAMPIONSHIP');
        }

        expect(wrapper.find('[data-testid="round-count-input"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="playoff-toggle"]').exists()).toBe(true);
    });

    it('should enforce min 4 participants when 2v2 mode is selected (AC 4)', async () => {
        const wrapper = mount(CreateTournamentModal, {
            props: {
                isOpen: true,
                rulePresets: mockRulePresets,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        // Select 2v2 Fixed Teams
        const mode2v2 = wrapper.find('[data-testid="mode-option-2v2-fixed"]');
        if (mode2v2.exists()) {
            await mode2v2.trigger('click');
        } else {
            await wrapper.find('[data-testid="mode-selector"]').setValue('TWO_VS_TWO_FIXED_TEAMS');
        }

        // Set min participants to 2
        await wrapper.find('[data-testid="min-participants-input"]').setValue(2);
        await wrapper.find('[data-testid="create-tournament-submit-button"]').trigger('click');

        expect(wrapper.emitted('create')).toBeFalsy();
        expect(wrapper.find('[data-testid="participants-validation-error"]').exists()).toBe(true);
    });

    it('should emit create event with form payload when valid data submitted (AC 2, AC 6)', async () => {
        const wrapper = mount(CreateTournamentModal, {
            props: {
                isOpen: true,
                rulePresets: mockRulePresets,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        const futureDate = new Date(Date.now() + 86400000 * 5);
        const pad = (n: number) => String(n).padStart(2, '0');
        const futureDateStr = `${futureDate.getFullYear()}-${pad(futureDate.getMonth() + 1)}-${pad(futureDate.getDate())}T${pad(futureDate.getHours())}:${pad(futureDate.getMinutes())}`;

        await wrapper.find('[data-testid="tournament-name-input"]').setValue('Championship 2026');
        await wrapper.find('[data-testid="min-participants-input"]').setValue(4);
        await wrapper.find('[data-testid="max-participants-input"]').setValue(16);
        await wrapper.find('[data-testid="registration-deadline-input"]').setValue(futureDateStr);
        await wrapper.find('[data-testid="create-tournament-submit-button"]').trigger('click');

        expect(wrapper.emitted('create')).toBeTruthy();
        expect(wrapper.emitted('create')![0][0]).toEqual(expect.objectContaining({
            name: 'Championship 2026',
            format: 'CUP',
            mode: 'ONE_VS_ONE_PERSONAL',
            minParticipants: 4,
            maxParticipants: 16,
            ruleConfigurationId: mockRulePresets[0].id,
            registrationDeadline: expect.any(String),
        }));
    });

    it('should emit close event when cancel button or backdrop is clicked', async () => {
        const wrapper = mount(CreateTournamentModal, {
            props: {
                isOpen: true,
                rulePresets: mockRulePresets,
            },
            global: {
                mocks: {
                    $t: (msg: string) => msg,
                },
            },
        });

        await wrapper.find('[data-testid="create-tournament-cancel-button"]').trigger('click');

        expect(wrapper.emitted('close')).toBeTruthy();
    });
});
