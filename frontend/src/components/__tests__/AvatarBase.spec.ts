import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import AvatarBase from '@/components/AvatarBase.vue'

describe('AvatarBase', () => {
  it('renders a preset SVG avatar when valid preset name is provided', () => {
    const wrapper = mount(AvatarBase, {
      props: { avatar: 'ball-classic' },
    })

    const svg = wrapper.find('[data-testid="avatar-svg"]')
    expect(svg.exists()).toBe(true)
    expect(svg.find('use').attributes('href')).toBe('/avatars.svg#ball-classic')
    expect(wrapper.find('img').exists()).toBe(false)
  })

  it('renders anonymous fallback SVG when avatar is anonymous', () => {
    const wrapper = mount(AvatarBase, {
      props: { avatar: 'anonymous' },
    })

    const svg = wrapper.find('[data-testid="avatar-svg"]')
    expect(svg.exists()).toBe(true)
    expect(svg.find('use').attributes('href')).toBe('/avatars.svg#anonymous')
  })

  it('renders anonymous fallback SVG when avatar is null or undefined', () => {
    const wrapper = mount(AvatarBase, {
      props: { avatar: null },
    })

    const svg = wrapper.find('[data-testid="avatar-svg"]')
    expect(svg.exists()).toBe(true)
    expect(svg.find('use').attributes('href')).toBe('/avatars.svg#anonymous')
  })

  it('renders img element when avatar is a URL', () => {
    const wrapper = mount(AvatarBase, {
      props: { avatar: 'http://example.com/avatar.png' },
    })

    expect(wrapper.find('img').exists()).toBe(true)
    expect(wrapper.find('img').attributes('src')).toBe('http://example.com/avatar.png')
  })
})
