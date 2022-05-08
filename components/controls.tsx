import React from 'react'
import { Button, OverlayTrigger, Tooltip } from 'react-bootstrap'

import arrowsRotate from '@iconify/icons-fa6-solid/arrows-rotate'
import moon from '@iconify/icons-fa6-solid/moon'
import sliders from '@iconify/icons-fa6-solid/sliders'
import sun from '@iconify/icons-fa6-solid/sun'
import trash from '@iconify/icons-fa6-solid/trash'
import { Icon } from '@iconify/react'

import { DEFAULT_SEARCH_SETTINGS } from '../lib/settings'
import { AppProps } from '../pages'
import styles from '../styles/Controls.module.css'

const Controls: React.FC<AppProps> = ({ settings, setSettings }) => {
  const handleToggleTheme = () => {
    setSettings((previousSettings) => ({
      ...previousSettings,
      isDarkTheme: !previousSettings.isDarkTheme,
    }))
    console.log('[handleToggleTheme]')
  }
  const handleResetConditions = () => {
    setSettings((previousSettings) => ({
      ...previousSettings,
      search: DEFAULT_SEARCH_SETTINGS,
    }))
    console.log('[handleResetConditions]')
  }
  const handleRefresh = () => {
    console.log('[handleRefresh]')
  }
  const handleToggleSearchBox = () => {
    setSettings((previousSettings) => ({
      ...previousSettings,
      isOpeningSearchBox: !previousSettings.isOpeningSearchBox,
    }))
    console.log('[handleToggleSearchBox]')
  }

  return (
    <div>
      <OverlayTrigger
        placement="top"
        overlay={<Tooltip>{settings.isDarkTheme ? 'ライトテーマに切り替える' : 'ダークテーマに切り替える'}</Tooltip>}
      >
        <Button variant="secondary" size="sm" className={styles.controlButton} onClick={handleToggleTheme}>
          <Icon icon={settings.isDarkTheme ? sun : moon} />
        </Button>
      </OverlayTrigger>

      <OverlayTrigger placement="top" overlay={<Tooltip>検索条件をリセットする</Tooltip>}>
        <Button variant="secondary" size="sm" className={styles.controlButton} onClick={handleResetConditions}>
          <Icon icon={trash} />
        </Button>
      </OverlayTrigger>

      <OverlayTrigger placement="top" overlay={<Tooltip>データを更新する</Tooltip>}>
        <Button variant="secondary" size="sm" className={styles.controlButton} onClick={handleRefresh}>
          <Icon icon={arrowsRotate} />
        </Button>
      </OverlayTrigger>

      <OverlayTrigger
        placement="top"
        overlay={<Tooltip>{settings.isOpeningSearchBox ? '検索パネルを折りたたむ' : '検索パネルを表示する'}</Tooltip>}
      >
        <Button variant="secondary" size="sm" className={styles.controlButton} onClick={handleToggleSearchBox}>
          <Icon icon={sliders} />
        </Button>
      </OverlayTrigger>
    </div>
  )
}

export default Controls
