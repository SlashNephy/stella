export type PicPlatform = 'Twitter' | 'Pixiv' | 'Nijie'

export type PicSensitiveLevel = 0 | 1 | 2 | 3

export type PicKind = 0 | 1

export type PicExt = 'jpg' | 'jpeg' | 'png' | 'gif' | 'mp4'

export type PicMediaType = 'image' | 'jpg' | 'png' | 'video' | 'gif' | 'mp4'

export type PicSort =
  | 'title_ascending'
  | 'title_descending'
  | 'author_ascending'
  | 'author_descending'
  | 'rating_descending'
  | 'rating_ascending'
  | 'like_descending'
  | 'like_ascending'
  | 'bookmark_descending'
  | 'bookmark_ascending'
  | 'view_descending'
  | 'view_ascending'
  | 'retweet_descending'
  | 'retweet_ascending'
  | 'reply_descending'
  | 'reply_ascending'
  | 'added_descending'
  | 'added_ascending'
  | 'created_descending'
  | 'created_ascending'
  | 'manual_updated_descending'
  | 'manual_updated_ascending'
  | 'auto_updated_descending'
  | 'auto_updated_ascending'
  | 'random'
