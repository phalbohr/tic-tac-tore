import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useRuleConfigStore } from '@/stores/useRuleConfigStore';

import type { RuleConfig } from '@/services/ruleConfigService';

describe('useRuleConfigStore ATDD Specifications', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.restoreAllMocks();
    });

    const mockPreset: RuleConfig = {
        id: '00000000-0000-0000-0000-000000000001',
        name: 'ITSF Standard Matchplay',
        type: 'PRESET',
        createdBy: '00000000-0000-0000-0000-000000000000',
        goalLimit: 5,
        gameLimit: 3,
        winByTwo: true,
        absoluteScoreCap: 8,
        timeoutsPerGame: 2,
        timeoutDurationSeconds: 30,
        possessionLimit5BarSeconds: 10,
        possessionLimitOtherSeconds: 15,
        sideSwapRule: 'BETWEEN_GAMES',
        restartRule: 'CONCEDING_TEAM',
        spinningAllowed: false,
        aerialsAllowed: false,
        positionSwapRule: 'BETWEEN_GAMES',
        pointDistribution: 'WIN_LOSS_3_0',
    };

    const mockCustomRule: RuleConfig = {
        id: '11111111-1111-1111-1111-111111111111',
        name: 'Office Fast 7',
        type: 'CUSTOM',
        createdBy: '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a',
        goalLimit: 7,
        gameLimit: 1,
        winByTwo: false,
        absoluteScoreCap: null,
        timeoutsPerGame: 1,
        timeoutDurationSeconds: 20,
        possessionLimit5BarSeconds: 10,
        possessionLimitOtherSeconds: 15,
        sideSwapRule: 'NONE',
        restartRule: 'CONCEDING_TEAM',
        spinningAllowed: false,
        aerialsAllowed: false,
        positionSwapRule: 'FREE',
        pointDistribution: 'WIN_LOSS_2_0',
    };

    it('should fetch all available rule configurations (presets + custom)', async () => {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => [mockPreset, mockCustomRule],
        } as Response);

        const store = useRuleConfigStore();
        await store.fetchAllRules();

        expect(store.presets).toHaveLength(1);
        expect(store.presets[0]?.name).toBe('ITSF Standard Matchplay');
        expect(store.customRules).toHaveLength(1);
        expect(store.customRules[0]?.name).toBe('Office Fast 7');
        expect(store.allRules).toHaveLength(2);
    });

    it('should fetch only presets when requested', async () => {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => [mockPreset],
        } as Response);

        const store = useRuleConfigStore();
        await store.fetchPresets();

        expect(store.presets).toHaveLength(1);
        expect(store.presets[0]?.type).toBe('PRESET');
    });

    it('should create a custom rule configuration and append to customRules', async () => {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => mockCustomRule,
        } as Response);

        const store = useRuleConfigStore();
        const created = await store.createCustomRule({
            name: 'Office Fast 7',
            goalLimit: 7,
            gameLimit: 1,
            winByTwo: false,
            absoluteScoreCap: null,
            timeoutsPerGame: 1,
            timeoutDurationSeconds: 20,
            possessionLimit5BarSeconds: 10,
            possessionLimitOtherSeconds: 15,
            sideSwapRule: 'NONE',
            restartRule: 'CONCEDING_TEAM',
            spinningAllowed: false,
            aerialsAllowed: false,
            positionSwapRule: 'FREE',
            pointDistribution: 'WIN_LOSS_2_0',
        });

        expect(created.id).toBe(mockCustomRule.id);
        expect(store.customRules).toContainEqual(mockCustomRule);
    });

    it('should delete a custom rule configuration and remove from customRules', async () => {
        const store = useRuleConfigStore();
        store.customRules = [mockCustomRule];

        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true,
            status: 204,
        } as Response);

        await store.deleteCustomRule(mockCustomRule.id);

        expect(store.customRules).toHaveLength(0);
    });

    it('should select a rule and expose it through selectedRule getter', () => {
        const store = useRuleConfigStore();
        store.presets = [mockPreset];
        store.customRules = [mockCustomRule];

        store.selectRule(mockCustomRule.id);

        expect(store.selectedRuleId).toBe(mockCustomRule.id);
        expect(store.selectedRule).toEqual(mockCustomRule);
    });

    it('should handle API errors gracefully during rule creation', async () => {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: false,
            status: 400,
            json: async () => ({ message: 'Custom rule quota exceeded' }),
        } as Response);

        const store = useRuleConfigStore();

        await expect(store.createCustomRule({
            name: 'Over Quota Rule',
            goalLimit: 5,
            gameLimit: 3,
            winByTwo: false,
            absoluteScoreCap: null,
            timeoutsPerGame: 2,
            timeoutDurationSeconds: 30,
            possessionLimit5BarSeconds: 10,
            possessionLimitOtherSeconds: 15,
            sideSwapRule: 'BETWEEN_GAMES',
            restartRule: 'CONCEDING_TEAM',
            spinningAllowed: false,
            aerialsAllowed: false,
            positionSwapRule: 'BETWEEN_GAMES',
            pointDistribution: 'WIN_LOSS_3_0',
        })).rejects.toThrow();
    });
});
