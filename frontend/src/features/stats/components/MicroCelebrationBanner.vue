<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { PlayerInsight } from '@/services/insightService'

const props = defineProps<{
  insight: PlayerInsight
}>()

const emit = defineEmits<{
  (e: 'dismiss'): void
}>()

const router = useRouter()
const { t } = useI18n()

let timer: ReturnType<typeof setTimeout> | null = null

onMounted(() => {
  timer = setTimeout(() => {
    emit('dismiss')
  }, 4000)
})

onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
  }
})

const localizedTitle = computed(() => {
  return t(props.insight.titleKey, props.insight.params || {})
})

const localizedDescription = computed(() => {
  return t(props.insight.descriptionKey, props.insight.params || {})
})

function handleDismiss() {
  if (timer) {
    clearTimeout(timer)
  }
  emit('dismiss')
}

function handleDrillDown() {
  if (props.insight.drillDownUrl) {
    router.push(props.insight.drillDownUrl)
  }
  handleDismiss()
}
</script>

<template>
  <div
    role="status"
    aria-live="polite"
    class="w-full max-w-xl mx-auto mb-4 p-4 rounded-xl border border-amber-500/30 bg-ch-surface-card bg-gradient-to-r from-amber-500/10 via-ch-surface-card to-amber-500/5 shadow-md flex items-start justify-between gap-3 animate-fade-in"
    data-testid="micro-celebration-banner"
  >
    <div class="flex items-start gap-3 flex-1 min-w-0">
      <div class="w-10 h-10 rounded-full bg-amber-500/20 text-amber-500 flex items-center justify-center shrink-0">
        <span class="material-symbols-outlined text-xl">
          {{ insight.icon || 'celebration' }}
        </span>
      </div>

      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 mb-0.5">
          <span class="text-xs font-semibold text-amber-500 uppercase tracking-wider">
            {{ t('insights.celebrationTitle') }}
          </span>
        </div>
        <h4 class="text-sm font-bold text-ch-text-bright truncate">
          {{ localizedTitle }}
        </h4>
        <p class="text-xs text-ch-text-secondary mt-0.5 leading-relaxed line-clamp-2">
          {{ localizedDescription }}
        </p>

        <div v-if="insight.drillDownUrl" class="mt-2">
          <button
            type="button"
            class="inline-flex items-center gap-1 text-xs font-medium text-ch-primary hover:text-ch-primary-hover focus:outline-none"
            data-testid="celebration-drilldown-btn"
            @click="handleDrillDown"
          >
            <span>{{ t('insights.drillDown') }}</span>
            <span class="material-symbols-outlined text-xs">arrow_forward</span>
          </button>
        </div>
      </div>
    </div>

    <button
      type="button"
      class="text-ch-text-subtle hover:text-ch-text-bright p-1 rounded-md transition-colors focus:outline-none shrink-0"
      :aria-label="t('common.close')"
      data-testid="celebration-close-btn"
      @click="handleDismiss"
    >
      <span class="material-symbols-outlined text-lg">close</span>
    </button>
  </div>
</template>
