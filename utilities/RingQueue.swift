import Foundation

class RingQueue<E: Equatable>: Sequence, CustomStringConvertible {
  private let elementRemoved: ((_ element: E) -> Void)?

  var queue: [E?]
  var nextIndex: Int = 0
  private(set) var size: Int = 0

  var capacity: Int { return queue.count }
  private var firstOffset: Int { return (nextIndex + (capacity - size)) % capacity }

  init(_ capacity: Int, elementRemoved: ((_ element: E) -> Void)? = nil) {
    self.elementRemoved = elementRemoved
    self.queue = Array.init(repeating: nil, count: capacity)
  }

  private func offsetForIndex(_ index: Int) -> Int {
    return (firstOffset + index) % capacity
  }

  subscript(index: Int) -> E? {
    return 0.until(size) ~= index ? queue[offsetForIndex(index)] : nil
  }

  func add(_ element: E) -> E? {
    if capacity == 0 {
      elementRemoved?(element)
      return element
    }
    let removed = queue[nextIndex]
    queue[nextIndex] = element
    nextIndex = (nextIndex + 1) % capacity
    size = Swift.min(size + 1, capacity)
    if let e = removed {
      elementRemoved?(e)
    }
    return removed
  }

  func remove(_ element: E) -> E? {
    if capacity == 0 {
      return nil
    }
    var removed: E? = nil
    let firstOffset = self.firstOffset
    var i = firstOffset
    var j = (i + 1) % capacity
    for it in 0.until(size) {
      if removed == nil && queue[i] == element {
        removed = queue[i]
        queue[i] = nil
        size -= 1
      }
      if removed != nil && it < size {  // don't shift the last element (which will be null)
        queue[i] = queue[j]  // shift elements into the removed locations
        queue[j] = nil  // final location will be empty
      }
      i = j
      j = (j + 1) % capacity
    }
    if let e = removed {
      nextIndex = (firstOffset + size) % capacity  // add into the empty location
      elementRemoved?(e)
    }
    return removed
  }

  func removeAll(_ element: E) -> Bool {
    var removed = false
    while remove(element) != nil {
      removed = true
    }
    return removed
  }

  func clear(invokesElementRemoved: Bool = true) {
    var index = nextIndex
    for _ in 0.until(capacity) {
      let removed = queue[index]
      queue[index] = nil
      index = (index  + 1) % capacity
      if invokesElementRemoved, let e = removed {
        elementRemoved?(e)
      }
    }
    nextIndex = 0
    size = 0
  }

  class RingQueueIterator: IteratorProtocol {
    private let ringQueue: RingQueue
    private var i = 0

    init(_ ringQueue: RingQueue) {
      self.ringQueue = ringQueue
    }

    func next() -> E? {
      guard i < ringQueue.size else { return nil }
      let element = ringQueue[i]!
      i += 1
      return element
    }
  }

  func makeIterator() -> RingQueueIterator {
    return RingQueueIterator(self)
  }

  var description: String {
    return ("\(type(of: self)){" +
      "capacity=\(capacity)" +
      ", nextIndex=\(nextIndex)" +
      ", size=\(size)" +
      ", queue=\(queue)" +
      "}")
  }
}
