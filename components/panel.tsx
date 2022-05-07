import React from 'react'
import { Alert } from 'react-bootstrap'

import styles from '../styles/TopPanel.module.css'
import Controls from './controls'
import SearchBox from './search/box'
import Summary from './summary'

const TopPanel: React.FC = () => {
  const [isOpeningSearchBox, setOpeningSearchBox] = React.useState(false)
  const handleToggleSearchBox = () => setOpeningSearchBox(!isOpeningSearchBox)

  return (
    <Alert variant="dark" className={styles.topPanel}>
      <Alert.Heading>stella</Alert.Heading>

      <Controls isOpeningSearchBox={isOpeningSearchBox} handleToggleSearchBox={handleToggleSearchBox} />
      <p>各メディアの著作権はそれぞれのプレースホルダーに帰属します。</p>
      <hr />
      <Summary />

      {isOpeningSearchBox && <hr /> && <SearchBox />}
    </Alert>
  )
}

export default TopPanel
