import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  type RuleConfig,
  type CreateRuleConfigRequest,
  getRuleConfigurations,
  createRuleConfiguration,
  deleteRuleConfiguration,
} from '../services/ruleConfigService';

export type { RuleConfig, CreateRuleConfigRequest };

export const useRuleConfigStore = defineStore('ruleConfig', () => {
  const presets = ref<RuleConfig[]>([]);
  const customRules = ref<RuleConfig[]>([]);
  const selectedRuleId = ref<string | null>(null);
  const loading = ref<boolean>(false);
  const error = ref<string | null>(null);

  const allRules = computed<RuleConfig[]>(() => [
    ...presets.value,
    ...customRules.value,
  ]);

  const selectedRule = computed<RuleConfig | null>(() => {
    if (!selectedRuleId.value) return null;
    return allRules.value.find((r) => r.id === selectedRuleId.value) || null;
  });

  const getRuleById = computed(() => (id: string) => {
    return allRules.value.find((r) => r.id === id) || null;
  });

  async function fetchAllRules() {
    loading.value = true;
    error.value = null;
    try {
      const data = await getRuleConfigurations();
      presets.value = data.filter((r) => r.type === 'PRESET');
      customRules.value = data.filter((r) => r.type === 'CUSTOM');
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch rules';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchPresets() {
    loading.value = true;
    error.value = null;
    try {
      const data = await getRuleConfigurations('PRESET');
      presets.value = data;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch presets';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createCustomRule(ruleData: CreateRuleConfigRequest): Promise<RuleConfig> {
    loading.value = true;
    error.value = null;
    try {
      const created = await createRuleConfiguration(ruleData);
      customRules.value.push(created);
      return created;
    } catch (err: any) {
      error.value = err.message || 'Failed to create custom rule';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteCustomRule(id: string): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      await deleteRuleConfiguration(id);
      customRules.value = customRules.value.filter((r) => r.id !== id);
      if (selectedRuleId.value === id) {
        selectedRuleId.value = null;
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to delete custom rule';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function selectRule(id: string | null) {
    selectedRuleId.value = id;
  }

  return {
    presets,
    customRules,
    selectedRuleId,
    loading,
    error,
    allRules,
    selectedRule,
    getRuleById,
    fetchAllRules,
    fetchPresets,
    createCustomRule,
    deleteCustomRule,
    selectRule,
  };
});
