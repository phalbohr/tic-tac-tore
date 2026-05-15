<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'

const { t } = useI18n()
const authStore = useAuthStore()
</script>

<template>
  <main class="flex flex-col items-center justify-center min-h-screen gap-8 p-6">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-gray-900 mb-2">{{ t('home.title') }}</h1>
      <p class="text-gray-500 text-lg">{{ t('home.subtitle') }}</p>
    </div>

    <div v-if="!authStore.isAuthenticated" class="flex flex-col items-center gap-4">
      <p class="text-gray-600">{{ t('home.signInMessage') }}</p>
      <GoogleOAuthButton />
    </div>

    <div v-else class="flex flex-col items-center gap-4">
      <p class="text-gray-700 text-xl">{{ t('home.welcomeBack') }}</p>
      <p class="text-gray-500">{{ t('home.comingSoon') }}</p>
      <button @click="authStore.logout()" class="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition">{{ t('auth.signOut') }}</button>
    </div>
  </main>
</template>
