package blue.starry.stella

import kotlin.properties.ReadOnlyProperty
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object Env {
    val HOST by stringOrNull

    val HTTP_HOST by string { "0.0.0.0" }
    val HTTP_PORT by int { 6742 }

    val DB_HOST by string { "0.0.0.0" }
    val DB_PORT by int { 27017 }
    val DB_USER by stringOrNull
    val DB_PASSWORD by stringOrNull
    val DB_NAME by string { "stella" }

    val TWITTER_CK by stringOrNull
    val TWITTER_CS by stringOrNull
    val TWITTER_AT by stringOrNull
    val TWITTER_ATS by stringOrNull

    val PIXIV_REFRESH_TOKEN by stringOrNull

    val NIJIE_EMAIL by stringOrNull
    val NIJIE_PASSWORD by stringOrNull

    val ENABLE_DATABASE_MIGRATION by boolean { false }
    val ENABLE_REFETCH_MISSING_MEDIA by boolean { false }
    val REFETCH_MISSING_MEDIA_INTERVAL_MINUTES by long { 3.hours.inWholeMinutes }

    val ENABLE_REFRESH_ENTRY by boolean { false }
    val REFRESH_ENTRY_INTERVAL_MINUTES by long { 10.minutes.inWholeMinutes }
    val REFRESH_ENTRY_THRESHOLD_MINUTES by long { 7.days.inWholeMinutes }

    val ENABLE_WATCH_TWITTER by boolean { false }
    val ENABLE_WATCH_PIXIV by boolean { false }
    val ENABLE_WATCH_NIJIE by boolean { false }
    val WATCH_INTERVAL_SECONDS by long { 5.minutes.inWholeSeconds }
    val WATCH_THEN_FOLLOW_TWITTER by boolean { false }
    val WATCH_THEN_FOLLOW_PIXIV by boolean { false }
    val WATCH_THEN_FOLLOW_NIJIE by boolean { false }

    val USER_AGENT by string { "Stella/1.0 (+https://github.com/SlashNephy/Stella)" }
    val LOG_LEVEL by string { "INFO" }
    val DRYRUN by boolean { false }
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

private fun boolean(default: () -> Boolean): ReadOnlyProperty<Env, Boolean> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toIntOrNull()?.equals(1) ?: default()
}
