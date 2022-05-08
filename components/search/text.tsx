import React, { ChangeEventHandler } from 'react'
import { Col, Form, InputGroup, Row } from 'react-bootstrap'

import fileLines from '@iconify/icons-fa6-solid/file-lines'
import font from '@iconify/icons-fa6-solid/font'
import magnifyingGlass from '@iconify/icons-fa6-solid/magnifying-glass'
import tags from '@iconify/icons-fa6-solid/tags'
import user from '@iconify/icons-fa6-solid/user'
import { Icon, IconifyIcon } from '@iconify/react'

import { TextSearchSettings } from '../../lib/settings'
import { AppProps } from '../../pages'

type TextDefinition = {
  readonly name: keyof TextSearchSettings
  readonly icon: IconifyIcon
  readonly placeholder: string
}

const DEFINITIONS: TextDefinition[] = [
  {
    name: 'title',
    icon: font,
    placeholder: 'タイトル...',
  },
  {
    name: 'description',
    icon: fileLines,
    placeholder: '本文...',
  },
  {
    name: 'tags',
    icon: tags,
    placeholder: 'タグ... (カンマ区切り)',
  },
  {
    name: 'author',
    icon: user,
    placeholder: '作者名...',
  },
]

const TextFormGroup: React.FC<AppProps> = ({ setSettings, settings }) => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={magnifyingGlass} /> 検索
      </Form.Label>

      <Row>
        {DEFINITIONS.map(({ icon, name, placeholder }, index) => (
          <React.Fragment key={index}>
            <Col>
              <TextInput
                settings={settings}
                setSettings={setSettings}
                name={name}
                icon={icon}
                placeholder={placeholder}
              />
            </Col>
          </React.Fragment>
        ))}
      </Row>
    </Form.Group>
  )
}

type TextInputProps = AppProps & TextDefinition

const TextInput: React.FC<TextInputProps> = ({ icon, name, placeholder, setSettings, settings }) => {
  const handleChange: ChangeEventHandler<HTMLInputElement> = (event) => {
    const newValue = event.target.value

    setSettings((previousSettings) => ({
      ...previousSettings,
      search: {
        ...previousSettings.search,
        [name]: newValue,
      },
    }))

    console.log(`[handleChange] ${name}: ${newValue}`)
  }

  return (
    <InputGroup>
      <InputGroup.Text>
        <Icon icon={icon} />
      </InputGroup.Text>
      <Form.Control type="text" placeholder={placeholder} value={settings.search[name]} onChange={handleChange} />
    </InputGroup>
  )
}

export default TextFormGroup
