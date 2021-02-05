# stella
Demo is available on [stella.starry.blue](https://stella.starry.blue).

## これはなに
各プラットフォームでお気に入り / ブックマークした画像を一元管理できる Web アプリケーションです。  
個人利用を想定しています。

対応プラットフォーム
- [Twitter](https://twitter.com)
- [Pixiv](https://pixiv.net)
- [Nijie](https://nijie.info)

## Docker
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
    build: app
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
      PIXIV_EMAIL: xxx
      PIXIV_PASSWORD: xxx
      NIJIE_EMAIL: xxx
      NIJIE_PASSWORD: xxx

volumes:
  db:
    driver: local

  media:
    driver: local
```

```shell
git clone https://github.com/SlashNephy/stella app

# To start
docker-compose up -d
# To view logs
docker-compose logs -f
# To stop
docker-compose stop
# To destroy
docker-compose down
```

## License
stella is provided under the MIT license.
