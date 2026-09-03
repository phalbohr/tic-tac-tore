<script setup lang="ts">
import { computed } from 'vue'
import { useMatchDraftStore, MatchType } from '../stores/matchDraftStore'
import BaseButton from '@/core/components/BaseButton.vue'

defineOptions({
  name: 'MatchTypePicker',
})

const props = withDefaults(
  defineProps<{
    isLocked?: boolean
  }>(),
  {
    isLocked: false,
  },
)

const store = useMatchDraftStore()
const effectivelyLocked = computed(() => Boolean(props.isLocked || store.isTournamentMatch))
</script>

<template>
  <div class="flex gap-4 w-full">
    <BaseButton
      class="flex-1"
      :class="{ 'pointer-events-none opacity-80': effectivelyLocked }"
      :disabled="effectivelyLocked"
      :variant="store.matchType === MatchType.ONE_VS_ONE ? 'primary' : 'secondary'"
      @click="!effectivelyLocked && store.setMatchType(MatchType.ONE_VS_ONE)"
    >
      1v1
    </BaseButton>
    <BaseButton
      class="flex-1"
      :class="{ 'pointer-events-none opacity-80': effectivelyLocked }"
      :disabled="effectivelyLocked"
      :variant="store.matchType === MatchType.TWO_VS_TWO ? 'primary' : 'secondary'"
      @click="!effectivelyLocked && store.setMatchType(MatchType.TWO_VS_TWO)"
    >
      2v2
    </BaseButton>
  </div>
</template>
