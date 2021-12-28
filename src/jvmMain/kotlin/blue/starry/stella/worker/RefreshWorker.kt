package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegister
import kotlinx.coroutines.*
import org.litote.kmongo.*
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object RefreshWorker {
    fun start() {
        GlobalScope.launch {
            while (isActive) {
                try {
                    check()
                } catch (e: CancellationException) {
                    break
                } catch (e: Throwable) {
                    logger.error(e) { "RefreshWorker で例外が発生しました。" }
                }

                delay(15.minutes)
            }
        }
    }

    private suspend fun check() {
        delay(30.seconds)

        val filter = and(
            PicEntry::timestamp / PicEntry.Timestamp::archived eq false,
            or(
                PicEntry::timestamp / PicEntry.Timestamp::auto_updated eq null,
                PicEntry::timestamp / PicEntry.Timestamp::auto_updated lte Instant.now().toEpochMilli() - Env.AUTO_REFRESH_THRESHOLD
            )
        )

        for (pic in StellaMongoDBPicCollection.find(filter).limit(200).toList()) {
            try {
                MediaRegister.registerByUrl(pic.url, true)
                delay(3.seconds)
            } catch (e: CancellationException) {
                return
            } catch (t: Throwable) {
                continue
            }
        }
    }
}
