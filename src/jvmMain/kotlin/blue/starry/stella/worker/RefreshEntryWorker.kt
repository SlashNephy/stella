package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.models.PicEntry
import org.litote.kmongo.*
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

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
        BatchUpdater.updateMany(filter, Env.REFRESH_ENTRY_LIMIT) { true }
    }
}
