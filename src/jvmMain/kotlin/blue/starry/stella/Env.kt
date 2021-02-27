package blue.starry.stella

import kotlin.properties.ReadOnlyProperty

object Env {
    val HOST by stringOrNull

    val HTTP_HOST by string { "0.0.0.0" }
    val HTTP_PORT by int { 6742 }

    val DB_HOST by string { "0.0.0.0" }
    val DB_PORT by int { 27017 }
    val DB_USER by stringOrNull
    val DB_PASSWORD by stringOrNull
    val DB_NAME by string { "stella" }

    val AUTO_REFRESH_THRESHOLD by long { 6 * 60 * 60 * 1000L }
    val CHECK_INTERVAL_MINS by int { 1 }

    val TWITTER_CK by stringOrNull
    val TWITTER_CS by stringOrNull
    val TWITTER_AT by stringOrNull
    val TWITTER_ATS by stringOrNull
    val PIXIV_EMAIL by stringOrNull
    val PIXIV_PASSWORD by stringOrNull
    val NIJIE_EMAIL by stringOrNull
    val NIJIE_PASSWORD by stringOrNull
}

private val stringOrNull: ReadOnlyProperty<Env, String?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
    }

private fun string(default: () -> String): ReadOnlyProperty<Env, String> = ReadOnlyProperty { _, property ->
    System.getenv(property.name) ?: default()
}

private fun int(default: () -> Int): ReadOnlyProperty<Env, Int> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toIntOrNull() ?: default()
}

private fun long(default: () -> Long): ReadOnlyProperty<Env, Long> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toLongOrNull() ?: default()
}
