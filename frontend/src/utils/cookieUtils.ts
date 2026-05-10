const COOKIE_SEPARATOR = '; '
const EQUALS_SIGN = '='

export function getCookie(name: string): string | undefined {
  const row = document.cookie
    .split(COOKIE_SEPARATOR)
    .find((row) => row.startsWith(`${name}${EQUALS_SIGN}`))

  if (!row) return undefined

  return row.substring(row.indexOf(EQUALS_SIGN) + 1)
}
