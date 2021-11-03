package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.models.PicModel
import kotlinx.coroutines.*
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.lte
import org.litote.kmongo.or
import java.time.Instant
import kotlin.time.Duration

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

                delay(Duration.minutes(15))
            }
        }
    }

    private suspend fun check() {
        delay(Duration.seconds(30))

        val filter = or(
            PicModel::timestamp / PicModel.Timestamp::auto_updated eq null,
            PicModel::timestamp / PicModel.Timestamp::auto_updated lte Instant.now().toEpochMilli() - Env.AUTO_REFRESH_THRESHOLD
        )

        for (pic in StellaMongoDBPicCollection.find(filter).limit(200).toList()) {
            try {
                MediaRegister.registerByUrl(pic.url, pic.user, true)
            } catch (e: CancellationException) {
                return
            } catch (e: Throwable) {
                logger.error(e) { "エントリー: \"${pic.title}\" (${pic.url}) の更新に失敗しました。" }
            } finally {
                delay(Duration.seconds(3))
            }
        }
    }
}
