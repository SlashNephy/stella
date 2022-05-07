import React from 'react'
import { Col, Form, InputGroup, Row } from 'react-bootstrap'

import calendarCheck from '@iconify/icons-fa6-regular/calendar-check'
import calendarDays from '@iconify/icons-fa6-regular/calendar-days'
import calendarPlus from '@iconify/icons-fa6-regular/calendar-plus'
import clock from '@iconify/icons-fa6-regular/clock'
import { Icon } from '@iconify/react'

const TimeFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={clock}></Icon> 期間
      </Form.Label>

      <Row>
        <Col>
          <InputGroup>
            <InputGroup.Text>
              <Icon icon={calendarDays} />
            </InputGroup.Text>
            <Form.Control type="date" />
            <InputGroup.Text>～</InputGroup.Text>
            <Form.Control type="date" />
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <InputGroup.Text>
              <Icon icon={calendarPlus} />
            </InputGroup.Text>
            <Form.Control type="date" />
            <InputGroup.Text>～</InputGroup.Text>
            <Form.Control type="date" />
          </InputGroup>
        </Col>

        <Col>
          <InputGroup>
            <InputGroup.Text>
              <Icon icon={calendarCheck} />
            </InputGroup.Text>
            <Form.Control type="date" />
            <InputGroup.Text>～</InputGroup.Text>
            <Form.Control type="date" />
          </InputGroup>
        </Col>
      </Row>
    </Form.Group>
  )
}

export default TimeFormGroup
