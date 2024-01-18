package utilities

import kotlinx.coroutines.*
import mu.KLogging
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine-based monitor to invoke a task at periodic intervals.
 */
class Monitor(
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val name: String,
    val delayMillis: Long,
    val task: suspend () -> Unit
) : CoroutineScope {
    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, t -> logger.error(t) { javaClass.name } }
    override val coroutineContext: CoroutineContext get() = dispatcher + job + handler
    private val running = AtomicBoolean(false)

    fun stop() {
        if (!running.getAndSet(false)) return
        logger.info { "Monitor.stop: $name" }
        running.set(false)
        try {
            coroutineContext.cancelChildren()
        } catch (t: Throwable) {
            logger.error(t) { "shutdown" }
        }
    }

    fun start(): Monitor {
        if (running.getAndSet(true)) {
            logger.debug { "Monitor.start: already started: $name" }
            return this
        }
        logger.info { "Monitor.start: $name" }
        launch {
            while (running.get()) {
                delay(delayMillis)
                try {
                    task()
                } catch (e: CancellationException) {
                    logger.debug { "Monitor.run: cancelled: $name" }
                } catch (t: Throwable) {
                    logger.error(t) { "Monitor.run: ${t.message}" }
                }
            }
        }
        return this
    }

    fun shutdown() {
        stop()
        coroutineContext.cancel()
    }

    suspend fun awaitShutdown() {
        job.join()
    }

    suspend fun shutdownAndWait() {
        shutdown()
        awaitShutdown()
    }

    companion object : KLogging()
}
