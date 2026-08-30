<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useChallengeStore } from '@/features/challenge/stores/useChallengeStore'
import type { ChallengeItem } from '@/services/challengeService'
import AvatarBase from '@/components/AvatarBase.vue'

const emit = defineEmits<{
  (e: 'challengeAccepted', challenge: ChallengeItem): void
  (e: 'challengeDeclined', challenge: ChallengeItem): void
  (e: 'challengeCancelled', challenge: ChallengeItem): void
}>()

const { t } = useI18n()
const router = useRouter()
const challengeStore = useChallengeStore()

const activeTab = ref<'incoming' | 'outgoing'>('incoming')
const processingId = ref<string | null>(null)
const actionError = ref<string | null>(null)

const incomingList = computed(() => challengeStore.incomingChallenges)
const outgoingList = computed(() => challengeStore.outgoingChallenges)

onMounted(async () => {
  try {
    await Promise.all([
      challengeStore.fetchIncoming(),
      challengeStore.fetchOutgoing(),
    ])
  } catch (err) {
    console.error('Failed to load challenges', err)
  }
})

async function handleAccept(challenge: ChallengeItem) {
  processingId.value = challenge.id
  actionError.value = null
  try {
    await challengeStore.acceptChallenge(challenge.id)
    emit('challengeAccepted', challenge)
  } catch (err: any) {
    actionError.value = err.message || t('challenge.errors.acceptFailed', 'Failed to accept challenge')
  } finally {
    processingId.value = null
  }
}

async function handleDecline(challenge: ChallengeItem) {
  processingId.value = challenge.id
  actionError.value = null
  try {
    await challengeStore.declineChallenge(challenge.id)
    emit('challengeDeclined', challenge)
  } catch (err: any) {
    actionError.value = err.message || t('challenge.errors.declineFailed', 'Failed to decline challenge')
  } finally {
    processingId.value = null
  }
}

async function handleCancel(challenge: ChallengeItem) {
  processingId.value = challenge.id
  actionError.value = null
  try {
    await challengeStore.cancelChallenge(challenge.id)
    emit('challengeCancelled', challenge)
  } catch (err: any) {
    actionError.value = err.message || t('challenge.errors.cancelFailed', 'Failed to cancel challenge')
  } finally {
    processingId.value = null
  }
}

