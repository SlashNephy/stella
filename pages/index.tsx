import type { NextPage } from 'next'
import Head from 'next/head'
import { Container } from 'react-bootstrap'

import TopPanel from '../components/panel'

const Home: NextPage = () => {
  return (
    <Container>
      <Head>
        <title>Favorites | stella</title>
        <meta name="description" content="Generated by create next app" />
      </Head>

      <TopPanel />
    </Container>
  )
}

export default Home
