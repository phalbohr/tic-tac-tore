<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { RuleConfig, CreateRuleConfigRequest, SideSwapRule, RestartRule, PositionSwapRule, PointDistribution } from '@/services/ruleConfigService';
import BaseButton from '@/core/components/BaseButton.vue';

defineOptions({
  name: 'RuleTemplateModal',
});

const props = defineProps<{
  isOpen: boolean;
  initialTemplate?: RuleConfig | null;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'save', payload: CreateRuleConfigRequest): void;
}>();

const { t } = useI18n();

const name = ref('');
const goalLimit = ref(5);
const gameLimit = ref(3);
const winByTwo = ref(true);
const absoluteScoreCap = ref<number | null>(8);
const timeoutsPerGame = ref(2);
const timeoutDurationSeconds = ref(30);
const possessionLimit5BarSeconds = ref(10);
const possessionLimitOtherSeconds = ref(15);
const sideSwapRule = ref<SideSwapRule>('BETWEEN_GAMES');
const restartRule = ref<RestartRule>('CONCEDING_TEAM');
const spinningAllowed = ref(false);
const aerialsAllowed = ref(false);
const positionSwapRule = ref<PositionSwapRule>('BETWEEN_GAMES');
const pointDistribution = ref<PointDistribution>('WIN_LOSS_3_0');

const nameError = ref('');

function resetForm() {
  if (props.initialTemplate) {
    name.value = props.initialTemplate.name || '';
    goalLimit.value = props.initialTemplate.goalLimit ?? 5;
    gameLimit.value = props.initialTemplate.gameLimit ?? 3;
    winByTwo.value = props.initialTemplate.winByTwo ?? true;
    absoluteScoreCap.value = props.initialTemplate.absoluteScoreCap ?? null;
    timeoutsPerGame.value = props.initialTemplate.timeoutsPerGame ?? 2;
    timeoutDurationSeconds.value = props.initialTemplate.timeoutDurationSeconds ?? 30;
    possessionLimit5BarSeconds.value = props.initialTemplate.possessionLimit5BarSeconds ?? 10;
    possessionLimitOtherSeconds.value = props.initialTemplate.possessionLimitOtherSeconds ?? 15;
    sideSwapRule.value = props.initialTemplate.sideSwapRule ?? 'BETWEEN_GAMES';
    restartRule.value = props.initialTemplate.restartRule ?? 'CONCEDING_TEAM';
    spinningAllowed.value = props.initialTemplate.spinningAllowed ?? false;
    aerialsAllowed.value = props.initialTemplate.aerialsAllowed ?? false;
    positionSwapRule.value = props.initialTemplate.positionSwapRule ?? 'BETWEEN_GAMES';
    pointDistribution.value = props.initialTemplate.pointDistribution ?? 'WIN_LOSS_3_0';
  } else {
    name.value = '';
    goalLimit.value = 5;
    gameLimit.value = 3;
    winByTwo.value = true;
    absoluteScoreCap.value = 8;
    timeoutsPerGame.value = 2;
    timeoutDurationSeconds.value = 30;
    possessionLimit5BarSeconds.value = 10;
    possessionLimitOtherSeconds.value = 15;
    sideSwapRule.value = 'BETWEEN_GAMES';
    restartRule.value = 'CONCEDING_TEAM';
    spinningAllowed.value = false;
    aerialsAllowed.value = false;
    positionSwapRule.value = 'BETWEEN_GAMES';
    pointDistribution.value = 'WIN_LOSS_3_0';
  }
  nameError.value = '';
}

watch(
  () => props.isOpen,
  (val) => {
    if (val) {
      resetForm();
    }
  },
  { immediate: true }
);

watch(
  () => props.initialTemplate,
  () => {
    if (props.isOpen) {
      resetForm();
    }
  }
);

function handleSave() {
  const trimmedName = name.value.trim();
  if (!trimmedName || trimmedName.length > 50) {
    nameError.value = t('rules.validation.nameRequired', 'Name is required (max 50 characters)');
    return;
  }

  const payload: CreateRuleConfigRequest = {
    name: trimmedName,
    goalLimit: Number(goalLimit.value),
    gameLimit: Number(gameLimit.value),
    winByTwo: Boolean(winByTwo.value),
    absoluteScoreCap: absoluteScoreCap.value ? Number(absoluteScoreCap.value) : null,
    timeoutsPerGame: Number(timeoutsPerGame.value),
    timeoutDurationSeconds: Number(timeoutDurationSeconds.value),
    possessionLimit5BarSeconds: Number(possessionLimit5BarSeconds.value),
    possessionLimitOtherSeconds: Number(possessionLimitOtherSeconds.value),
    sideSwapRule: sideSwapRule.value,
    restartRule: restartRule.value,
    spinningAllowed: Boolean(spinningAllowed.value),
    aerialsAllowed: Boolean(aerialsAllowed.value),
    positionSwapRule: positionSwapRule.value,
    pointDistribution: pointDistribution.value,
  };

  emit('save', payload);
}

