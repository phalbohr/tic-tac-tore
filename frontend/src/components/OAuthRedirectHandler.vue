<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  // Security: JWT Leaked in URL - Removed token extraction from URL parameters.
  // The backend now sets the JWT in an HttpOnly cookie during the redirect.
  
  // We assume the cookie is set if we reached this handler via the OAuth2 flow.
  authStore.setAuthenticated(true)

  let intentUrl = '/'
  try {
    intentUrl = sessionStorage.getItem('intent_url') || '/'
    if (!intentUrl.startsWith('/')) intentUrl = '/'
    sessionStorage.removeItem('intent_url')
  } catch { console.warn('sessionStorage disabled') }

  router.push(intentUrl || '/')
})
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="text-center">
      <p class="text-gray-500 animate-pulse">Completing secure sign-in…</p>
    </div>
  </div>
</template>
