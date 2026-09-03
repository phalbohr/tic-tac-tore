<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useRuleConfigStore } from '@/stores/useRuleConfigStore'
import { useMatchDraftStore } from '../stores/matchDraftStore'
import type { CreateRuleConfigRequest } from '@/services/ruleConfigService'
import RuleTemplateModal from './RuleTemplateModal.vue'

defineOptions({
  name: 'RulePicker',
})

const props = withDefaults(
  defineProps<{
    isLocked?: boolean
  }>(),
  {
    isLocked: false,
  },
)

const { t } = useI18n()
const authStore = useAuthStore()
const ruleStore = useRuleConfigStore()
const draftStore = useMatchDraftStore()

const effectivelyLocked = computed(() => Boolean(props.isLocked || draftStore.isTournamentMatch))

const selectedRuleId = ref<string | null>(
  ruleStore.selectedRuleId || authStore.profile?.defaultRuleConfigurationId || null,
)
const isModalOpen = ref(false)
const modalError = ref('')

watch(
  () => authStore.profile?.defaultRuleConfigurationId,
  (newDef) => {
    if (newDef && !selectedRuleId.value) {
      const defaultRule = ruleStore.allRules.find((r) => r.id === newDef)
      if (defaultRule) {
        selectRule(defaultRule.id, defaultRule.name)
      }
    }
  },
)

onMounted(async () => {
  if (ruleStore.allRules.length === 0) {
    try {
      await ruleStore.fetchAllRules()
    } catch {
      // ignore
    }
  }
  if (!selectedRuleId.value && authStore.profile?.defaultRuleConfigurationId) {
    const defaultRule = ruleStore.allRules.find(
      (r) => r.id === authStore.profile?.defaultRuleConfigurationId,
    )
    if (defaultRule) {
      selectRule(defaultRule.id, defaultRule.name)
    }
  }
})

const selectedRule = computed(() => {
  if (selectedRuleId.value) {
    const found = ruleStore.allRules.find((r) => r.id === selectedRuleId.value)
    if (found) return found
  }
  return (
    ruleStore.allRules.find(
      (r) =>
        ruleStore.selectedRuleId === r.id ||
        draftStore.ruleSystem?.toUpperCase() === r.name?.toUpperCase() ||
        draftStore.ruleSystem === r.id,
    ) || null
  )
})

function isSelected(rule: { id: string; name: string }) {
  return (
    selectedRuleId.value === rule.id ||
    ruleStore.selectedRuleId === rule.id ||
    draftStore.ruleSystem?.toUpperCase() === rule.name?.toUpperCase() ||
    draftStore.ruleSystem === rule.id
  )
}

function openModal() {
  if (effectivelyLocked.value) return
  modalError.value = ''
  isModalOpen.value = true
}

function selectRule(ruleId: string, ruleName: string) {
  if (effectivelyLocked.value && selectedRuleId.value && selectedRuleId.value !== ruleId) return
  selectedRuleId.value = ruleId
  ruleStore.selectRule(ruleId)
  draftStore.ruleSystem = ruleName
  draftStore.loadRuleConfig()
}

async function handleSetAsDefault(ruleId: string) {
  if (effectivelyLocked.value) return
  try {
    await authStore.updateProfile({ defaultRuleConfigurationId: ruleId })
  } catch (error) {
    console.error('Failed to set default rule', error)
  }
}

async function handleSaveCustomRule(payload: CreateRuleConfigRequest) {
  modalError.value = ''
  try {
    const created = await ruleStore.createCustomRule(payload)
    isModalOpen.value = false
    selectRule(created.id, created.name)
  } catch (err: unknown) {
    modalError.value = err instanceof Error ? err.message : t('common.error', 'An error occurred')
  }
}
</script>

<template>
  <div class="flex flex-col gap-2 w-full text-start" data-testid="rule-picker">
    <!-- Informative Locked Notice Banner -->
    <div
      v-if="effectivelyLocked"
      class="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-surface-container-highest text-on-surface-variant text-xs font-medium mb-1"
      data-testid="locked-rule-notice"
    >
      <span class="material-symbols-outlined text-sm text-primary">lock</span>
      <span>{{ t('rules.lockedTournamentNotice', 'Rule system is locked to tournament settings (FR45)') }}</span>
    </div>

    <div class="flex justify-between items-center mb-1">
      <div class="flex items-center gap-2">
        <h3 class="text-on-surface font-headline font-bold text-sm">
          {{ t('rules.pickerTitle', 'Rule System') }}
        </h3>
        <button
          v-if="!effectivelyLocked && selectedRule && selectedRule.id !== authStore.profile?.defaultRuleConfigurationId"
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
        v-if="!effectivelyLocked"
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
        :disabled="effectivelyLocked && !isSelected(rule)"
        @click="!effectivelyLocked && selectRule(rule.id, rule.name)"
        class="px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all flex items-center gap-1"
        :class="[
          isSelected(rule)
            ? 'bg-primary text-background shadow-md active cursor-default'
            : effectivelyLocked
              ? 'bg-surface-container-highest text-on-surface/40 pointer-events-none opacity-50 cursor-not-allowed'
              : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80 cursor-pointer',
        ]"
        :data-testid="`rule-chip-${rule.id}`"
      >
        <span v-if="effectivelyLocked && isSelected(rule)" class="material-symbols-outlined text-xs">
          lock
        </span>
        <span>{{ rule.name }}</span>
        <span
          v-if="effectivelyLocked && isSelected(rule)"
          class="text-[10px] uppercase font-bold tracking-wider opacity-80 ml-1"
        >
          {{ t('rules.tournamentBadge', 'Tournament Rule') }}
        </span>
        <span
          v-if="!effectivelyLocked && rule.id === authStore.profile?.defaultRuleConfigurationId"
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
      v-if="!effectivelyLocked"
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
