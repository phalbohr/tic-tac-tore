import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getCsrfHeaders } from '../utils/cookieUtils';

export interface RuleConfig {
    id?: string;
    name: string;
    goalLimit: number;
    gameLimit: number;
    winByTwo: boolean;
    type?: string;
}

export const useRuleConfigStore = defineStore('ruleConfig', () => {
    const presets = ref<RuleConfig[]>([]);

    async function fetchPresets() {
        const response = await fetch('/api/v1/rule-configurations?type=PRESET');
        if (response.ok) {
            presets.value = await response.json();
        }
    }

    async function createCustomRule(ruleData: Omit<RuleConfig, 'id' | 'type'>) {
        const response = await fetch('/api/v1/rule-configurations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getCsrfHeaders(),
            },
            body: JSON.stringify(ruleData),
        });

        if (response.ok) {
            return await response.json();
        } else {
            throw new Error('Failed to create custom rule');
        }
    }

    return { presets, fetchPresets, createCustomRule };
});
