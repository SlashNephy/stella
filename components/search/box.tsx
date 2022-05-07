import React from 'react'

import OptionsFormGroup from './options'
import RatingFormGroup from './rating'
import TextFormGroup from './text'
import TimeFormGroup from './time'

const SearchBox: React.FC = () => {
  return (
    <div>
      <TextFormGroup />
      <hr />
      <TimeFormGroup />
      <hr />
      <RatingFormGroup />
      <hr />
      <OptionsFormGroup />
    </div>
  )
}

export default SearchBox
