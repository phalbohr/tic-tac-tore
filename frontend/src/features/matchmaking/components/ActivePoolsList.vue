<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePoolStore } from '../stores/poolStore'
import { useAuthStore } from '@/stores/auth'
import AvatarBase from '@/components/AvatarBase.vue'
import BaseButton from '@/core/components/BaseButton.vue'
import type { PoolResponse, SkillLevel } from '../types/pool'

const emit = defineEmits<{
  (e: 'joined', pool: PoolResponse): void
}>()

const { t, locale } = useI18n()
const poolStore = usePoolStore()
const authStore = useAuthStore()

const joiningPoolId = ref<string | null>(null)
const initialLoading = ref(true)
const fetchError = ref<string | null>(null)
const joinError = ref<string | null>(null)

async function loadPools() {
  fetchError.value = null
  try {
    await poolStore.fetchActivePools()
  } catch (err: any) {
    fetchError.value =
      err?.message || poolStore.error || t('pool.fetchFailed', 'Failed to load matchmaking pools')
  } finally {
    initialLoading.value = false
  }
}

onMounted(() => {
  loadPools()
})

function isUserParticipant(pool: PoolResponse): boolean {
  if (!authStore.profile?.id) return false
  return pool.participants.some((p) => p.userId === authStore.profile?.id)
}

function getHostParticipant(pool: PoolResponse) {
  return pool.participants.find((p) => p.role === 'HOST') || pool.participants[0]
}

