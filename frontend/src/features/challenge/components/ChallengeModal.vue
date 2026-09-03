<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useChallengeStore } from '@/features/challenge/stores/useChallengeStore'
import { getRuleConfigurations, type RuleConfig } from '@/services/ruleConfigService'
import AvatarBase from '@/components/AvatarBase.vue'

interface Props {
  modelValue: boolean
  targetPlayer?: { id: string; nickname: string; avatar?: string } | null
  targetGroup?: { id: string; name: string } | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'challengeSent', challenge: any): void
}>()

const { t } = useI18n()
const authStore = useAuthStore()
const challengeStore = useChallengeStore()

const matchType = ref<'ONE_VS_ONE' | 'TWO_VS_TWO'>('ONE_VS_ONE')
const ruleConfigs = ref<RuleConfig[]>([])
const selectedRuleConfigId = ref<string>('')
const message = ref('')
const isSubmitting = ref(false)
const error = ref('')

const isGroupChallenge = computed(() => !!props.targetGroup)

function resetForm() {
  error.value = ''
  message.value = ''
  if (props.targetGroup) {
    matchType.value = 'TWO_VS_TWO'
  } else {
    matchType.value = 'ONE_VS_ONE'
  }
  if (
    authStore.profile?.defaultRuleConfigurationId &&
    ruleConfigs.value.some((r) => r.id === authStore.profile?.defaultRuleConfigurationId)
  ) {
    selectedRuleConfigId.value = authStore.profile.defaultRuleConfigurationId
  } else {
    selectedRuleConfigId.value = ''
  }
}

async function loadRuleConfigs() {
  try {
    ruleConfigs.value = await getRuleConfigurations()
    if (authStore.profile?.defaultRuleConfigurationId) {
      const exists = ruleConfigs.value.some(
        (r) => r.id === authStore.profile?.defaultRuleConfigurationId,
      )
      if (exists) {
        selectedRuleConfigId.value = authStore.profile.defaultRuleConfigurationId
      }
    }
  } catch (err) {
    console.error('Failed to load rule configs for challenge modal', err)
  }
}

onMounted(() => {
  loadRuleConfigs()
  if (props.modelValue) {
    resetForm()
  }
})

watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) {
      resetForm()
    }
  },
)

