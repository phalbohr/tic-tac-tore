<script setup lang="ts">
import { computed } from 'vue'
import { useStatsStore } from '@/features/stats/stores/useStatsStore'

const statsStore = useStatsStore()
const isDemoEnabled = computed(() => statsStore.isDemoModeEnabled)

function toggleDemoMode() {
  statsStore.toggleDemoMode(!isDemoEnabled.value)
}
</script>

<template>
  <section class="ch-demo-toggle pt-6 space-y-3">
    <h3 class="font-headline text-[10px] font-bold uppercase tracking-widest text-primary/80 ml-1">
      Demo Mode
    </h3>
    <div class="flex items-center justify-between p-4 bg-surface-container-low rounded-xl">
      <div class="flex flex-col">
        <span class="text-on-surface font-headline font-semibold text-sm">Enable Demo Data</span>
        <span class="text-on-surface-variant text-[10px]">Show mock statistics when you have no matches</span>
      </div>
      
      <button 
        @click="toggleDemoMode"
        :class="[
          'relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background',
          isDemoEnabled ? 'bg-primary' : 'bg-surface-container-highest'
        ]"
        role="switch"
        :aria-checked="isDemoEnabled"
        data-testid="demo-mode-toggle"
      >
        <span class="sr-only">Toggle Demo Mode</span>
        <span 
          :class="[
            'inline-block h-4 w-4 transform rounded-full bg-white transition-transform',
            isDemoEnabled ? 'translate-x-6' : 'translate-x-1'
          ]"
        />
      </button>
    </div>
  </section>
</template>

