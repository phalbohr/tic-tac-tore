import { defineStore } from 'pinia'
import { ref } from 'vue'
import { i18n, type SupportedLocale, SUPPORTED_LOCALES } from '@/plugins/i18n'

const LOCALE_KEY = 'ttt_locale'

export const useLocaleStore = defineStore('locale', () => {
  const locale = ref<SupportedLocale>(i18n.global.locale.value as SupportedLocale)

  function setLocale(newLocale: SupportedLocale) {
    if (!SUPPORTED_LOCALES.includes(newLocale)) return
    locale.value = newLocale
    ;(i18n.global.locale as { value: SupportedLocale }).value = newLocale
    localStorage.setItem(LOCALE_KEY, newLocale)
  }

  return { locale, setLocale }
})
