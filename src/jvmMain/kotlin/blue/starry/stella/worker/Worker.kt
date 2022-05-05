package blue.starry.stella.worker

import blue.starry.stella.Stella
import blue.starry.stella.create
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

abstract class Worker(private val interval: Duration?): CoroutineScope {
    val logger by lazy {
        KotlinLogging.create("Stella.${this::class.simpleName}")
    }

    final override val coroutineContext: CoroutineContext
        get() = Stella.Dispatcher

    abstract suspend fun run()

    @OptIn(ExperimentalTime::class)
    fun start() {
        launch {
            try {
                while (isActive) {
                    run()

                    if (interval == null) {
                        break
                    }

                    delay(interval + Random.nextInt(0, 30).seconds)
                }
            } catch (e: CancellationException){
                throw e
            } catch (t: Throwable) {
                logger.error(t) { "Worker has been stopped." }
            }
        }
    }
}
