import { createI18n } from 'vue-i18n'
import en from '@/locales/en.json'
import de from '@/locales/de.json'

export type SupportedLocale = 'en' | 'de'
export const SUPPORTED_LOCALES: SupportedLocale[] = ['en', 'de']
const LOCALE_KEY = 'ttt_locale'

export function detectLocale(): SupportedLocale {
  try {
    const stored = localStorage.getItem(LOCALE_KEY) as SupportedLocale | null
    if (stored && SUPPORTED_LOCALES.includes(stored)) return stored
    const browser = (
      (navigator?.language ?? '').split('-')[0] ?? ''
    ).toLowerCase() as SupportedLocale
    return SUPPORTED_LOCALES.includes(browser) ? browser : 'en'
  } catch {
    return 'en'
  }
}

export const i18n = createI18n({
  legacy: false,
  locale: detectLocale(),
  fallbackLocale: 'en',
  messages: { en, de },
  datetimeFormats: {
    en: {
      short: { year: 'numeric', month: '2-digit', day: '2-digit' },
      long: { year: 'numeric', month: 'long', day: 'numeric' },
    },
    de: {
      short: { year: 'numeric', month: '2-digit', day: '2-digit' },
      long: { year: 'numeric', month: 'long', day: 'numeric' },
    },
  },
  numberFormats: {
    en: {
      decimal: { style: 'decimal', minimumFractionDigits: 0, maximumFractionDigits: 2 },
      percent: { style: 'percent', minimumFractionDigits: 0 },
    },
    de: {
      decimal: { style: 'decimal', minimumFractionDigits: 0, maximumFractionDigits: 2 },
      percent: { style: 'percent', minimumFractionDigits: 0 },
    },
  },
})
