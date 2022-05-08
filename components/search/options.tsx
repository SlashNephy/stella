import React from 'react'
import { Col, Row } from 'react-bootstrap'

import { AppProps } from '../../pages'
import ContentsFilterFormGroup from './contentsFilter'
import DisplayFormGroup from './display'
import MediaTypeFormGroup from './mediaType'
import PlatformFormGroup from './platform'
import SortFormGroup from './sort'

const OptionsFormGroup: React.FC<AppProps> = ({ setSettings, settings }) => {
  return (
    <div>
      <Row>
        <Col>
          <PlatformFormGroup settings={settings} setSettings={setSettings} />
        </Col>
        <Col>
          <MediaTypeFormGroup settings={settings} setSettings={setSettings} />
        </Col>
        <Col>
          <SortFormGroup settings={settings} setSettings={setSettings} />
        </Col>
      </Row>

      <hr />

      <Row>
        <Col>
          <ContentsFilterFormGroup settings={settings} setSettings={setSettings} />
        </Col>
        <Col>
          <DisplayFormGroup settings={settings} setSettings={setSettings} />
        </Col>
      </Row>
    </div>
  )
}

export default OptionsFormGroup
