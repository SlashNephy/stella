package blue.starry.stella.worker

import blue.starry.stella.api.toPic
import blue.starry.stella.collection
import blue.starry.stella.logger
import com.mongodb.client.model.Filters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.minutes
import kotlin.time.seconds

object RefreshWorker {
    fun start() {
        GlobalScope.launch {
            while (true) {
                try {
                    check()
                } catch (e: Throwable) {
                    logger.error(e) { "RefreshWorker で例外が発生しました。" }
                }

                delay(15.minutes)
            }
        }
    }

    private suspend fun check() {
        delay(30.seconds)

        val filter = Filters.or(
            Filters.eq("timestamp.auto_updated", null),
            Filters.lte("timestamp.auto_updated", Calendar.getInstance().timeInMillis - 6 * 60 * 60 * 1000)
        )
        collection.find(filter).limit(200).toList().forEach { document ->
            val pic = document.toPic()

            try {
                MediaRegister.registerByUrl(pic.url, pic.user, true)

                logger.info { "エントリー: \"${pic.title}\" (${pic.url}) を更新しました。" }
            } catch (e: Throwable) {
                logger.error(e) { "エントリー: \"${pic.title}\" (${pic.url}) の更新に失敗しました。" }
            } finally {
                delay(3.seconds)
            }
        }
    }
}
