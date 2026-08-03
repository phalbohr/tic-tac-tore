<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

const props = withDefaults(defineProps<{
  isOpen: boolean
  isSubmitting?: boolean
}>(), {
  isSubmitting: false
})

const emit = defineEmits<{
  (e: 'submit', payload: { reason: string; customReason: string }): void
  (e: 'cancel'): void
}>()

const selectedReason = ref<string>('')
const customReason = ref<string>('')

const rootRef = ref<HTMLElement | null>(null)

watch(
  () => props.isOpen,
  async (newVal) => {
    if (newVal) {
      selectedReason.value = ''
      customReason.value = ''
      await nextTick()
      rootRef.value?.focus()
    }
  }
)

const predefinedReasons = [
  { value: 'Wrong score', key: 'match.reasonWrongScore' },
  { value: 'Wrong players', key: 'match.reasonWrongPlayers' },
  { value: 'Did not play', key: 'match.reasonDidNotPlay' },
  { value: 'Other', key: 'match.reasonOther' }
]

const isSubmitDisabled = computed(() => {
  if (props.isSubmitting) return true
  if (!selectedReason.value) return true
  if (selectedReason.value === 'Other' && !customReason.value.trim()) return true
  return false
})

function handleSubmit() {
  if (isSubmitDisabled.value) return
  emit('submit', {
    reason: selectedReason.value,
    customReason: customReason.value.trim()
  })
}

function handleCancel() {
  selectedReason.value = ''
  customReason.value = ''
  emit('cancel')
}
</script>

<template>
  <div
    v-if="isOpen"
    ref="rootRef"
    class="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fade-in"
    @keydown.esc="handleCancel"
    tabindex="-1"
    role="dialog"
    aria-modal="true"
    :aria-label="$t('match.rejectMatch')"
  >
    <div
      class="w-full max-w-md bg-neutral-900 text-white rounded-2xl p-6 shadow-2xl space-y-6 transform transition-all animate-scale-up"
    >
      <div class="flex items-center justify-between">
        <h3 class="text-xl font-bold text-neutral-100" data-testid="rejection-dialog-title">
          {{ $t('match.rejectMatch') }}
        </h3>
        <button
          type="button"
          class="w-12 h-12 flex items-center justify-center rounded-full text-neutral-400 hover:text-white hover:bg-neutral-800 transition-colors"
          aria-label="Close dialog"
          @click="handleCancel"
        >
          ✕
        </button>
      </div>

      <p class="text-sm text-neutral-400">
        {{ $t('match.selectReason') }}
      </p>

      <div class="space-y-3" role="radiogroup">
        <label
          v-for="reasonObj in predefinedReasons"
          :key="reasonObj.value"
          :class="[
            'flex items-center gap-3 px-4 min-h-12 rounded-xl cursor-pointer transition-all border-none text-base font-medium select-none',
            selectedReason === reasonObj.value
              ? 'bg-red-500/20 text-red-300 ring-2 ring-red-500/50'
              : 'bg-neutral-800 text-neutral-200 hover:bg-neutral-750'
          ]"
        >
          <input
            type="radio"
            name="rejectionReason"
            :value="reasonObj.value"
            v-model="selectedReason"
            class="w-5 h-5 accent-red-500 cursor-pointer"
          />
          <span>{{ $t(reasonObj.key, reasonObj.value) }}</span>
        </label>
      </div>

      <div class="space-y-2">
        <textarea
          v-model="customReason"
          maxlength="200"
          rows="3"
          :placeholder="$t('match.customReasonPlaceholder')"
          class="w-full bg-neutral-800 text-neutral-100 placeholder-neutral-500 rounded-xl p-3 border-none focus:outline-none focus:ring-2 focus:ring-red-500/50 resize-none text-sm"
          data-testid="rejection-free-text"
          data-test-id="reject-free-text-field"
        ></textarea>
        <div class="text-right text-xs text-neutral-500">
          {{ customReason.length }}/200
        </div>
      </div>

      <div class="flex items-center gap-3 pt-2">
        <button
          type="button"
          class="flex-1 min-h-12 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-200 font-semibold transition-colors"
          @click="handleCancel"
        >
          {{ $t('common.cancel') }}
        </button>

        <button
          type="button"
          :disabled="isSubmitDisabled"
          :class="[
            'flex-1 min-h-12 rounded-xl font-semibold transition-all',
            isSubmitDisabled
              ? 'bg-neutral-800 text-neutral-500 cursor-not-allowed opacity-50'
              : 'bg-red-600 hover:bg-red-500 text-white shadow-lg shadow-red-600/30'
          ]"
          data-testid="submit-rejection-btn"
          @click="handleSubmit"
        >
          {{ $t('match.submitRejection') }}
        </button>
      </div>
    </div>
  </div>
</template>
