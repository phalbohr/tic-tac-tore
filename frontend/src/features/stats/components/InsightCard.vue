<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { PlayerInsight } from '@/services/insightService'

const props = defineProps<{
  insight: PlayerInsight
}>()

const router = useRouter()
const { t } = useI18n()

const localizedTitle = computed(() => {
  return t(props.insight.titleKey, props.insight.params || {})
})

const localizedDescription = computed(() => {
  return t(props.insight.descriptionKey, props.insight.params || {})
})

const categoryLabel = computed(() => {
  const catKey = `insights.category.${props.insight.category.toLowerCase()}`
  return t(catKey)
})

const categoryBadgeClass = computed(() => {
  switch (props.insight.category) {
    case 'STREAK':
      return 'bg-amber-500/10 text-amber-500 border-amber-500/20'
    case 'TREND':
      return 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20'
    case 'POSITION':
      return 'bg-sky-500/10 text-sky-500 border-sky-500/20'
    case 'PARTNERSHIP':
      return 'bg-purple-500/10 text-purple-500 border-purple-500/20'
    case 'MILESTONE':
      return 'bg-amber-400/10 text-amber-400 border-amber-400/20'
    default:
      return 'bg-ch-surface-highest text-ch-text-subtle border-ch-border'
  }
})

function handleDrillDown() {
  if (props.insight.drillDownUrl) {
    router.push(props.insight.drillDownUrl)
  }
}
</script>

<template>
  <div
    class="p-4 rounded-xl border border-ch-border bg-ch-surface-card shadow-sm flex flex-col justify-between transition-all duration-200 hover:border-ch-primary/30"
    data-testid="insight-card"
  >
    <div>
      <div class="flex items-center justify-between gap-2 mb-3">
        <div class="flex items-center gap-2">
          <span
            class="material-symbols-outlined text-xl text-ch-primary"
            data-testid="insight-icon"
          >
            {{ insight.icon || 'lightbulb' }}
          </span>
          <span
            class="px-2 py-0.5 text-xs font-medium rounded-full border"
            :class="categoryBadgeClass"
            data-testid="insight-category"
          >
            {{ categoryLabel }}
          </span>
        </div>
      </div>

      <h4 class="text-base font-semibold text-ch-text-bright mb-1" data-testid="insight-title">
        {{ localizedTitle }}
      </h4>

      <p class="text-sm text-ch-text-secondary leading-relaxed" data-testid="insight-description">
        {{ localizedDescription }}
      </p>
    </div>

    <div
      v-if="insight.drillDownUrl"
      class="mt-4 pt-3 border-t border-ch-border/50 flex justify-end"
    >
      <button
        type="button"
        class="inline-flex items-center gap-1.5 text-xs font-medium text-ch-primary hover:text-ch-primary-hover transition-colors focus:outline-none"
        data-testid="insight-drilldown-btn"
        @click="handleDrillDown"
      >
        <span>{{ t('insights.drillDown') }}</span>
        <span class="material-symbols-outlined text-sm">arrow_forward</span>
      </button>
    </div>
  </div>
</template>
