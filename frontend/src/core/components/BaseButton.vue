<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost'
}>()
</script>
<template>
  <button 
    class="flex items-center justify-center font-bold rounded-xl transition-colors h-14 px-4 disabled:opacity-50 disabled:cursor-not-allowed"
    :class="{
      'bg-primary text-background': variant === 'primary' || !variant,
      'bg-surface-container-highest text-on-surface': variant === 'secondary',
      'border-2 border-primary text-primary bg-transparent': variant === 'outline',
      'bg-transparent text-on-surface hover:bg-surface-container': variant === 'ghost'
    }"
  >
    <slot />
  </button>
</template>
