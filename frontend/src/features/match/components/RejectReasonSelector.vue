<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'submit', payload: { reason: string; customReason: string }): void
  (e: 'cancel'): void
}>()

const selectedReason = ref<string>('')
const customReason = ref<string>('')

const predefinedReasons = [
  'Wrong score',
  'Wrong players',
  'Did not play',
  'Other'
]

const isSubmitDisabled = computed(() => {
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
    class="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fade-in"
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
          v-for="reason in predefinedReasons"
          :key="reason"
          :class="[
            'flex items-center gap-3 px-4 min-h-12 rounded-xl cursor-pointer transition-all border-none text-base font-medium select-none',
            selectedReason === reason
              ? 'bg-red-500/20 text-red-300 ring-2 ring-red-500/50'
              : 'bg-neutral-800 text-neutral-200 hover:bg-neutral-750'
          ]"
        >
          <input
            type="radio"
            name="rejectionReason"
            :value="reason"
            v-model="selectedReason"
            class="w-5 h-5 accent-red-500 cursor-pointer"
          />
          <span>{{ reason }}</span>
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
