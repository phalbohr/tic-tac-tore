<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRuleConfigStore } from '@/stores/useRuleConfigStore';
import type { RuleConfig, CreateRuleConfigRequest } from '@/services/ruleConfigService';
import RuleTemplateModal from '@/features/match/components/RuleTemplateModal.vue';
import BaseButton from '@/core/components/BaseButton.vue';

defineOptions({
  name: 'RuleTemplateSection',
});

const { t } = useI18n();
const ruleStore = useRuleConfigStore();

const isModalOpen = ref(false);
const editingTemplate = ref<RuleConfig | null>(null);
const error = ref('');

const showDeleteConfirm = ref(false);
const deletingRuleId = ref<string | null>(null);

onMounted(async () => {
  if (ruleStore.presets.length === 0 && ruleStore.customRules.length === 0) {
    try {
      await ruleStore.fetchAllRules();
    } catch {
      // ignore fetch error if unauthenticated
    }
  }
});

function openCreateModal() {
  editingTemplate.value = null;
  error.value = '';
  isModalOpen.value = true;
}

function openEditAsNewModal(template: RuleConfig) {
  editingTemplate.value = template;
  error.value = '';
  isModalOpen.value = true;
}

async function handleSaveTemplate(payload: CreateRuleConfigRequest) {
  error.value = '';
  try {
    await ruleStore.createCustomRule(payload);
    isModalOpen.value = false;
  } catch (err: any) {
    error.value = err.message || t('common.error', 'An error occurred');
  }
}

function promptDelete(id: string) {
  deletingRuleId.value = id;
  showDeleteConfirm.value = true;
}

async function confirmDelete() {
  if (!deletingRuleId.value) return;
  error.value = '';
  try {
    await ruleStore.deleteCustomRule(deletingRuleId.value);
    showDeleteConfirm.value = false;
    deletingRuleId.value = null;
  } catch (err: any) {
    error.value = err.message || t('common.error', 'An error occurred');
  }
}
</script>

<template>
  <section class="space-y-3">
    <div class="flex justify-between items-center px-1">
      <h2 class="font-headline text-xs font-bold uppercase tracking-widest text-primary/80">
        {{ t('rules.sectionTitle', 'Rule Templates') }}
      </h2>
      <button
        type="button"
        @click="openCreateModal"
        data-testid="create-rule-template-button"
        class="text-xs font-bold text-primary hover:opacity-80 flex items-center gap-1 cursor-pointer transition-opacity"
      >
        <span class="material-symbols-outlined text-sm">add_circle</span>
        <span>{{ t('rules.createButton', 'Create Template') }}</span>
      </button>
    </div>

    <div v-if="error" class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold" data-testid="rule-section-error">
      {{ error }}
    </div>

    <div
      class="rule-template-list space-y-2"
      data-testid="rule-template-list"
    >
      <div
        v-if="ruleStore.loading && ruleStore.allRules.length === 0"
        class="p-4 rounded-xl bg-surface-container-low text-center text-xs text-on-surface-variant flex items-center justify-center gap-2"
      >
        <span class="material-symbols-outlined animate-spin text-sm">sync</span>
        <span>{{ t('common.loading', 'Loading...') }}</span>
      </div>

      <div
        v-else-if="ruleStore.allRules.length === 0"
        class="p-4 rounded-xl bg-surface-container-low text-center text-xs text-on-surface-variant"
      >
        {{ t('rules.noTemplates', 'No rule templates available.') }}
      </div>

      <div
        v-for="rule in ruleStore.allRules"
        :key="rule.id"
        :data-testid="`rule-item-${rule.id}`"
        class="flex items-center justify-between p-3.5 rounded-xl bg-surface-container-low hover:bg-surface-container-highest/60 transition-colors"
      >
        <div class="space-y-1 min-w-0 flex-1 mr-2 text-start">
          <div class="flex items-center gap-2">
            <span
              class="text-[10px] font-bold px-2 py-0.5 rounded-md uppercase tracking-wider"
              :class="rule.type === 'PRESET' ? 'bg-primary/20 text-primary' : 'bg-secondary/20 text-secondary'"
            >
              {{ rule.type }}
            </span>
            <span class="font-headline font-bold text-sm text-on-surface truncate">{{ rule.name }}</span>
          </div>
          <div class="text-[11px] text-on-surface-variant flex flex-wrap gap-x-2 gap-y-0.5">
            <span>Best of {{ rule.gameLimit }}</span>
            <span>•</span>
            <span>{{ rule.goalLimit }} goals</span>
            <template v-if="rule.winByTwo">
              <span>•</span>
              <span>Win by 2</span>
            </template>
          </div>
        </div>

        <div class="flex items-center gap-1 shrink-0">
          <button
            type="button"
            @click="openEditAsNewModal(rule)"
            :data-testid="`edit-as-new-rule-${rule.id}`"
            class="px-2 py-1 rounded-lg text-xs font-semibold text-on-surface-variant hover:text-primary hover:bg-surface-container-highest transition-colors cursor-pointer flex items-center gap-1"
            :title="t('rules.editAsNew', 'Edit as New')"
            :aria-label="`Edit as New ${rule.name}`"
          >
            <span class="material-symbols-outlined text-sm">content_copy</span>
            <span>Edit as New</span>
          </button>
          <button
            v-if="rule.type === 'CUSTOM'"
            type="button"
            @click="promptDelete(rule.id)"
            :data-testid="`delete-rule-${rule.id}`"
            class="p-1.5 rounded-lg text-on-surface-variant hover:text-error hover:bg-surface-container-highest transition-colors cursor-pointer"
            :title="t('common.delete', 'Delete')"
            :aria-label="`Delete ${rule.name}`"
          >
            <span class="material-symbols-outlined text-sm">delete</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Rule Template Modal -->
    <RuleTemplateModal
      :is-open="isModalOpen"
      :initial-template="editingTemplate"
      @close="isModalOpen = false"
      @save="handleSaveTemplate"
    />

    <!-- Delete Confirmation Modal -->
    <div
      v-if="showDeleteConfirm"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
      data-testid="delete-rule-confirm-modal"
    >
      <div class="bg-surface-container-low rounded-2xl p-5 max-w-xs w-full shadow-2xl space-y-4 text-center">
        <h3 class="font-bold text-base text-on-surface">Delete Rule Template?</h3>
        <p class="text-xs text-on-surface-variant">This template will be removed from your available selections.</p>
        <div class="flex gap-2 justify-end">
          <BaseButton variant="secondary" @click="showDeleteConfirm = false" data-testid="cancel-delete-rule-button">Cancel</BaseButton>
          <BaseButton variant="primary" @click="confirmDelete" data-testid="confirm-delete-rule-button">Delete</BaseButton>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.rule-template-list {
  border: 0px solid transparent;
}
</style>
