import React from 'react'
import { Col } from 'react-bootstrap'

import { AppProps, LOCAL_STORAGE_KEY } from '../../pages'
import OptionsFormGroup from './options'
import PopularityFormGroup from './popularity'
import TextFormGroup from './text'
import TimeFormGroup from './time'

const SearchBox: React.FC<AppProps> = ({ settings, setSettings }) => {
  React.useEffect(() => {
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(settings))
    console.info(`[settings is updated] ${JSON.stringify(settings)}`)
  }, [settings])

  return (
    <Col>
      <TextFormGroup settings={settings} setSettings={setSettings} />

      <hr />

      <TimeFormGroup settings={settings} setSettings={setSettings} />

      <hr />

      <PopularityFormGroup settings={settings} setSettings={setSettings} />

      <hr />

      <OptionsFormGroup settings={settings} setSettings={setSettings} />
    </Col>
  )
}

export default SearchBox