function formatTime(isoString: string): string {
  try {
    const d = new Date(isoString)
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}
</script>

<template>
  <div class="space-y-4" data-testid="pending-challenges-widget">
    <!-- Header & Tab Toggle -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-primary text-xl">swords</span>
        <h3 class="font-headline text-base font-bold text-on-surface">
          {{ t('challenge.sectionTitle', 'Match Challenges') }}
        </h3>
        <span
          v-if="incomingList.length > 0"
          data-testid="incoming-challenge-badge"
          class="px-2 py-0.5 rounded-full bg-primary/20 text-primary text-xs font-bold font-headline"
        >
          {{ incomingList.length }}
        </span>
      </div>

      <div class="flex rounded-xl bg-surface-container-highest p-1 gap-1">
        <button
          type="button"
          @click="activeTab = 'incoming'"
          data-testid="tab-incoming"
          :class="[
            'px-3 py-1 rounded-lg text-xs font-headline font-bold transition-all cursor-pointer',
            activeTab === 'incoming'
              ? 'bg-primary text-background shadow-sm'
              : 'text-on-surface-variant hover:text-on-surface'
          ]"
        >
          {{ t('challenge.incomingTab', 'Incoming') }} ({{ incomingList.length }})
        </button>
        <button
          type="button"
          @click="activeTab = 'outgoing'"
          data-testid="tab-outgoing"
          :class="[
            'px-3 py-1 rounded-lg text-xs font-headline font-bold transition-all cursor-pointer',
            activeTab === 'outgoing'
              ? 'bg-primary text-background shadow-sm'
              : 'text-on-surface-variant hover:text-on-surface'
          ]"
        >
          {{ t('challenge.outgoingTab', 'Sent') }} ({{ outgoingList.length }})
        </button>
      </div>
    </div>

    <!-- Error Banner -->
    <div v-if="actionError" class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold">
      {{ actionError }}
    </div>

    <!-- Incoming Challenges List -->
    <div v-if="activeTab === 'incoming'" class="space-y-3">
      <div
        v-if="incomingList.length === 0"
        data-testid="no-incoming-challenges"
        class="p-6 rounded-2xl bg-surface-container-low text-center text-xs text-on-surface-variant"
      >
        {{ t('challenge.noIncoming', 'No incoming challenges right now') }}
      </div>

      <div
        v-for="challenge in incomingList"
        :key="challenge.id"
        data-testid="incoming-challenge-card"
        class="p-4 rounded-2xl bg-surface-container-low space-y-3 transition-all hover:bg-surface-container"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-full bg-surface-container-highest overflow-hidden shrink-0">
              <AvatarBase :avatar="challenge.challengerAvatar" :name="challenge.challengerNickname" shape="circle" />
            </div>
            <div class="min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-sm font-bold text-on-surface truncate" data-testid="challenger-name">
                  {{ challenge.challengerNickname }}
                </span>
                <span
                  class="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-[10px] font-bold font-headline"
                  data-testid="match-type-chip"
                >
                  {{ challenge.matchType === 'TWO_VS_TWO' ? '2v2' : '1v1' }}
                </span>
                <span
                  v-if="challenge.targetGroupName"
                  class="px-2 py-0.5 rounded-full bg-surface-container-highest text-on-surface-variant text-[10px] font-medium"
                >
                  {{ challenge.targetGroupName }}
                </span>
              </div>
              <div class="text-[11px] text-on-surface-variant flex items-center gap-2 mt-0.5">
                <span v-if="challenge.ruleConfigName">{{ challenge.ruleConfigName }}</span>
                <span>{{ formatTime(challenge.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="challenge.message" class="text-xs text-on-surface bg-surface-container-highest/50 p-2.5 rounded-xl italic">
          "{{ challenge.message }}"
        </div>

        <!-- Action buttons -->
        <div class="flex gap-2 pt-1">
          <button
            type="button"
            :disabled="processingId === challenge.id"
            @click="handleAccept(challenge)"
            data-testid="accept-challenge-btn"
            class="flex-1 py-2.5 rounded-xl bg-primary text-background font-headline font-bold text-xs uppercase tracking-wider hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-1 cursor-pointer disabled:opacity-50"
          >
            <span class="material-symbols-outlined text-sm">check</span>
            <span>{{ t('challenge.accept', 'Accept') }}</span>
          </button>
          <button
            type="button"
            :disabled="processingId === challenge.id"
            @click="handleDecline(challenge)"
            data-testid="decline-challenge-btn"
            class="px-4 py-2.5 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold text-xs transition-colors cursor-pointer disabled:opacity-50"
          >
            {{ t('challenge.decline', 'Decline') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Outgoing Challenges List -->
    <div v-if="activeTab === 'outgoing'" class="space-y-3">
      <div
        v-if="outgoingList.length === 0"
        data-testid="no-outgoing-challenges"
        class="p-6 rounded-2xl bg-surface-container-low text-center text-xs text-on-surface-variant"
      >
        {{ t('challenge.noOutgoing', 'No pending outgoing challenges') }}
      </div>

      <div
        v-for="challenge in outgoingList"
        :key="challenge.id"
        data-testid="outgoing-challenge-card"
        class="p-4 rounded-2xl bg-surface-container-low space-y-3"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-full bg-surface-container-highest overflow-hidden shrink-0">
              <AvatarBase
                :avatar="challenge.targetPlayerAvatar"
                :name="challenge.targetPlayerNickname || challenge.targetGroupName || 'Target'"
                shape="circle"
              />
            </div>
            <div class="min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-sm font-bold text-on-surface truncate">
                  {{ challenge.targetPlayerNickname || challenge.targetGroupName }}
                </span>
                <span class="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-[10px] font-bold font-headline">
                  {{ challenge.matchType === 'TWO_VS_TWO' ? '2v2' : '1v1' }}
                </span>
                <span class="px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 text-[10px] font-bold">
                  {{ t('challenge.pending', 'Pending') }}
                </span>
              </div>
              <div class="text-[11px] text-on-surface-variant mt-0.5">
                {{ formatTime(challenge.createdAt) }}
              </div>
            </div>
          </div>

          <button
            type="button"
            :disabled="processingId === challenge.id"
            @click="handleCancel(challenge)"
            data-testid="cancel-challenge-btn"
            class="px-3 py-1.5 rounded-xl bg-surface-container-highest hover:bg-red-950/40 hover:text-red-400 text-on-surface-variant font-headline font-bold text-xs transition-colors cursor-pointer disabled:opacity-50"
          >
            {{ t('common.cancel', 'Cancel') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
