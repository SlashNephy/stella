import React from 'react'
import { Form } from 'react-bootstrap'

import fileImage from '@iconify/icons-fa6-solid/file-image'
import { Icon } from '@iconify/react'

const ExtensionFormGroup: React.FC = () => {
  return (
    <Form.Group>
      <Form.Label>
        <Icon icon={fileImage}></Icon> 拡張子
      </Form.Label>

      <Form.Select>
        <option>任意の拡張子</option>
        <option value="image">画像のみ</option>
        <option value="jpg">* JPG / JPEG</option>
        <option value="png">* PNG</option>
        <option value="video">動画のみ</option>
        <option value="gif">* GIF</option>
        <option value="mp4">* MP4</option>
      </Form.Select>
    </Form.Group>
  )
}

export default ExtensionFormGroup
