package utilities

import Analytics
import kotlinx.coroutines.*
import mu.KLogging
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

class Monitor(
  val dispatcher: CoroutineDispatcher,
  val name: String,
  val delayMillis: Long,
  val task: () -> Any?
) : CoroutineScope {
  private val job = SupervisorJob()
  private val handler = Analytics.coroutineExceptionHandler()
  override val coroutineContext: CoroutineContext get() = dispatcher + job + handler
  private val running = AtomicBoolean(false)

  constructor(
      name: String,
      delayMillis: Long,
      task: () -> Any?
  ) : this(Dispatchers.Main, name, delayMillis, task)

  fun stop() {
    logger.info { "Monitor.stop: $name" }
    running.set(false)
    coroutineContext.cancelChildren()  // cancel coroutines
  }

  fun start() {
    if (running.get()) {
      return
    }
    logger.info { "Monitor.start: $name" }
    running.set(true)
    launch {
      while (running.get()) {
        delay(delayMillis)
        task()
      }
    }
  }

  companion object : KLogging()
}
