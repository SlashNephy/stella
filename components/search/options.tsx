import React from 'react'
import { Col, Row } from 'react-bootstrap'

import ContentsFilterFormGroup from './contentsFilter'
import DisplayFormGroup from './display'
import ExtensionFormGroup from './extension'
import PlatformFormGroup from './platform'
import SortFormGroup from './sort'

const OptionsFormGroup: React.FC = () => {
  return (
    <div>
      <Row>
        <Col>
          <PlatformFormGroup />
        </Col>
        <Col>
          <ExtensionFormGroup />
        </Col>
        <Col>
          <SortFormGroup />
        </Col>
      </Row>

      <hr />

      <Row>
        <Col>
          <ContentsFilterFormGroup />
        </Col>
        <Col>
          <DisplayFormGroup />
        </Col>
      </Row>
    </div>
  )
}

export default OptionsFormGroup
