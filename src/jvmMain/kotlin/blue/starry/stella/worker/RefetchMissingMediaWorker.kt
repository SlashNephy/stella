package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import org.litote.kmongo.div
import org.litote.kmongo.ne
import kotlin.io.path.notExists
import kotlin.time.Duration.Companion.minutes

class RefetchMissingMediaWorker: Worker(Env.REFETCH_MISSING_MEDIA_INTERVAL_MINUTES.minutes) {
    override suspend fun run() {
        if (!Env.ENABLE_REFETCH_MISSING_MEDIA) {
            return
        }

        check()
    }

    private suspend fun check() {
        val filter = PicEntry::timestamp / PicEntry.Timestamp::archived ne true

        BatchUpdater.updateMany(filter, null) { entry ->
            entry.media.any {
                Stella.MediaDirectory.resolve(it.filename).notExists()
            }.also {
                if (it) {
                    logger.warn { "\"${entry.title}\" (${entry.url}) のメディアは削除されているため, 再取得を試みます。" }
                }
            }
        }
    }
}
