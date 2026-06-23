<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const authStore = useAuthStore()

const currentSlide = ref(0)
const totalSlides = 3
const carouselRef = ref<HTMLElement | null>(null)

const isCompleting = ref(false)
const errorMessage = ref('')

const handleKeydown = (e: KeyboardEvent) => {
  const target = e.target as HTMLElement
  if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) return
  
  if (e.key === 'Escape') {
    completeTutorial()
  } else if (e.key === 'ArrowRight' || e.key === ' ') {
    e.preventDefault() // prevent page scroll for space
    if (currentSlide.value < totalSlides - 1) {
      scrollToSlide(currentSlide.value + 1)
    } else if (e.key === ' ') {
      completeTutorial()
    }
  } else if (e.key === 'ArrowLeft') {
    if (currentSlide.value > 0) {
      scrollToSlide(currentSlide.value - 1)
    }
  }
}

let scrollTimeout: ReturnType<typeof setTimeout> | null = null
const handleScroll = () => {
  if (!carouselRef.value) return
  if (scrollTimeout) return
  scrollTimeout = setTimeout(() => {
    scrollTimeout = null
    if (!carouselRef.value) return
    const scrollLeft = carouselRef.value.scrollLeft
    const width = carouselRef.value.clientWidth
    if (width === 0) return
    currentSlide.value = Math.max(0, Math.min(totalSlides - 1, Math.round(scrollLeft / width)))
  }, 50)
}

const isScrolling = ref(false)

const scrollToSlide = (index: number) => {
  if (!carouselRef.value || isScrolling.value) return
  isScrolling.value = true
  
  const targetScrollLeft = carouselRef.value.clientWidth * index
  carouselRef.value.scrollTo({
    left: targetScrollLeft,
    behavior: 'smooth',
  })
  
  const checkScroll = () => {
    if (!carouselRef.value) return
    if (Math.abs(carouselRef.value.scrollLeft - targetScrollLeft) < 5) {
      isScrolling.value = false
    } else {
      requestAnimationFrame(checkScroll)
    }
  }
  requestAnimationFrame(checkScroll)
}

const completeTutorial = async () => {
  if (isCompleting.value) return
  isCompleting.value = true
  errorMessage.value = ''
  try {
    await authStore.updateProfile({ tutorialCompleted: true })
  } catch (error) {
    console.error('Failed to complete tutorial', error)
    const errStr = t('tutorial.error')
    errorMessage.value = errStr === 'tutorial.error' ? 'Failed to complete tutorial. Please try again.' : errStr
  } finally {
    isCompleting.value = false
  }
}

let prevOverflow = ''
const dialogRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (carouselRef.value) {
    carouselRef.value.addEventListener('scroll', handleScroll, { passive: true })
  }
  document.addEventListener('keydown', handleKeydown)
  prevOverflow = document.body.style.overflow
  document.body.style.overflow = 'hidden'
  if (dialogRef.value) dialogRef.value.focus()
})

onUnmounted(() => {
  if (carouselRef.value) {
    carouselRef.value.removeEventListener('scroll', handleScroll)
  }
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = prevOverflow
})
</script>

