<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  text: string
}>()

const isVisible = ref(false)
const triggerRef = ref<HTMLElement | null>(null)
const positionClass = ref<'center' | 'left' | 'right'>('center')

function show() {
  if (triggerRef.value) {
    const dialog = triggerRef.value.closest('[role="dialog"]') || document.body
    const dialogRect = dialog.getBoundingClientRect()
    const triggerRect = triggerRef.value.getBoundingClientRect()

    const triggerCenter = triggerRect.left + triggerRect.width / 2
    const distFromDialogLeft = triggerCenter - dialogRect.left
    const distFromDialogRight = dialogRect.right - triggerCenter

    const tooltipHalfWidth = 135
    if (distFromDialogLeft < tooltipHalfWidth + 16) {
      positionClass.value = 'left'
    } else if (distFromDialogRight < tooltipHalfWidth + 16) {
      positionClass.value = 'right'
    } else {
      positionClass.value = 'center'
    }
  }
  isVisible.value = true
}

function hide() {
  isVisible.value = false
}
</script>

<template>
  <div ref="triggerRef" class="relative inline-flex items-center ml-1" @mouseenter="show" @mouseleave="hide">
    <button
      type="button"
      class="w-4 h-4 rounded-full flex items-center justify-center text-[10px] font-bold text-on-surface-variant hover:text-primary hover:bg-surface-container-high transition-colors focus:outline-none focus:ring-1 focus:ring-primary cursor-help"
      aria-label="Help"
      tabindex="0"
      @focus="show"
      @blur="hide"
    >
      ?
    </button>

    <transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <div
        v-if="isVisible"
        role="tooltip"
        class="absolute bottom-full mb-2 w-64 p-2.5 rounded-lg bg-surface-container-highest text-on-surface text-xs leading-relaxed shadow-2xl z-50 pointer-events-none border-0"
        :class="{
          'left-1/2 -translate-x-1/2': positionClass === 'center',
          'left-0 translate-x-0': positionClass === 'left',
          'right-0 translate-x-0': positionClass === 'right',
        }"
        style="box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5);"
      >
        <div class="font-normal">{{ text }}</div>
        <!-- Arrow pointer -->
        <div
          class="absolute top-full border-solid border-t-surface-container-highest border-t-4 border-x-transparent border-x-4 border-b-0"
          :class="{
            'left-1/2 -translate-x-1/2': positionClass === 'center',
            'left-2': positionClass === 'left',
            'right-2': positionClass === 'right',
          }"
        ></div>
      </div>
    </transition>
  </div>
</template>
