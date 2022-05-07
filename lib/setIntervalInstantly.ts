export const setIntervalInstantly = (callback: () => void, ms: number): NodeJS.Timer => {
  const timer = setInterval(callback, ms)
  callback()
  return timer
}
