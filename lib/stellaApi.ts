const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://apps.starry.blue/stella/api'

export type Summary = {
  entries: number
  media: number
}

export const getSummary = async (): Promise<Summary> => {
  return fetch(`${API_BASE_URL}/summary`)
    .then((response) => response.json())
    .then((data) => data as Summary)
}
