<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMatchDraftStore, MatchType, type PlayerDto } from '../stores/matchDraftStore'
import { usePlayerGroupStore } from '@/features/group/stores/usePlayerGroupStore'
import AvatarBase from '@/components/AvatarBase.vue'
import PlayerSearchOverlay from './PlayerSearchOverlay.vue'
import PlayerGroupModal from '@/features/group/components/PlayerGroupModal.vue'

defineOptions({
  name: 'PlayerSelection',
})

let t = (key: string, defaultVal?: string) => defaultVal || key
try {
  const i18n = useI18n()
  if (i18n && i18n.t) {
    t = i18n.t
  }
} catch {
  // fallback for tests
}
const store = useMatchDraftStore()
const playerGroupStore = usePlayerGroupStore()

const maxPlayers = computed(() => (store.matchType === MatchType.ONE_VS_ONE ? 2 : 4))
const isGroupModalOpen = ref(false)
const selectedGroupId = ref<string | null>(null)

onMounted(async () => {
  if (playerGroupStore.groups.length === 0) {
    try {
      await playerGroupStore.fetchGroups()
    } catch {
      // ignore error
    }
  }
})

function getPlayer(id?: string) {
  if (!id) return undefined
  const inOpponents = store.frequentOpponents.find((p: PlayerDto) => p.id === id)
  if (inOpponents) return inOpponents
  for (const group of playerGroupStore.groups) {
    const inGroup = group.members?.find((m) => m.id === id)
    if (inGroup) return inGroup
  }
  return undefined
}

const activeGroup = computed(() => {
  if (!selectedGroupId.value) return null
  return playerGroupStore.getGroupById(selectedGroupId.value) || null
})

const quickPlayers = computed<Array<{ id: string; nickname: string; avatar?: string }>>(() => {
  if (activeGroup.value && activeGroup.value.members) {
    return activeGroup.value.members
  }
  return store.frequentOpponents
})

function toggleGroup(groupId: string) {
  if (selectedGroupId.value === groupId) {
    selectedGroupId.value = null
  } else {
    selectedGroupId.value = groupId
  }
}

async function handleSaveGroup(payload: { name: string; isFavorite: boolean; memberIds: string[] }) {
  try {
    const newGroup = await playerGroupStore.createGroup(payload)
    selectedGroupId.value = newGroup.id
    isGroupModalOpen.value = false
  } catch {
    // error handled in modal
  }
}
</script>

<template>
  <div class="flex flex-col gap-2 w-full mt-6">
    <div class="flex justify-between items-center mb-2">
      <h2 class="text-on-surface font-headline font-bold text-lg">
        {{ t('match.players', 'Players') }}
      </h2>
      <!-- Create Group Inline CTA -->
      <button
        type="button"
        @click="isGroupModalOpen = true"
        class="text-xs font-bold text-primary hover:opacity-80 flex items-center gap-1 cursor-pointer transition-opacity"
        data-testid="create-group-inline-btn"
      >
        <span class="material-symbols-outlined text-sm">group_add</span>
        <span>{{ t('groups.createGroup', '+ Group') }}</span>
      </button>
    </div>

    <!-- Player Group Chips -->
    <div
      v-if="playerGroupStore.groups.length > 0"
      class="flex gap-2 overflow-x-auto pb-2 mb-1 no-scrollbar select-none"
      data-testid="player-group-chips"
    >
      <button
        v-for="group in playerGroupStore.groups"
        :key="group.id"
        type="button"
        @click="toggleGroup(group.id)"
        class="px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all flex items-center gap-1 cursor-pointer"
        :class="
          selectedGroupId === group.id
            ? 'bg-primary text-background shadow-md'
            : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
        "
        :data-testid="`group-chip-${group.id}`"
      >
        <span v-if="group.isFavorite" class="material-symbols-outlined text-xs text-yellow-400">star</span>
        <span v-else class="material-symbols-outlined text-xs">groups</span>
        <span>{{ group.name }}</span>
      </button>
    </div>

    <!-- Player Slots -->
    <div
      v-for="index in maxPlayers"
      :key="index"
      class="player-slot h-16 flex items-center px-4 bg-surface-container-highest rounded-xl gap-4 mb-2"
    >
      <div v-if="maxPlayers === 4" class="w-4 text-center font-bold text-on-surface-variant text-sm">
        {{ index % 2 !== 0 ? 'D' : 'A' }}
      </div>
      <div class="w-10 h-10 rounded-full bg-surface-container-low flex items-center justify-center overflow-hidden">
        <span v-if="!store.selectedPlayers[index - 1]" class="text-on-surface-variant font-bold">{{ index }}</span>
        <AvatarBase
          v-else
          :avatar="getPlayer(store.selectedPlayers[index - 1])?.avatar"
          :name="getPlayer(store.selectedPlayers[index - 1])?.nickname"
          shape="circle"
        />
      </div>
      <span class="text-on-surface flex-1 truncate">
        {{
          store.selectedPlayers[index - 1]
            ? getPlayer(store.selectedPlayers[index - 1])?.nickname || `Player ${store.selectedPlayers[index - 1]}`
            : t('match.selectPlayer', 'Select Player')
        }}
      </span>
      <button
        v-if="store.selectedPlayers[index - 1]"
        type="button"
        @click="store.removePlayer(store.selectedPlayers[index - 1]!)"
        class="text-error font-bold px-2 cursor-pointer"
      >
        X
      </button>
      <button
        v-else
        type="button"
        @click="store.openSearch()"
        class="text-on-surface-variant hover:text-primary transition-colors px-2 cursor-pointer"
        data-testid="open-search-button"
        aria-label="Search for player"
      >
        <span class="material-symbols-outlined">search</span>
      </button>
    </div>

    <!-- Quick Player Selection (Filtered by group or Frequent Opponents) -->
    <div
      v-if="quickPlayers.length > 0 && store.selectedPlayers.length < maxPlayers"
      class="mt-4"
    >
      <h3 class="text-on-surface-variant font-bold text-sm mb-2">
        {{
          activeGroup
            ? activeGroup.name
            : t('match.frequentOpponents', 'Frequent Opponents')
        }}
      </h3>
      <div class="flex gap-2 overflow-x-auto pb-2">
        <button
          v-for="opponent in quickPlayers"
          :key="opponent.id"
          type="button"
          @click="store.addPlayer(opponent.id)"
          :disabled="store.selectedPlayers.includes(opponent.id)"
          class="flex flex-col items-center gap-1 min-w-[72px] opacity-100 disabled:opacity-50 cursor-pointer"
        >
          <div class="w-12 h-12 rounded-full bg-surface-container-highest overflow-hidden">
            <AvatarBase
              :avatar="opponent.avatar"
              :name="opponent.nickname"
              shape="circle"
            />
          </div>
          <span class="text-xs text-on-surface truncate w-full text-center">{{ opponent.nickname }}</span>
        </button>
      </div>
    </div>

    <PlayerSearchOverlay :is-open="store.isSearchOpen" @close="store.closeSearch()" />

    <!-- Inline Group Modal -->
    <PlayerGroupModal
      v-model="isGroupModalOpen"
      @save="handleSaveGroup"
    />
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
