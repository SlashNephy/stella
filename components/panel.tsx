import React from 'react'
import { Alert, Collapse } from 'react-bootstrap'

import packageJson from '../package.json'
import { AppProps } from '../pages'
import styles from '../styles/TopPanel.module.css'
import Controls from './controls'
import SearchBox from './search/box'
import Summary from './summary'

const TopPanel: React.FC<AppProps> = ({ settings, setSettings }) => {
  return (
    <Alert variant="dark" className={styles.topPanel}>
      <Alert.Heading>{packageJson.name}</Alert.Heading>

      <Controls settings={settings} setSettings={setSettings} />
      <hr />
      <Summary />

      <Collapse in={settings.isOpeningSearchBox}>
        <div>
          <hr />
          <SearchBox settings={settings} setSettings={setSettings} />
        </div>
      </Collapse>
    </Alert>
  )
}

export default TopPanel
