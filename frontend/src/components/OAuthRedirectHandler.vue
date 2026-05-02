<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  const token = route.query.token as string | undefined

  if (token) {
    authStore.setToken(token)
  }

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
    <p class="text-gray-500">Signing you in…</p>
  </div>
</template>
