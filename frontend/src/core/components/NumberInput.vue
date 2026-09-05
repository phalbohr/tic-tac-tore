<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: number | null | undefined
    min?: number
    max?: number
    step?: number
    disabled?: boolean
    placeholder?: string
    id?: string
    dataTestid?: string
  }>(),
  {
    min: 0,
    max: 100,
    step: 1,
    disabled: false,
    placeholder: '',
    id: undefined,
    dataTestid: undefined,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: number | null): void
}>()

const currentVal = computed(() => (props.modelValue != null ? props.modelValue : null))

function updateValue(val: number | null) {
  if (val === null) {
    emit('update:modelValue', null)
    return
  }
  let clamped = val
  if (props.min != null && clamped < props.min) clamped = props.min
  if (props.max != null && clamped > props.max) clamped = props.max
  emit('update:modelValue', clamped)
}

function handleInput(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.value === '') {
    updateValue(null)
  } else {
    const num = Number(target.value)
    if (!isNaN(num)) {
      updateValue(num)
    }
  }
}

function decrement() {
  if (props.disabled) return
  const cur = currentVal.value != null ? currentVal.value : props.min
  const next = cur - props.step
  updateValue(next)
}

function increment() {
  if (props.disabled) return
  const cur = currentVal.value != null ? currentVal.value : props.min
  const next = cur + props.step
  updateValue(next)
}

function handleWheel(e: WheelEvent) {
  if (props.disabled) return
  e.preventDefault()
  if (e.deltaY < 0) {
    increment()
  } else if (e.deltaY > 0) {
    decrement()
  }
}
</script>

<template>
  <div
    class="inline-flex items-center w-fit bg-surface-container rounded-lg transition-all focus-within:bg-surface-container-high focus-within:ring-1 focus-within:ring-primary overflow-hidden"
    :class="{ 'opacity-50 cursor-not-allowed': disabled }"
    @wheel.prevent="handleWheel"
  >
    <button
      type="button"
      :disabled="disabled || (currentVal != null && min != null && currentVal <= min)"
      class="px-2.5 py-1.5 text-on-surface-variant hover:text-on-surface hover:bg-surface-container-highest active:scale-95 disabled:opacity-25 disabled:cursor-not-allowed transition select-none flex items-center justify-center font-bold text-sm cursor-pointer"
      aria-label="Decrease value"
      tabindex="-1"
      @click="decrement"
    >
      <span class="material-symbols-outlined text-base">remove</span>
    </button>

    <input
      :id="id"
      type="number"
      :value="currentVal != null ? currentVal : ''"
      :min="min"
      :max="max"
      :step="step"
      :disabled="disabled"
      :placeholder="placeholder"
      :data-testid="dataTestid"
      class="number-input-field w-11 text-center bg-transparent text-on-surface font-semibold text-sm focus:outline-none py-1.5 px-0.5"
      @input="handleInput"
    />

    <button
      type="button"
      :disabled="disabled || (currentVal != null && max != null && currentVal >= max)"
      class="px-2.5 py-1.5 text-on-surface-variant hover:text-on-surface hover:bg-surface-container-highest active:scale-95 disabled:opacity-25 disabled:cursor-not-allowed transition select-none flex items-center justify-center font-bold text-sm cursor-pointer"
      aria-label="Increase value"
      tabindex="-1"
      @click="increment"
    >
      <span class="material-symbols-outlined text-base">add</span>
    </button>
  </div>
</template>

<style scoped>
.number-input-field::-webkit-outer-spin-button,
.number-input-field::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.number-input-field {
  -moz-appearance: textfield;
}
</style>
