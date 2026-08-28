<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { usePoolStore } from '../stores/poolStore';
import { useAuthStore } from '@/stores/auth';
import AvatarBase from '@/components/AvatarBase.vue';
import BaseButton from '@/core/components/BaseButton.vue';
import type { PoolResponse, SkillLevel } from '../types/pool';

const emit = defineEmits<{
  (e: 'joined', pool: PoolResponse): void;
}>();

const { t } = useI18n();
const poolStore = usePoolStore();
const authStore = useAuthStore();

const joiningPoolId = ref<string | null>(null);

onMounted(() => {
  try {
    const res = poolStore.fetchActivePools();
    if (res && typeof res.catch === 'function') {
      res.catch(() => {});
    }
  } catch {
    // Ignore fetch error on mount
  }
});

function isUserParticipant(pool: PoolResponse): boolean {
  if (!authStore.profile?.id) return false;
  return pool.participants.some((p) => p.userId === authStore.profile?.id);
}

function getHostParticipant(pool: PoolResponse) {
  return pool.participants.find((p) => p.role === 'HOST') || pool.participants[0];
}

function formatScheduledTime(dateStr?: string | null): string {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return dateStr;
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function getSkillLevelLabel(skill: SkillLevel): string {
  switch (skill) {
    case 'BEGINNER':
      return t('pool.skillBeginner', 'Beginner');
    case 'INTERMEDIATE':
      return t('pool.skillIntermediate', 'Intermediate');
    case 'ADVANCED':
      return t('pool.skillAdvanced', 'Advanced');
    case 'OPEN_FOR_ALL':
    default:
      return t('pool.skillOpenForAll', 'Open for All');
  }
}

async function handleJoin(poolId: string) {
  joiningPoolId.value = poolId;
  try {
    const updated = await poolStore.joinPool(poolId);
    emit('joined', updated);
  } catch (e) {
    // Error is set in store
  } finally {
    joiningPoolId.value = null;
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

    <!-- Empty State -->
    <div
      v-if="poolStore.activePools.length === 0"
      class="w-full bg-surface-container-low rounded-2xl p-6 text-center shadow-sm flex flex-col items-center gap-2"
      data-testid="empty-pools-state"
    >
      <div class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center text-xl mb-1">
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
            <div class="w-10 h-10 rounded-xl overflow-hidden bg-surface-container-high flex-shrink-0">
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
                {{ pool.startCondition === 'FILL_BASED' ? t('pool.fillBased', 'Immediate') : formatScheduledTime(pool.scheduledTime) }}
              </span>
            </div>
          </div>

          <!-- Format & Skill Badges -->
          <div class="flex items-center gap-1.5 flex-wrap justify-end">
            <span class="px-2 py-0.5 rounded-lg text-xs font-bold bg-secondary-container text-on-secondary-container">
              {{ pool.matchType === 'ONE_VS_ONE' ? '1v1' : '2v2' }}
            </span>
            <span class="px-2 py-0.5 rounded-lg text-xs font-medium bg-surface-container-high text-on-surface-variant">
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
              :disabled="joiningPoolId === pool.id"
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
