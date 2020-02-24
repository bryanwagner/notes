package utilities

import kotlin.math.min

class RingQueue<E>(
  capacity: Int,
  private val elementRemoved: ((element: E) -> Unit)? = null) : Iterable<E> {

  @Suppress("UNCHECKED_CAST")
  internal val queue: Array<E?> = arrayOfNulls<Any?>(capacity) as Array<E?>
  internal var nextIndex: Int = 0
  var size: Int = 0; private set

  val capacity: Int get() = queue.size
  private val firstOffset: Int get() = (nextIndex + (capacity - size)) % capacity

  private fun offsetForIndex(index: Int): Int = (firstOffset + index) % capacity

  operator fun get(index: Int): E? = if (index in 0 until size) queue[offsetForIndex(index)] else null

  fun add(element: E): E? {
    if (capacity == 0) {
      elementRemoved?.invoke(element)
      return element
    }
    val removed = queue[nextIndex]
    queue[nextIndex] = element
    nextIndex = (nextIndex + 1) % capacity
    size = min(size + 1, capacity)
    removed?.let {
      elementRemoved?.invoke(it)
    }
    return removed
  }

  fun remove(element: E): E? {
    if (capacity == 0) {
      return null
    }
    var removed: E? = null
    val firstOffset = this.firstOffset
    var i = firstOffset
    var j = (i + 1) % capacity
    repeat(size) {
      if (removed == null && queue[i] == element) {
        removed = queue[i]
        queue[i] = null
        size -= 1
      }
      if (removed != null && it < size) {  // don't shift the last element (which will be null)
        queue[i] = queue[j]  // shift elements into the removed locations
        queue[j] = null  // final location will be empty
      }
      i = j
      j = (j + 1) % capacity
    }
    removed?.let {
      nextIndex = (firstOffset + size) % capacity  // add into the empty location
      elementRemoved?.invoke(it)
    }
    return removed
  }

  fun removeAll(element: E): Boolean {
    var removed = false
    while (remove(element) != null) {
      removed = true
    }
    return removed
  }

  fun clear(invokesElementRemoved: Boolean = true) {
    var index = nextIndex
    repeat(capacity) {
      val removed = queue[index]
      queue[index] = null
      index = (index  + 1) % capacity
      if (invokesElementRemoved && removed != null) {
        elementRemoved?.invoke(removed)
      }
    }
    nextIndex = 0
    size = 0
  }

  inner class RingQueueIterator : Iterator<E> {
    private var i = 0

    override fun hasNext() = i < size

    override fun next(): E {
      val element = this@RingQueue[i]!!
      i += 1
      return element
    }
  }

  override fun iterator(): Iterator<E> {
    return RingQueueIterator()
  }

  override fun toString(): String {
    return ("${javaClass.simpleName}{" +
      "capacity=$capacity" +
      ", nextIndex=$nextIndex" +
      ", size=$size" +
      ", queue=${queue.contentToString()}" +
      "}")
  }
}