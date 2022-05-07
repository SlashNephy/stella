import React from 'react'
import { Col, Form, InputGroup, Row } from 'react-bootstrap'

import replyAll from '@iconify/icons-fa-solid/reply-all'
import retweet from '@iconify/icons-fa-solid/retweet'
import bookmark from '@iconify/icons-fa6-regular/bookmark'
import eye from '@iconify/icons-fa6-regular/eye'
import heart from '@iconify/icons-fa6-regular/heart'
import star from '@iconify/icons-fa6-regular/star'
import chartLine from '@iconify/icons-fa6-solid/chart-line'
import { Icon } from '@iconify/react'

const RatingFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={chartLine}></Icon> レート
      </Form.Label>

      <Row>
        <Col>
          <InputGroup>
            <Form.Control type="number"></Form.Control>
            <InputGroup.Text>≦</InputGroup.Text>
            <InputGroup.Text>
              <Icon icon={star} />
            </InputGroup.Text>
            <InputGroup.Text>≦</InputGroup.Text>
            <Form.Control type="number"></Form.Control>
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <Form.Control type="number"></Form.Control>
            <InputGroup.Text>≦</InputGroup.Text>
            <InputGroup.Text>
              <Icon icon={bookmark} />
            </InputGroup.Text>
            <InputGroup.Text>≦</InputGroup.Text>
            <Form.Control type="number"></Form.Control>
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <Form.Control type="number"></Form.Control>
            <InputGroup.Text>≦</InputGroup.Text>
            <InputGroup.Text>
              <Icon icon={eye} />
            </InputGroup.Text>
            <InputGroup.Text>≦</InputGroup.Text>
            <Form.Control type="number"></Form.Control>
          </InputGroup>
        </Col>
      </Row>

      <Row>
        <Col>
          <InputGroup>
            <Form.Control type="number"></Form.Control>
            <InputGroup.Text>≦</InputGroup.Text>
            <InputGroup.Text>
              <Icon icon={heart} />
            </InputGroup.Text>
            <InputGroup.Text>≦</InputGroup.Text>
            <Form.Control type="number"></Form.Control>
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <Form.Control type="number"></Form.Control>
            <InputGroup.Text>≦</InputGroup.Text>
            <InputGroup.Text>
              <Icon icon={retweet} />
            </InputGroup.Text>
            <InputGroup.Text>≦</InputGroup.Text>
            <Form.Control type="number"></Form.Control>
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <Form.Control type="number"></Form.Control>
            <InputGroup.Text>≦</InputGroup.Text>
            <InputGroup.Text>
              <Icon icon={replyAll} />
            </InputGroup.Text>
            <InputGroup.Text>≦</InputGroup.Text>
            <Form.Control type="number"></Form.Control>
          </InputGroup>
        </Col>
      </Row>
    </Form.Group>
  )
}

export default RatingFormGroup
