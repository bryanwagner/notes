package utilities

object TimeUtils {

  fun start(): Long = System.nanoTime()

  fun stopSeconds(startNanos: Long): Float = (System.nanoTime() - startNanos) * 1e-9f

  fun stopMillis(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1000000

  fun stopMicros(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1000

  fun stopNanos(startNanos: Long): Long = (System.nanoTime() - startNanos)

  inline fun seconds(block: () -> Unit): Float {
    val startNanos = start()
    block()
    return stopSeconds(startNanos)
  }

  inline fun millis(block: () -> Unit): Long {
    val startNanos = start()
    block()
    return stopMillis(startNanos)
  }

  inline fun nanos(block: () -> Unit): Long {
    val startNanos = start()
    block()
    return stopNanos(startNanos)
  }
}