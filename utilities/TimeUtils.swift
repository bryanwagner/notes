import Foundation
import CoreMedia

class TimeUtils {

  // see: https://kandelvijaya.com/2016/10/25/precisiontiminginios/
  static func nanos() -> UInt64 {
    var info = mach_timebase_info()
    guard mach_timebase_info(&info) == KERN_SUCCESS else { return 0 }
    let currentTime = mach_absolute_time()
    return currentTime * UInt64(info.numer) / UInt64(info.denom)
  }

  static func start() -> UInt64 {
    return nanos()
  }

  static func stopSeconds(_ startNanos: UInt64) -> Double {
    return Double(nanos() - startNanos) * 1e-9
  }

  static func stopMillis(_ startNanos: UInt64) -> UInt64 {
    return (nanos() - startNanos) / 1000000
  }

  static func stopMicros(_ startNanos: UInt64) -> UInt64 {
    return (nanos() - startNanos) / 1000
  }

  static func stopNanos(_ startNanos: UInt64) -> UInt64 {
    return (nanos() - startNanos)
  }

  static func seconds(_ block: () -> Void) -> Double {
    let startNanos = start()
    block()
    return stopSeconds(startNanos)
  }

  static func millis(_ block: () -> Void) -> UInt64 {
    let startNanos = start()
    block()
    return stopMillis(startNanos)
  }

  static func nanos(_ block: () -> Void) -> UInt64 {
    let startNanos = start()
    block()
    return stopNanos(startNanos)
  }

  static var currentTimeMillis: UInt64 {
    return UInt64(Date().timeIntervalSince1970 * 1000)
  }
}

public extension Int {
  public var seconds: DispatchTimeInterval {
    return DispatchTimeInterval.seconds(self)
  }

  public var millis: DispatchTimeInterval {
    return DispatchTimeInterval.milliseconds(self)
  }

  public var durationSeconds: TimeInterval {
    return Double(self)
  }

  public var durationMillis: TimeInterval {
    return Double(self) * 1e-3
  }
}

public extension Double {
  public var seconds: DispatchTimeInterval {
    return DispatchTimeInterval.nanoseconds(Int(self * 1e9))
  }

  public var millis: DispatchTimeInterval {
    return DispatchTimeInterval.nanoseconds(Int(self * 1e6))
  }

  public var durationSeconds: TimeInterval {
    return self
  }

  public var durationMillis: TimeInterval {
    return self * 1e-3
  }

  public var cmSeconds: CMTime {
    return CMTimeMake(value: Int64(1000.0 * self), timescale: 1000)
  }

  public var cmMillis: CMTime {
    return CMTimeMake(value: Int64(self), timescale: 1000)
  }
}

extension Date {
  init(seconds: UInt64) {
    self = Date(timeIntervalSince1970: TimeInterval(seconds))
  }

  init(millis: UInt64) {
    self = Date(timeIntervalSince1970: TimeInterval(millis) / 1000)
  }

  var millis: UInt64 {
    return UInt64(timeIntervalSince1970 * 1000)
  }
}
