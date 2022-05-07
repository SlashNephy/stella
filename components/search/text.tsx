import React from 'react'
import { Col, Form, InputGroup, Row } from 'react-bootstrap'

import fileLines from '@iconify/icons-fa6-solid/file-lines'
import font from '@iconify/icons-fa6-solid/font'
import magnifyingGlass from '@iconify/icons-fa6-solid/magnifying-glass'
import tags from '@iconify/icons-fa6-solid/tags'
import user from '@iconify/icons-fa6-solid/user'
import { Icon } from '@iconify/react'

const TextFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={magnifyingGlass}></Icon> 検索
      </Form.Label>

      <Row>
        <Col>
          <InputGroup>
            <InputGroup.Text>
              <Icon icon={font} />
            </InputGroup.Text>
            <Form.Control type="text" placeholder="タイトル..." />
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <InputGroup.Text>
              <Icon icon={fileLines} />
            </InputGroup.Text>
            <Form.Control type="text" placeholder="本文..." />
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <InputGroup.Text>
              <Icon icon={tags} />
            </InputGroup.Text>
            <Form.Control type="text" placeholder="タグ..." />
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <InputGroup.Text>
              <Icon icon={user} />
            </InputGroup.Text>
            <Form.Control type="text" placeholder="作者名..." />
          </InputGroup>
        </Col>
      </Row>
    </Form.Group>
  )
}

export default TextFormGroup
