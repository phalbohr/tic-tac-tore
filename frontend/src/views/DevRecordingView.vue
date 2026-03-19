<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import MatchRecordingForm from '@/components/MatchRecordingForm.vue'
import MatchScoring from '@/components/MatchScoring.vue'
import type { User } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const showToken = ref(false)
const copyStatus = ref('Copy to clipboard')

const step = ref(1) // 1: Player Selection, 2: Scoring
const players = ref<{
  creator: User,
  teammate: User,
  opponent1: User,
  opponent2: User
} | null>(null)

// Mock users for development
const availableUsers: User[] = [
  { id: 1, name: 'Alice', email: 'alice@example.com' },
  { id: 2, name: 'Bob', email: 'bob@example.com' },
  { id: 3, name: 'Charlie', email: 'charlie@example.com' },
  { id: 4, name: 'David', email: 'david@example.com' },
  { id: 5, name: 'Eve', email: 'eve@example.com' }
]

const devLogin = () => {
  authStore.login('dev-mock-token', {
    id: 10,
    name: 'Dev User',
    email: 'dev@example.com'
  })
}

// Auto-login in dev mode if not authenticated
onMounted(() => {
  if (!authStore.isAuthenticated) {
    devLogin()
  }
})

const handleFormSubmit = (data: { teammateId: number, opponent1Id: number, opponent2Id: number }) => {
  const teammate = availableUsers.find(u => u.id === data.teammateId)!
  const opponent1 = availableUsers.find(u => u.id === data.opponent1Id)!
  const opponent2 = availableUsers.find(u => u.id === data.opponent2Id)!
  
  players.value = {
    creator: authStore.user!,
    teammate,
    opponent1,
    opponent2
  }
  step.value = 2
}

const handleFinish = (games: any) => {
  console.log('Match Finished:', games)
  alert('Match recorded locally! Data printed to console.')
  step.value = 1
  players.value = null
}

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
  <div class="min-h-screen bg-gray-50 py-12 px-4 font-sans">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <div>
          <h1 class="text-2xl font-black text-gray-900 uppercase tracking-tighter">
            Match Recording Dev Tool
          </h1>
          <div v-if="authStore.isAuthenticated" class="flex items-center gap-2 mt-1">
            <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
            <span class="text-xs font-bold text-gray-500 uppercase tracking-widest">
              Logged in as {{ authStore.user?.name }}
            </span>
          </div>
        </div>
        
        <div class="flex gap-2">
          <template v-if="authStore.isAuthenticated">
            <button
              @click="showToken = !showToken"
              class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold rounded-lg transition-all active:scale-95 text-xs uppercase tracking-wider"
            >
              {{ showToken ? 'Hide Token' : 'Show Token' }}
            </button>
            <button
              @click="authStore.logout()"
              class="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-600 font-bold rounded-lg border border-red-200 transition-all active:scale-95 text-xs uppercase tracking-wider"
            >
              Logout
            </button>
          </template>
          <button
            v-else
            @click="devLogin"
            class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-lg transition-all active:scale-95 text-xs uppercase tracking-wider"
          >
            Mock Login
          </button>
        </div>
      </div>

      <!-- Token Panel -->
      <div v-if="showToken && authStore.isAuthenticated" class="mb-8 bg-white p-6 rounded-2xl shadow-xl border border-indigo-100 animate-in fade-in slide-in-from-top-4 duration-300">
        <div class="relative">
          <textarea
            readonly
            :value="authStore.token"
            rows="2"
            class="w-full p-4 bg-gray-50 border border-gray-200 rounded-xl text-xs font-mono text-gray-700 focus:outline-none break-all resize-none mb-4"
          ></textarea>
          <button
            @click="copyToClipboard"
            class="w-full py-2 bg-gray-900 hover:bg-black text-white font-bold rounded-lg text-xs uppercase tracking-widest transition-all active:scale-[0.98]"
          >
            {{ copyStatus }}
          </button>
        </div>
      </div>

      <!-- Content -->
      <div v-if="!authStore.isAuthenticated" class="text-center py-20 bg-white rounded-3xl border border-gray-100 shadow-sm">
        <div class="text-4xl mb-4">🔐</div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">Authentication Required</h2>
        <p class="text-gray-500 text-sm">Please log in to use the development tools.</p>
      </div>

      <div v-else class="animate-in fade-in duration-500">
        <!-- Form Flow -->
        <div v-if="step === 1">
          <MatchRecordingForm 
            :available-users="availableUsers"
            @submit="handleFormSubmit"
          />
        </div>

        <div v-else-if="step === 2 && players">
          <MatchScoring 
            :players="players"
            @finish="handleFinish"
          />
          <div class="mt-6 text-center">
            <button @click="step = 1" class="text-sm font-bold text-gray-400 hover:text-gray-600 transition-colors">
              ← Back to Player Selection
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
