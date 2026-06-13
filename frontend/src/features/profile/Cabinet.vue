<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import AvatarBase from '@/components/AvatarBase.vue'
import AvatarPicker from '@/components/AvatarPicker.vue'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const nickname = ref('')
const selectedLanguage = ref('EN')
const isDropdownOpen = ref(false)
const message = ref('')
const error = ref('')
const isUpdating = ref(false)
const isAvatarPickerOpen = ref(false)

async function handleAvatarSelect(selectedAvatar: string) {
  if (isUpdating.value) return
  isUpdating.value = true
  isAvatarPickerOpen.value = false
  error.value = ''
  message.value = ''
  try {
    await authStore.updateProfile({ avatar: selectedAvatar })
    message.value = t('cabinet.avatarSuccess')
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('common.error')
  } finally {
    isUpdating.value = false
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/')
    return
  }
  if (!authStore.profile) {
    await authStore.fetchProfile()
  }
  if (authStore.profile) {
    nickname.value = authStore.profile.nickname
    selectedLanguage.value = authStore.profile.language || 'EN'
  }
})

function toggleDropdown() {
  isDropdownOpen.value = !isDropdownOpen.value
}

async function selectLanguage(lang: 'EN' | 'DE') {
  if (isUpdating.value) return
  isUpdating.value = true
  const previousLang = selectedLanguage.value
  selectedLanguage.value = lang
  isDropdownOpen.value = false
  error.value = ''
  message.value = ''
  try {
    await authStore.updateProfile({ language: lang })
  } catch (err) {
    selectedLanguage.value = previousLang
    error.value = err instanceof Error ? err.message : t('common.error')
  } finally {
    isUpdating.value = false
  }
}

async function handleSave() {
  if (isUpdating.value) return
  isUpdating.value = true
  message.value = ''
  error.value = ''
  try {
    await authStore.updateProfile({ nickname: nickname.value, language: selectedLanguage.value })
    if (authStore.profile) {
      nickname.value = authStore.profile.nickname
    }
    message.value = t('cabinet.successMessage')
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('common.error')
  } finally {
    isUpdating.value = false
  }
}

function goBack() {
  router.push('/')
}

const showDeleteModal = ref(false)
const isDeleting = ref(false)

function openDeleteModal() {
  error.value = ''
  showDeleteModal.value = true
}

