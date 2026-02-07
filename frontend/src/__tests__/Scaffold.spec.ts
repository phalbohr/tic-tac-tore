import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import router from '../router'
import { useCounterStore } from '../stores/counter'

describe('Scaffold Verification', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('router should be configured', () => {
    expect(router).toBeDefined()
    expect(router.options.routes).toBeDefined()
    expect(router.options.routes.length).toBeGreaterThan(0)
  })

  it('pinia store should be functional', () => {
    const counter = useCounterStore()
    expect(counter.count).toBe(0)
    counter.increment()
    expect(counter.count).toBe(1)
  })
})
