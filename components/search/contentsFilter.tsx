import React, { ChangeEventHandler } from 'react'
import { Form } from 'react-bootstrap'

import ban from '@iconify/icons-fa-solid/ban'
import checkCircle from '@iconify/icons-fa-solid/check-circle'
import exclamationTriangle from '@iconify/icons-fa-solid/exclamation-triangle'
import filter from '@iconify/icons-fa-solid/filter'
import skullCrossbones from '@iconify/icons-fa-solid/skull-crossbones'
import { Icon, IconifyIcon } from '@iconify/react'

import { PicSensitiveLevel } from '../../lib/models'
import { AppProps } from '../../pages'

type ContentsFilterDefinition = {
  readonly value: PicSensitiveLevel
  readonly label: string
  readonly icon: IconifyIcon
}

const DEFINITIONS: ContentsFilterDefinition[] = [
  {
    value: 0,
    label: '全年齢対象',
    icon: checkCircle,
  },
  {
    value: 1,
    label: 'R-15 (軽度な性描写)',
    icon: exclamationTriangle,
  },
  {
    value: 2,
    label: 'R-18 (露骨な性描写)',
    icon: ban,
  },
  {
    value: 3,
    label: 'R-18G (グロテスクな表現)',
    icon: skullCrossbones,
  },
]

const ContentsFilterFormGroup: React.FC<AppProps> = (props) => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={filter} /> コンテンツフィルター
      </Form.Label>

      {DEFINITIONS.map((def) => (
        <React.Fragment key={def.value}>
          <ContentsFilterInput
            settings={props.settings}
            setSettings={props.setSettings}
            value={def.value}
            label={def.label}
            icon={def.icon}
          />
        </React.Fragment>
      ))}
    </Form.Group>
  )
}

type ContentsFilterInputProps = AppProps & ContentsFilterDefinition

const ContentsFilterInput: React.FC<ContentsFilterInputProps> = ({ icon, label, setSettings, settings, value }) => {
  const handleChange: ChangeEventHandler<HTMLInputElement> = (event) => {
    const checked = event.target.checked

    setSettings((previousSettings) => {
      const sensitiveLevels = new Set(previousSettings.search.sensitiveLevels)

      if (checked) {
        sensitiveLevels.add(value)
      } else {
        sensitiveLevels.delete(value)
      }

      return {
        ...previousSettings,
        search: {
          ...previousSettings.search,
          sensitiveLevels: Array.from(sensitiveLevels),
        },
      }
    })

    console.log(`[handleChange] sensitiveLevels: ${value} ${checked ? 'added' : 'removed'}`)
  }

  return (
    <Form.Check type="checkbox">
      <Form.Check.Input
        type="checkbox"
        checked={settings.search.sensitiveLevels.includes(value)}
        onChange={handleChange}
      ></Form.Check.Input>
      <Form.Check.Label>
        <Icon icon={icon} /> {label}
      </Form.Check.Label>
    </Form.Check>
  )
}

export default ContentsFilterFormGroup
