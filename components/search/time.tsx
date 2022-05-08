import React, { ChangeEventHandler } from 'react'
import { Col, Form, InputGroup, Row } from 'react-bootstrap'

import calendarCheck from '@iconify/icons-fa6-regular/calendar-check'
import calendarDays from '@iconify/icons-fa6-regular/calendar-days'
import calendarPlus from '@iconify/icons-fa6-regular/calendar-plus'
import clock from '@iconify/icons-fa6-regular/clock'
import { Icon, IconifyIcon } from '@iconify/react'

import { TimeSearchSettings } from '../../lib/settings'
import { AppProps } from '../../pages'

type TimeDefinition = {
  readonly name: keyof TimeSearchSettings
  readonly label: string
  readonly icon: IconifyIcon
}

const DEFINITIONS: TimeDefinition[] = [
  {
    name: 'created',
    label: '投稿日時',
    icon: calendarDays,
  },
  {
    name: 'added',
    label: '追加日時',
    icon: calendarPlus,
  },
  {
    name: 'updated',
    label: '更新日時',
    icon: calendarCheck,
  },
]

const TimeFormGroup: React.FC<AppProps> = ({ setSettings, settings }) => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={clock} /> 期間
      </Form.Label>

      <Row>
        {DEFINITIONS.map(({ icon, label, name }) => (
          <React.Fragment key={name}>
            <Col>
              <TimeInput settings={settings} setSettings={setSettings} name={name} label={label} icon={icon} />
            </Col>
          </React.Fragment>
        ))}
      </Row>
    </Form.Group>
  )
}

type TimeInputProps = AppProps & TimeDefinition

const TimeInput: React.FC<TimeInputProps> = ({ setSettings, settings, icon, name }) => {
  const handleSinceChange: ChangeEventHandler<HTMLInputElement> = (event) => {
    const newValue = event.target.value

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        [name]: {
          ...previousSettings.search[name],
          since: newValue,
        },
      },
    }))

    console.log(`[handleSinceChange] ${name}: ${newValue}`)
  }
  const handleUntilChange: ChangeEventHandler<HTMLInputElement> = (event) => {
    const newValue = event.target.value

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        [name]: {
          ...previousSettings.search[name],
          until: newValue,
        },
      },
    }))

    console.log(`[handleUntilChange] ${name}: ${newValue}`)
  }

  return (
    <InputGroup>
      <InputGroup.Text>
        <Icon icon={icon} />
      </InputGroup.Text>
      <Form.Control type="date" value={settings.search[name]?.since} onChange={handleSinceChange} />
      <InputGroup.Text>～</InputGroup.Text>
      <Form.Control type="date" value={settings.search[name]?.until} onChange={handleUntilChange} />
    </InputGroup>
  )
}

export default TimeFormGroup
