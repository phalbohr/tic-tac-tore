<script setup lang="ts">
import { ref, watch, onUnmounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type {
  RuleConfig,
  CreateRuleConfigRequest,
  MatchFormat,
  WinByTwoRule,
  SideSwapRule,
  RestartRule,
  PositionSwapRule,
  PointDistribution,
} from '@/services/ruleConfigService'
import BaseButton from '@/core/components/BaseButton.vue'
import NumberInput from '@/core/components/NumberInput.vue'
import CustomSelect from '@/core/components/CustomSelect.vue'
import BaseTooltip from '@/core/components/BaseTooltip.vue'

defineOptions({
  name: 'RuleTemplateModal',
})

const props = defineProps<{
  isOpen: boolean
  initialTemplate?: RuleConfig | null
  errorMessage?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', payload: CreateRuleConfigRequest): void
}>()

const { t } = useI18n()

const name = ref('')
const matchFormat = ref<MatchFormat>('BEST_OF_N')
const goalLimit = ref(5)
const gameLimit = ref(5)
const gamesToWin = ref(3)
const winByTwoRule = ref<WinByTwoRule>('DECISIVE_GAME_ONLY')
const absoluteScoreCap = ref<number | null>(8)
const timeoutsPerGame = ref(2)
const timeoutDurationSeconds = ref(30)
const possessionLimit5BarSeconds = ref(10)
const possessionLimitOtherSeconds = ref(15)
const sideSwapRule = ref<SideSwapRule>('BETWEEN_GAMES')
const restartRule = ref<RestartRule>('CONCEDING_TEAM')
const spinningAllowed = ref(false)
const aerialsAllowed = ref(false)
const positionSwapRule = ref<PositionSwapRule>('FREE')
const pointDistribution = ref<PointDistribution>('WIN_LOSS_3_0')

const formError = ref('')

function resetForm() {
  if (props.initialTemplate) {
    name.value = props.initialTemplate.name || ''
    matchFormat.value = props.initialTemplate.matchFormat ?? 'BEST_OF_N'
    goalLimit.value = props.initialTemplate.goalLimit ?? 5
    gameLimit.value = props.initialTemplate.gameLimit ?? (matchFormat.value === 'FIXED_GAMES' ? 2 : 5)
    gamesToWin.value =
      props.initialTemplate.gamesToWin ??
      (matchFormat.value === 'FIXED_GAMES' ? gameLimit.value : Math.floor(gameLimit.value / 2) + 1)
    winByTwoRule.value =
      props.initialTemplate.winByTwoRule ??
      (props.initialTemplate.winByTwo ? 'ALL_GAMES' : 'NONE')
    absoluteScoreCap.value =
      winByTwoRule.value !== 'NONE' ? (props.initialTemplate.absoluteScoreCap ?? 8) : null
    timeoutsPerGame.value = props.initialTemplate.timeoutsPerGame ?? 2
    timeoutDurationSeconds.value = props.initialTemplate.timeoutDurationSeconds ?? 30
    possessionLimit5BarSeconds.value = props.initialTemplate.possessionLimit5BarSeconds ?? 10
    possessionLimitOtherSeconds.value = props.initialTemplate.possessionLimitOtherSeconds ?? 15
    sideSwapRule.value = props.initialTemplate.sideSwapRule ?? 'BETWEEN_GAMES'
    restartRule.value = props.initialTemplate.restartRule ?? 'CONCEDING_TEAM'
    spinningAllowed.value = props.initialTemplate.spinningAllowed ?? false
    aerialsAllowed.value = props.initialTemplate.aerialsAllowed ?? false
    positionSwapRule.value = props.initialTemplate.positionSwapRule ?? 'FREE'
    pointDistribution.value =
      props.initialTemplate.pointDistribution ??
      (matchFormat.value === 'FIXED_GAMES' ? 'ONE_POINT_PER_GAME_WON' : 'WIN_LOSS_3_0')
  } else {
    name.value = ''
    matchFormat.value = 'BEST_OF_N'
    goalLimit.value = 5
    gameLimit.value = 3
    gamesToWin.value = 2
    winByTwoRule.value = 'DECISIVE_GAME_ONLY'
    absoluteScoreCap.value = 8
    timeoutsPerGame.value = 2
    timeoutDurationSeconds.value = 30
    possessionLimit5BarSeconds.value = 10
    possessionLimitOtherSeconds.value = 15
    sideSwapRule.value = 'BETWEEN_GAMES'
    restartRule.value = 'CONCEDING_TEAM'
    spinningAllowed.value = false
    aerialsAllowed.value = false
    positionSwapRule.value = 'FREE'
    pointDistribution.value = 'WIN_LOSS_3_0'
  }
  formError.value = ''
}

// Lock body scrolling when modal is open
watch(
  () => props.isOpen,
  (val) => {
    if (val) {
      document.body.style.overflow = 'hidden'
      resetForm()
    } else {
      document.body.style.overflow = ''
    }
  },
  { immediate: true },
)

onUnmounted(() => {
  document.body.style.overflow = ''
})

watch(
  () => props.initialTemplate,
  () => {
    if (props.isOpen) {
      resetForm()
    }
  },
)

watch(
  () => props.errorMessage,
  (val) => {
    if (val) {
      formError.value = val
    }
  },
)

// Reactively handle matchFormat toggle
function setMatchFormat(format: MatchFormat) {
  matchFormat.value = format
  if (format === 'BEST_OF_N') {
    if (gameLimit.value % 2 === 0) {
      gameLimit.value = 5
    }
    gamesToWin.value = Math.floor(gameLimit.value / 2) + 1
    if (pointDistribution.value === 'ONE_POINT_PER_GAME_WON') {
      pointDistribution.value = 'WIN_LOSS_3_0'
    }
    if (winByTwoRule.value === 'NONE') {
      winByTwoRule.value = 'DECISIVE_GAME_ONLY'
      absoluteScoreCap.value = goalLimit.value + 3
    }
  } else {
    gameLimit.value = 2
    gamesToWin.value = 2
    pointDistribution.value = 'ONE_POINT_PER_GAME_WON'
    winByTwoRule.value = 'NONE'
    absoluteScoreCap.value = null
  }
}

// Reactively update gamesToWin when gameLimit changes
watch(gameLimit, (newVal) => {
  if (matchFormat.value === 'BEST_OF_N') {
    gamesToWin.value = Math.floor((newVal || 1) / 2) + 1
  } else {
    gamesToWin.value = newVal || 1
  }
})

// Reactively update score cap when goalLimit changes
watch(goalLimit, (newVal) => {
  if (
    winByTwoRule.value !== 'NONE' &&
    absoluteScoreCap.value != null &&
    Number(absoluteScoreCap.value) <= Number(newVal)
  ) {
    absoluteScoreCap.value = Number(newVal) + 3
  }
})

// Reactively adjust absoluteScoreCap when winByTwoRule changes
watch(winByTwoRule, (newRule) => {
  if (newRule === 'NONE') {
    absoluteScoreCap.value = null
  } else if (absoluteScoreCap.value == null) {
    absoluteScoreCap.value = Number(goalLimit.value) + 3
  }
})

// Options for dropdowns
const winByTwoOptions = computed(() => [
  { value: 'NONE', label: t('rules.options.winByTwo.NONE', 'Disabled') },
  { value: 'ALL_GAMES', label: t('rules.options.winByTwo.ALL_GAMES', 'Every Game') },
  {
    value: 'DECISIVE_GAME_ONLY',
    label: t('rules.options.winByTwo.DECISIVE_GAME_ONLY', 'Decisive Game Only (Tie-break)'),
  },
])

const sideSwapOptions = computed(() => [
  { value: 'NONE', label: t('rules.options.sideSwap.NONE', 'None') },
  { value: 'BETWEEN_GAMES', label: t('rules.options.sideSwap.BETWEEN_GAMES', 'Between Games') },
  {
    value: 'AFTER_HALF_POINTS',
    label: t('rules.options.sideSwap.AFTER_HALF_POINTS', 'After Half Points'),
  },
])

const restartRuleOptions = computed(() => [
  { value: 'CONCEDING_TEAM', label: t('rules.options.restart.CONCEDING_TEAM', 'Conceding Team') },
  { value: 'RANDOM_DROP', label: t('rules.options.restart.RANDOM_DROP', 'Random Drop') },
])

const positionSwapOptions = computed(() => [
  { value: 'FREE', label: t('rules.options.positionSwap.FREE', 'Free') },
  { value: 'BETWEEN_GAMES', label: t('rules.options.positionSwap.BETWEEN_GAMES', 'Between Games') },
  { value: 'NEVER', label: t('rules.options.positionSwap.NEVER', 'Never') },
])

const pointDistributionOptions = computed(() => [
  { value: 'WIN_LOSS_3_0', label: t('rules.options.points.WIN_LOSS_3_0', '3 - 0 (Win/Loss)') },
  { value: 'WIN_LOSS_2_0', label: t('rules.options.points.WIN_LOSS_2_0', '2 - 0 (Win/Loss)') },
  {
    value: 'WIN_DRAW_LOSS_3_1_0',
    label: t('rules.options.points.WIN_DRAW_LOSS_3_1_0', '3 - 1 - 0 (Win/Draw/Loss)'),
  },
  {
    value: 'ONE_POINT_PER_GAME_WON',
    label: t('rules.options.points.ONE_POINT_PER_GAME_WON', '1 win - 1 pt (Per Game Won)'),
  },
])

function handleSave() {
  const trimmedName = name.value.trim()
  if (!trimmedName || trimmedName.length > 50) {
    formError.value = t('rules.validation.nameRequired', 'Name is required (max 50 characters)')
    return
  }

  if (matchFormat.value === 'BEST_OF_N') {
    if (gamesToWin.value < 1 || gamesToWin.value > gameLimit.value) {
      formError.value = t(
        'rules.validation.gamesToWinInvalid',
        'Games to win must be between 1 and the game limit',
      )
      return
    }
  }

  if (
    winByTwoRule.value !== 'NONE' &&
    absoluteScoreCap.value != null &&
    Number(absoluteScoreCap.value) <= Number(goalLimit.value)
  ) {
    formError.value = t(
      'rules.validation.capMustBeGreater',
      'Absolute score cap must be greater than goal limit',
    )
    return
  }

  const payload: CreateRuleConfigRequest = {
    name: trimmedName,
    matchFormat: matchFormat.value,
    goalLimit: Number(goalLimit.value),
    gameLimit: Number(gameLimit.value),
    gamesToWin:
      matchFormat.value === 'FIXED_GAMES'
        ? Number(gameLimit.value)
        : Number(gamesToWin.value),
    winByTwoRule: winByTwoRule.value,
    absoluteScoreCap:
      winByTwoRule.value !== 'NONE' && absoluteScoreCap.value != null
        ? Number(absoluteScoreCap.value)
        : null,
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
  }

  emit('save', payload)
}

function handleClose() {
  emit('close')
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
      :aria-label="
        initialTemplate
          ? t('rules.modal.editAsNew', 'Edit Rule Template as New')
          : t('rules.modal.createTitle', 'Create Rule Template')
      "
      class="bg-surface-container-low rounded-2xl w-full max-w-lg max-h-[90vh] flex flex-col shadow-2xl overflow-hidden border-0"
      style="border-width: 0px;"
      data-testid="rule-template-modal"
    >
      <!-- Modal Header -->
      <div class="flex items-center justify-between p-4 bg-surface-container">
        <h2 class="text-lg font-bold text-on-surface">
          {{
            initialTemplate
              ? t('rules.modal.editAsNew', 'Edit Rule Template as New')
              : t('rules.modal.createTitle', 'Create Rule Template')
          }}
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
      <div class="flex-1 overflow-y-auto p-4 space-y-4 text-start custom-modal-scroll overscroll-contain">
        <!-- Section: General -->
        <div class="space-y-3 bg-surface-container-highest/60 p-4 rounded-xl">
          <div class="flex items-center justify-between">
            <h3 class="text-xs font-bold uppercase tracking-wider text-primary">
              {{ t('rules.sections.general', 'General') }}
            </h3>
          </div>

          <!-- Template Name -->
          <div>
            <div class="flex items-center mb-1">
              <label
                for="template-name-input"
                class="block text-xs font-semibold text-on-surface"
              >
                {{ t('rules.fields.name', 'Template Name') }} *
              </label>
            </div>
            <input
              id="template-name-input"
              v-model="name"
              type="text"
              maxlength="50"
              placeholder="e.g. ITSF Championship Fast"
              class="w-full px-3 py-2 rounded-lg bg-surface-container text-on-surface text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:bg-surface-container-high transition-colors"
              data-testid="template-name-input"
            />
            <p
              v-if="formError || errorMessage"
              class="text-error text-xs mt-1 font-medium"
              data-testid="name-validation-error"
            >
              {{ formError || errorMessage }}
            </p>
          </div>

          <!-- Match Format Switcher -->
          <div>
            <div class="flex items-center mb-1.5">
              <span class="text-xs font-semibold text-on-surface">
                {{ t('rules.fields.matchFormat', 'Match Format') }}
              </span>
              <BaseTooltip :text="t('rules.tooltips.matchFormat')" />
            </div>
            <div class="grid grid-cols-2 gap-2 bg-surface-container p-1 rounded-xl">
              <button
                type="button"
                @click="setMatchFormat('BEST_OF_N')"
                data-testid="match-format-best-of"
                class="py-2 px-3 rounded-lg text-xs font-bold transition-all flex items-center justify-center gap-1 cursor-pointer"
                :class="
                  matchFormat === 'BEST_OF_N'
                    ? 'bg-primary text-background shadow-md'
                    : 'text-on-surface-variant hover:text-on-surface'
                "
              >
                <span class="material-symbols-outlined text-sm">trophy</span>
                <span>{{ t('rules.options.matchFormat.BEST_OF_N', 'Best of N') }}</span>
              </button>
              <button
                type="button"
                @click="setMatchFormat('FIXED_GAMES')"
                data-testid="match-format-fixed"
                class="py-2 px-3 rounded-lg text-xs font-bold transition-all flex items-center justify-center gap-1 cursor-pointer"
                :class="
                  matchFormat === 'FIXED_GAMES'
                    ? 'bg-primary text-background shadow-md'
                    : 'text-on-surface-variant hover:text-on-surface'
                "
              >
                <span class="material-symbols-outlined text-sm">equalizer</span>
                <span>{{ t('rules.options.matchFormat.FIXED_GAMES', 'Fixed Games') }}</span>
              </button>
            </div>
          </div>

          <!-- Game and Goal Limits -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <div class="flex items-center mb-1">
                <label
                  for="goal-limit-input"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.goalLimit', 'Goal Limit') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.goalLimit')" />
              </div>
              <NumberInput
                id="goal-limit-input"
                v-model="goalLimit"
                :min="1"
                :max="100"
                data-testid="goal-limit-input"
              />
            </div>

            <div>
              <div class="flex items-center mb-1">
                <label
                  for="game-limit-input"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.gameLimit', 'Game Limit') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.gameLimit')" />
              </div>
              <NumberInput
                id="game-limit-input"
                v-model="gameLimit"
                :min="1"
                :max="15"
                data-testid="game-limit-input"
              />
            </div>
          </div>

          <!-- Games to win (Only for Best of N) -->
          <div v-if="matchFormat === 'BEST_OF_N'">
            <div class="flex items-center mb-1">
              <label
                for="games-to-win-input"
                class="block text-xs font-semibold text-on-surface"
              >
                {{ t('rules.fields.gamesToWin', 'Games to Win') }}
              </label>
              <BaseTooltip :text="t('rules.tooltips.gamesToWin')" />
            </div>
            <NumberInput
              id="games-to-win-input"
              v-model="gamesToWin"
              :min="1"
              :max="gameLimit"
              data-testid="games-to-win-input"
            />
          </div>
        </div>

        <!-- Section: Game Flow & Tie-Break -->
        <div class="space-y-3 bg-surface-container-highest/60 p-4 rounded-xl">
          <div class="flex items-center justify-between">
            <h3 class="text-xs font-bold uppercase tracking-wider text-primary">
              {{ t('rules.sections.gameFlow', 'Game Flow & Tie-Break') }}
            </h3>
          </div>

          <!-- Win By 2 Rule (Tie-break Scope) -->
          <div>
            <div class="flex items-center mb-1">
              <label
                for="win-by-two-rule-select"
                class="block text-xs font-semibold text-on-surface"
              >
                {{ t('rules.fields.winByTwo', 'Win by 2 Goals') }}
              </label>
              <BaseTooltip :text="t('rules.tooltips.winByTwo')" />
            </div>
            <CustomSelect
              id="win-by-two-rule-select"
              v-model="winByTwoRule"
              :options="winByTwoOptions"
              data-testid="win-by-two-select"
            />
          </div>

          <!-- Absolute Score Cap (if Win By 2 is active) -->
          <div v-if="winByTwoRule !== 'NONE'">
            <div class="flex items-center mb-1">
              <label
                for="absolute-score-cap-input"
                class="block text-xs font-semibold text-on-surface"
              >
                {{ t('rules.fields.absoluteCap', 'Absolute Score Cap') }}
              </label>
              <BaseTooltip :text="t('rules.tooltips.absoluteCap')" />
            </div>
            <NumberInput
              id="absolute-score-cap-input"
              v-model="absoluteScoreCap"
              :min="goalLimit + 1"
              :max="100"
              placeholder="e.g. 8"
              data-testid="absolute-score-cap-input"
            />
          </div>
        </div>

        <!-- Section: In-Game Mechanics -->
        <div class="space-y-3 bg-surface-container-highest/60 p-4 rounded-xl">
          <div class="flex items-center justify-between">
            <h3 class="text-xs font-bold uppercase tracking-wider text-primary">
              {{ t('rules.sections.inGameMechanics', 'In-Game Mechanics') }}
            </h3>
          </div>

          <!-- Group 1: Timeouts -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <div class="flex items-center mb-1">
                <label
                  for="timeouts-per-game-input"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.timeouts', 'Timeouts / Game') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.timeouts')" />
              </div>
              <NumberInput
                id="timeouts-per-game-input"
                v-model="timeoutsPerGame"
                :min="0"
                :max="10"
                data-testid="timeouts-per-game-input"
              />
            </div>

            <div>
              <div class="flex items-center mb-1">
                <label
                  for="timeout-duration-input"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.timeoutDuration', 'Duration (s)') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.timeoutDuration')" />
              </div>
              <NumberInput
                id="timeout-duration-input"
                v-model="timeoutDurationSeconds"
                :min="0"
                :max="300"
                :step="5"
                data-testid="timeout-duration-input"
              />
            </div>
          </div>

          <!-- Group 2: Possession Limits -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <div class="flex items-center mb-1">
                <label
                  for="possession-5bar-input"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.possession5Bar', '5-Bar Limit (s)') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.possession5Bar')" />
              </div>
              <NumberInput
                id="possession-5bar-input"
                v-model="possessionLimit5BarSeconds"
                :min="0"
                :max="60"
                data-testid="possession-5bar-input"
              />
            </div>

            <div>
              <div class="flex items-center mb-1">
                <label
                  for="possession-other-input"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.possessionOther', 'Other Rods (s)') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.possessionOther')" />
              </div>
              <NumberInput
                id="possession-other-input"
                v-model="possessionLimitOtherSeconds"
                :min="0"
                :max="60"
                data-testid="possession-other-input"
              />
            </div>
          </div>

          <!-- Group 3: Table Conduct Dropdowns in 2x2 Grid -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <div class="flex items-center mb-1">
                <label
                  for="side-swap-rule-select"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.sideSwap', 'Side Swap') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.sideSwap')" />
              </div>
              <CustomSelect
                id="side-swap-rule-select"
                v-model="sideSwapRule"
                :options="sideSwapOptions"
                data-testid="side-swap-rule-select"
              />
            </div>

            <div>
              <div class="flex items-center mb-1">
                <label
                  for="restart-rule-select"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.restart', 'Restart Rule') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.restart')" />
              </div>
              <CustomSelect
                id="restart-rule-select"
                v-model="restartRule"
                :options="restartRuleOptions"
                data-testid="restart-rule-select"
              />
            </div>

            <div>
              <div class="flex items-center mb-1">
                <label
                  for="position-swap-rule-select"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.positionSwap', 'Position Swap') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.positionSwap')" />
              </div>
              <CustomSelect
                id="position-swap-rule-select"
                v-model="positionSwapRule"
                :options="positionSwapOptions"
                data-testid="position-swap-rule-select"
              />
            </div>

            <div>
              <div class="flex items-center mb-1">
                <label
                  for="point-distribution-select"
                  class="block text-xs font-semibold text-on-surface"
                >
                  {{ t('rules.fields.points', 'Points Dist.') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.points')" />
              </div>
              <CustomSelect
                id="point-distribution-select"
                v-model="pointDistribution"
                :options="pointDistributionOptions"
                data-testid="point-distribution-select"
              />
            </div>
          </div>

          <!-- Group 4: Rules of Play (Checkboxes) -->
          <div class="space-y-2 pt-1">
            <div class="flex items-center justify-between py-1">
              <div class="flex items-center">
                <label
                  for="spinning-allowed-checkbox"
                  class="text-sm text-on-surface font-medium cursor-pointer select-none"
                >
                  {{ t('rules.fields.spinningAllowed', 'Allow Spinning') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.spinningAllowed')" />
              </div>
              <input
                id="spinning-allowed-checkbox"
                v-model="spinningAllowed"
                type="checkbox"
                class="w-5 h-5 rounded accent-primary bg-surface-container cursor-pointer transition-transform active:scale-95"
                data-testid="spinning-allowed-checkbox"
              />
            </div>

            <div class="flex items-center justify-between py-1">
              <div class="flex items-center">
                <label
                  for="aerials-allowed-checkbox"
                  class="text-sm text-on-surface font-medium cursor-pointer select-none"
                >
                  {{ t('rules.fields.aerialsAllowed', 'Allow Aerials') }}
                </label>
                <BaseTooltip :text="t('rules.tooltips.aerialsAllowed')" />
              </div>
              <input
                id="aerials-allowed-checkbox"
                v-model="aerialsAllowed"
                type="checkbox"
                class="w-5 h-5 rounded accent-primary bg-surface-container cursor-pointer transition-transform active:scale-95"
                data-testid="aerials-allowed-checkbox"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Footer Actions -->
      <div class="p-4 bg-surface-container flex gap-3 justify-end">
        <BaseButton variant="secondary" @click="handleClose" data-testid="cancel-template-button">
          {{ t('common.cancel', 'Cancel') }}
        </BaseButton>
        <BaseButton variant="primary" @click="handleSave" data-testid="save-template-button">
          {{ t('rules.modal.saveButton', 'Save Template') }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-modal-scroll {
  scrollbar-width: thin;
  scrollbar-color: #393431 transparent;
}
.custom-modal-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-modal-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.custom-modal-scroll::-webkit-scrollbar-thumb {
  background-color: #393431;
  border-radius: 9999px;
}
.custom-modal-scroll::-webkit-scrollbar-thumb:hover {
  background-color: #4b4440;
}
</style>
