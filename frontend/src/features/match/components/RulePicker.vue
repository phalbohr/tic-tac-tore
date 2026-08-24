<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useRuleConfigStore } from '@/stores/useRuleConfigStore';
import { useMatchDraftStore } from '../stores/matchDraftStore';
import type { CreateRuleConfigRequest } from '@/services/ruleConfigService';
import RuleTemplateModal from './RuleTemplateModal.vue';

defineOptions({
  name: 'RulePicker',
});

const { t } = useI18n();
const authStore = useAuthStore();
const ruleStore = useRuleConfigStore();
const draftStore = useMatchDraftStore();

const selectedRuleId = ref<string | null>(
  ruleStore.selectedRuleId || authStore.profile?.defaultRuleConfigurationId || null
);
const isModalOpen = ref(false);
const modalError = ref('');

watch(
  () => authStore.profile?.defaultRuleConfigurationId,
  (newDef) => {
    if (newDef && !selectedRuleId.value) {
      const defaultRule = ruleStore.allRules.find((r) => r.id === newDef);
      if (defaultRule) {
        selectRule(defaultRule.id, defaultRule.name);
      }
    }
  }
);

onMounted(async () => {
  if (ruleStore.allRules.length === 0) {
    try {
      await ruleStore.fetchAllRules();
    } catch {
      // ignore
    }
  }
  if (!selectedRuleId.value && authStore.profile?.defaultRuleConfigurationId) {
    const defaultRule = ruleStore.allRules.find(
      (r) => r.id === authStore.profile?.defaultRuleConfigurationId
    );
    if (defaultRule) {
      selectRule(defaultRule.id, defaultRule.name);
    }
  }
});

const selectedRule = computed(() => {
  if (selectedRuleId.value) {
    const found = ruleStore.allRules.find((r) => r.id === selectedRuleId.value);
    if (found) return found;
  }
  return (
    ruleStore.allRules.find(
      (r) =>
        ruleStore.selectedRuleId === r.id ||
        draftStore.ruleSystem?.toUpperCase() === r.name?.toUpperCase() ||
        draftStore.ruleSystem === r.id
    ) || null
  );
});

function isSelected(rule: { id: string; name: string }) {
  return (
    selectedRuleId.value === rule.id ||
    ruleStore.selectedRuleId === rule.id ||
    draftStore.ruleSystem?.toUpperCase() === rule.name?.toUpperCase() ||
    draftStore.ruleSystem === rule.id
  );
}

function openModal() {
  modalError.value = '';
  isModalOpen.value = true;
}

function selectRule(ruleId: string, ruleName: string) {
  selectedRuleId.value = ruleId;
  ruleStore.selectRule(ruleId);
  draftStore.ruleSystem = ruleName;
  draftStore.loadRuleConfig();
}

async function handleSetAsDefault(ruleId: string) {
  try {
    await authStore.updateProfile({ defaultRuleConfigurationId: ruleId });
  } catch (error) {
    console.error('Failed to set default rule', error);
  }
}

async function handleSaveCustomRule(payload: CreateRuleConfigRequest) {
  modalError.value = '';
  try {
    const created = await ruleStore.createCustomRule(payload);
    isModalOpen.value = false;
    selectRule(created.id, created.name);
  } catch (err: unknown) {
    modalError.value = err instanceof Error ? err.message : t('common.error', 'An error occurred');
  }
}
</script>

<template>
  <div class="flex flex-col gap-2 w-full text-start" data-testid="rule-picker">
    <div class="flex justify-between items-center mb-1">
      <div class="flex items-center gap-2">
        <h3 class="text-on-surface font-headline font-bold text-sm">
          {{ t('rules.pickerTitle', 'Rule System') }}
        </h3>
        <button
          v-if="selectedRule && selectedRule.id !== authStore.profile?.defaultRuleConfigurationId"
          type="button"
          @click="handleSetAsDefault(selectedRule.id)"
          data-test="set-as-default-rule-btn"
          class="text-xs font-bold text-secondary hover:text-primary flex items-center gap-1 cursor-pointer transition-colors"
          :title="t('rules.setAsDefault', 'Set as default')"
        >
          <span class="material-symbols-outlined text-xs">push_pin</span>
          <span>{{ t('rules.setAsDefault', 'Set as default') }}</span>
        </button>
      </div>
      <button
        type="button"
        @click="openModal"
        data-testid="create-custom-rule-inline-btn"
        class="text-xs font-bold text-primary hover:opacity-80 flex items-center gap-1 cursor-pointer transition-opacity"
      >
        <span class="material-symbols-outlined text-sm">add_circle</span>
        <span>{{ t('rules.createCustom', '+ Custom Rule') }}</span>
      </button>
    </div>

    <!-- Rule Chips -->
    <div
      class="flex gap-2 overflow-x-auto pb-2 no-scrollbar select-none"
      data-testid="rule-system-chips"
    >
      <button
        v-for="rule in ruleStore.allRules"
        :key="rule.id"
        type="button"
        :data-rule-id="rule.id"
        @click="selectRule(rule.id, rule.name)"
        class="px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all flex items-center gap-1 cursor-pointer"
        :class="[
          isSelected(rule)
            ? 'bg-primary text-background shadow-md active'
            : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
        ]"
        :data-testid="`rule-chip-${rule.id}`"
      >
        <span>{{ rule.name }}</span>
        <span
          v-if="rule.id === authStore.profile?.defaultRuleConfigurationId"
          data-test="default-indicator"
          class="material-symbols-outlined text-xs text-yellow-400"
          :title="t('common.default', 'Default')"
        >
          push_pin
        </span>
      </button>
    </div>

    <!-- Rule Template Modal -->
    <RuleTemplateModal
      :is-open="isModalOpen"
      :error-message="modalError"
      @close="isModalOpen = false"
      @save="handleSaveCustomRule"
    />
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
