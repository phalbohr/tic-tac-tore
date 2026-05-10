export function getCookie(name: string): string | undefined {
  const row = document.cookie
    .split('; ')
    .find((row) => row.startsWith(`${name}=`))

  if (!row) return undefined

  return row.substring(row.indexOf('=') + 1)
}
