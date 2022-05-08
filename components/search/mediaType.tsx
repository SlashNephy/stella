import React, { ChangeEventHandler } from 'react'
import { Form } from 'react-bootstrap'

import fileImage from '@iconify/icons-fa6-solid/file-image'
import { Icon } from '@iconify/react'

import { PicMediaType } from '../../lib/models'
import { AppProps } from '../../pages'

type MediaTypeDefinition = {
  readonly value?: PicMediaType
  readonly label: string
}

const DEFINITIONS: MediaTypeDefinition[] = [
  {
    label: '任意のメディアタイプ',
  },
  {
    value: 'image',
    label: '画像 - すべて',
  },
  {
    value: 'jpg',
    label: '画像 - JPG',
  },
  {
    value: 'png',
    label: '画像 - PNG',
  },
  {
    value: 'video',
    label: '動画 - すべて',
  },
  {
    value: 'gif',
    label: '動画 - GIF',
  },
  {
    value: 'mp4',
    label: '動画 - MP4',
  },
]

const MediaTypeFormGroup: React.FC<AppProps> = ({ setSettings, settings }) => {
  const handleChange: ChangeEventHandler<HTMLSelectElement> = (event) => {
    const newValue = event.target.value as PicMediaType

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        mediaType: newValue,
      },
    }))

    console.log(`[handleChange] mediaType: ${newValue}`)
  }

  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={fileImage} /> メディアタイプ
      </Form.Label>

      <Form.Select value={settings.search.mediaType} onChange={handleChange}>
        {DEFINITIONS.map(({ label, value }) => (
          <React.Fragment key={label}>
            <option value={value ?? ''}>{label}</option>
          </React.Fragment>
        ))}
      </Form.Select>
    </Form.Group>
  )
}

export default MediaTypeFormGroup
