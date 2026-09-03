<template>
  <div
    class="min-h-screen bg-surface text-on-surface p-6 flex flex-col items-center justify-center text-center"
  >
    <div class="max-w-md w-full bg-surface-container-highest p-6 rounded-2xl shadow-lg space-y-4">
      <div v-if="isProcessed" class="space-y-2">
        <div
          class="w-16 h-16 bg-success-container text-on-success-container rounded-full mx-auto flex items-center justify-center text-2xl font-bold"
        >
          ✓
        </div>
        <h1 class="text-xl font-bold">Match Processed</h1>
        <p class="text-sm text-on-surface-variant">
          This match has already been confirmed or processed by your opponent.
        </p>
      </div>

      <div v-else class="space-y-3">
        <h1 class="text-xl font-bold">Review Match #{{ matchId }}</h1>
        <p class="text-sm text-on-surface-variant">
          Pending peer verification. Confirm or challenge the submitted match score below.
        </p>
        <button
          @click="markConfirmed"
          class="w-full min-h-[56px] bg-primary text-on-primary rounded-xl font-bold hover:opacity-90 active:scale-95 transition-all"
        >
          Confirm Match Result
        </button>
      </div>

      <router-link
        to="/"
        class="inline-block mt-4 text-sm font-semibold text-primary hover:underline"
      >
        Return to Home Hub
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const matchId = computed(() => route.params.id as string)

const isProcessed = ref(route.query.status === 'confirmed' || route.query.status === 'processed')

function markConfirmed() {
  isProcessed.value = true
}
</script>
