<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import PendingApprovals from '@/components/PendingApprovals.vue'

interface Game {
  gameNumber: number
  teamAScore: number
  teamBScore: number
}

interface Match {
  id: string
  creatorName: string
  teamAAttackerName: string
  teamADefenderName: string
  teamBAttackerName: string
  teamBDefenderName: string
  status: string
  createdAt: string
  games: Game[]
}

const API_BASE_URL = 'http://localhost:8080/api/v1'
const authStore = useAuthStore()
const pendingMatches = ref<Match[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const fetchPendingMatches = async () => {
  loading.value = true
  error.value = null
  try {
    console.log('DEBUG: Current User ID:', authStore.user?.id)
    console.log('Fetching pending matches for user token...', authStore.token?.substring(0, 10) + '...')
    const response = await fetch(`${API_BASE_URL}/matches/pending`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    if (!response.ok) {
      const errBody = await response.text()
      console.error('Fetch failed:', response.status, errBody)
      throw new Error(`Failed to fetch pending matches: ${response.status}`)
    }
    const data = await response.json()
    console.log('Pending matches received:', data)
    pendingMatches.value = data
  } catch (err: any) {
    console.error('Fetch error:', err)
    error.value = err.message
  } finally {
    loading.value = false
  }
}

const handleApprove = async (matchId: string) => {
  error.value = null
  try {
    const response = await fetch(`${API_BASE_URL}/matches/${matchId}/approve`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    if (!response.ok) throw new Error('Failed to approve match')
    // Remove from local list
    pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
  } catch (err: any) {
    error.value = err.message
  }
}

const handleReject = async (matchId: string) => {
  error.value = null
  try {
    const response = await fetch(`${API_BASE_URL}/matches/${matchId}/reject`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    if (!response.ok) throw new Error('Failed to reject match')
    // Remove from local list
    pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
  } catch (err: any) {
    error.value = err.message
  }
}

onMounted(fetchPendingMatches)
</script>

<template>
  <div class="min-h-screen bg-gray-50 py-12 px-4 font-sans">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <h1 class="text-2xl font-black text-gray-900 uppercase tracking-tighter">
          Match Approvals
        </h1>
        <router-link
          to="/dev-recording"
          class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold rounded-lg transition-all active:scale-95 text-xs uppercase tracking-wider flex items-center"
        >
          ← Back to Recorder
        </router-link>
      </div>

      <div v-if="loading" class="flex justify-center items-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>

      <div v-else class="animate-in fade-in duration-500 space-y-4">
        <div v-if="error" class="bg-red-50 border border-red-200 rounded-2xl p-4 flex justify-between items-center">
          <p class="text-red-600 text-sm font-bold">{{ error }}</p>
          <button 
            @click="fetchPendingMatches"
            class="text-xs bg-red-600 text-white px-3 py-1 rounded-lg font-black uppercase tracking-wider"
          >
            Retry
          </button>
        </div>

        <PendingApprovals 
          :matches="pendingMatches"
          @approve="handleApprove"
          @reject="handleReject"
        />
      </div>
    </div>
  </div>
</template>
