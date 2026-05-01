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

const seedData = ref<any>(null)
const availableUsers = ref<User[]>([])

const handleSeed = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/dev/seed', { method: 'POST' })
    const data = await response.json()
    seedData.value = data
    availableUsers.value = Object.values(data).map((u: any) => ({
      id: String(u.id),
      name: u.name,
      email: u.email
    }))
  } catch (err) {
    console.error('Seed failed:', err)
  }
}

const loginAs = (key: 'me' | 'opp1') => {
  if (!seedData.value) return
  const u = seedData.value[key]
  authStore.login(u.token, {
    id: String(u.id),
    name: u.name,
    email: u.email
  })
}

onMounted(handleSeed)

const handleFormSubmit = (data: { teammateId: string, opponent1Id: string, opponent2Id: string }) => {
  const teammate = availableUsers.value.find(u => u.id === data.teammateId)!
  const opponent1 = availableUsers.value.find(u => u.id === data.opponent1Id)!
  const opponent2 = availableUsers.value.find(u => u.id === data.opponent2Id)!
  
  players.value = {
    creator: authStore.user!,
    teammate,
    opponent1,
    opponent2
  }
  step.value = 2
}

const handleFinish = async (games: any) => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/matches', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        teamAAttackerId: players.value!.creator.id,
        teamADefenderId: players.value!.teammate.id,
        teamBAttackerId: players.value!.opponent1.id,
        teamBDefenderId: players.value!.opponent2.id,
        games: games.map((g: any) => ({
          teamAScore: g.team1Score,
          teamBScore: g.team2Score
        }))
      })
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.error || 'Failed to record match')
    }
    
    alert('Match recorded successfully! Now logout and login as Account B to approve.')
    step.value = 1
    players.value = null
  } catch (err: any) {
    alert(err.message)
  }
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
  <div class="py-12 px-4">
    <div class="max-w-4xl mx-auto">
      <!-- Token Panel (Optional here, can stay if useful for dev) -->
      <div v-if="showToken && authStore.isAuthenticated" class="mb-8 bg-white p-6 rounded-2xl shadow-xl border border-indigo-100 animate-in fade-in slide-in-from-top-4 duration-300">
        <div class="relative">
          <label class="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Your Current JWT Token</label>
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
        <p class="text-gray-500 text-sm">Please use the login buttons in the navigation bar.</p>
      </div>

      <div v-else class="animate-in fade-in duration-500">
        <div class="mb-8 flex justify-between items-center bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h2 class="text-xl font-black text-gray-900 uppercase tracking-tight">Record New Match</h2>
          <button @click="showToken = !showToken" class="text-[10px] font-black text-indigo-600 uppercase tracking-widest hover:underline">
            {{ showToken ? 'Hide Debug Token' : 'Show Debug Token' }}
          </button>
        </div>

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
