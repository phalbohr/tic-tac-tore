<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

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
</script>

<template>
  <div class="py-12 px-4 flex flex-col items-center justify-center">
    <div class="max-w-2xl w-full bg-white rounded-2xl shadow-xl p-8 border border-gray-100">
      <div class="flex items-center justify-between mb-8">
        <h1 class="text-2xl font-black text-indigo-600 uppercase tracking-tighter">
          Dev Token Panel
        </h1>
      </div>

      <div v-if="authStore.isAuthenticated" class="space-y-4 animate-in fade-in duration-500">
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
        <p class="text-gray-500">Please use the login buttons in the navigation bar to see your token.</p>
      </div>
    </div>

    <p class="mt-8 text-xs text-gray-400 font-medium text-center">
      Debug Mode: Active • Protocol: Conductor Phase 3
    </p>
  </div>
</template>
