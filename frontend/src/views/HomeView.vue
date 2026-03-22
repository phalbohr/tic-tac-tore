<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const copyStatus = ref('Copy to clipboard')

const copyToClipboard = async () => {
  if (authStore.token) {
    try {
      await navigator.clipboard.writeText(authStore.token)
      copyStatus.value = 'Copied! ✅'
      setTimeout(() => {
        copyStatus.value = 'Copy to clipboard'
      }, 2000)
    } catch (err) {
      console.error('Failed to copy: ', err)
      copyStatus.value = 'Error! ❌'
    }
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-6">
    <div class="max-w-2xl w-full bg-white rounded-2xl shadow-xl p-8 border border-gray-100">
      <div class="flex items-center justify-between mb-8">
        <h1 class="text-2xl font-black text-indigo-600 uppercase tracking-tighter">
          Dev Token Panel
        </h1>
        <div class="flex gap-2">
          <router-link
            to="/leaderboards"
            class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold rounded-lg transition-all active:scale-95 text-xs uppercase tracking-wider flex items-center"
          >
            Leaderboards
          </router-link>
          <button
            v-if="!authStore.isAuthenticated"
            @click="goToLogin"
            class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-lg transition-all active:scale-95 shadow-md"
          >
            Go to Login
          </button>
          <button
            v-else
            @click="authStore.logout()"
            class="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-600 font-bold rounded-lg border border-red-200 transition-all active:scale-95"
          >
            Logout
          </button>
        </div>
      </div>

      <div v-if="authStore.isAuthenticated" class="space-y-4">
        <div class="relative">
          <label class="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
            Your JWT Token
          </label>
          <textarea
            readonly
            :value="authStore.token"
            rows="8"
            class="w-full p-4 bg-gray-50 border border-gray-200 rounded-xl text-sm font-mono text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 break-all resize-none"
          ></textarea>
        </div>
        
        <button
          @click="copyToClipboard"
          class="w-full py-4 bg-gray-900 hover:bg-black text-white font-black rounded-xl transition-all active:scale-[0.98] shadow-lg flex items-center justify-center gap-2"
        >
          <span>{{ copyStatus }}</span>
        </button>
      </div>

      <div v-else class="text-center py-12">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
          <span class="text-2xl">🔑</span>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">Not Authenticated</h2>
        <p class="text-gray-500">Please login to see and copy your JWT token.</p>
      </div>
    </div>

    <p class="mt-8 text-xs text-gray-400 font-medium">
      Debug Mode: Active • Protocol: Conductor Phase 2
    </p>
  </div>
</template>
