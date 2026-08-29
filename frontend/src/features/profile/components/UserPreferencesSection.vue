<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { usePlayerGroupStore } from '@/stores/usePlayerGroupStore'
import { useRuleConfigStore } from '@/stores/useRuleConfigStore'

const { t } = useI18n()
const authStore = useAuthStore()
const playerGroupStore = usePlayerGroupStore()
const ruleConfigStore = useRuleConfigStore()
const isUpdating = ref(false)

onMounted(async () => {
  if (!authStore.profile) {
    try {
      await authStore.fetchProfile()
    } catch {
      // ignore fetch error if unauthenticated
    }
  }
  if (playerGroupStore.groups.length === 0) {
    try {
      await playerGroupStore.fetchGroups()
    } catch {
      // ignore fetch error
    }
  }
  if (ruleConfigStore.presets.length === 0 && ruleConfigStore.customRules.length === 0) {
    try {
      await ruleConfigStore.fetchAllRules()
    } catch {
      // ignore fetch error
    }
  }
})

const selectedGroupId = computed({
  get: () => authStore.profile?.defaultGroupId || '',
  set: async (val: string) => {
    try {
      await authStore.updateProfile({
        defaultGroupId: val ? val : null,
      })
    } catch (err) {
      console.error('Failed to update default group', err)
    }
  },
})

const selectedRuleId = computed({
  get: () => authStore.profile?.defaultRuleConfigurationId || '',
  set: async (val: string) => {
    try {
      await authStore.updateProfile({
        defaultRuleConfigurationId: val ? val : null,
      })
    } catch (err) {
      console.error('Failed to update default rule configuration', err)
    }
  },
})

const poolNotificationsEnabled = computed(() => authStore.profile?.poolNotificationsEnabled ?? true)

async function togglePoolNotifications() {
  if (isUpdating.value) return
  isUpdating.value = true
  try {
    if (!authStore.profile) {
      await authStore.fetchProfile()
    }
    const currentVal = authStore.profile?.poolNotificationsEnabled ?? true
    await authStore.updateProfile({
      poolNotificationsEnabled: !currentVal,
    })
  } catch (err) {
    console.error('Failed to update pool notifications preference', err)
  } finally {
    isUpdating.value = false
  }
}
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

      <!-- Matchmaking Pool Notifications Toggle -->
      <div class="pt-2 flex items-center justify-between">
        <div class="flex flex-col">
          <span class="text-on-surface font-headline font-semibold text-sm">
            {{ t('cabinet.poolNotifications', 'Matchmaking Pool Notifications') }}
          </span>
          <span class="text-on-surface-variant text-[10px]">
            {{ t('cabinet.poolNotificationsDesc', 'Receive push notifications when new matchmaking pools are created') }}
          </span>
        </div>

        <button
          type="button"
          role="switch"
          :aria-checked="poolNotificationsEnabled"
          :aria-busy="isUpdating"
          :disabled="isUpdating"
          data-test="pool-notifications-toggle"
          @click="togglePoolNotifications"
          :class="[
            'relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background',
            isUpdating ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer',
            poolNotificationsEnabled ? 'bg-primary' : 'bg-surface-container-highest'
          ]"
        >
          <span class="sr-only">Toggle Matchmaking Pool Notifications</span>
          <span
            :class="[
              'inline-block h-4 w-4 transform rounded-full bg-white transition-transform',
              poolNotificationsEnabled ? 'translate-x-6' : 'translate-x-1'
            ]"
          />
        </button>
      </div>
    </div>
  </section>
</template>
