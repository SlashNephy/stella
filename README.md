# stella

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.30-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/stella)](https://github.com/SlashNephy/stella/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/stella/Docker)](https://hub.docker.com/r/slashnephy/stella)
[![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/slashnephy/stella)](https://hub.docker.com/r/slashnephy/stella)
[![Docker Pulls](https://img.shields.io/docker/pulls/slashnephy/stella)](https://hub.docker.com/r/slashnephy/stella)
[![license](https://img.shields.io/github/license/SlashNephy/stella)](https://github.com/SlashNephy/stella/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/stella)](https://github.com/SlashNephy/stella/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/stella)](https://github.com/SlashNephy/stella/pulls)

- [Demo](https://stella-public.starry.blue/)

---

## これはなに

各プラットフォームでお気に入り / ブックマークした画像を一元管理できる Web アプリケーションです。  
個人利用を想定しています。

対応プラットフォーム

- [Twitter](https://twitter.com)
- [Pixiv](https://pixiv.net)
- [Nijie](https://nijie.info)

## Docker

環境構築が容易なので Docker で導入することをおすすめします。

現在のベースイメージは `openjdk:17-jdk-alpine` です。いくつかフレーバーを用意しています。

- `slashnephy/stella:latest`  
  master ブランチへのプッシュの際にビルドされます。安定しています。
- `slashnephy/stella:dev`  
  dev ブランチへのプッシュの際にビルドされます。開発版のため, 不安定である可能性があります。
- `slashnephy/stella:<version>`  
  GitHub 上のリリースに対応します。

`docker-compose.yml`

```yaml
version: '3.8'

services:
  db:
    image: mongo
    restart: always
    volumes:
      - db:/data/db
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: password
      MONGO_INITDB_DATABASE: stella

  stella:
    container_name: stella
    image: slashnephy/stella:latest
    restart: always
    ports:
      - 8080:8080/tcp
    volumes:
      - media:/app/media
    environment:
      HOST: stella.example.com
      HTTP_HOST: 0.0.0.0
      HTTP_PORT: 8080
      DB_HOST: db
      DB_PORT: 27017
      DB_USER: root
      DB_PASSWORD: password
      DB_NAME: stella
      AUTO_REFRESH_THRESHOLD: 21600000
      CHECK_INTERVAL_MINS: 1
      TWITTER_CK: xxx
      TWITTER_CS: xxx
      TWITTER_AT: xxx
      TWITTER_ATS: xxx
      PIXIV_REFRESH_TOKEN: xxx
      NIJIE_EMAIL: xxx
      NIJIE_PASSWORD: xxx

volumes:
  db:
    driver: local

  media:
    driver: local
```

```console
# イメージ更新
docker pull slashnephy/stella:latest

# 起動
docker-compose up -d

# ログ表示
docker-compose logs -f

# 停止
docker-compose down
```

## TODO

- Kotlin/JS で書き直す

## License

stella is provided under the MIT license.
