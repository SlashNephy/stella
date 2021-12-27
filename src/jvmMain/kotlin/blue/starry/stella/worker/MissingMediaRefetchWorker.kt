package blue.starry.stella.worker

import blue.starry.penicillin.core.exceptions.PenicillinTwitterApiException
import blue.starry.penicillin.core.exceptions.TwitterApiError
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.models.PicModel
import kotlinx.coroutines.*
import org.litote.kmongo.eq
import java.nio.file.Files
import kotlin.time.Duration.Companion.minutes

object MissingMediaRefetchWorker {
    fun start() {
        GlobalScope.launch {
            while (isActive) {
                try {
                    check()
                } catch (e: CancellationException) {
                    break
                } catch (e: Throwable) {
                    logger.error(e) { "MissingMediaRefetchWorker で例外が発生しました。" }
                }

                delay(15.minutes)
            }
        }
    }

    private suspend fun check() {
        for (pic in StellaMongoDBPicCollection.find().toList()) {
            if (pic.media.all { Files.exists(mediaDirectory.resolve(it.filename)) }) {
                continue
            }

            logger.info { "\"${pic.title}\" (${pic.url}) はキャッシュが存在しないため 再取得します。" }

            try {
                MediaRegister.registerByUrl(pic.url, true)

                logger.info { "エントリー: \"${pic.title}\" (${pic.url}) を更新しました。" }
            } catch (e: CancellationException) {
                return
            } catch (e: PenicillinTwitterApiException) {
                when (e.error) {
                    TwitterApiError.NoStatusFound, TwitterApiError.ResourceNotFound -> {
                        StellaMongoDBPicCollection.deleteOne(PicModel::url eq pic.url)
                        logger.warn { "\"${pic.title}\" (${pic.url}) は削除されているため, エントリを削除しました。" }
                    }
                    TwitterApiError.SuspendedUser -> {
                        StellaMongoDBPicCollection.deleteOne(PicModel::url eq pic.url)
                        logger.warn { "\"${pic.title}\" (${pic.url}) は作者が凍結されているため, エントリを削除しました。" }
                    }
                    TwitterApiError.CannotSeeProtectedStatus -> {
                        logger.warn { "\"${pic.title}\" (${pic.url}) は鍵垢のため, スキップしました。" }
                    }
                    TwitterApiError.RateLimitExceeded -> {
                        logger.warn { "レートリミットに到達したため 一時停止します。" }
                        return
                    }
                    else -> {
                        logger.error(e) { "\"${pic.title}\" (${pic.url}) の取得に失敗しました。" }
                    }
                }
            } catch (e: Throwable) {
                logger.error(e) { "エントリー: \"${pic.title}\" (${pic.url}) の更新に失敗しました。" }
            }
        }
    }
}
