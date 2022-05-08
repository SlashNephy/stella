import React, { ChangeEventHandler } from 'react'
import { Form } from 'react-bootstrap'

import cloud from '@iconify/icons-fa6-solid/cloud'
import { Icon } from '@iconify/react'

import { PicPlatform } from '../../lib/models'
import { AppProps } from '../../pages'

type PlatformDefinition = {
  readonly value?: PicPlatform
  readonly label: string
}

const DEFINITIONS: PlatformDefinition[] = [
  {
    label: '任意のプラットフォーム',
  },
  {
    value: 'Twitter',
    label: 'Twitter',
  },
  {
    value: 'Pixiv',
    label: 'Pixiv',
  },
  {
    value: 'Nijie',
    label: 'ニジエ (R-18)',
  },
]

const PlatformFormGroup: React.FC<AppProps> = ({ setSettings, settings }) => {
  const handleChange: ChangeEventHandler<HTMLSelectElement> = (event) => {
    const newValue = event.target.value as PicPlatform

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        platform: newValue,
      },
    }))

    console.log(`[handleChange] platform: ${newValue}`)
  }

  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={cloud} /> プラットフォーム
      </Form.Label>

      <Form.Select value={settings.search.platform} onChange={handleChange}>
        {DEFINITIONS.map(({ label, value }) => (
          <React.Fragment key={label}>
            <option value={value ?? ''}>{label}</option>
          </React.Fragment>
        ))}
      </Form.Select>
    </Form.Group>
  )
}

export default PlatformFormGroup
