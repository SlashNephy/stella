import React from 'react'
import { Button, OverlayTrigger, Tooltip } from 'react-bootstrap'

import arrowsRotate from '@iconify/icons-fa6-solid/arrows-rotate'
import moon from '@iconify/icons-fa6-solid/moon'
import sliders from '@iconify/icons-fa6-solid/sliders'
import sun from '@iconify/icons-fa6-solid/sun'
import trash from '@iconify/icons-fa6-solid/trash'
import { Icon } from '@iconify/react'

import styles from '../styles/Controls.module.css'

type ControlsProps = {
  readonly isOpeningSearchBox: boolean
  readonly handleToggleSearchBox: () => void
}

const Controls: React.FC<ControlsProps> = ({ isOpeningSearchBox, handleToggleSearchBox }) => {
  const [isDarkTheme, setDarkTheme] = React.useState(false)
  const handleToggleTheme = () => setDarkTheme(!isDarkTheme)

  return (
    <div>
      <OverlayTrigger
        placement="top"
        overlay={<Tooltip>{isDarkTheme ? 'ライトテーマに切り替える' : 'ダークテーマに切り替える'}</Tooltip>}
      >
        <Button variant="secondary" size="sm" className={styles.controlButton} onClick={handleToggleTheme}>
          <Icon icon={isDarkTheme ? sun : moon}></Icon>
        </Button>
      </OverlayTrigger>

      <OverlayTrigger placement="top" overlay={<Tooltip>検索条件をリセットする</Tooltip>}>
        <Button variant="secondary" size="sm" className={styles.controlButton}>
          <Icon icon={trash}></Icon>
        </Button>
      </OverlayTrigger>

      <OverlayTrigger placement="top" overlay={<Tooltip>データを更新する</Tooltip>}>
        <Button variant="secondary" size="sm" className={styles.controlButton}>
          <Icon icon={arrowsRotate}></Icon>
        </Button>
      </OverlayTrigger>

      <OverlayTrigger
        placement="top"
        overlay={<Tooltip>{isOpeningSearchBox ? '検索パネルを折りたたむ' : '検索パネルを表示する'}</Tooltip>}
      >
        <Button variant="secondary" size="sm" className={styles.controlButton} onClick={handleToggleSearchBox}>
          <Icon icon={sliders}></Icon>
        </Button>
      </OverlayTrigger>
    </div>
  )
}

export default Controls
