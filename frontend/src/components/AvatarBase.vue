<script setup lang="ts">
import { computed } from 'vue'
import { AVATARS } from '@/assets/avatars'

const props = defineProps<{
  avatar?: string | null
}>()

const isSvgPreset = computed(() => {
  if (!props.avatar) return false
  return props.avatar in AVATARS
})

const isAnonymous = computed(() => {
  return props.avatar === 'anonymous' || !props.avatar
})

const svgContent = computed(() => {
  if (isSvgPreset.value) {
    return AVATARS[props.avatar as string]
  }
  if (isAnonymous.value) {
    return AVATARS['anonymous']
  }
  return ''
})

const isExternalImage = computed(() => {
  return props.avatar && !isSvgPreset.value && !isAnonymous.value && (props.avatar.startsWith('http') || props.avatar.startsWith('/'))
})
</script>

<template>
  <div class="avatar-base w-full h-full flex items-center justify-center overflow-hidden">
    <div v-if="isSvgPreset || isAnonymous" class="w-full h-full flex items-center justify-center" v-html="svgContent" />
    <img v-else-if="isExternalImage" :src="props.avatar!" alt="Avatar" class="w-full h-full object-cover" />
    <div v-else class="w-full h-full flex items-center justify-center bg-surface-container-highest text-on-surface-variant" v-html="AVATARS['anonymous']" />
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
