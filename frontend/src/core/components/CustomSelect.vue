<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  modelValue: string | number
  options: Array<{ value: string | number; label: string }>
  id?: string
  dataTestid?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: any): void
}>()

const isOpen = ref(false)
const containerRef = ref<HTMLElement | null>(null)

const selectedOption = computed(() =>
  props.options.find((opt) => String(opt.value) === String(props.modelValue)),
)

function toggleDropdown() {
  if (props.disabled) return
  isOpen.value = !isOpen.value
}

function selectOption(val: string | number) {
  emit('update:modelValue', val)
  isOpen.value = false
}

function handleClickOutside(event: MouseEvent) {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

function onNativeSelectChange(e: Event) {
  const target = e.target as HTMLSelectElement
  emit('update:modelValue', target.value)
}
</script>

<template>
  <div ref="containerRef" class="relative w-full" @keydown.escape.stop="isOpen = false">
    <!-- Custom Trigger Button -->
    <button
      :id="id"
      type="button"
      :disabled="disabled"
      class="w-full pl-3 pr-8 py-2 rounded-lg bg-surface-container text-on-surface text-sm font-medium text-left flex items-center justify-between cursor-pointer focus:outline-none focus:ring-1 focus:ring-primary focus:bg-surface-container-high transition-all disabled:opacity-50 disabled:cursor-not-allowed"
      aria-haspopup="listbox"
      :aria-expanded="isOpen"
      @click="toggleDropdown"
    >
      <span class="truncate">{{ selectedOption?.label || '' }}</span>
      <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2 text-on-surface-variant">
        <span
          class="material-symbols-outlined text-lg transition-transform duration-200"
          :class="{ 'rotate-180': isOpen }"
        >
          expand_more
        </span>
      </div>
    </button>

    <!-- Custom Dropdown Menu (100% Dark) -->
    <transition
      enter-active-class="transition duration-100 ease-out"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition duration-75 ease-in"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <div
        v-if="isOpen"
        class="absolute left-0 right-0 mt-1.5 py-1 rounded-xl bg-surface-container-high text-on-surface shadow-2xl z-50 overflow-hidden border border-white/5 max-h-56 overflow-y-auto"
        role="listbox"
        style="box-shadow: 0 12px 28px rgba(0, 0, 0, 0.6);"
      >
        <button
          v-for="opt in options"
          :key="opt.value"
          type="button"
          role="option"
          :aria-selected="String(opt.value) === String(modelValue)"
          class="w-full px-3 py-2 text-left text-xs font-medium flex items-center justify-between transition-colors cursor-pointer"
          :class="
            String(opt.value) === String(modelValue)
              ? 'bg-primary/20 text-primary font-bold'
              : 'text-on-surface hover:bg-surface-container-highest'
          "
          @click="selectOption(opt.value)"
        >
          <span>{{ opt.label }}</span>
          <span
            v-if="String(opt.value) === String(modelValue)"
            class="material-symbols-outlined text-sm text-primary ml-2"
          >
            check
          </span>
        </button>
      </div>
    </transition>

    <!-- Synced hidden native select for full unit-test & form parity -->
    <select
      :value="modelValue"
      :data-testid="dataTestid"
      class="sr-only"
      tabindex="-1"
      aria-hidden="true"
      @change="onNativeSelectChange"
    >
      <option v-for="opt in options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>
  </div>
</template>

