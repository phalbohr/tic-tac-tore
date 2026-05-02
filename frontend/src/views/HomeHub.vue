<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'

const authStore = useAuthStore()
</script>

<template>
  <main class="flex flex-col items-center justify-center min-h-screen gap-8 p-6">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-gray-900 mb-2">Tic-Tac-Tore</h1>
      <p class="text-gray-500 text-lg">Foosball statistics platform</p>
    </div>

    <div v-if="!authStore.isAuthenticated" class="flex flex-col items-center gap-4">
      <p class="text-gray-600">Sign in to track your matches</p>
      <GoogleOAuthButton />
    </div>

    <div v-else class="flex flex-col items-center gap-4">
      <p class="text-gray-700 text-xl">Welcome back! 👋</p>
      <p class="text-gray-500">Your foosball dashboard is coming soon.</p>
    </div>
  </main>
</template>
