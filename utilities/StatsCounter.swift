import Foundation

class StatsCounter: CustomStringConvertible {
  let name: String
  var count: Int = 0
  var last: Double = 0.0
  var sum: Double = 0.0
  var sumSquared: Double = 0.0
  var min: Double = Double.greatestFiniteMagnitude
  var max: Double = -Double.greatestFiniteMagnitude

  init(_ name: String) {
    self.name = name
  }

  func reset() {
    count = 0
    last = 0.0
    sum = 0.0
    sumSquared = 0.0
    min = Double.greatestFiniteMagnitude
    max = -Double.greatestFiniteMagnitude
  }

  var average: Double { return count == 0 ? 0.0 : (sum / Double(count)) }
  var variance: Double { return count > 1 ? Swift.max(0.0, (sumSquared - sum * sum / Double(count)) / Double(count - 1)) : 0.0 }
  var stdev: Double { return sqrt(variance) }
  var unbiasedVariance: Double {
    let periodsInverse = 1.0 / Double(count)
    return count <= 1 ? 0.0 : Swift.max(0.0, periodsInverse * sumSquared - (periodsInverse * sum) * (periodsInverse * sum))
  }
  var unbiasedStdev: Double { return sqrt(unbiasedVariance) }

  func percentOfMinMax(_ value: Double) -> Double {
    return clamp(divNonzero(value - min, max - min), 0.0, 1.0)
  }

  func percentOfStdevFromAverage(_ value: Double) -> Double {
    return clamp(divNonzero(value - average, stdev), 0.0, 1.0)
  }

  private func clamp(_ value: Double, _ min: Double, _ max: Double) -> Double {
    return Swift.max(min, Swift.min(max, value))
  }

  private func divNonzero(_ a: Double, _ b: Double) -> Double {
    return b == 0.0 ? 0.0 : (a / b)
  }

  func add(_ value: Double) {
    count += 1
    last = value
    sum += value
    sumSquared += value * value
    min = Swift.min(min, value)
    max = Swift.max(max, value)
  }

  func addMillis(_ block: () -> Void) {
    let millis = TimeUtils.millis(block)
    add(Double(millis))
    Analytics.trackAppMillis(millis, source: self, name: name)
  }

  func addMillisForResult<T>(_ block: () -> T) -> T {
    let startNanos = TimeUtils.start()
    let result = block()
    let millis = TimeUtils.stopMillis(startNanos)
    add(Double(millis))
    Analytics.trackAppMillis(millis, source: self, name: name)
    return result
  }

  var description: String {
    return ("\(type(of: self)){" +
      String(format: "average=%.3f", average) +
      String(format: ", stdev=%.3f", stdev) +
      String(format: ", count=%d", count) +
      String(format: ", last=%.3f", last) +
      (min == Double.greatestFiniteMagnitude ? ", min=init" : String(format: ", min=%.3f", min)) +
      (max == -Double.greatestFiniteMagnitude ? ", max=init" : String(format: ", max=%.3f", max)) +
      "}")
  }
}