function handleClose() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  error.value = ''
  isSubmitting.value = true

  try {
    const payload = {
      targetPlayerId: props.targetPlayer?.id,
      targetGroupId: props.targetGroup?.id,
      matchType: matchType.value,
      ruleConfigId: selectedRuleConfigId.value || undefined,
      message: message.value.trim() || undefined,
    }

    const created = await challengeStore.createChallenge(payload)
    emit('challengeSent', created)
    handleClose()
  } catch (err: any) {
    error.value = err.message || t('challenge.errors.createFailed', 'Failed to send challenge')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md"
      role="dialog"
      aria-modal="true"
    >
      <div
        class="w-full max-w-md bg-surface-container-low rounded-2xl p-6 space-y-5 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]"
      >
        <!-- Header -->
        <div class="flex justify-between items-center">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-xl">swords</span>
            <h2 class="font-headline text-lg font-bold text-on-surface">
              {{ t('challenge.modalTitle', 'Challenge to Match') }}
            </h2>
          </div>
          <button
            type="button"
            @click="handleClose"
            data-testid="challenge-cancel-btn"
            class="text-on-surface-variant hover:text-on-surface p-1 rounded-lg transition-colors cursor-pointer"
            aria-label="Close"
          >
            <span class="material-symbols-outlined text-xl">close</span>
          </button>
        </div>

        <!-- Target Info Header -->
        <div class="p-3 bg-surface-container-highest/60 rounded-xl flex items-center gap-3">
          <template v-if="targetPlayer">
            <div class="w-10 h-10 rounded-full bg-surface-container-low overflow-hidden shrink-0">
              <AvatarBase
                :avatar="targetPlayer.avatar"
                :name="targetPlayer.nickname"
                shape="circle"
              />
            </div>
            <div class="min-w-0">
              <div class="text-xs text-on-surface-variant font-medium">
                {{ t('challenge.challengingPlayer', 'Challenging player') }}
              </div>
              <div
                class="text-sm font-bold text-on-surface truncate"
                data-testid="target-player-name"
              >
                {{ targetPlayer.nickname }}
              </div>
            </div>
          </template>
          <template v-else-if="targetGroup">
            <div
              class="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center shrink-0"
            >
              <span class="material-symbols-outlined text-xl">groups</span>
            </div>
            <div class="min-w-0">
              <div class="text-xs text-on-surface-variant font-medium">
                {{ t('challenge.challengingGroup', 'Challenging group') }}
              </div>
              <div
                class="text-sm font-bold text-on-surface truncate"
                data-testid="target-group-name"
              >
                {{ targetGroup.name }}
              </div>
            </div>
          </template>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4 flex-grow overflow-y-auto pr-1">
          <!-- Error banner -->
          <div
            v-if="error"
            class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold"
            data-testid="challenge-error"
          >
            {{ error }}
          </div>

          <!-- Match Type Selector -->
          <div class="space-y-1.5">
            <label class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80">
              {{ t('challenge.matchTypeLabel', 'Match Format') }}
            </label>
            <div class="grid grid-cols-2 gap-2">
              <button
                type="button"
                data-testid="match-type-1v1"
                @click="matchType = 'ONE_VS_ONE'"
                :class="[
                  'py-2.5 rounded-xl font-headline text-xs font-bold transition-all cursor-pointer flex items-center justify-center gap-1.5',
                  matchType === 'ONE_VS_ONE'
                    ? 'bg-primary text-background shadow-md'
                    : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80',
                ]"
              >
                <span class="material-symbols-outlined text-sm">person</span>
                <span>1v1</span>
              </button>
              <button
                type="button"
                data-testid="match-type-2v2"
                @click="matchType = 'TWO_VS_TWO'"
                :class="[
                  'py-2.5 rounded-xl font-headline text-xs font-bold transition-all cursor-pointer flex items-center justify-center gap-1.5',
                  matchType === 'TWO_VS_TWO'
                    ? 'bg-primary text-background shadow-md'
                    : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80',
                ]"
              >
                <span class="material-symbols-outlined text-sm">group</span>
                <span>2v2</span>
              </button>
            </div>
          </div>

          <!-- Rule Template Selector -->
          <div class="space-y-1.5">
            <label
              for="challenge-rule-config"
              class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80"
            >
              {{ t('challenge.ruleTemplateLabel', 'Rule Configuration (Optional)') }}
            </label>
            <select
              id="challenge-rule-config"
              v-model="selectedRuleConfigId"
              data-testid="challenge-rule-select"
              class="w-full bg-surface-container-highest text-on-surface px-4 py-3 rounded-xl font-headline text-sm focus:outline-none focus:ring-2 focus:ring-primary transition-all cursor-pointer"
            >
              <option value="">
                {{ t('challenge.defaultRules', 'Default / Standard Rules') }}
              </option>
              <option v-for="rule in ruleConfigs" :key="rule.id" :value="rule.id">
                {{ rule.name }} ({{ rule.goalLimit }} {{ t('match.goals', 'goals') }})
              </option>
            </select>
          </div>

          <!-- Custom Message Input -->
          <div class="space-y-1.5">
            <div class="flex justify-between items-center">
              <label
                for="challenge-message"
                class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80"
              >
                {{ t('challenge.messageLabel', 'Message (Optional)') }}
              </label>
              <span class="text-[10px] text-on-surface-variant font-mono">
                {{ message.length }}/255
              </span>
            </div>
            <textarea
              id="challenge-message"
              v-model="message"
              maxlength="255"
              rows="2"
              :placeholder="
                t('challenge.messagePlaceholder', 'e.g. Ready for a rematch? Head to table 1!')
              "
              data-testid="challenge-message-input"
              class="w-full bg-surface-container-highest text-on-surface px-4 py-2.5 rounded-xl font-headline text-xs focus:outline-none focus:ring-2 focus:ring-primary transition-all placeholder:text-on-surface-variant/50 resize-none"
            />
          </div>

          <!-- Actions -->
          <div class="flex gap-2 pt-2">
            <button
              type="submit"
              :disabled="isSubmitting"
              data-testid="challenge-submit-btn"
              class="flex-1 py-3 rounded-xl bg-primary text-background font-headline font-bold text-xs uppercase tracking-wider hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-1.5 cursor-pointer disabled:opacity-50"
            >
              <span class="material-symbols-outlined text-sm">send</span>
              <span>{{
                isSubmitting
                  ? t('common.sending', 'Sending...')
                  : t('challenge.sendChallenge', 'Send Challenge')
              }}</span>
            </button>
            <button
              type="button"
              @click="handleClose"
              class="px-4 py-3 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold text-xs transition-colors cursor-pointer"
            >
              {{ t('common.cancel', 'Cancel') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>
