<script setup lang="ts">
import { computed } from 'vue'
import { AVATAR_KEYS } from '@/assets/avatars'
import { getInitials } from '@/utils/avatar'

const props = withDefaults(
  defineProps<{
    avatar?: string | null
    name?: string | null
    shape?: 'circle' | 'square'
  }>(),
  {
    shape: 'circle',
  },
)

const isUrl = computed(() => {
  return !!(
    props.avatar &&
    (props.avatar.startsWith('http://') || props.avatar.startsWith('https://'))
  )
})

const hasCustomAvatar = computed(() => {
  return !!(
    props.avatar &&
    props.avatar !== 'anonymous' &&
    (isUrl.value || (AVATAR_KEYS as readonly string[]).includes(props.avatar))
  )
})

const initials = computed(() => {
  return getInitials(props.name)
})

const resolvedAvatar = computed(() => {
  if (hasCustomAvatar.value && props.avatar) return props.avatar
  if (props.avatar === 'anonymous') return 'anonymous'
  if (props.name) {
    let hash = 0
    for (let i = 0; i < props.name.length; i++) {
      hash = props.name.charCodeAt(i) + ((hash << 5) - hash)
    }
    const idx = Math.abs(hash) % AVATAR_KEYS.length
    return AVATAR_KEYS[idx]
  }
  return AVATAR_KEYS[0]
})

const shapeClass = computed(() => {
  return props.shape === 'square' ? 'rounded-xl' : 'rounded-full'
})
</script>

<template>
  <div
    class="avatar-base relative w-full h-full flex items-center justify-center overflow-hidden shrink-0 select-none"
    :class="shapeClass"
  >
    <img
      v-if="isUrl"
      :src="props.avatar!"
      :alt="props.name || 'User avatar'"
      class="w-full h-full absolute inset-0 z-0 object-cover"
      data-testid="avatar-img"
    />
    <svg
      v-else
      class="w-full h-full absolute inset-0 z-0"
      aria-hidden="true"
      data-testid="avatar-svg"
    >
      <use :href="`/avatars.svg#${resolvedAvatar}`" />
    </svg>
    <span
      v-if="initials && !isUrl"
      class="text-xs font-bold tracking-wider leading-none z-10 text-white"
      style="text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8)"
      data-testid="avatar-initials"
    >
      {{ initials }}
    </span>
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
