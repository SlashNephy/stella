import { PicMediaType, PicPlatform, PicSensitiveLevel, PicSort } from './models'

export type TextSearchSettings = {
  readonly title?: string
  readonly description?: string
  readonly tags?: string
  readonly author?: string
}

// TODO: Date で管理したい
export type TimeSearchSetting = {
  readonly since?: string
  readonly until?: string
}

export type TimeSearchSettings = {
  readonly created?: TimeSearchSetting
  readonly added?: TimeSearchSetting
  readonly updated?: TimeSearchSetting
}

export type PopularitySearchSetting = {
  readonly min?: number
  readonly max?: number
}

export type PopularitySearchSettings = {
  readonly rating?: PopularitySearchSetting
  readonly bookmark?: PopularitySearchSetting
  readonly view?: PopularitySearchSetting
  readonly like?: PopularitySearchSetting
  readonly retweet?: PopularitySearchSetting
  readonly reply?: PopularitySearchSetting
}

export type SearchOptionsSettings = {
  readonly platform?: PicPlatform
  readonly mediaType?: PicMediaType
  readonly sensitiveLevels: PicSensitiveLevel[]
  readonly sort: PicSort
}

export type SearchSettings = TextSearchSettings &
  TimeSearchSettings &
  PopularitySearchSettings &
  SearchOptionsSettings & {
    readonly showAllMedia: boolean
  }

export type Settings = {
  readonly search: SearchSettings
  readonly isOpeningSearchBox?: boolean
  readonly isDarkTheme?: boolean
}

export const DEFAULT_SEARCH_SETTINGS: SearchSettings = {
  sensitiveLevels: [0],
  sort: 'manual_updated_descending',
  showAllMedia: true,
}

export const DEFAULT_SETTINGS: Settings = {
  search: DEFAULT_SEARCH_SETTINGS,
}
