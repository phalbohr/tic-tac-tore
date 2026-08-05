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

  it('renders anonymous fallback SVG when avatar is null or undefined and name is not provided', () => {
    const wrapper = mount(AvatarBase, {
      props: { avatar: null },
    })

    const svg = wrapper.find('[data-testid="avatar-svg"]')
    expect(svg.exists()).toBe(true)
    expect(svg.find('use').attributes('href')).toBe('/avatars.svg#ball-classic')
  })

  it('renders initials when custom avatar is missing and name is provided', () => {
    const wrapper = mount(AvatarBase, {
      props: { name: 'John Doe' },
    })

    const initials = wrapper.find('[data-testid="avatar-initials"]')
    expect(initials.exists()).toBe(true)
    expect(initials.text()).toBe('JD')
    expect(wrapper.find('[data-testid="avatar-svg"]').exists()).toBe(true)
  })

  it('renders custom avatar image AND initials if valid preset and name are provided', () => {
    const wrapper = mount(AvatarBase, {
      props: { avatar: 'ball-classic', name: 'John Doe' },
    })

    const svg = wrapper.find('[data-testid="avatar-svg"]')
    expect(svg.exists()).toBe(true)
    const initials = wrapper.find('[data-testid="avatar-initials"]')
    expect(initials.exists()).toBe(true)
    expect(initials.text()).toBe('JD')
  })

  it('renders rounded-full for circle shape and rounded-xl for square shape', () => {
    const circleWrapper = mount(AvatarBase, {
      props: { shape: 'circle' }
    })
    expect(circleWrapper.classes()).toContain('rounded-full')

    const squareWrapper = mount(AvatarBase, {
      props: { shape: 'square' }
    })
    expect(squareWrapper.classes()).toContain('rounded-xl')
  })

  it('renders an img element when a valid HTTP/HTTPS URL is provided as avatar', () => {
    const avatarUrl = 'https://lh3.googleusercontent.com/a/default-user'
    const wrapper = mount(AvatarBase, {
      props: { avatar: avatarUrl, name: 'John Doe' }
    })

    const img = wrapper.find('[data-testid="avatar-img"]')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toBe(avatarUrl)
    expect(wrapper.find('[data-testid="avatar-svg"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="avatar-initials"]').exists()).toBe(false)
  })
})

