<script setup lang="ts">
import { computed } from 'vue'
import { AVATAR_KEYS } from '@/assets/avatars'
import { getInitials } from '@/utils/avatar'

const props = defineProps<{
  avatar?: string | null
  name?: string | null
}>()

const hasCustomAvatar = computed(() => {
  return !!(props.avatar && props.avatar !== 'anonymous' && (AVATAR_KEYS as readonly string[]).includes(props.avatar))
})

const initials = computed(() => {
  if (hasCustomAvatar.value) return ''
  return getInitials(props.name)
})

const resolvedAvatar = computed(() => {
  if (hasCustomAvatar.value && props.avatar) return props.avatar
  return 'anonymous'
})
</script>

<template>
  <div
    class="avatar-base w-full h-full flex items-center justify-center overflow-hidden rounded-full shrink-0"
    :class="initials ? 'bg-surface-container-high text-on-surface font-bold border border-outline/20 select-none' : ''"
  >
    <span v-if="initials" class="text-xs font-bold tracking-wider leading-none" data-testid="avatar-initials">
      {{ initials }}
    </span>
    <svg v-else class="w-full h-full" aria-hidden="true" data-testid="avatar-svg">
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

