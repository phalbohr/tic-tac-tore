<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { PlayerGroupResponse, PlayerSummaryDto } from '@/services/playerGroupService'
import type { PlayerDto } from '@/features/match/stores/matchDraftStore'
import AvatarBase from '@/components/AvatarBase.vue'
import PlayerSearchOverlay from '@/features/match/components/PlayerSearchOverlay.vue'

interface Props {
  modelValue: boolean
  group?: PlayerGroupResponse | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', payload: { name: string; isFavorite: boolean; memberIds: string[] }): void
}>()

let t = (key: string, defaultVal?: string) => defaultVal || key
try {
  const i18n = useI18n()
  if (i18n && i18n.t) {
    t = i18n.t
  }
} catch {
  // fallback for tests
}

const name = ref('')
const isFavorite = ref(false)
const members = ref<PlayerSummaryDto[]>([])
const isPlayerSearchOpen = ref(false)
const error = ref('')

const isEdit = computed(() => !!props.group)

watch(
  () => props.group,
  (newGroup) => {
    if (newGroup) {
      name.value = newGroup.name
      isFavorite.value = newGroup.isFavorite
      members.value = [...(newGroup.members || [])]
    } else {
      name.value = ''
      isFavorite.value = false
      members.value = []
    }
    error.value = ''
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen && !props.group) {
      name.value = ''
      isFavorite.value = false
      members.value = []
      error.value = ''
    }
  }
)

function handleClose() {
  emit('update:modelValue', false)
}

function handleAddPlayer(player: PlayerDto) {
  if (!members.value.some((m) => m.id === player.id)) {
    members.value.push({
      id: player.id,
      nickname: player.nickname,
      avatar: player.avatar,
    })
  }
  isPlayerSearchOpen.value = false
}

function handleRemoveMember(id: string) {
  members.value = members.value.filter((m) => m.id !== id)
}

function handleSubmit() {
  const trimmedName = name.value.trim()
  if (!trimmedName) {
    error.value = t('groups.nameRequired', 'Group name is required')
    return
  }
  if (trimmedName.length > 50) {
    error.value = t('groups.nameTooLong', 'Group name cannot exceed 50 characters')
    return
  }
  emit('save', {
    name: trimmedName,
    isFavorite: isFavorite.value,
    memberIds: members.value.map((m) => m.id),
  })
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md"
      role="dialog"
      aria-modal="true"
    >
      <div
        class="w-full max-w-md bg-surface-container-low rounded-2xl p-6 space-y-5 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]"
      >
        <div class="flex justify-between items-center">
          <h2 class="font-headline text-lg font-bold text-on-surface">
            {{ isEdit ? t('groups.editTitle', 'Edit Group') : t('groups.createTitle', 'Create Group') }}
          </h2>
          <button
            type="button"
            @click="handleClose"
            data-testid="group-cancel-btn"
            class="text-on-surface-variant hover:text-on-surface p-1 rounded-lg transition-colors cursor-pointer"
            aria-label="Close"
          >
            <span class="material-symbols-outlined text-xl">close</span>
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4 flex-grow overflow-y-auto pr-1">
          <div v-if="error" class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold">
            {{ error }}
          </div>

          <!-- Group Name Input -->
          <div class="space-y-1">
            <label for="group-name" class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80">
              {{ t('groups.nameLabel', 'Group Name') }}
            </label>
            <input
              id="group-name"
              v-model="name"
              type="text"
              maxlength="50"
              required
              :placeholder="t('groups.namePlaceholder', 'e.g. Tuesday Squad')"
              data-testid="group-name-input"
              class="w-full bg-surface-container-highest text-on-surface px-4 py-3 rounded-xl font-headline text-sm focus:outline-none focus:ring-2 focus:ring-primary transition-all placeholder:text-on-surface-variant/50"
            />
          </div>

          <!-- Favorites Checkbox -->
          <div class="flex items-center gap-3 p-3 bg-surface-container-highest/50 rounded-xl">
            <input
              id="is-favorite"
              v-model="isFavorite"
              type="checkbox"
              data-testid="group-favorite-checkbox"
              class="w-4 h-4 rounded text-primary focus:ring-primary accent-primary cursor-pointer"
            />
            <label for="is-favorite" class="font-headline text-xs font-medium text-on-surface cursor-pointer select-none">
              {{ t('groups.isFavoriteLabel', 'Mark as Favorites') }}
            </label>
          </div>

          <!-- Members List -->
          <div class="space-y-2">
            <div class="flex justify-between items-center">
              <span class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80">
                {{ t('groups.membersLabel', 'Members') }} ({{ members.length }})
              </span>
              <button
                type="button"
                @click="isPlayerSearchOpen = true"
                class="text-xs font-bold text-primary hover:opacity-80 flex items-center gap-1 cursor-pointer"
                data-testid="add-member-btn"
              >
                <span class="material-symbols-outlined text-sm">person_add</span>
                <span>{{ t('groups.addMember', 'Add Member') }}</span>
              </button>
            </div>

            <div v-if="members.length === 0" class="p-4 rounded-xl bg-surface-container-highest/30 text-center text-xs text-on-surface-variant">
              {{ t('groups.noMembers', 'No members added yet') }}
            </div>

            <div v-else class="space-y-1.5 max-h-40 overflow-y-auto">
              <div
                v-for="member in members"
                :key="member.id"
                class="flex items-center justify-between p-2 rounded-xl bg-surface-container-highest/70"
              >
                <div class="flex items-center gap-2.5 min-w-0">
                  <div class="w-8 h-8 rounded-full bg-surface-container-low overflow-hidden shrink-0">
                    <AvatarBase :avatar="member.avatar" :name="member.nickname" shape="circle" />
                  </div>
                  <span class="text-xs font-medium text-on-surface truncate">{{ member.nickname }}</span>
                </div>
                <button
                  type="button"
                  @click="handleRemoveMember(member.id)"
                  class="text-error hover:opacity-80 p-1 cursor-pointer"
                  aria-label="Remove member"
                >
                  <span class="material-symbols-outlined text-sm">delete</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex gap-2 pt-2">
            <button
              type="submit"
              data-testid="group-save-btn"
              class="flex-1 py-3 rounded-xl bg-primary text-background font-headline font-bold text-xs uppercase tracking-wider hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-1 cursor-pointer"
            >
              <span>{{ t('common.save', 'Save') }}</span>
            </button>
            <button
              type="button"
              @click="handleClose"
              class="px-4 py-3 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold text-xs transition-colors cursor-pointer"
            >
              {{ t('common.cancel', 'Cancel') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>

  <!-- Player Search Overlay for Member Selection -->
  <PlayerSearchOverlay
    :is-open="isPlayerSearchOpen"
    :custom-select="true"
    @select="handleAddPlayer"
    @close="isPlayerSearchOpen = false"
  />
</template>
