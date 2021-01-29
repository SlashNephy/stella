package blue.starry.stella.worker

import blue.starry.penicillin.core.exceptions.PenicillinTwitterApiException
import blue.starry.penicillin.core.exceptions.TwitterApiError
import blue.starry.stella.api.toPic
import blue.starry.stella.collection
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import com.mongodb.client.model.Filters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.Files
import kotlin.time.minutes

object MissingMediaRefetchWorker {
    fun start() {
        GlobalScope.launch {
            while (true) {
                try {
                    check()
                } catch (e: Throwable) {
                    logger.error(e) { "MissingMediaRefetchWorker で例外が発生しました。" }
                }

                delay(15.minutes)
            }
        }
    }

    private suspend fun check() {
        for (document in collection.find().toList()) {
            val pic = document.toPic()
            if (pic.media.all { Files.exists(mediaDirectory.resolve(it.filename)) }) {
                continue
            }

            logger.info { "\"${pic.title}\" (${pic.url}) はキャッシュが存在しないため 再取得します。" }

            try {
                MediaRegister.registerByUrl(pic.url, pic.user, true)

                logger.info { "エントリー: \"${pic.title}\" (${pic.url}) を更新しました。" }
            } catch (e: PenicillinTwitterApiException) {
                when (e.error) {
                    TwitterApiError.NoStatusFound, TwitterApiError.ResourceNotFound -> {
                        collection.deleteOne(Filters.eq("url", pic.url))
                        logger.warn { "\"${pic.title}\" (${pic.url}) は削除されているため, エントリを削除しました。" }
                    }
                    TwitterApiError.SuspendedUser -> {
                        collection.deleteOne(Filters.eq("url", pic.url))
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
