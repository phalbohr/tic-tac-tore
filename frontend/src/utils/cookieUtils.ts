const COOKIE_SEPARATOR = '; '
const EQUALS_SIGN = '='

export function getCookie(name: string): string | undefined {
  if (typeof document === 'undefined') return undefined
  const matches = document.cookie.match(new RegExp(
    '(?:^|; )' + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + '=([^;]*)'
  ))
  const val = matches?.[1]
  return val !== undefined ? decodeURIComponent(val) : undefined
}





export function deleteCookie(name: string) {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
}

export function getCsrfHeaders(): Record<string, string> {
  const token = getCookie('XSRF-TOKEN')
  return token ? { 'X-XSRF-TOKEN': decodeURIComponent(token) } : {}
}
