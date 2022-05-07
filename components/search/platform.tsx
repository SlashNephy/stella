import React from 'react'
import { Form } from 'react-bootstrap'

import cloud from '@iconify/icons-fa6-solid/cloud'
import { Icon } from '@iconify/react'

const PlatformFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={cloud}></Icon> プラットフォーム
      </Form.Label>

      <Form.Select>
        <option>任意のプラットフォーム</option>
        <option value="Twitter">Twitter</option>
        <option value="Pixiv">Pixiv</option>
        <option value="Nijie">Nijie</option>
      </Form.Select>
    </Form.Group>
  )
}

export default PlatformFormGroup
