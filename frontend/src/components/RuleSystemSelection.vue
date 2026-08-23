<script setup lang="ts">
import { useRouter } from 'vue-router';
import NewMatchFlow from '@/features/match/components/NewMatchFlow.vue';

defineOptions({
  name: 'RuleSystemSelection'
});

const router = useRouter();

function handleCancel() {
  router.push('/');
}

function handleComplete() {
  router.push('/');
}
</script>

<template>
  <div class="min-h-screen bg-background text-on-surface flex flex-col items-center p-4">
    <div class="w-full max-w-md">
      <NewMatchFlow @cancel="handleCancel" @complete="handleComplete" />
    </div>
  </div>
</template>