async function confirmDelete() {
  if (isDeleting.value) return
  isDeleting.value = true
  error.value = ''
  message.value = ''
  try {
    await authStore.deleteAccount()
    showDeleteModal.value = false
    router.push('/')
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('common.error')
  } finally {
    isDeleting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-background text-on-surface flex flex-col items-center">
    <!-- Top Bar -->
    <header class="bg-surface-container-low/80 backdrop-blur-xl text-primary font-headline tracking-tight top-0 sticky z-50 flex justify-between items-center w-full max-w-md px-6 py-4">
      <button @click="goBack" class="hover:opacity-80 transition-opacity active:scale-95 flex items-center justify-center">
        <span class="material-symbols-outlined text-on-surface text-xl">home</span>
      </button>
      <h1 class="text-lg font-bold text-on-surface tracking-tight text-center flex-1">{{ t('cabinet.title') }}</h1>
      <button @click="goBack" class="hover:opacity-80 transition-opacity active:scale-95 flex items-center justify-center">
        <span class="material-symbols-outlined text-on-surface text-xl">arrow_back</span>
      </button>
    </header>

    <main class="w-full max-w-md px-6 py-6 flex-grow space-y-6">
      <!-- Avatar Section -->
      <section class="flex flex-col items-center">
        <div 
          class="relative group cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-primary rounded-xl" 
          @click="isAvatarPickerOpen = true" 
          @keydown.enter="isAvatarPickerOpen = true"
          @keydown.space.prevent="isAvatarPickerOpen = true"
          tabindex="0"
          role="button" 
          aria-label="Change avatar" 
          data-testid="change-avatar-button"
        >
          <div class="w-24 h-24 rounded-xl overflow-hidden shadow-2xl bg-surface-container-low transition-transform duration-200 hover:scale-105 active:scale-95 flex items-center justify-center">
            <AvatarBase :avatar="authStore.profile?.avatar" />
          </div>
          <!-- Edit Overlay Icon -->
          <div class="absolute inset-0 bg-black/40 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-center justify-center text-white pointer-events-none">
            <span class="material-symbols-outlined text-2xl">edit</span>
          </div>
        </div>
        <div class="mt-3 text-center" v-if="authStore.profile">
          <h2 class="font-headline text-xl font-bold tracking-tight text-on-surface">{{ authStore.profile.nickname }}</h2>
          <p class="text-secondary font-headline text-[10px] uppercase tracking-[0.2em] mt-0.5 opacity-70">Clubhouse Member</p>
        </div>
      </section>

      <!-- Feedback Messages -->
      <div v-if="message" data-testid="success-message" class="p-3 bg-primary-container/20 text-primary rounded-xl text-xs font-semibold text-center">
        {{ message }}
      </div>
      <div v-if="error" data-testid="error-message" class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold text-center">
        {{ error }}
      </div>

      <!-- Form Content -->
      <div class="space-y-6">
        <!-- Nickname Field -->
        <div class="space-y-2">
          <label for="nickname" class="font-headline text-[10px] font-bold uppercase tracking-widest text-primary/80 ml-1">
            {{ t('cabinet.nickname') }}
          </label>
          <input 
            id="nickname"
            v-model="nickname"
            type="text"
            class="w-full bg-surface-container-highest text-on-surface px-4 py-2.5 rounded-lg focus:outline-none focus:ring-0 font-headline text-base transition-all"
            placeholder="Enter nickname"
          />
          <p class="text-[9px] text-on-surface-variant font-headline italic flex items-center gap-2 px-1">
            <span class="material-symbols-outlined text-[12px]">info</span>
            {{ t('cabinet.cooldownMessage') }}
          </p>
        </div>

        <!-- Language Selection -->
        <div class="space-y-2">
          <label class="font-headline text-[10px] font-bold uppercase tracking-widest text-primary/80 ml-1">
            {{ t('cabinet.language') }}
          </label>
          
          <div class="relative">
            <button 
              role="button"
              aria-haspopup="listbox"
              aria-label="Language"
              :aria-expanded="isDropdownOpen"
              @click="toggleDropdown"
              data-testid="language-select"
              class="w-full flex justify-between items-center bg-surface-container-low text-on-surface px-4 py-2.5 rounded-lg font-headline text-sm hover:bg-surface-container-highest/50 transition-colors"
            >
              <span>{{ selectedLanguage === 'EN' ? t('cabinet.english') : t('cabinet.german') }}</span>
              <span class="material-symbols-outlined text-sm">expand_more</span>
            </button>

            <!-- Dropdown Options -->
            <div 
              v-if="isDropdownOpen"
              role="listbox"
              class="absolute z-10 w-full mt-1 bg-surface-container-highest rounded-lg shadow-xl py-1 overflow-hidden"
            >
              <div 
                role="option"
                data-testid="lang-en"
                :aria-selected="selectedLanguage === 'EN'"
                @click="selectLanguage('EN')"
                class="px-4 py-2 text-sm font-headline cursor-pointer hover:bg-primary-container/20 hover:text-primary transition-colors flex items-center gap-2"
              >
                {{ t('cabinet.english') }}
              </div>
              <div 
                role="option"
                data-testid="lang-de"
                :aria-selected="selectedLanguage === 'DE'"
                @click="selectLanguage('DE')"
                class="px-4 py-2 text-sm font-headline cursor-pointer hover:bg-primary-container/20 hover:text-primary transition-colors flex items-center gap-2"
              >
                {{ t('cabinet.german') }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Danger Zone -->
      <section class="pt-6 space-y-3">
        <h3 class="font-headline text-[10px] font-bold uppercase tracking-widest text-red-400 ml-1">
          {{ t('cabinet.dangerZone') }}
        </h3>
        <button 
          @click="openDeleteModal"
          data-testid="delete-account-button"
          class="w-full py-3 rounded-lg bg-red-950/20 hover:bg-red-950/40 text-red-400 font-headline font-bold text-sm transition-colors flex items-center justify-center gap-2"
        >
          <span class="material-symbols-outlined text-sm">delete_forever</span>
          {{ t('cabinet.deleteAccount') }}
        </button>
      </section>
    </main>

    <!-- Footer Action -->
    <footer class="w-full max-w-md px-6 pb-6 pt-2">
      <button 
        @click="handleSave"
        data-testid="save-button"
        class="w-full py-3.5 rounded-xl bg-gradient-to-br from-primary to-primary-container text-background font-headline font-extrabold uppercase tracking-[0.2em] shadow-xl hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-3"
      >
        {{ t('common.save') }}
        <span aria-hidden="true" class="material-symbols-outlined font-bold">check_circle</span>
      </button>
      <p class="text-center mt-3 text-[9px] text-on-surface-variant font-headline uppercase tracking-widest opacity-40">
        Tic-Tac-Tore • Clubhouse Edition
      </p>
    </footer>

    <!-- Delete Confirmation Modal -->
    <Transition name="fade">
      <div 
        v-if="showDeleteModal" 
        class="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/75 backdrop-blur-md"
        role="dialog"
        aria-modal="true"
      >
        <div class="w-full max-w-sm bg-surface-container-low rounded-2xl p-6 space-y-6 shadow-2xl">
          <div class="text-center space-y-2">
            <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-red-950/30 text-red-400 mb-2">
              <span class="material-symbols-outlined text-2xl">warning</span>
            </div>
            <h2 class="font-headline text-lg font-bold text-on-surface">
              {{ t('cabinet.deleteTitle') }}
            </h2>
            <p class="text-xs text-on-surface-variant leading-relaxed">
              {{ t('cabinet.deleteConfirmMessage') }}
            </p>
          </div>

          <div v-if="error" data-testid="modal-error-message" class="p-3 bg-red-950/40 text-red-400 rounded-xl text-xs font-semibold text-center">
            {{ error }}
          </div>

          <div class="flex flex-col gap-2">
            <button 
              @click="confirmDelete"
              data-testid="confirm-delete-button"
              :disabled="isDeleting"
              class="w-full py-3 rounded-xl bg-red-600 hover:bg-red-700 text-white font-headline font-extrabold uppercase tracking-wider text-xs transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <span v-if="isDeleting" class="animate-spin material-symbols-outlined text-sm">sync</span>
              <span v-else>{{ t('cabinet.confirmDelete') }}</span>
            </button>
            
            <button 
              @click="showDeleteModal = false"
              :disabled="isDeleting"
              class="w-full py-3 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold text-xs transition-colors"
            >
              {{ t('common.cancel') }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Avatar Picker Modal -->
    <Transition name="fade">
      <AvatarPicker 
        v-if="isAvatarPickerOpen"
        @select="handleAvatarSelect"
        @close="isAvatarPickerOpen = false"
      />
    </Transition>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
