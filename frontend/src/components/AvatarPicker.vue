<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { AVATAR_KEYS } from '@/assets/avatars'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  (e: 'select', avatar: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()

// Use the avatar keys array from avatars.ts
const presetAvatars = AVATAR_KEYS

const selected = ref<string | null>(null)
const modalRef = ref<HTMLElement | null>(null)
let previouslyFocusedElement: HTMLElement | null = null

function selectAvatar(avatar: string) {
  selected.value = avatar
  emit('select', avatar)
}

function handleKeyDown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    emit('close')
  } else if (event.key === 'Tab') {
    if (!modalRef.value) return
    const focusableElements = modalRef.value.querySelectorAll<HTMLElement>(
      'button:not([disabled]), [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    )
    if (focusableElements.length === 0) return
    
    const firstElement = focusableElements[0]
    const lastElement = focusableElements[focusableElements.length - 1]

    if (firstElement && lastElement) {
      if (event.shiftKey) {
        if (document.activeElement === firstElement) {
          lastElement.focus()
          event.preventDefault()
        }
      } else {
        if (document.activeElement === lastElement) {
          firstElement.focus()
          event.preventDefault()
        }
      }
    }
  }
}

function handleGridKeyDown(event: KeyboardEvent, index: number) {
  const columns = 4
  let newIndex = index

  switch (event.key) {
    case 'ArrowRight':
      newIndex = (index + 1) % presetAvatars.length
      break
    case 'ArrowLeft':
      newIndex = (index - 1 + presetAvatars.length) % presetAvatars.length
      break
    case 'ArrowDown':
      if (index + columns < presetAvatars.length) {
        newIndex = index + columns
      }
      break
    case 'ArrowUp':
      if (index - columns >= 0) {
        newIndex = index - columns
      }
      break
    case 'Home':
      newIndex = 0
      break
    case 'End':
      newIndex = presetAvatars.length - 1
      break
    default:
      return
  }

  event.preventDefault()
  
  const buttons = modalRef.value?.querySelectorAll<HTMLElement>('[data-testid^="avatar-option-"]')
  if (buttons && buttons.length > newIndex) {
    const btn = buttons[newIndex]
    if (btn) btn.focus()
  }
}

onMounted(() => {
  previouslyFocusedElement = document.activeElement as HTMLElement
  modalRef.value?.focus()
  window.addEventListener('keydown', handleKeyDown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeyDown)
  if (previouslyFocusedElement) {
    previouslyFocusedElement.focus()
  }
})
</script>

<template>
  <div 
    ref="modalRef"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md" 
    role="dialog" 
    aria-modal="true"
    tabindex="-1"
    @click.self="emit('close')"
    data-testid="avatar-picker-backdrop"
  >
    <div class="w-full max-w-md bg-surface-container-low rounded-2xl p-6 shadow-2xl space-y-6 flex flex-col max-h-[90vh]">
      
      <!-- Header -->
      <div class="flex justify-between items-center">
        <h2 class="font-headline text-lg font-bold text-on-surface tracking-tight">
          {{ t('avatarPicker.title') }}
        </h2>
        <button 
          @click="emit('close')"
          class="w-8 h-8 flex items-center justify-center rounded-full bg-surface-container-highest/50 text-on-surface hover:bg-surface-container-highest active:scale-95 transition-all"
          data-testid="close-picker-icon-button"
          aria-label="Close"
        >
          <span class="material-symbols-outlined text-sm">close</span>
        </button>
      </div>

      <!-- Grid of Avatars -->
      <div class="grid grid-cols-4 gap-4 overflow-y-auto pr-1 py-1 flex-grow scrollbar-thin">
        <button
          v-for="(avatar, index) in presetAvatars"
          :key="avatar"
          @click="selectAvatar(avatar)"
          @keydown="handleGridKeyDown($event, index)"
          :data-testid="'avatar-option-' + avatar"
          :aria-label="avatar"
          :class="[
            'aspect-square p-2.5 rounded-xl bg-surface-container-highest transition-all duration-200 active:scale-95 flex items-center justify-center shadow-sm hover:shadow-md hover:translate-y-[-2px]',
            selected === avatar ? 'bg-primary/20 scale-105 shadow-md' : 'hover:bg-surface-container-highest/80'
          ]"
        >
          <div class="w-full h-full flex items-center justify-center">
            <svg class="w-full h-full" aria-hidden="true">
              <use :href="`/avatars.svg#${avatar}`" />
            </svg>
          </div>
        </button>
      </div>

      <!-- Action Button -->
      <div class="pt-2">
        <button 
          @click="emit('close')"
          class="w-full py-3 rounded-xl bg-surface-container-highest text-on-surface font-headline font-bold text-sm hover:bg-surface-container-highest/80 active:scale-95 transition-colors"
          data-testid="cancel-picker-button"
        >
          {{ t('common.cancel') }}
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
.scrollbar-thin {
  scrollbar-width: thin;
  scrollbar-color: var(--md-sys-color-surface-container-highest, #4a403d) transparent;
}
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
