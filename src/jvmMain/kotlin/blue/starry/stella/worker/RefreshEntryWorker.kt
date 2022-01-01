package blue.starry.stella.worker

import blue.starry.penicillin.core.exceptions.PenicillinTwitterApiException
import blue.starry.penicillin.core.exceptions.TwitterApiError
import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegistory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RefreshEntryWorker: Worker(15.minutes) {
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
                PicEntry::timestamp / PicEntry.Timestamp::auto_updated lte Instant.now().toEpochMilli() - Env.AUTO_REFRESH_THRESHOLD
            )
        )
        val entries = Stella.PicCollection.find(filter).limit(200).toList()

        val jobs = entries.map { entry ->
            launch {
                checkEach(entry)
            }
        }
        jobs.joinAll()
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
