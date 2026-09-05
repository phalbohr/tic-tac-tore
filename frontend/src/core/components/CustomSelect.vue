<script setup lang="ts">
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

function onChange(e: Event) {
  const target = e.target as HTMLSelectElement
  emit('update:modelValue', target.value)
}
</script>

<template>
  <div class="relative w-full">
    <select
      :id="id"
      :value="modelValue"
      :disabled="disabled"
      :data-testid="dataTestid"
      class="w-full pl-3 pr-8 py-2 rounded-lg bg-surface-container text-on-surface text-sm font-medium appearance-none cursor-pointer focus:outline-none focus:ring-1 focus:ring-primary focus:bg-surface-container-high transition-all disabled:opacity-50 disabled:cursor-not-allowed"
      @change="onChange"
    >
      <option
        v-for="opt in options"
        :key="opt.value"
        :value="opt.value"
        class="bg-surface-container-highest text-on-surface py-1"
      >
        {{ opt.label }}
      </option>
    </select>
    <div
      class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2 text-on-surface-variant"
    >
      <span class="material-symbols-outlined text-lg">expand_more</span>
    </div>
  </div>
</template>
