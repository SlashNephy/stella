package blue.starry.stella.worker

import blue.starry.penicillin.core.exceptions.PenicillinTwitterApiException
import blue.starry.penicillin.core.exceptions.TwitterApiError
import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegistory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.litote.kmongo.*
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RefreshEntryWorker: Worker(Env.REFRESH_ENTRY_INTERVAL_MINUTES.minutes) {
    override suspend fun run() {
        if (!Env.ENABLE_REFRESH_ENTRY) {
            return
        }

        check()
    }

    private suspend fun check() {
        val filter = and(
            PicEntry::timestamp / PicEntry.Timestamp::archived ne true,
            or(
                PicEntry::timestamp / PicEntry.Timestamp::auto_updated eq null,
                PicEntry::timestamp / PicEntry.Timestamp::auto_updated lte Instant.now().toEpochMilli() - Env.REFRESH_ENTRY_THRESHOLD_MINUTES.minutes.inWholeMilliseconds
            )
        )
        BatchUpdater.updateMany(filter) { true }
    }

    private suspend fun checkEach(entry: PicEntry) {
        try {
            delay(3.seconds)
            MediaRegistory.registerByUrl(entry.url, true)
        } catch (e: PenicillinTwitterApiException) {
            when (e.error) {
                TwitterApiError.NoStatusFound -> {
                    Stella.PicCollection.updateOne(
                        PicEntry::url eq entry.url,
                        setValue(PicEntry::timestamp / PicEntry.Timestamp::archived, true)
                    )
                }
            }

        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            return
        }
    }
}
