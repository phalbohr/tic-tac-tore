<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  const token = route.query.token as string
  
  if (token) {
    const user = { id: '0', name: 'Google User', email: '' }
    auth.login(token, user)
    router.push('/')
  } else {
    router.push('/login')
  }
})
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="text-center">
      <p class="text-lg font-medium text-gray-600">Completing login, please wait...</p>
    </div>
  </div>
</template>
