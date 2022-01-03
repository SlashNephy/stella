package blue.starry.stella.worker

import blue.starry.penicillin.core.exceptions.PenicillinTwitterApiException
import blue.starry.penicillin.core.exceptions.TwitterApiError
import blue.starry.penicillin.extensions.rateLimit
import blue.starry.stella.Stella
import blue.starry.stella.create
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegistory
import io.ktor.client.features.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.bson.conversions.Bson
import org.litote.kmongo.combine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object BatchUpdater {
    private val logger = KotlinLogging.create("Stella.BatchUpdater")

    private val rateLimitsMutex = Mutex()
    private val rateLimits = mutableMapOf<PicEntry.Platform, Long?>()

    suspend fun updateMany(preFilter: Bson, postFilter: (PicEntry) -> Boolean) = coroutineScope {
        val entries = Stella.PicCollection.find(preFilter).limit(200).toFlow().filter(postFilter)

        val jobs = entries.map { entry ->
            launch {
                updateOne(entry, true)
            }
        }.toList()
        jobs.joinAll()
    }

    suspend fun updateOne(entry: PicEntry, auto: Boolean): Boolean {
        when (val result = update(entry, auto)) {
            UpdateResult.Successful -> {
                return true
            }

            UpdateResult.Missing -> {
                Stella.PicCollection.updateOne(
                    PicEntry::url eq entry.url,
                    combine(
                        setValue(PicEntry::timestamp / PicEntry.Timestamp::archived, true),
                        setValue(PicEntry::timestamp / PicEntry.Timestamp::auto_updated, Instant.now().toEpochMilli())
                    )
                )
                logger.warn { "\"${entry.title}\" (${entry.url}) は削除されているため, エントリーをアーカイブしました。" }
            }
            UpdateResult.TemporallyUnavailable -> {
                Stella.PicCollection.updateOne(
                    PicEntry::url eq entry.url,
                    setValue(PicEntry::timestamp / PicEntry.Timestamp::auto_updated, Instant.now().toEpochMilli())
                )
                logger.warn { "\"${entry.title}\" (${entry.url}) は一時的に利用できないため, スキップしました。" }
            }
            is UpdateResult.Failed -> {
                logger.warn(result.cause) { "\"${entry.title}\" (${entry.url}) の更新中に不明な例外が発生しました。" }
            }

            is UpdateResult.Canceled -> {
                logger.debug(result.cause) { "\"${entry.title}\" (${entry.url}) の更新中にキャンセルが要求されました。" }
                throw result.cause
            }
            is UpdateResult.Cooldown -> {
                rateLimitsMutex.withLock {
                    rateLimits[result.platform] = result.resetAt
                }

                logger.warn {
                    val instant = Instant.ofEpochMilli(result.resetAt)
                    val resetAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                    "\"${entry.title}\" (${entry.url}) の更新中にレートリミットに到達しました。${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(resetAt)} に解除されます。"
                }
            }
        }

        return false
    }

    private suspend fun update(entry: PicEntry, auto: Boolean): UpdateResult {
        delay(Random.nextInt(0..10).seconds)

        rateLimitsMutex.withLock {
            val resetAt = rateLimits[entry.platform]

            if (resetAt != null && Instant.now().toEpochMilli() < resetAt) {
                return UpdateResult.Cooldown(entry.platform, resetAt)
            }
        }

        return runCatching {
            MediaRegistory.registerByUrl(entry.url, auto)
        }.map {
            UpdateResult.Successful
        }.getOrElse { cause ->
            when (cause) {
                is CancellationException -> {
                    UpdateResult.Canceled(cause)
                }
                is ResponseException -> {
                    val status = cause.response.status
                    when {
                        status == HttpStatusCode.NotFound -> {
                            UpdateResult.Missing
                        }
                        status == HttpStatusCode.TooManyRequests -> {
                            UpdateResult.Cooldown(entry.platform, Instant.now().toEpochMilli() + 3.hours.inWholeMilliseconds)
                        }
                        status.value in (500 until 600) -> {
                            UpdateResult.TemporallyUnavailable
                        }
                        else -> {
                            UpdateResult.Failed(cause)
                        }
                    }
                }
                is PenicillinTwitterApiException -> {
                    when (cause.error) {
                        TwitterApiError.NoStatusFound, TwitterApiError.ResourceNotFound -> {
                            UpdateResult.Missing
                        }
                        TwitterApiError.SuspendedUser, TwitterApiError.CannotSeeProtectedStatus -> {
                            UpdateResult.TemporallyUnavailable
                        }
                        TwitterApiError.RateLimitExceeded -> {
                            UpdateResult.Cooldown(entry.platform, cause.rateLimit!!.resetAt.timestamp)
                        }
                        else -> {
                            UpdateResult.Failed(cause)
                        }
                    }
                }
                else -> {
                    UpdateResult.Failed(cause)
                }
            }
        }
    }

    private sealed class UpdateResult {
        object Successful: UpdateResult()

        object Missing: UpdateResult()
        object TemporallyUnavailable: UpdateResult()
        data class Failed(val cause: Throwable): UpdateResult()

        data class Canceled(val cause: CancellationException): UpdateResult()
        data class Cooldown(val platform: PicEntry.Platform, val resetAt: Long): UpdateResult()
    }
}
