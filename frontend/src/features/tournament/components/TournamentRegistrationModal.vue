<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type {
  TournamentDto,
  RegisterTournamentPayload,
} from '@/features/tournament/types/tournament'

interface Props {
  isOpen: boolean
  tournament: TournamentDto | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'register', payload: RegisterTournamentPayload): void
}>()

const { t } = useI18n()

const partnerId = ref('')
const partnerNickname = ref('')
const error = ref('')

const isFixed2v2 = computed(() => {
  return props.tournament?.mode === 'TWO_VS_TWO_FIXED_TEAMS'
})

watch(
  () => props.isOpen,
  (open) => {
    if (open) {
      partnerId.value = ''
      partnerNickname.value = ''
      error.value = ''
    }
  },
)

function handleClose() {
  emit('close')
}

function handleSubmit() {
  error.value = ''

  if (isFixed2v2.value) {
    if (!partnerId.value.trim()) {
      error.value = t('tournament.registration.partnerRequired')
      return
    }
    emit('register', { partnerId: partnerId.value.trim() })
  } else {
    emit('register', { partnerId: null })
  }
}
</script>

<template>
  <div
    v-if="isOpen && tournament"
    data-testid="tournament-registration-modal"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs"
    @click.self="handleClose"
  >
    <div
      class="w-full max-w-lg bg-surface-container-low rounded-2xl shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150"
    >
      <div class="px-6 py-5 flex items-center justify-between border-b border-outline-variant/10">
        <h2 class="text-xl font-bold text-on-surface">
          {{ t('tournament.registration.modalTitle') }}
        </h2>
        <button
          data-testid="close-registration-modal-btn"
          type="button"
          class="p-2 text-on-surface-variant hover:text-on-surface rounded-full transition-colors"
          @click="handleClose"
        >
          <span class="sr-only">{{ t('common.close') }}</span>
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <div class="px-6 py-5 space-y-4">
        <div class="p-4 rounded-xl bg-surface-container-high space-y-1">
          <div class="text-sm font-semibold text-primary">
            {{ tournament.name }}
          </div>
          <div class="text-xs text-on-surface-variant">
            {{ tournament.format }} • {{ tournament.mode }}
          </div>
        </div>

        <div v-if="!isFixed2v2" class="space-y-2">
          <h3 class="font-medium text-on-surface">
            {{ t('tournament.registration.confirmSoloTitle') }}
          </h3>
          <p class="text-sm text-on-surface-variant">
            {{ t('tournament.registration.confirmSoloText', { name: tournament.name }) }}
          </p>
        </div>

        <div v-else class="space-y-3">
          <div>
            <h3 class="font-medium text-on-surface">
              {{ t('tournament.registration.selectPartnerTitle') }}
            </h3>
            <p class="text-sm text-on-surface-variant">
              {{ t('tournament.registration.selectPartnerText') }}
            </p>
          </div>

          <div>
            <label class="block text-xs font-medium text-on-surface-variant mb-1">
              Partner User ID
            </label>
            <input
              v-model="partnerId"
              data-testid="partner-search-input"
              type="text"
              class="w-full px-4 py-2.5 rounded-xl bg-surface-container-high text-on-surface placeholder:text-on-surface-variant/60 focus:outline-hidden focus:ring-2 focus:ring-primary"
              :placeholder="t('tournament.registration.partnerSearchPlaceholder')"
            />
          </div>

          <div
            v-if="error"
            data-testid="partner-required-error"
            class="text-xs text-error font-medium"
          >
            {{ error }}
          </div>
        </div>
      </div>

      <div
        class="px-6 py-4 flex items-center justify-end gap-3 bg-surface-container-low border-t border-outline-variant/10"
      >
        <button
          type="button"
          class="px-4 py-2 text-sm font-medium text-on-surface-variant hover:text-on-surface rounded-xl transition-colors"
          @click="handleClose"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          data-testid="confirm-registration-btn"
          type="button"
          :disabled="loading"
          class="px-5 py-2 text-sm font-medium text-on-primary bg-primary hover:bg-primary/90 disabled:opacity-50 rounded-xl transition-colors shadow-sm flex items-center gap-2"
          @click="handleSubmit"
        >
          <span
            v-if="loading"
            class="inline-block w-4 h-4 border-2 border-on-primary border-t-transparent rounded-full animate-spin"
          ></span>
          {{
            loading
              ? t('tournament.registration.submitting')
              : t('tournament.registration.submitRegistration')
          }}
        </button>
      </div>
    </div>
  </div>
</template>
