const COOKIE_SEPARATOR = '; '
const EQUALS_SIGN = '='

export function getCookie(name: string): string | undefined {
  const row = document.cookie
    .split(COOKIE_SEPARATOR)
    .find((row) => row.startsWith(`${name}${EQUALS_SIGN}`))

  if (!row) return undefined

  return row.substring(row.indexOf(EQUALS_SIGN) + 1)
}

export function deleteCookie(name: string) {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
}
