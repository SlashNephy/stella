import React from 'react'

import { setIntervalInstantly } from '../lib/setIntervalInstantly'
import { getSummary, Summary } from '../lib/stellaApi'

const UPDATE_INTERVAL_MS = 60 * 1000

const Summary: React.FC = () => {
  const [summary, setSummary] = React.useState<Summary | null>(null)
  const [isLoadingSummary, setLoadingSummary] = React.useState(true)

  React.useEffect(() => {
    const interval = setIntervalInstantly(() => {
      getSummary().then((data) => {
        setSummary(data)
        setLoadingSummary(false)
      })
    }, UPDATE_INTERVAL_MS)
    return () => clearInterval(interval)
  }, [])

  if (isLoadingSummary) {
    return <span>Loading...</span>
  }
  if (!summary) {
    return <span>No summary</span>
  }

  return (
    <span>
      現在 <strong>{summary.entries}</strong> 件のエントリー / <strong>{summary.media}</strong> 個のメディア
      が登録されています。
    </span>
  )
}

export default Summary
