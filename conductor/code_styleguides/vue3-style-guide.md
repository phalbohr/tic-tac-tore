# Vue 3 Style Guide

## 1. Component Structure

### Single-File Components (SFC)
- Use the `.vue` extension for all components.
- Enforce the order of tags: `<script setup>`, `<template>`, `<style>`.

```vue
<script setup lang="ts">
// Logic
</script>

<template>
  <!-- Template -->
</template>

<style scoped lang="scss">
/* Styles */
</style>
```

### Naming Conventions
- **File Names:** Use PascalCase for Single-File Components (e.g., `MyComponent.vue`).
- **Component Names:** Use multi-word PascalCase names to avoid conflicts with HTML elements (e.g., `TodoItem`, `UserProfile`).

## 2. Script Setup (Composition API)

### Syntax
- Use `<script setup>` syntax for concise and performant components.
- Use TypeScript (`lang="ts"`) for better type safety.

```vue
<script setup lang="ts">
import { ref, computed } from 'vue';

const props = defineProps<{
  title: string;
  count: number;
}>();

const emit = defineEmits<{
  (e: 'update', value: number): void;
}>();

const doubleCount = computed(() => props.count * 2);
</script>
```

### Reactivity
- Use `ref` for primitive values and `reactive` for objects/arrays when appropriate, though `ref` is generally preferred for consistency.
- Always access `.value` when working with `ref` inside the script.

## 3. Template Best Practices

### Directives
- Use strict directives (`v-if`, `v-for`, `v-bind`, `v-on`).
- Always provide a unique `:key` for `v-for`.

```html
<ul>
  <li v-for="item in items" :key="item.id">
    {{ item.name }}
  </li>
</ul>
```

### Self-Closing Components
- Components with no content should be self-closing in templates.

```html
<MyComponent />
```

## 4. Props and Events

### Definition
- Define props and emits using type-only declarations in `<script setup>`.

```ts
// Props
const props = withDefaults(defineProps<{
  msg?: string;
  labels?: string[];
}>(), {
  msg: 'hello',
  labels: () => ['one', 'two']
});

// Emits
const emit = defineEmits<{
  (e: 'change', id: number): void;
  (e: 'delete', id: number): void;
}>();
```

## 5. State Management (Pinia)
- Use Pinia for global state management.
- Keep stores modular and focused on specific domains (e.g., `useUserStore`, `useCartStore`).
