import React from 'react'
import { Form } from 'react-bootstrap'

import ban from '@iconify/icons-fa-solid/ban'
import checkCircle from '@iconify/icons-fa-solid/check-circle'
import exclamationTriangle from '@iconify/icons-fa-solid/exclamation-triangle'
import filter from '@iconify/icons-fa-solid/filter'
import skullCrossbones from '@iconify/icons-fa-solid/skull-crossbones'
import { Icon } from '@iconify/react'

const ContentsFilterFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={filter}></Icon> コンテンツフィルター
      </Form.Label>

      <Form.Check type="checkbox">
        <Form.Check.Input type="checkbox"></Form.Check.Input>
        <Form.Check.Label>
          <Icon icon={checkCircle} /> 全年齢対象
        </Form.Check.Label>
      </Form.Check>

      <Form.Check type="checkbox">
        <Form.Check.Input type="checkbox"></Form.Check.Input>
        <Form.Check.Label>
          <Icon icon={exclamationTriangle} /> R-15 (軽度な性描写)
        </Form.Check.Label>
      </Form.Check>

      <Form.Check type="checkbox">
        <Form.Check.Input type="checkbox"></Form.Check.Input>
        <Form.Check.Label>
          <Icon icon={ban} /> R-18 (露骨な性描写)
        </Form.Check.Label>
      </Form.Check>

      <Form.Check type="checkbox">
        <Form.Check.Input type="checkbox"></Form.Check.Input>
        <Form.Check.Label>
          <Icon icon={skullCrossbones} /> R-18G (グロテスクな表現)
        </Form.Check.Label>
      </Form.Check>
    </Form.Group>
  )
}

export default ContentsFilterFormGroup
