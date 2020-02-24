import Foundation

/**
 * Timer implementation based on Monitor.kt and:
 * https://medium.com/over-engineering/a-background-repeating-timer-in-swift-412cecfd2ef9
 */
class Monitor {
  let name: String
  let seconds: TimeInterval
  var queue: DispatchQueue = DispatchQueue.main
  var task: (() -> Void)?

  private enum State {
    case stopped
    case started
  }
  private let state = AtomicReference<State>(.stopped)

  private lazy var timer: DispatchSourceTimer = {
    let t = DispatchSource.makeTimerSource()
    t.schedule(deadline: .now() + self.seconds, repeating: self.seconds)
    t.setEventHandler { [weak self] in
      if let queue = self?.queue, let task = self?.task {
        queue.async {
          task()
        }
      }
    }
    return t
  }()

  init(_ name: String, seconds: TimeInterval, task: (() -> Void)? = nil) {
    self.name = name
    self.seconds = seconds
    self.task = task
  }

  deinit {
    timer.setEventHandler {}
    // If the timer is suspended, calling cancel without resuming triggers a crash.
    // See: https://forums.developer.apple.com/thread/15902
    timer.cancel()
    start()
  }

  func stop() {
    state.perform { state in
      guard state != .stopped else { return }
      state = .stopped
      timer.suspend()
    }
  }

  func start() {
    state.perform { state in
      guard state != .started else { return }
      state = .started
      timer.resume()
    }
  }
}
