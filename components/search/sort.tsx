import React, { ChangeEventHandler } from 'react'
import { Form } from 'react-bootstrap'

import sortAlphaDown from '@iconify/icons-fa-solid/sort-alpha-down'
import { Icon } from '@iconify/react'

import { PicSort } from '../../lib/models'
import { AppProps } from '../../pages'

type SortDefinition = {
  readonly value: PicSort
  readonly label: string
}

const DEFINITIONS: SortDefinition[] = [
  { value: 'title_ascending', label: 'タイトル (A→Z順)' },
  { value: 'title_descending', label: 'タイトル (Z→A順)' },
  { value: 'author_ascending', label: '作者名 (A→Z順)' },
  { value: 'author_descending', label: '作者名 (Z→A順)' },
  { value: 'rating_descending', label: 'レート (降順)' },
  { value: 'rating_ascending', label: 'レート (昇順)' },
  { value: 'like_descending', label: 'いいね数 (降順)' },
  { value: 'like_ascending', label: 'いいね数 (昇順)' },
  { value: 'bookmark_descending', label: 'ブックマーク数 (降順)' },
  { value: 'bookmark_ascending', label: 'ブックマーク数 (昇順)' },
  { value: 'view_descending', label: '閲覧数 (降順)' },
  { value: 'view_ascending', label: '閲覧数 (昇順)' },
  { value: 'retweet_descending', label: 'RT数 (降順)' },
  { value: 'retweet_ascending', label: 'RT数 (昇順)' },
  { value: 'reply_descending', label: 'リプライ数 / コメント数 (降順)' },
  { value: 'reply_ascending', label: 'リプライ数 / コメント数 (昇順)' },
  { value: 'added_descending', label: '追加日時 (新しい順)' },
  { value: 'added_ascending', label: '追加日時 (古い順)' },
  { value: 'created_descending', label: '投稿日時 (新しい順)' },
  { value: 'created_ascending', label: '投稿日時 (古い順)' },
  { value: 'manual_updated_descending', label: '更新日時 (新しい順)' },
  { value: 'manual_updated_ascending', label: '更新日時 (古い順)' },
  { value: 'auto_updated_descending', label: '自動更新日時 (新しい順)' },
  { value: 'auto_updated_ascending', label: '自動更新日時 (古い順)' },
  { value: 'random', label: 'ランダム' },
]

const SortFormGroup: React.FC<AppProps> = ({ setSettings, settings }) => {
  const handleChange: ChangeEventHandler<HTMLSelectElement> = (event) => {
    const newValue = event.target.value as PicSort

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        sort: newValue,
      },
    }))

    console.log(`[handleChange] sort: ${newValue}`)
  }

  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={sortAlphaDown} /> ソート
      </Form.Label>

      <Form.Select value={settings.search.sort} onChange={handleChange}>
        {DEFINITIONS.map(({ label, value }) => (
          <React.Fragment key={value}>
            <option value={value}>{label}</option>
          </React.Fragment>
        ))}
      </Form.Select>
    </Form.Group>
  )
}

export default SortFormGroup
