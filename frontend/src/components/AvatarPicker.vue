<script setup lang="ts">
import { ref } from 'vue'
import { AVATARS } from '@/assets/avatars'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  (e: 'select', avatar: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()

// Exclude 'anonymous' from the picker grid
const presetAvatars = Object.keys(AVATARS).filter(key => key !== 'anonymous')

const selected = ref<string | null>(null)

function selectAvatar(avatar: string) {
  selected.value = avatar
  emit('select', avatar)
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md" role="dialog" aria-modal="true">
    <div class="w-full max-w-md bg-surface-container-low rounded-2xl p-6 shadow-2xl space-y-6 flex flex-col max-h-[90vh]">
      
      <!-- Header -->
      <div class="flex justify-between items-center">
        <h2 class="font-headline text-lg font-bold text-on-surface tracking-tight">
          {{ t('avatarPicker.title') }}
        </h2>
        <button 
          @click="emit('close')"
          class="w-8 h-8 flex items-center justify-center rounded-full bg-surface-container-highest/50 text-on-surface hover:bg-surface-container-highest active:scale-95 transition-all"
        >
          <span class="material-symbols-outlined text-sm">close</span>
        </button>
      </div>

      <!-- Grid of Avatars -->
      <div class="grid grid-cols-4 gap-4 overflow-y-auto pr-1 py-1 flex-grow scrollbar-thin">
        <button
          v-for="avatar in presetAvatars"
          :key="avatar"
          @click="selectAvatar(avatar)"
          :data-testid="'avatar-option-' + avatar"
          :class="[
            'aspect-square p-2.5 rounded-xl bg-surface-container-highest transition-all duration-200 active:scale-95 flex items-center justify-center shadow-sm hover:shadow-md hover:translate-y-[-2px]',
            selected === avatar ? 'bg-primary/20 scale-105 shadow-md' : 'hover:bg-surface-container-highest/80'
          ]"
        >
          <div class="w-full h-full flex items-center justify-center" v-html="AVATARS[avatar]" />
        </button>
      </div>

      <!-- Action Button -->
      <div class="pt-2">
        <button 
          @click="emit('close')"
          class="w-full py-3 rounded-xl bg-surface-container-highest text-on-surface font-headline font-bold text-sm hover:bg-surface-container-highest/80 active:scale-95 transition-colors"
        >
          {{ t('common.cancel') }}
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
.scrollbar-thin::-webkit-scrollbar {
  width: 4px;
}
.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}
.scrollbar-thin::-webkit-scrollbar-thumb {
  background: var(--md-sys-color-surface-container-highest, #4a403d);
  border-radius: 2px;
}
</style>
