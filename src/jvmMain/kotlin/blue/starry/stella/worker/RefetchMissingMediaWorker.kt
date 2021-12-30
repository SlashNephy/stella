package blue.starry.stella.worker

import blue.starry.penicillin.core.exceptions.PenicillinTwitterApiException
import blue.starry.penicillin.core.exceptions.TwitterApiError
import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegistory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.litote.kmongo.eq
import java.nio.file.Files
import kotlin.time.Duration.Companion.minutes

class RefetchMissingMediaWorker: Worker(15.minutes) {
    override suspend fun run() {
        if (!Env.ENABLE_MISSING_MEDIA_REFETCH) {
            return
        }

        check()
    }

    private suspend fun check() {
        delay(1.minutes)

        for (pic in Stella.PicCollection.find().toList()) {
            if (pic.media.all { Files.exists(Stella.MediaDirectory.resolve(it.filename)) } || pic.timestamp.archived) {
                continue
            }

            logger.info { "\"${pic.title}\" (${pic.url}) はキャッシュが存在しないため, 再取得します。" }

            try {
                MediaRegistory.registerByUrl(pic.url, true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: PenicillinTwitterApiException) {
                when (e.error) {
                    TwitterApiError.NoStatusFound, TwitterApiError.ResourceNotFound -> {
                        Stella.PicCollection.deleteOne(PicEntry::url eq pic.url)
                        logger.warn { "\"${pic.title}\" (${pic.url}) は削除されているため, エントリを削除しました。" }
                    }
                    TwitterApiError.SuspendedUser -> {
                        logger.warn { "\"${pic.title}\" (${pic.url}) は作者が凍結されているため, スキップしました。" }
                    }
                    TwitterApiError.CannotSeeProtectedStatus -> {
                        logger.warn { "\"${pic.title}\" (${pic.url}) は鍵垢のため, スキップしました。" }
                    }
                    TwitterApiError.RateLimitExceeded -> {
                        logger.warn { "レートリミットに到達したため一時停止します。" }
                        break
                    }
                }
            }
        }
    }
}
