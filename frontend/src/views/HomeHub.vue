<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'

const TITLE = 'Tic-Tac-Tore'
const SUBTITLE = 'Foosball statistics platform'
const SIGN_IN_MESSAGE = 'Sign in to track your matches'
const WELCOME_MESSAGE = 'Welcome back! 👋'
const COMING_SOON_MESSAGE = 'Your foosball dashboard is coming soon.'
const SIGN_OUT_LABEL = 'Sign Out'

const authStore = useAuthStore()
</script>

<template>
  <main class="flex flex-col items-center justify-center min-h-screen gap-8 p-6">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-gray-900 mb-2">{{ TITLE }}</h1>
      <p class="text-gray-500 text-lg">{{ SUBTITLE }}</p>
    </div>

    <div v-if="!authStore.isAuthenticated" class="flex flex-col items-center gap-4">
      <p class="text-gray-600">{{ SIGN_IN_MESSAGE }}</p>
      <GoogleOAuthButton />
    </div>

    <div v-else class="flex flex-col items-center gap-4">
      <p class="text-gray-700 text-xl">{{ WELCOME_MESSAGE }}</p>
      <p class="text-gray-500">{{ COMING_SOON_MESSAGE }}</p>
      <button @click="authStore.logout()" class="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition">{{ SIGN_OUT_LABEL }}</button>
    </div>
  </main>
</template>
