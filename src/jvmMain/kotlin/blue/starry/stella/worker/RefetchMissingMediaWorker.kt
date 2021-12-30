package blue.starry.stella.worker

import blue.starry.penicillin.core.exceptions.PenicillinTwitterApiException
import blue.starry.penicillin.core.exceptions.TwitterApiError
import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegistory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.litote.kmongo.div
import org.litote.kmongo.eq
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.minutes

class RefetchMissingMediaWorker: Worker(15.minutes) {
    override suspend fun run() {
        if (!Env.ENABLE_MISSING_MEDIA_REFETCH) {
            return
        }

        check()
    }

    private suspend fun check() {
        val filter = PicEntry::timestamp / PicEntry.Timestamp::archived eq false
        val entries = Stella.PicCollection.find(filter).toList()

        val jobs = entries.map { entry ->
            launch {
                checkEach(entry)
            }
        }
        jobs.joinAll()
    }

    private suspend fun checkEach(entry: PicEntry) {
        if (entry.media.all { Stella.MediaDirectory.resolve(it.filename).exists() }) {
            return
        }

        logger.info { "\"${entry.title}\" (${entry.url}) はキャッシュが存在しないため, 再取得します。" }

        try {
            MediaRegistory.registerByUrl(entry.url, true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: PenicillinTwitterApiException) {
            when (e.error) {
                TwitterApiError.NoStatusFound, TwitterApiError.ResourceNotFound -> {
                    Stella.PicCollection.deleteOne(PicEntry::url eq entry.url)
                    logger.warn { "\"${entry.title}\" (${entry.url}) は削除されているため, エントリを削除しました。" }
                }
                TwitterApiError.SuspendedUser -> {
                    logger.warn { "\"${entry.title}\" (${entry.url}) は作者が凍結されているため, スキップしました。" }
                }
                TwitterApiError.CannotSeeProtectedStatus -> {
                    logger.warn { "\"${entry.title}\" (${entry.url}) は鍵垢のため, スキップしました。" }
                }
                TwitterApiError.RateLimitExceeded -> {
                    logger.warn { "レートリミットに到達したため一時停止します。" }
                }
            }
        } catch (t: Throwable) {
            return
        }
    }
}
