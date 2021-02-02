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
```bash
git clone https://github.com/SlashNephy/stella
curl -O https://raw.githubusercontent.com/SlashNephy/stella/master/docker-compose.yml

vi docker-compose.yml
# ビルドパスを変更: `build: .` -> `build: stella`
# 環境変数を調整

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
