<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePlayerGroupStore } from '@/features/group/stores/usePlayerGroupStore'
import type { PlayerGroupResponse } from '@/services/playerGroupService'
import PlayerGroupModal from '@/features/group/components/PlayerGroupModal.vue'
import AvatarBase from '@/components/AvatarBase.vue'

let t = (key: string, defaultVal?: string) => defaultVal || key
try {
  const i18n = useI18n()
  if (i18n && i18n.t) {
    t = i18n.t
  }
} catch {
  // fallback for tests
}
const store = usePlayerGroupStore()

const isModalOpen = ref(false)
const editingGroup = ref<PlayerGroupResponse | null>(null)
const error = ref('')

onMounted(async () => {
  if (store.groups.length === 0) {
    try {
      await store.fetchGroups()
    } catch {
      // ignore fetch error if unauthenticated
    }
  }
})

function openCreateModal() {
  editingGroup.value = null
  error.value = ''
  isModalOpen.value = true
}

function openEditModal(group: PlayerGroupResponse) {
  editingGroup.value = group
  error.value = ''
  isModalOpen.value = true
}

async function handleSaveGroup(payload: { name: string; isFavorite: boolean; memberIds: string[] }) {
  error.value = ''
  try {
    if (editingGroup.value) {
      await store.updateGroup(editingGroup.value.id, payload)
    } else {
      await store.createGroup(payload)
    }
    isModalOpen.value = false
  } catch (err: any) {
    error.value = err.message || t('common.error', 'An error occurred')
  }
}

async function handleDeleteGroup(id: string) {
  error.value = ''
  try {
    await store.deleteGroup(id)
  } catch (err: any) {
    error.value = err.message || t('common.error', 'An error occurred')
  }
}
</script>

<template>
  <section class="space-y-3">
    <div class="flex justify-between items-center px-1">
      <h2 class="font-headline text-xs font-bold uppercase tracking-widest text-primary/80">
        {{ t('groups.sectionTitle', 'Player Groups') }}
      </h2>
      <button
        type="button"
        @click="openCreateModal"
        data-testid="create-group-button"
        class="text-xs font-bold text-primary hover:opacity-80 flex items-center gap-1 cursor-pointer transition-opacity"
      >
        <span class="material-symbols-outlined text-sm">add_circle</span>
        <span>{{ t('groups.createButton', 'Create Group') }}</span>
      </button>
    </div>

    <div v-if="error" class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold">
      {{ error }}
    </div>

    <div
      class="player-group-list space-y-2"
      data-testid="player-group-list"
    >
      <div
        v-if="store.loading && store.groups.length === 0"
        class="p-4 rounded-xl bg-surface-container-low text-center text-xs text-on-surface-variant flex items-center justify-center gap-2"
      >
        <span class="material-symbols-outlined animate-spin text-sm">sync</span>
        <span>{{ t('common.loading', 'Loading...') }}</span>
      </div>

      <div
        v-else-if="store.groups.length === 0"
        class="p-4 rounded-xl bg-surface-container-low text-center text-xs text-on-surface-variant"
      >
        {{ t('groups.noGroups', 'No player groups yet. Create groups to quickly select teams!') }}
      </div>

      <div
        v-for="group in store.groups"
        :key="group.id"
        :data-testid="`group-item-${group.id}`"
        class="flex items-center justify-between p-3.5 rounded-xl bg-surface-container-low hover:bg-surface-container-highest/60 transition-colors"
      >
        <div class="space-y-1.5 min-w-0 flex-1 mr-2">
          <div class="flex items-center gap-1.5">
            <span v-if="group.isFavorite" class="material-symbols-outlined text-sm text-yellow-400">star</span>
            <span class="font-headline font-bold text-sm text-on-surface truncate">{{ group.name }}</span>
          </div>
          <div v-if="group.members && group.members.length > 0" class="flex items-center gap-1">
            <div class="flex -space-x-1.5 overflow-hidden">
              <div
                v-for="member in group.members.slice(0, 4)"
                :key="member.id"
                class="w-6 h-6 rounded-full bg-surface-container-highest border border-surface-container-low overflow-hidden"
              >
                <AvatarBase :avatar="member.avatar" :name="member.nickname" shape="circle" />
              </div>
            </div>
            <span class="text-[11px] text-on-surface-variant ml-1 font-medium">
              {{ group.members.length }} {{ t('groups.membersCount', 'members') }}
            </span>
          </div>
          <div v-else class="text-[11px] text-on-surface-variant font-medium">
            0 {{ t('groups.membersCount', 'members') }}
          </div>
        </div>

        <div class="flex items-center gap-1 shrink-0">
          <button
            type="button"
            @click="openEditModal(group)"
            :data-testid="`edit-group-${group.id}`"
            class="p-1.5 rounded-lg text-on-surface-variant hover:text-primary hover:bg-surface-container-highest transition-colors cursor-pointer"
            aria-label="Edit group"
          >
            <span class="material-symbols-outlined text-sm">edit</span>
          </button>
          <button
            type="button"
            @click="handleDeleteGroup(group.id)"
            :data-testid="`delete-group-${group.id}`"
            class="p-1.5 rounded-lg text-on-surface-variant hover:text-error hover:bg-surface-container-highest transition-colors cursor-pointer"
            aria-label="Delete group"
          >
            <span class="material-symbols-outlined text-sm">delete</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <PlayerGroupModal
      v-model="isModalOpen"
      :group="editingGroup"
      @save="handleSaveGroup"
    />
  </section>
</template>

<style scoped>
.player-group-list {
  border-top-width: 0px;
  border-bottom-width: 0px;
  border-left-width: 0px;
  border-right-width: 0px;
}
</style>
