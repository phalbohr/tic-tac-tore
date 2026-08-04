export function getInitials(name?: string | null): string {
  if (!name) return ''
  const trimmed = name.trim()
  if (!trimmed) return ''
  const words = trimmed.split(/\s+/).filter(Boolean)
  const first = words[0]
  if (!first) return ''
  if (words.length === 1) {
    return first.charAt(0).toUpperCase()
  }
  const second = words[1]
  if (!second) {
    return first.charAt(0).toUpperCase()
  }
  return (first.charAt(0) + second.charAt(0)).toUpperCase()
}
