import React, { ChangeEventHandler } from 'react'
import { Form } from 'react-bootstrap'

import magic from '@iconify/icons-fa-solid/magic'
import images from '@iconify/icons-fa6-regular/images'
import { Icon } from '@iconify/react'

import { AppProps } from '../../pages'

const DisplayFormGroup: React.FC<AppProps> = ({ setSettings, settings }) => {
  const handleChange: ChangeEventHandler<HTMLInputElement> = (event) => {
    const newValue = event.target.checked

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        showAllMedia: newValue,
      },
    }))

    console.log(`[handleChange] showAllMedia: ${newValue}`)
  }

  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={magic} /> 表示オプション
      </Form.Label>

      <Form.Check type="checkbox">
        <Form.Check.Input
          type="checkbox"
          checked={settings.search.showAllMedia}
          onChange={handleChange}
        ></Form.Check.Input>
        <Form.Check.Label>
          <Icon icon={images} /> 複数枚ある場合はすべて表示する
        </Form.Check.Label>
      </Form.Check>
    </Form.Group>
  )
}

export default DisplayFormGroup
