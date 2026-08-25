<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { usePlayerGroupStore } from '@/stores/usePlayerGroupStore'
import { useRuleConfigStore } from '@/stores/useRuleConfigStore'

const { t } = useI18n()
const authStore = useAuthStore()
const playerGroupStore = usePlayerGroupStore()
const ruleConfigStore = useRuleConfigStore()

onMounted(async () => {
  if (playerGroupStore.groups.length === 0) {
    playerGroupStore.fetchGroups().catch(() => {})
  }
  if (ruleConfigStore.presets.length === 0 && ruleConfigStore.customRules.length === 0) {
    ruleConfigStore.fetchAllRules().catch(() => {})
  }
})

const selectedGroupId = computed({
  get: () => authStore.profile?.defaultGroupId || '',
  set: async (val: string) => {
    await authStore.updateProfile({
      defaultGroupId: val ? val : null,
    })
  },
})

const selectedRuleId = computed({
  get: () => authStore.profile?.defaultRuleConfigurationId || '',
  set: async (val: string) => {
    await authStore.updateProfile({
      defaultRuleConfigurationId: val ? val : null,
    })
  },
})
</script>

<template>
  <section class="bg-surface-container-low rounded-2xl p-5 space-y-4 shadow-sm">
    <div class="flex items-center gap-2 text-primary font-headline">
      <span class="material-symbols-outlined text-lg">tune</span>
      <h3 class="text-sm font-bold tracking-tight text-on-surface">
        {{ t('cabinet.defaultPreferences', 'Default Match Preferences') }}
      </h3>
    </div>

    <div class="space-y-3">
      <!-- Default Player Group Selector -->
      <div class="space-y-1.5">
        <label
          for="default-group-select"
          class="font-headline text-[10px] font-bold uppercase tracking-widest text-primary/80 ml-1"
        >
          {{ t('cabinet.defaultPlayerGroup', 'Default Player Group') }}
        </label>
        <select
          id="default-group-select"
          v-model="selectedGroupId"
          data-test="default-group-select"
          class="w-full bg-surface-container-highest text-on-surface px-3.5 py-2.5 rounded-xl font-headline text-sm focus:outline-none focus:ring-1 focus:ring-primary transition-all cursor-pointer"
        >
          <option value="">{{ t('common.none', 'None') }}</option>
          <option
            v-for="group in playerGroupStore.groups"
            :key="group.id"
            :value="group.id"
          >
            {{ group.name }} (Team)
          </option>
        </select>
      </div>

      <!-- Default Rule Template Selector -->
      <div class="space-y-1.5">
        <label
          for="default-rule-select"
          class="font-headline text-[10px] font-bold uppercase tracking-widest text-primary/80 ml-1"
        >
          {{ t('cabinet.defaultRuleTemplate', 'Default Rule Template') }}
        </label>
        <select
          id="default-rule-select"
          v-model="selectedRuleId"
          data-test="default-rule-select"
          class="w-full bg-surface-container-highest text-on-surface px-3.5 py-2.5 rounded-xl font-headline text-sm focus:outline-none focus:ring-1 focus:ring-primary transition-all cursor-pointer"
        >
          <option value="">{{ t('common.none', 'None') }}</option>
          <optgroup
            v-if="ruleConfigStore.presets.length > 0"
            :label="t('rules.presets', 'Presets')"
          >
            <option
              v-for="preset in ruleConfigStore.presets"
              :key="preset.id"
              :value="preset.id"
            >
              {{ preset.name }} (Preset)
            </option>
          </optgroup>
          <optgroup
            v-if="ruleConfigStore.customRules.length > 0"
            :label="t('rules.custom', 'Custom Templates')"
          >
            <option
              v-for="rule in ruleConfigStore.customRules"
              :key="rule.id"
              :value="rule.id"
            >
              {{ rule.name }} (Custom)
            </option>
          </optgroup>
        </select>
      </div>
    </div>
  </section>
</template>
