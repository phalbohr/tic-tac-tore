<script setup lang="ts">
import { useMatchDraftStore, MatchType } from '../stores/matchDraftStore'
import BaseButton from '@/core/components/BaseButton.vue'

defineOptions({
  name: 'MatchTypePicker'
})

const store = useMatchDraftStore()
</script>

<template>
  <div class="flex gap-4 w-full">
    <BaseButton 
      class="flex-1"
      :variant="store.matchType === MatchType.ONE_VS_ONE ? 'primary' : 'secondary'"
      @click="store.setMatchType(MatchType.ONE_VS_ONE)"
    >
      1v1
    </BaseButton>
    <BaseButton 
      class="flex-1"
      :variant="store.matchType === MatchType.TWO_VS_TWO ? 'primary' : 'secondary'"
      @click="store.setMatchType(MatchType.TWO_VS_TWO)"
    >
      2v2
    </BaseButton>
  </div>
</template>
