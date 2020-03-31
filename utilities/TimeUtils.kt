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

  data class ResultWithSeconds<T>(val result: T, val seconds: Float)
  inline fun <T> resultWithSeconds(block: () -> T): ResultWithSeconds<T> {
    val startNanos = start()
    val result = block()
    return ResultWithSeconds(result, stopSeconds(startNanos))
  }

  data class ResultWithMillis<T>(val result: T, val millis: Long)
  inline fun <T> resultWithMillis(block: () -> T): ResultWithMillis<T> {
    val startNanos = start()
    val result = block()
    return ResultWithMillis(result, stopMillis(startNanos))
  }

  data class ResultWithNanos<T>(val result: T, val nanos: Long)
  inline fun <T> resultWithNanos(block: () -> T): ResultWithNanos<T> {
    val startNanos = start()
    val result = block()
    return ResultWithNanos(result, stopNanos(startNanos))
  }
}