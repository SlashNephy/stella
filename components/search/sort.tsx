import React from 'react'
import { Form } from 'react-bootstrap'

import sortAlphaDown from '@iconify/icons-fa-solid/sort-alpha-down'
import { Icon } from '@iconify/react'

const SortFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={sortAlphaDown}></Icon> ソート
      </Form.Label>

      <Form.Select defaultValue="manual_updated_descending">
        <option value="title_ascending">タイトル (A→Z順)</option>
        <option value="title_descending">タイトル (Z→A順)</option>
        <option value="author_ascending">作者名 (A→Z順)</option>
        <option value="author_descending">作者名 (Z→A順)</option>
        <option value="rating_descending">レート (降順)</option>
        <option value="rating_ascending">レート (昇順)</option>
        <option value="like_descending">いいね数 (降順)</option>
        <option value="like_ascending">いいね数 (昇順)</option>
        <option value="bookmark_descending">ブックマーク数 (降順)</option>
        <option value="bookmark_ascending">ブックマーク数 (昇順)</option>
        <option value="view_descending">閲覧数 (降順)</option>
        <option value="view_ascending">閲覧数 (昇順)</option>
        <option value="retweet_descending">RT数 (降順)</option>
        <option value="retweet_ascending">RT数 (昇順)</option>
        <option value="reply_descending">リプライ数 / コメント数 (降順)</option>
        <option value="reply_ascending">リプライ数 / コメント数 (昇順)</option>
        <option value="added_descending">追加日時 (新しい順)</option>
        <option value="added_ascending">追加日時 (古い順)</option>
        <option value="created_descending">投稿日時 (新しい順)</option>
        <option value="created_ascending">投稿日時 (古い順)</option>
        <option value="manual_updated_descending">更新日時 (新しい順)</option>
        <option value="manual_updated_ascending">更新日時 (古い順)</option>
        <option value="auto_updated_descending">自動更新日時 (新しい順)</option>
        <option value="auto_updated_ascending">自動更新日時 (古い順)</option>
        <option value="random">ランダム</option>
      </Form.Select>
    </Form.Group>
  )
}

export default SortFormGroup
