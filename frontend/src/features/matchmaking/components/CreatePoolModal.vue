<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { usePoolStore } from '../stores/poolStore';
import type { MatchType, StartCondition, SkillLevel, PoolResponse } from '../types/pool';

interface Props {
  isOpen: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'created', pool: PoolResponse): void;
}>();

const { t } = useI18n();
const poolStore = usePoolStore();

const matchType = ref<MatchType>('ONE_VS_ONE');
const startCondition = ref<StartCondition>('FILL_BASED');
const scheduledTime = ref<string>('');
const skillLevel = ref<SkillLevel>('OPEN_FOR_ALL');
const errorMessage = ref<string | null>(null);
const isSubmitting = ref(false);

watch(
  () => props.isOpen,
  (open) => {
    if (open) {
      matchType.value = 'ONE_VS_ONE';
      startCondition.value = 'FILL_BASED';
      scheduledTime.value = '';
      skillLevel.value = 'OPEN_FOR_ALL';
      errorMessage.value = null;
      isSubmitting.value = false;
    }
  }
);

function handleClose() {
  emit('close');
}

async function handleSubmit() {
  errorMessage.value = null;
  let isoScheduledTime: string | null = null;

  if (startCondition.value === 'SCHEDULED_TIME') {
    if (!scheduledTime.value) {
      errorMessage.value = t('pool.scheduledTimeRequired', 'Scheduled time is required for scheduled pools');
      return;
    }
    const parsedDate = new Date(scheduledTime.value);
    if (isNaN(parsedDate.getTime())) {
      errorMessage.value = t('pool.invalidDate', 'Invalid scheduled date format');
      return;
    }
    isoScheduledTime = parsedDate.toISOString();
  }

  isSubmitting.value = true;
  try {
    const created = await poolStore.createPool({
      matchType: matchType.value,
      startCondition: startCondition.value,
      scheduledTime: isoScheduledTime,
      skillLevel: skillLevel.value,
    });
    emit('created', created);
    emit('close');
  } catch (err: any) {
    errorMessage.value = err.message || t('common.error', 'Something went wrong');
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      data-test="create-pool-modal"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md"
      role="dialog"
      aria-modal="true"
    >
      <div
        class="w-full max-w-md bg-surface-container-low rounded-2xl p-6 space-y-5 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]"
      >
        <div class="flex justify-between items-center">
          <h2 class="font-headline text-lg font-bold text-on-surface">
            {{ t('pool.createTitle', 'Create Want to Play Pool') }}
          </h2>
          <button
            type="button"
            @click="handleClose"
            data-test="cancel-pool-btn"
            class="text-on-surface-variant hover:text-on-surface p-1 rounded-lg transition-colors cursor-pointer"
            aria-label="Close"
          >
            <span class="material-symbols-outlined text-xl">close</span>
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-5 flex-grow overflow-y-auto pr-1">
          <div v-if="errorMessage" data-test="error-banner" class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold">
            {{ errorMessage }}
          </div>

          <!-- Match Type Segmented Control -->
          <div class="space-y-2">
            <label class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80">
              {{ t('pool.matchType', 'Match Format') }}
            </label>
            <div class="grid grid-cols-2 gap-2 bg-surface-container-highest p-1 rounded-xl">
              <button
                type="button"
                data-test="match-type-1v1"
                :class="[
                  'py-2.5 rounded-lg font-headline text-xs font-bold transition-all cursor-pointer text-center',
                  matchType === 'ONE_VS_ONE'
                    ? 'active bg-primary text-background shadow-md'
                    : 'text-on-surface-variant hover:text-on-surface'
                ]"
                @click="matchType = 'ONE_VS_ONE'"
              >
                {{ t('pool.type1v1', '1 vs 1') }}
              </button>
              <button
                type="button"
                data-test="match-type-2v2"
                :class="[
                  'py-2.5 rounded-lg font-headline text-xs font-bold transition-all cursor-pointer text-center',
                  matchType === 'TWO_VS_TWO'
                    ? 'active bg-primary text-background shadow-md'
                    : 'text-on-surface-variant hover:text-on-surface'
                ]"
                @click="matchType = 'TWO_VS_TWO'"
              >
                {{ t('pool.type2v2', '2 vs 2') }}
              </button>
            </div>
          </div>

          <!-- Start Condition Segmented Control -->
          <div class="space-y-2">
            <label class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80">
              {{ t('pool.startCondition', 'Start Condition') }}
            </label>
            <div class="grid grid-cols-2 gap-2 bg-surface-container-highest p-1 rounded-xl">
              <button
                type="button"
                data-test="condition-fill"
                :class="[
                  'py-2.5 rounded-lg font-headline text-xs font-bold transition-all cursor-pointer text-center',
                  startCondition === 'FILL_BASED'
                    ? 'active bg-primary text-background shadow-md'
                    : 'text-on-surface-variant hover:text-on-surface'
                ]"
                @click="startCondition = 'FILL_BASED'"
              >
                {{ t('pool.fillBased', 'Immediate (Fill-based)') }}
              </button>
              <button
                type="button"
                data-test="condition-scheduled"
                :class="[
                  'py-2.5 rounded-lg font-headline text-xs font-bold transition-all cursor-pointer text-center',
                  startCondition === 'SCHEDULED_TIME'
                    ? 'active bg-primary text-background shadow-md'
                    : 'text-on-surface-variant hover:text-on-surface'
                ]"
                @click="startCondition = 'SCHEDULED_TIME'"
              >
                {{ t('pool.scheduledTime', 'Scheduled Time') }}
              </button>
            </div>
          </div>

          <!-- DateTime Picker for Scheduled Pool -->
          <div v-if="startCondition === 'SCHEDULED_TIME'" class="space-y-2">
            <label for="scheduled-time" class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80">
              {{ t('pool.scheduledTimeLabel', 'Start Date & Time') }}
            </label>
            <input
              id="scheduled-time"
              v-model="scheduledTime"
              type="datetime-local"
              data-test="datetime-picker"
              class="w-full bg-surface-container-highest text-on-surface px-4 py-3 rounded-xl font-headline text-sm focus:outline-none focus:ring-2 focus:ring-primary transition-all"
              required
            />
          </div>

          <!-- Skill Level Selector -->
          <div class="space-y-2">
            <label for="skill-level" class="font-headline text-xs font-bold uppercase tracking-wider text-primary/80">
              {{ t('pool.skillLevel', 'Skill Level Filter') }}
            </label>
            <select
              id="skill-level"
              v-model="skillLevel"
              data-test="skill-level-select"
              class="w-full bg-surface-container-highest text-on-surface px-4 py-3 rounded-xl font-headline text-sm focus:outline-none focus:ring-2 focus:ring-primary transition-all cursor-pointer"
            >
              <option value="OPEN_FOR_ALL">{{ t('pool.skillOpenForAll', 'Open for All') }}</option>
              <option value="BEGINNER">{{ t('pool.skillBeginner', 'Beginner') }}</option>
              <option value="INTERMEDIATE">{{ t('pool.skillIntermediate', 'Intermediate') }}</option>
              <option value="ADVANCED">{{ t('pool.skillAdvanced', 'Advanced') }}</option>
            </select>
          </div>

          <!-- Actions -->
          <div class="flex gap-2 pt-2">
            <button
              type="submit"
              data-test="submit-pool-btn"
              :disabled="isSubmitting"
              @click="handleSubmit"
              class="flex-1 py-3 rounded-xl bg-primary text-background font-headline font-bold text-xs uppercase tracking-wider hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-1 cursor-pointer disabled:opacity-50"
            >
              <span>{{ isSubmitting ? t('common.loading', 'Loading...') : t('pool.createPool', 'Create Pool') }}</span>
            </button>
            <button
              type="button"
              @click="handleClose"
              class="px-4 py-3 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold text-xs transition-colors cursor-pointer"
            >
              {{ t('common.cancel', 'Cancel') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>