<template>
  <div
    ref="dialogRef"
    class="fixed inset-0 z-[100] bg-surface-container/95 backdrop-blur-xl flex flex-col w-full h-full outline-none"
    role="dialog"
    aria-modal="true"
    aria-label="Tutorial"
    tabindex="-1"
  >
    <!-- Carousel Container -->
    <div
      ref="carouselRef"
      class="flex-1 flex overflow-x-auto snap-x snap-mandatory hide-scrollbar"
      data-testid="tutorial-carousel"
    >
      <!-- Slide 1 -->
      <div
        class="w-full h-full flex-shrink-0 snap-center flex flex-col items-center justify-center p-8 gap-8"
        :class="{ invisible: currentSlide !== 0 }"
        :aria-hidden="currentSlide !== 0"
      >
        <div class="w-48 h-48 bg-primary/10 rounded-full flex items-center justify-center">
          <span class="material-symbols-outlined text-primary" style="font-size: 10rem;">add_circle</span>
        </div>
        <div class="text-center space-y-4 max-w-xs">
          <h2 class="text-3xl font-headline font-bold text-on-surface">
            {{ t('tutorial.slide1.title') }}
          </h2>
          <p class="text-on-surface-variant font-body text-lg leading-relaxed">
            {{ t('tutorial.slide1.description') }}
          </p>
        </div>
      </div>

      <!-- Slide 2 -->
      <div
        class="w-full h-full flex-shrink-0 snap-center flex flex-col items-center justify-center p-8 gap-8"
        :class="{ invisible: currentSlide !== 1 }"
        :aria-hidden="currentSlide !== 1"
      >
        <div class="w-48 h-48 bg-primary/10 rounded-full flex items-center justify-center">
          <span class="material-symbols-outlined text-primary" style="font-size: 10rem;">notifications_active</span>
        </div>
        <div class="text-center space-y-4 max-w-xs">
          <h2 class="text-3xl font-headline font-bold text-on-surface">
            {{ t('tutorial.slide2.title') }}
          </h2>
          <p class="text-on-surface-variant font-body text-lg leading-relaxed">
            {{ t('tutorial.slide2.description') }}
          </p>
        </div>
      </div>

      <!-- Slide 3 -->
      <div
        class="w-full h-full flex-shrink-0 snap-center flex flex-col items-center justify-center p-8 gap-8"
        :class="{ invisible: currentSlide !== 2 }"
        :aria-hidden="currentSlide !== 2"
      >
        <div class="w-48 h-48 bg-primary/10 rounded-full flex items-center justify-center">
          <span class="material-symbols-outlined text-primary" style="font-size: 10rem;">leaderboard</span>
        </div>
        <div class="text-center space-y-4 max-w-xs">
          <h2 class="text-3xl font-headline font-bold text-on-surface">
            {{ t('tutorial.slide3.title') }}
          </h2>
          <p class="text-on-surface-variant font-body text-lg leading-relaxed">
            {{ t('tutorial.slide3.description') }}
          </p>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div
      v-if="errorMessage"
      class="text-error text-center font-body text-sm px-4 py-2"
      role="alert"
    >
      {{ errorMessage }}
    </div>

    <!-- Navigation / Controls -->
    <div class="h-32 flex flex-col items-center justify-center gap-6 px-8 pb-8">
      <!-- Dots -->
      <div class="flex gap-3">
        <button
          v-for="(_, index) in totalSlides"
          :key="index"
          @click="scrollToSlide(index)"
          class="w-2.5 h-2.5 rounded-full transition-all duration-300"
          :class="currentSlide === index ? 'bg-primary scale-125' : 'bg-on-surface/20'"
          :aria-current="currentSlide === index ? 'true' : undefined"
          :aria-label="`Go to slide ${index + 1}`"
        />
      </div>

      <!-- Actions -->
      <div class="flex justify-between w-full max-w-sm items-center">
        <button
          @click="completeTutorial"
          class="text-on-surface-variant font-headline font-medium tracking-wide hover:text-on-surface transition-colors px-4 py-2"
          data-testid="tutorial-skip"
        >
          {{ t('tutorial.skip') }}
        </button>

        <button
          v-if="currentSlide < totalSlides - 1"
          @click="scrollToSlide(currentSlide + 1)"
          class="bg-primary text-on-primary px-8 py-3 rounded-full font-headline font-bold tracking-wide shadow-lg hover:shadow-xl transition-all active:scale-95"
          data-testid="tutorial-next"
        >
          {{ t('tutorial.next') }}
        </button>

        <button
          v-else
          @click="completeTutorial"
          :disabled="isCompleting"
          class="bg-primary text-on-primary px-8 py-3 rounded-full font-headline font-bold tracking-wide shadow-lg hover:shadow-xl transition-all active:scale-95 disabled:opacity-50"
          data-testid="tutorial-finish"
        >
          {{ isCompleting ? t('common.loading') : t('tutorial.finish') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}
</style>