function formatScheduledTime(dateStr?: string | null): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString(locale.value, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getSkillLevelLabel(skill: SkillLevel): string {
  switch (skill) {
    case 'BEGINNER':
      return t('pool.skillBeginner', 'Beginner')
    case 'INTERMEDIATE':
      return t('pool.skillIntermediate', 'Intermediate')
    case 'ADVANCED':
      return t('pool.skillAdvanced', 'Advanced')
    case 'OPEN_FOR_ALL':
    default:
      return t('pool.skillOpenForAll', 'Open for All')
  }
}

async function handleJoin(poolId: string) {
  if (joiningPoolId.value || poolStore.isLoading) {
    return
  }
  joiningPoolId.value = poolId
  joinError.value = null
  try {
    const updated = await poolStore.joinPool(poolId)
    emit('joined', updated)
  } catch (e: any) {
    joinError.value = e?.message || poolStore.error || t('pool.joinFailed', 'Failed to join pool')
  } finally {
    joiningPoolId.value = null
  }
}
</script>

<template>
  <div class="w-full flex flex-col gap-4 text-left" data-testid="active-pools-container">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-bold font-headline text-on-surface">
        {{ t('pool.activePoolsTitle', 'Active Matchmaking Pools') }}
      </h2>
      <span
        v-if="poolStore.activePools.length > 0"
        class="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-primary-container text-on-primary-container"
      >
        {{ poolStore.activePools.length }}
      </span>
    </div>

    <!-- Join Error Banner -->
    <div
      v-if="joinError"
      class="w-full bg-error-container text-on-error-container rounded-xl p-3 text-xs flex items-center justify-between gap-2"
      data-testid="join-error-banner"
    >
      <span>{{ joinError }}</span>
      <button
        type="button"
        class="font-bold underline text-xs ml-2 cursor-pointer"
        @click="joinError = null"
      >
        ✕
      </button>
    </div>

    <!-- Fetch Error Banner -->
    <div
      v-if="fetchError"
      class="w-full bg-error-container text-on-error-container rounded-xl p-3 text-xs flex items-center justify-between gap-2"
      data-testid="fetch-error-banner"
    >
      <span>{{ fetchError }}</span>
      <button
        type="button"
        class="font-bold underline text-xs ml-2 cursor-pointer"
        @click="loadPools"
      >
        {{ t('common.retry', 'Retry') }}
      </button>
    </div>

    <!-- Loading State (Prevents Flickering Empty State) -->
    <div
      v-if="initialLoading && poolStore.activePools.length === 0"
      class="w-full bg-surface-container-low rounded-2xl p-6 text-center shadow-sm flex items-center justify-center py-8"
      data-testid="loading-pools-state"
    >
      <div class="flex items-center gap-2 text-sm text-on-surface-variant font-medium">
        <span class="animate-spin">⏳</span>
        <span>{{ t('pool.loadingPools', 'Loading matchmaking pools...') }}</span>
      </div>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="poolStore.activePools.length === 0"
      class="w-full bg-surface-container-low rounded-2xl p-6 text-center shadow-sm flex flex-col items-center gap-2"
      data-testid="empty-pools-state"
    >
      <div
        class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center text-xl mb-1"
      >
        ⚽
      </div>
      <p class="text-sm text-on-surface-variant font-medium">
        {{ t('pool.noActivePools', 'No open pools right now. Start one with Want to Play!') }}
      </p>
    </div>

    <!-- Pool Cards List -->
    <div v-else class="w-full flex flex-col gap-3">
      <div
        v-for="pool in poolStore.activePools"
        :key="pool.id"
        class="w-full bg-surface-container-low rounded-2xl p-4 shadow-sm flex flex-col gap-3 transition-all hover:bg-surface-container"
        :data-testid="`pool-card-${pool.id}`"
      >
        <!-- Header: Creator Info & Format / Skill Badges -->
        <div class="flex items-center justify-between gap-2">
          <div class="flex items-center gap-3 min-w-0">
            <div
              class="w-10 h-10 rounded-xl overflow-hidden bg-surface-container-high flex-shrink-0"
            >
              <AvatarBase
                :avatar="getHostParticipant(pool)?.avatar"
                :name="pool.creatorNickname"
                shape="square"
              />
            </div>
            <div class="flex flex-col min-w-0">
              <span class="font-bold text-sm text-on-surface truncate">
                {{ pool.creatorNickname }}
              </span>
              <span class="text-xs text-on-surface-variant">
                {{
                  pool.startCondition === 'FILL_BASED'
                    ? t('pool.fillBased', 'Immediate')
                    : formatScheduledTime(pool.scheduledTime)
                }}
              </span>
            </div>
          </div>

          <!-- Format & Skill Badges -->
          <div class="flex items-center gap-1.5 flex-wrap justify-end">
            <span
              class="px-2 py-0.5 rounded-lg text-xs font-bold bg-secondary-container text-on-secondary-container"
            >
              {{ pool.matchType === 'ONE_VS_ONE' ? '1v1' : '2v2' }}
            </span>
            <span
              class="px-2 py-0.5 rounded-lg text-xs font-medium bg-surface-container-high text-on-surface-variant"
            >
              {{ getSkillLevelLabel(pool.skillLevel) }}
            </span>
          </div>
        </div>

        <!-- Participants Slots & Action Area -->
        <div class="flex items-center justify-between gap-4 pt-1">
          <!-- Slots visualization -->
          <div class="flex items-center gap-2">
            <div class="flex items-center -space-x-2">
              <div
                v-for="participant in pool.participants"
                :key="participant.userId"
                class="w-8 h-8 rounded-full border-2 border-surface-container-low overflow-hidden bg-surface-container-high flex-shrink-0"
                :title="participant.nickname"
              >
                <AvatarBase
                  :avatar="participant.avatar"
                  :name="participant.nickname"
                  shape="circle"
                />
              </div>
              <div
                v-for="idx in Math.max(0, pool.requiredPlayers - pool.participants.length)"
                :key="`open-slot-${idx}`"
                class="w-8 h-8 rounded-full border-2 border-surface-container-low bg-surface-container-highest flex items-center justify-center text-xs text-on-surface-variant/50 font-bold"
              >
                +
              </div>
            </div>
            <span class="text-xs font-semibold text-on-surface-variant ml-1">
              {{ pool.currentPlayers }}/{{ pool.requiredPlayers }}
            </span>
          </div>

          <!-- Action Button / Joined State -->
          <div>
            <span
              v-if="isUserParticipant(pool)"
              class="px-3 py-1.5 rounded-full text-xs font-bold bg-primary/15 text-primary flex items-center gap-1"
              :data-testid="`joined-pool-badge-${pool.id}`"
            >
              ✓ {{ t('pool.joined', 'Joined') }}
            </span>

            <BaseButton
              v-else-if="pool.status === 'OPEN' && pool.currentPlayers < pool.requiredPlayers"
              variant="primary"
              class="!h-9 !px-4 text-xs font-bold rounded-full"
              :disabled="Boolean(joiningPoolId) || poolStore.isLoading"
              :data-testid="`join-pool-btn-${pool.id}`"
              @click="handleJoin(pool.id)"
            >
              <span v-if="joiningPoolId === pool.id" class="flex items-center gap-1.5">
                <span class="animate-spin text-xs">⏳</span>
                {{ t('pool.joining', 'Joining...') }}
              </span>
              <span v-else>
                {{ t('pool.join', 'Join') }}
              </span>
            </BaseButton>

            <span
              v-else
              class="px-3 py-1.5 rounded-full text-xs font-medium bg-surface-container-highest text-on-surface-variant"
            >
              {{ t('pool.full', 'Full') }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
