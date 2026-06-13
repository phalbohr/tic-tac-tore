<script setup lang="ts">
import { computed } from 'vue'
import { AVATAR_KEYS } from '@/assets/avatars'

const props = defineProps<{
  avatar?: string | null
}>()

const isSvgPreset = computed(() => {
  if (!props.avatar) return false
  // Use safe array check instead of 'in' operator to avoid hasOwnProperty / prototype pollution issues
  return (AVATAR_KEYS as readonly string[]).includes(props.avatar)
})

const isAnonymous = computed(() => {
  return props.avatar === 'anonymous' || !props.avatar
})

const isExternalImage = computed(() => {
  return !!(props.avatar && !isSvgPreset.value && !isAnonymous.value && (props.avatar.startsWith('http') || props.avatar.startsWith('/')))
})
</script>

<template>
  <div class="avatar-base w-full h-full flex items-center justify-center overflow-hidden">
    <svg v-if="isSvgPreset || isAnonymous" class="w-full h-full" aria-hidden="true" data-testid="avatar-svg">
      <use :href="`/avatars.svg#${isAnonymous ? 'anonymous' : props.avatar}`" />
    </svg>
    <img v-else-if="isExternalImage" :src="props.avatar!" alt="Avatar" class="w-full h-full object-cover" />
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
