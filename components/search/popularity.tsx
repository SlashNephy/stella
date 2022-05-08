import React, { ChangeEventHandler } from 'react'
import { Col, Form, InputGroup, Row } from 'react-bootstrap'
import Latex from 'react-latex-next'

import replyAll from '@iconify/icons-fa-solid/reply-all'
import retweet from '@iconify/icons-fa-solid/retweet'
import bookmark from '@iconify/icons-fa6-regular/bookmark'
import eye from '@iconify/icons-fa6-regular/eye'
import heart from '@iconify/icons-fa6-regular/heart'
import star from '@iconify/icons-fa6-regular/star'
import chartLine from '@iconify/icons-fa6-solid/chart-line'
import { Icon, IconifyIcon } from '@iconify/react'

import { chunk } from '../../lib/chunk'
import { PopularitySearchSettings } from '../../lib/settings'
import { AppProps } from '../../pages'

import 'katex/dist/katex.min.css'

type PopularityDefinition = {
  readonly name: keyof PopularitySearchSettings
  readonly icon: IconifyIcon
}

const DEFINITIONS: PopularityDefinition[] = [
  {
    name: 'rating',
    icon: star,
  },
  {
    name: 'bookmark',
    icon: bookmark,
  },
  {
    name: 'view',
    icon: eye,
  },
  {
    name: 'like',
    icon: heart,
  },
  {
    name: 'retweet',
    icon: retweet,
  },
  {
    name: 'reply',
    icon: replyAll,
  },
]

const PopularityFormGroup: React.FC<AppProps> = (props) => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={chartLine} /> レート
      </Form.Label>

      {chunk(DEFINITIONS, 3).map((row, index) => (
        <React.Fragment key={index}>
          <Row>
            {row.map(({ icon, name }) => (
              <React.Fragment key={name}>
                <Col>
                  <PopularityInput name={name} icon={icon} settings={props.settings} setSettings={props.setSettings} />
                </Col>
              </React.Fragment>
            ))}
          </Row>
        </React.Fragment>
      ))}
    </Form.Group>
  )
}

type PopularityInputProps = AppProps & PopularityDefinition

const PopularityInput: React.FC<PopularityInputProps> = ({ icon, name, settings, setSettings }) => {
  const handleMinChange: ChangeEventHandler<HTMLInputElement> = (event) => {
    const newValue = parseInt(event.target.value)

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        [name]: {
          ...previousSettings.search[name],
          min: newValue,
        },
      },
    }))

    console.log(`[handleMinChange] ${name}: ${newValue}`)
  }
  const handleMaxChange: ChangeEventHandler<HTMLInputElement> = (event) => {
    const newValue = parseInt(event.target.value)

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        [name]: {
          ...previousSettings.search[name],
          max: newValue,
        },
      },
    }))

    console.log(`[handleMaxChange] ${name}: ${newValue}`)
  }

  return (
    <InputGroup>
      <Form.Control type="number" value={settings.search[name]?.min} onChange={handleMinChange}></Form.Control>
      <InputGroup.Text>
        <Latex strict={true}>$ \leq $</Latex>
      </InputGroup.Text>
      <InputGroup.Text>
        <Icon icon={icon} />
      </InputGroup.Text>
      <InputGroup.Text>
        <Latex strict={true}>$ \leq $</Latex>
      </InputGroup.Text>
      <Form.Control type="number" value={settings.search[name]?.max} onChange={handleMaxChange}></Form.Control>
    </InputGroup>
  )
}

export default PopularityFormGroup
