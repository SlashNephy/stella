package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegistory
import io.ktor.util.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.litote.kmongo.*
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RefreshEntryWorker: Worker(15.minutes) {
    override suspend fun run() {
        try {
            check()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    private suspend fun check() {
        delay(15.seconds)

        val filter = and(
            PicEntry::timestamp / PicEntry.Timestamp::archived eq false,
            or(
                PicEntry::timestamp / PicEntry.Timestamp::auto_updated eq null,
                PicEntry::timestamp / PicEntry.Timestamp::auto_updated lte Instant.now().toEpochMilli() - Env.AUTO_REFRESH_THRESHOLD
            )
        )

        for (pic in Stella.PicCollection.find(filter).limit(200).toList()) {
            try {
                MediaRegistory.registerByUrl(pic.url, true)
                delay(3.seconds)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                continue
            }
        }
    }
}
