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
    const response = await fetch(`${API_BASE_URL}/matches/pending`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    if (!response.ok) {
      throw new Error(`Failed to fetch pending matches: ${response.status}`)
    }
    const data = await response.json()
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
    pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
  } catch (err: any) {
    error.value = err.message
  }
}

onMounted(fetchPendingMatches)
</script>

<template>
  <div class="py-12 px-4">
    <div class="max-w-4xl mx-auto">
      <div class="mb-8 flex justify-between items-center bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <h1 class="text-2xl font-black text-gray-900 uppercase tracking-tighter">
          Match Approvals
        </h1>
      </div>

      <div v-if="loading" class="text-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
        <p class="text-gray-500 font-bold uppercase tracking-widest text-xs">Loading pending matches...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 p-6 rounded-2xl border border-red-100 text-center">
        <p class="text-red-600 font-bold mb-4">{{ error }}</p>
        <button 
          @click="fetchPendingMatches" 
          class="px-6 py-2 bg-red-600 text-white rounded-xl font-black uppercase text-xs tracking-widest active:scale-95 transition-all"
        >
          Retry
        </button>
      </div>

      <div v-else class="animate-in fade-in duration-500">
        <PendingApprovals 
          :matches="pendingMatches"
          @approve="handleApprove"
          @reject="handleReject"
        />
      </div>
    </div>
  </div>
</template>
