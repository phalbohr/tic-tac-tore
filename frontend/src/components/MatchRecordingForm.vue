<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useAuthStore, type User } from '@/stores/auth'

/**
 * MatchRecordingForm component for player selection in a 2v2 match.
 * Creator (current user) is automatically included as a participant.
 */

interface Props {
  availableUsers?: User[]
}

const props = withDefaults(defineProps<Props>(), {
  availableUsers: () => []
})

const authStore = useAuthStore()

const form = reactive({
  teammateId: null as number | null,
  opponent1Id: null as number | null,
  opponent2Id: null as number | null
})

const selectedIds = computed(() => {
  const currentUserId = authStore.user?.id
  const ids = [
    currentUserId,
    form.teammateId,
    form.opponent1Id,
    form.opponent2Id
  ]
  return ids.filter((id): id is number => id != null)
})

const isFormValid = computed(() => {
  return authStore.user !== null && 
         form.teammateId !== null && 
         form.opponent1Id !== null && 
         form.opponent2Id !== null
})

const fields = computed(() => [
  { id: 'teammate', label: 'Your Teammate', key: 'teammateId', placeholder: 'Select teammate...' },
  { id: 'opponent1', label: 'Opponent 1', key: 'opponent1Id', placeholder: 'Select opponent 1...' },
  { id: 'opponent2', label: 'Opponent 2', key: 'opponent2Id', placeholder: 'Select opponent 2...' }
] as const)

const getAvailableFor = (currentId: number | null) => {
  return props.availableUsers.filter(user => 
    user.id === currentId || !selectedIds.value.includes(user.id)
  )
}

const emit = defineEmits<{
  (e: 'submit', data: { teammateId: number, opponent1Id: number, opponent2Id: number }): void
}>()

const handleSubmit = () => {
  if (isFormValid.value) {
    emit('submit', {
      teammateId: form.teammateId!,
      opponent1Id: form.opponent1Id!,
      opponent2Id: form.opponent2Id!
    })
  }
}
</script>

<template>
  <div class="max-w-md mx-auto bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
    <h2 class="text-xl font-black text-gray-900 uppercase tracking-tight mb-6 flex items-center gap-2">
      <span class="p-2 bg-indigo-100 text-indigo-600 rounded-lg text-sm">🏓</span>
      Record New Match
    </h2>

    <form @submit.prevent="handleSubmit" class="space-y-6">
      <div v-for="field in fields" :key="field.id">
        <label :for="field.id" class="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">
          {{ field.label }}
        </label>
        <div class="relative">
          <select
            :id="field.id"
            v-model="form[field.key]"
            class="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 appearance-none pr-10"
          >
            <option :value="null">{{ field.placeholder }}</option>
            <option v-for="user in getAvailableFor(form[field.key])" :key="user.id" :value="user.id">
              {{ user.name }}
            </option>
          </select>
          <div class="absolute inset-y-0 right-0 flex items-center px-3 pointer-events-none text-gray-400">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </div>
        </div>
      </div>

      <button
        type="submit"
        :disabled="!isFormValid"
        class="w-full py-4 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-200 disabled:text-gray-400 text-white font-black rounded-xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-200 mt-4 cursor-pointer disabled:cursor-not-allowed"
      >
        Continue to Scores
      </button>
    </form>
  </div>
</template>
