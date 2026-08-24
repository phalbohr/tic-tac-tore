<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRuleConfigStore } from '@/stores/useRuleConfigStore';
import { useMatchDraftStore } from '../stores/matchDraftStore';
import type { CreateRuleConfigRequest } from '@/services/ruleConfigService';
import RuleTemplateModal from './RuleTemplateModal.vue';

defineOptions({
  name: 'RulePicker',
});

const { t } = useI18n();
const ruleStore = useRuleConfigStore();
const draftStore = useMatchDraftStore();

const isModalOpen = ref(false);
const modalError = ref('');

onMounted(async () => {
  if (ruleStore.allRules.length === 0) {
    try {
      await ruleStore.fetchAllRules();
    } catch {
      // ignore
    }
  }
});

function openModal() {
  modalError.value = '';
  isModalOpen.value = true;
}

function selectRule(ruleId: string, ruleName: string) {
  ruleStore.selectRule(ruleId);
  draftStore.ruleSystem = ruleName;
  draftStore.loadRuleConfig();
}

async function handleSaveCustomRule(payload: CreateRuleConfigRequest) {
  modalError.value = '';
  try {
    const created = await ruleStore.createCustomRule(payload);
    isModalOpen.value = false;
    selectRule(created.id, created.name);
  } catch (err: any) {
    modalError.value = err.message || t('common.error', 'An error occurred');
  }
}
</script>

<template>
  <div class="flex flex-col gap-2 w-full text-start" data-testid="rule-picker">
    <div class="flex justify-between items-center mb-1">
      <h3 class="text-on-surface font-headline font-bold text-sm">
        {{ t('rules.pickerTitle', 'Rule System') }}
      </h3>
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
        @click="selectRule(rule.id, rule.name)"
        class="px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all flex items-center gap-1 cursor-pointer"
        :class="
          (ruleStore.selectedRuleId === rule.id || draftStore.ruleSystem?.toUpperCase() === rule.name?.toUpperCase() || draftStore.ruleSystem === rule.id)
            ? 'bg-primary text-background shadow-md'
            : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
        "
        :data-testid="`rule-chip-${rule.id}`"
      >
        <span>{{ rule.name }}</span>
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