function handleClose() {
  emit('close');
}
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
    data-testid="rule-template-modal-overlay"
  >
    <div
      role="dialog"
      :aria-label="initialTemplate ? t('rules.modal.editAsNew', 'Edit Rule Template as New') : t('rules.modal.createTitle', 'Create Rule Template')"
      class="bg-surface-container-low rounded-2xl w-full max-w-lg max-h-[90vh] flex flex-col shadow-2xl overflow-hidden border-0"
      style="border-width: 0px; border-bottom-width: 0px;"
      data-testid="rule-template-modal"
    >
      <!-- Modal Header -->
      <div class="flex items-center justify-between p-4 bg-surface-container">
        <h2 class="text-lg font-bold text-on-surface">
          {{ initialTemplate ? t('rules.modal.editAsNew', 'Edit Rule Template as New') : t('rules.modal.createTitle', 'Create Rule Template') }}
        </h2>
        <button
          type="button"
          @click="handleClose"
          class="p-1 rounded-full text-on-surface-variant hover:bg-surface-container-high cursor-pointer transition-colors"
          data-testid="close-modal-button"
          aria-label="Close"
        >
          <span class="material-symbols-outlined text-xl">close</span>
        </button>
      </div>

      <!-- Modal Body Form -->
      <div class="flex-1 overflow-y-auto p-4 space-y-5 text-start">
        <!-- Section: General -->
        <div class="space-y-3 bg-surface-container-highest p-3 rounded-xl">
          <h3 class="text-xs font-bold uppercase tracking-wider text-primary">
            {{ t('rules.sections.general', 'General') }}
          </h3>

          <div>
            <label for="template-name-input" class="block text-xs font-semibold text-on-surface mb-1">
              {{ t('rules.fields.name', 'Template Name') }} *
            </label>
            <input
              id="template-name-input"
              v-model="name"
              type="text"
              maxlength="50"
              placeholder="e.g. Office Standard Fast"
              class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
              data-testid="template-name-input"
            />
            <p
              v-if="nameError"
              class="text-error text-xs mt-1"
              data-testid="name-validation-error"
            >
              {{ nameError }}
            </p>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label for="goal-limit-input" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.goalLimit', 'Goal Limit') }} (1–100)
              </label>
              <input
                id="goal-limit-input"
                v-model.number="goalLimit"
                type="number"
                min="1"
                max="100"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="goal-limit-input"
              />
            </div>

            <div>
              <label for="game-limit-input" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.gameLimit', 'Game Limit') }} (1–15)
              </label>
              <input
                id="game-limit-input"
                v-model.number="gameLimit"
                type="number"
                min="1"
                max="15"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="game-limit-input"
              />
            </div>
          </div>
        </div>

        <!-- Section: Game Flow & Tie-Break -->
        <div class="space-y-3 bg-surface-container-highest p-3 rounded-xl">
          <h3 class="text-xs font-bold uppercase tracking-wider text-primary">
            {{ t('rules.sections.gameFlow', 'Game Flow & Tie-Break') }}
          </h3>

          <div class="flex items-center justify-between py-1">
            <label for="win-by-two-checkbox" class="text-sm text-on-surface font-medium cursor-pointer">
              {{ t('rules.fields.winByTwo', 'Win by 2 Goals') }}
            </label>
            <input
              id="win-by-two-checkbox"
              v-model="winByTwo"
              type="checkbox"
              class="w-5 h-5 rounded accent-primary cursor-pointer"
              data-testid="win-by-two-checkbox"
            />
          </div>

          <div v-if="winByTwo">
            <label for="absolute-score-cap-input" class="block text-xs font-semibold text-on-surface mb-1">
              {{ t('rules.fields.absoluteCap', 'Absolute Score Cap') }}
            </label>
            <input
              id="absolute-score-cap-input"
              v-model.number="absoluteScoreCap"
              type="number"
              min="1"
              max="100"
              placeholder="e.g. 8"
              class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
              data-testid="absolute-score-cap-input"
            />
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label for="timeouts-per-game-input" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.timeouts', 'Timeouts / Game') }}
              </label>
              <input
                id="timeouts-per-game-input"
                v-model.number="timeoutsPerGame"
                type="number"
                min="0"
                max="10"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="timeouts-per-game-input"
              />
            </div>

            <div>
              <label for="timeout-duration-input" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.timeoutDuration', 'Duration (s)') }}
              </label>
              <input
                id="timeout-duration-input"
                v-model.number="timeoutDurationSeconds"
                type="number"
                min="0"
                max="300"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="timeout-duration-input"
              />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label for="possession-5bar-input" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.possession5Bar', '5-Bar Limit (s)') }}
              </label>
              <input
                id="possession-5bar-input"
                v-model.number="possessionLimit5BarSeconds"
                type="number"
                min="0"
                max="60"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="possession-5bar-input"
              />
            </div>

            <div>
              <label for="possession-other-input" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.possessionOther', 'Other Rods (s)') }}
              </label>
              <input
                id="possession-other-input"
                v-model.number="possessionLimitOtherSeconds"
                type="number"
                min="0"
                max="60"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="possession-other-input"
              />
            </div>
          </div>
        </div>

        <!-- Section: Conduct & Swap Rules -->
        <div class="space-y-3 bg-surface-container-highest p-3 rounded-xl">
          <h3 class="text-xs font-bold uppercase tracking-wider text-primary">
            {{ t('rules.sections.conduct', 'Match Conduct & Rules') }}
          </h3>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label for="side-swap-rule-select" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.sideSwap', 'Side Swap') }}
              </label>
              <select
                id="side-swap-rule-select"
                v-model="sideSwapRule"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="side-swap-rule-select"
              >
                <option value="NONE">None</option>
                <option value="BETWEEN_GAMES">Between Games</option>
                <option value="AFTER_HALF_POINTS">After Half Points</option>
              </select>
            </div>

            <div>
              <label for="restart-rule-select" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.restart', 'Restart Rule') }}
              </label>
              <select
                id="restart-rule-select"
                v-model="restartRule"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="restart-rule-select"
              >
                <option value="CONCEDING_TEAM">Conceding Team</option>
                <option value="RANDOM_DROP">Random Drop</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label for="position-swap-rule-select" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.positionSwap', 'Position Swap') }}
              </label>
              <select
                id="position-swap-rule-select"
                v-model="positionSwapRule"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="position-swap-rule-select"
              >
                <option value="BETWEEN_GAMES">Between Games</option>
                <option value="FREE">Free</option>
                <option value="NEVER">Never</option>
              </select>
            </div>

            <div>
              <label for="point-distribution-select" class="block text-xs font-semibold text-on-surface mb-1">
                {{ t('rules.fields.points', 'Points Dist.') }}
              </label>
              <select
                id="point-distribution-select"
                v-model="pointDistribution"
                class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                data-testid="point-distribution-select"
              >
                <option value="WIN_LOSS_3_0">3 - 0 (Win/Loss)</option>
                <option value="WIN_LOSS_2_0">2 - 0 (Win/Loss)</option>
                <option value="WIN_DRAW_LOSS_3_1_0">3 - 1 - 0 (Draws)</option>
              </select>
            </div>
          </div>

          <div class="flex items-center justify-between py-1">
            <label for="spinning-allowed-checkbox" class="text-sm text-on-surface cursor-pointer">
              {{ t('rules.fields.spinningAllowed', 'Allow Spinning') }}
            </label>
            <input
              id="spinning-allowed-checkbox"
              v-model="spinningAllowed"
              type="checkbox"
              class="w-5 h-5 rounded accent-primary cursor-pointer"
              data-testid="spinning-allowed-checkbox"
            />
          </div>

          <div class="flex items-center justify-between py-1">
            <label for="aerials-allowed-checkbox" class="text-sm text-on-surface cursor-pointer">
              {{ t('rules.fields.aerialsAllowed', 'Allow Aerials') }}
            </label>
            <input
              id="aerials-allowed-checkbox"
              v-model="aerialsAllowed"
              type="checkbox"
              class="w-5 h-5 rounded accent-primary cursor-pointer"
              data-testid="aerials-allowed-checkbox"
            />
          </div>
        </div>
      </div>

      <!-- Modal Footer Actions -->
      <div class="p-4 bg-surface-container flex gap-3 justify-end">
        <BaseButton
          variant="secondary"
          @click="handleClose"
          data-testid="cancel-template-button"
        >
          {{ t('common.cancel', 'Cancel') }}
        </BaseButton>
        <BaseButton
          variant="primary"
          @click="handleSave"
          data-testid="save-template-button"
        >
          {{ t('rules.modal.saveButton', 'Save Template') }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>
