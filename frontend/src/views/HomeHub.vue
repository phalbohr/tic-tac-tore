<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'

const { t } = useI18n()
const authStore = useAuthStore()

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchProfile()
  }
})
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

    <div v-else class="flex flex-col items-center gap-6">
      <div v-if="authStore.profile" class="flex flex-col items-center gap-3">
        <img 
          :src="authStore.profile.avatar" 
          alt="User Avatar" 
          class="w-24 h-24 rounded-full border-2 border-indigo-100 shadow-sm bg-white"
        />
        <p class="text-gray-800 text-2xl font-semibold">
          {{ t('home.welcomeBack') }}, {{ authStore.profile.nickname }}
        </p>
      </div>
      <div v-else class="animate-pulse flex flex-col items-center gap-3">
        <div class="w-24 h-24 bg-gray-200 rounded-full"></div>
        <div class="h-8 w-48 bg-gray-200 rounded"></div>
      </div>

      <p class="text-gray-500 italic">{{ t('home.comingSoon') }}</p>
      <button 
        @click="authStore.logout()" 
        class="px-6 py-2 bg-orange-50 text-orange-600 border border-orange-200 rounded-lg hover:bg-orange-100 transition-colors font-medium"
      >
        {{ t('auth.signOut') }}
      </button>
    </div>
  </main>
</template>
