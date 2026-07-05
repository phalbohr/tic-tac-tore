<script setup lang="ts">
import { useStatsStore } from '../stores/useStatsStore'
import { useRouter } from 'vue-router'

const statsStore = useStatsStore()
const router = useRouter()

function enableDemoData() {
  statsStore.toggleDemoMode(true)
}

function recordFirstMatch() {
  router.push('/matches/new')
}
</script>

<template>
  <div class="ch-empty-state w-full flex items-center justify-center p-4">
    <div class="w-full max-w-sm bg-surface-container-low rounded-2xl p-6 space-y-6 shadow-2xl text-center">
      <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-container text-primary mb-2">
        <span class="material-symbols-outlined text-3xl">sports_soccer</span>
      </div>
      
      <div>
        <h2 class="font-headline text-xl font-bold text-on-surface mb-2">No Matches Yet</h2>
        <p class="text-sm text-on-surface-variant leading-relaxed">
          Record your first match to start tracking your statistics and climb the leaderboard!
        </p>
      </div>

      <div class="flex flex-col gap-3 pt-2">
        <button 
          @click="recordFirstMatch"
          class="w-full py-3.5 rounded-xl bg-gradient-to-br from-primary to-primary-container text-background font-headline font-extrabold uppercase tracking-wider shadow-xl hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-2"
        >
          <span class="material-symbols-outlined font-bold">add_circle</span>
          Record First Match
        </button>
        
        <button 
          @click="enableDemoData"
          class="w-full py-3.5 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold uppercase tracking-wider text-sm transition-colors flex items-center justify-center gap-2"
        >
          <span class="material-symbols-outlined text-sm">visibility</span>
          Toggle Demo Data
        </button>
      </div>
    </div>
  </div>
</template>

