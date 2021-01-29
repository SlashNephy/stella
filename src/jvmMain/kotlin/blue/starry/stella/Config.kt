package blue.starry.stella

object Config {
    val Host: String? = System.getenv("HOST")

    val HttpHost = System.getenv("HTTP_HOST") ?: "0.0.0.0"
    val HttpPort = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 6742

    val DatabaseHost = System.getenv("DB_HOST") ?: "0.0.0.0"
    val DatabasePort = System.getenv("DB_PORT")?.toIntOrNull() ?: 27017
    val DatabaseUser: String? = System.getenv("DB_USER")
    val DatabasePassword: String? = System.getenv("DB_PASSWORD")
    val DatabaseName = System.getenv("DB_NAME") ?: "stella"

    val AutoRefreshThreshold = System.getenv("AUTO_REFRESH_THRESHOLD")?.toLongOrNull() ?: 6 * 60 * 60 * 1000
    val CheckIntervalMins = System.getenv("CHECK_INTERVAL_MINS")?.toIntOrNull() ?: 1

    val TwitterConsumerKey: String? = System.getenv("TWITTER_CK")
    val TwitterConsumerSecret: String? = System.getenv("TWITTER_CS")
    val TwitterAccessToken: String? = System.getenv("TWITTER_AT")
    val TwitterAccessTokenSecret: String? = System.getenv("TWITTER_ATS")
    val PixivEmail: String? = System.getenv("PIXIV_EMAIL")
    val PixivPassword: String? = System.getenv("PIXIV_PASSWORD")
    val NijieEmail: String? = System.getenv("NIJIE_EMAIL")
    val NijiePassword: String? = System.getenv("NIJIE_PASSWORD")
}
