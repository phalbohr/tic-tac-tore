<script setup lang="ts">
/**
 * PendingApprovals component for viewing and confirming matches where the user is an opponent.
 */

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

interface Props {
  matches?: Match[]
}

const props = withDefaults(defineProps<Props>(), {
  matches: () => []
})

const emit = defineEmits<{
  (e: 'approve', matchId: string): void
  (e: 'reject', matchId: string): void
}>()

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <div class="max-w-md mx-auto space-y-4">
    <h2 class="text-xl font-black text-gray-900 uppercase tracking-tight mb-6 flex items-center gap-2">
      <span class="p-2 bg-yellow-100 text-yellow-600 rounded-lg text-sm">⏳</span>
      Pending Approvals
    </h2>

    <div v-if="props.matches.length === 0" class="bg-gray-50 border-2 border-dashed border-gray-200 rounded-2xl p-8 text-center">
      <p class="text-gray-400 font-medium">No pending approvals</p>
    </div>

    <div v-for="match in props.matches" :key="match.id" class="bg-white rounded-2xl shadow-xl p-6 border border-gray-100 overflow-hidden">
      <div class="flex justify-between items-start mb-4">
        <div>
          <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">Created by {{ match.creatorName }}</p>
          <p class="text-sm font-medium text-gray-600">{{ formatDate(match.createdAt) }}</p>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="p-3 bg-indigo-50 rounded-xl">
          <p class="text-[10px] font-black text-indigo-400 uppercase tracking-widest mb-1">Team A</p>
          <p class="text-sm font-bold text-indigo-900 truncate">{{ match.teamAAttackerName }} & {{ match.teamADefenderName }}</p>
        </div>
        <div class="p-3 bg-emerald-50 rounded-xl">
          <p class="text-[10px] font-black text-emerald-400 uppercase tracking-widest mb-1">Team B (You)</p>
          <p class="text-sm font-bold text-emerald-900 truncate">{{ match.teamBAttackerName }} & {{ match.teamBDefenderName }}</p>
        </div>
      </div>

      <div class="space-y-2 mb-6 border-y border-gray-50 py-4">
        <div v-for="game in match.games" :key="game.gameNumber" class="flex items-center justify-between px-2">
          <span class="text-xs font-bold text-gray-400">Game {{ game.gameNumber }}</span>
          <div class="flex items-center gap-4">
            <span class="text-sm font-black text-indigo-600">{{ game.teamAScore }}</span>
            <span class="text-xs font-bold text-gray-300"> - </span>
            <span class="text-sm font-black text-emerald-600">{{ game.teamBScore }}</span>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <button
          @click="emit('reject', match.id)"
          class="reject-btn py-3 bg-red-50 hover:bg-red-100 text-red-600 font-black rounded-xl transition-all active:scale-[0.98] cursor-pointer"
        >
          Reject
        </button>
        <button
          @click="emit('approve', match.id)"
          class="approve-btn py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-black rounded-xl transition-all active:scale-[0.98] shadow-lg shadow-emerald-200 cursor-pointer"
        >
          Approve
        </button>
      </div>
    </div>
  </div>
</template>
