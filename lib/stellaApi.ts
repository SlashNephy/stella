import { PicExt, PicKind, PicPlatform, PicSensitiveLevel } from './models'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://apps.starry.blue/stella/api'

export type Summary = {
  entries: number
  media: number
}

export type Pic = {
  _id: string
  title: string
  description: string
  url: string
  tags: {
    value: string
    user?: string
    locked: boolean
  }[]
  platform: PicPlatform
  sensitive_level: PicSensitiveLevel
  kind: PicKind
  timestamp: {
    created: number
    added: number
    manual_updated: number
    auto_updated: number
    archived: boolean
  }
  author: {
    name: string
    username?: string
    url: string
    id?: string
  }
  media: {
    index: number
    filename: string
    original: string
    ext: PicExt
  }[]
  rating: {
    count: number
    score: number
  }
  popularity: {
    like?: number
    bookmark?: number
    view?: number
    retweet?: number
    reply?: number
  }
}

export type PicTags = {
  tags: string[]
}

export const getSummary = async (): Promise<Summary> => {
  return fetch(`${API_BASE_URL}/summary`)
    .then((response) => response.json())
    .then((data) => data as Summary)
}

export const refreshEntry = async (pic: Pic): Promise<Pic> => {
  return fetch(`${API_BASE_URL}/pic/${pic._id}/refresh`, {
    method: 'PUT',
  })
    .then((response) => response.json())
    .then((data) => data as Pic)
}

export const addTag = async (pic: Pic, tag: string): Promise<Pic> => {
  const body = new FormData()
  body.append('tag', tag)

  return fetch(`${API_BASE_URL}/pic/${pic._id}/tag`, {
    method: 'PUT',
    body,
  })
    .then((response) => response.json())
    .then((data) => data as Pic)
}

export const deleteTag = async (pic: Pic, tag: string): Promise<Pic> => {
  const body = new FormData()
  body.append('tag', tag)

  return fetch(`${API_BASE_URL}/pic/${pic._id}/tag`, {
    method: 'DELETE',
    body,
  })
    .then((response) => response.json())
    .then((data) => data as Pic)
}

export const updateSensitiveLevel = async (pic: Pic, level: string): Promise<Pic> => {
  const body = new FormData()
  body.append('sensitive_level', level)

  return fetch(`${API_BASE_URL}/pic/${pic._id}/sensitive_level`, {
    method: 'PATCH',
    body,
  })
    .then((response) => response.json())
    .then((data) => data as Pic)
}

export const getRelationalTags = async (pic: Pic, count: number): Promise<PicTags> => {
  const params = new URLSearchParams()
  params.append('id', pic._id)
  params.append('sensitive_level', pic.sensitive_level.toString())
  params.append('count', count.toString())

  return fetch(`${API_BASE_URL}/query/tags?${params}`)
    .then((response) => response.json())
    .then((data) => data as PicTags)
}

export const searchTags = async (pic: Pic, name: string, count: number): Promise<PicTags> => {
  const params = new URLSearchParams()
  params.append('id', pic._id)
  params.append('name', name)
  params.append('sensitive_level', pic.sensitive_level.toString())
  params.append('count', count.toString())

  return fetch(`${API_BASE_URL}/query/tags?${params}`)
    .then((response) => response.json())
    .then((data) => data as PicTags)
}

export const getMediaUrl = (pic: Pic, index: number): string => {
  return `${API_BASE_URL}/media/${pic.media[index].filename}`
}
