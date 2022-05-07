import React from 'react'
import { Form } from 'react-bootstrap'

import magic from '@iconify/icons-fa-solid/magic'
import images from '@iconify/icons-fa6-regular/images'
import { Icon } from '@iconify/react'

const DisplayFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={magic}></Icon> 表示オプション
      </Form.Label>

      <Form.Check type="checkbox">
        <Form.Check.Input type="checkbox"></Form.Check.Input>
        <Form.Check.Label>
          <Icon icon={images} /> 複数枚ある場合はすべて表示する
        </Form.Check.Label>
      </Form.Check>
    </Form.Group>
  )
}

export default DisplayFormGroup
