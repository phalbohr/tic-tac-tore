<script setup lang="ts">
import { computed } from 'vue'
import { AVATAR_KEYS } from '@/assets/avatars'

const props = defineProps<{
  avatar?: string | null
}>()

const isSvgPreset = computed(() => {
  if (!props.avatar) return false
  return AVATAR_KEYS.includes(props.avatar as any)
})

const isAnonymous = computed(() => {
  return props.avatar === 'anonymous' || !props.avatar
})

</script>

<template>
  <div class="avatar-base w-full h-full flex items-center justify-center overflow-hidden">
    <svg v-if="isSvgPreset || isAnonymous" class="w-full h-full" aria-hidden="true" data-testid="avatar-svg">
      <use :href="`/avatars.svg#${isAnonymous ? 'anonymous' : props.avatar}`" />
    </svg>
    <svg v-else class="w-full h-full bg-surface-container-highest text-on-surface-variant" aria-hidden="true" data-testid="avatar-svg-fallback">
      <use href="/avatars.svg#anonymous" />
    </svg>
  </div>
</template>

<style scoped>
.avatar-base {
  user-select: none;
}
:deep(svg) {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
