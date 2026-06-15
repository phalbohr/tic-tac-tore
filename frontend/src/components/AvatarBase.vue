<script setup lang="ts">
import { computed } from 'vue'
import { AVATAR_KEYS } from '@/assets/avatars'

const props = defineProps<{
  avatar?: string | null
}>()

const resolvedAvatar = computed(() => {
  if (!props.avatar || props.avatar === 'anonymous') return 'anonymous'
  if ((AVATAR_KEYS as readonly string[]).includes(props.avatar)) return props.avatar
  return 'anonymous'
})
</script>

<template>
  <div class="avatar-base w-full h-full flex items-center justify-center overflow-hidden">
    <svg class="w-full h-full" aria-hidden="true" data-testid="avatar-svg">
      <use :href="`/avatars.svg#${resolvedAvatar}`" />
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
