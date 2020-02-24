package utilities

import Analytics
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class StatsCounter(
  val name: String,
  var count: Int = 0,
  var last: Double = 0.0,
  var sum: Double = 0.0,
  var sumSquared: Double = 0.0,
  var min: Double = Double.MAX_VALUE,
  var max: Double = -Double.MAX_VALUE
) {

  fun reset() {
    count = 0
    last = 0.0
    sum = 0.0
    sumSquared = 0.0
    min = Double.MAX_VALUE
    max = -Double.MAX_VALUE
  }

  val average: Double get() = if (count == 0) 0.0 else (sum / count)
  val variance: Double get() = if (count > 1) max(0.0, (sumSquared - sum * sum / count) / (count - 1)) else 0.0
  val stdev: Double get() = sqrt(variance)
  val unbiasedVariance: Double get() {
    val periodsInverse = 1.0 / count
    return if (count <= 1) 0.0 else max(0.0, periodsInverse * sumSquared - (periodsInverse * sum) * (periodsInverse * sum))
  }
  val unbiasedStdev: Double get() = sqrt(unbiasedVariance)

  fun percentOfMinMax(value: Double): Double = clamp(divNonzero(value - min, max - min), 0.0, 1.0)

  fun percentOfStdevFromAverage(value: Double): Double = clamp(divNonzero(value - average, stdev), 0.0, 1.0)

  private fun clamp(value: Double, min: Double, max: Double): Double = max(min, min(max, value))

  private fun divNonzero(a: Double, b: Double): Double = if (b == 0.0) 0.0 else (a / b)

  fun add(value: Double) {
    ++count
    last = value
    sum += value
    sumSquared += value * value
    min = min(min, value)
    max = max(max, value)
  }

  inline fun addMillis(block: () -> Unit) {
    val millis = TimeUtils.millis(block)
    add(millis.toDouble())
    Analytics.trackAppMillis(millis, this, name)
  }

  inline fun <T> addMillisForResult(block: () -> T): T {
    val startNanos = TimeUtils.start()
    val result = block()
    val millis = TimeUtils.stopMillis(startNanos)
    add(millis.toDouble())
    Analytics.trackAppMillis(millis, this, name)
    return result
  }

  override fun toString(): String {
    return ("${javaClass.simpleName}{" +
      "average=%.3f".format(average) +
      ", stdev=%.3f".format(stdev) +
      ", count=$count" +
      ", last=%.3f".format(last) +
      (if (min == Double.MAX_VALUE) ", min=init" else ", min=%.3f".format(min)) +
      (if (max == -Double.MAX_VALUE) ", max=init" else ", max=%.3f".format(max)) +
      "}")
  }
}