<template>
  <div class="rule-system-selection">
    <h2>Rule System Selection</h2>
    <div v-if="presets.length > 0">
      <h3>Presets</h3>
      <ul>
        <li v-for="preset in presets" :key="preset.id">
          {{ preset.name }} (Goal Limit: {{ preset.goalLimit }}, Game Limit: {{ preset.gameLimit }})
        </li>
      </ul>
    </div>

    <div>
      <h3>Create Custom Rule</h3>
      <form @submit.prevent="submitCustomRule">
        <div>
          <label>Name:</label>
          <input v-model="customRule.name" type="text" required />
        </div>
        <div>
          <label>Goal Limit:</label>
          <input v-model.number="customRule.goalLimit" type="number" required />
        </div>
        <div>
          <label>Game Limit:</label>
          <input v-model.number="customRule.gameLimit" type="number" required />
        </div>
        <div>
          <label>Win By Two:</label>
          <input v-model="customRule.winByTwo" type="checkbox" />
        </div>
        <button type="submit">Create Custom Rule</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useRuleConfigStore } from '../stores/useRuleConfigStore';

defineOptions({
  name: 'RuleSystemSelection'
});

const store = useRuleConfigStore();
const { presets } = storeToRefs(store);

const customRule = ref({
  name: '',
  goalLimit: 5,
  gameLimit: 1,
  winByTwo: false
});

onMounted(async () => {
  await store.fetchPresets();
});

const submitCustomRule = async () => {
  try {
    await store.createCustomRule(customRule.value);
    alert('Custom rule created successfully!');
    customRule.value = { name: '', goalLimit: 5, gameLimit: 1, winByTwo: false };
  } catch {
    alert('Failed to create custom rule');
  }
};
</script>

<style scoped>
.rule-system-selection {
  padding: 20px;
}
form div {
  margin-bottom: 10px;
}
</style>
