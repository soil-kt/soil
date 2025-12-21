// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

// TODO: Significantly slower than java.util.PriorityQueue.
//  Consider rewriting the implementation later.

/**
 * Priority queue implementation.
 *
 * @param E Element that implemented [Comparable] interface.
 * @param capacity Initial capacity.
 * @constructor Creates a priority queue with the specified capacity.
 */
class PriorityQueue<E>(
    capacity: Int
) where E : Comparable<E> {

    private val data = ArrayList<E>(capacity)

    /**
     * Adds the specified [element] to the queue.
     */
    fun push(element: E) {
        val index = data.binarySearch(element)
        if (index < 0) {
            data.add(-index - 1, element)
        } else {
            data.add(index, element)
        }
    }

    /**
     * Returns the element with the highest priority.
     *
     * @return The element with the highest priority, or `null` if the queue is empty.
     */
    fun peek(): E? {
        return data.firstOrNull()
    }

    /**
     * Removes the element with the highest priority.
     *
     * @return The element with the highest priority, or `null` if the queue is empty.
     */
    fun pop(): E? {
        return data.removeFirstOrNull()
    }

    /**
     * Removes the specified [element] from the queue.
     *
     * @param element The element to be removed.
     * @return `true` if the element was removed, `false` otherwise.
     */
    fun remove(element: E): Boolean {
        val index = data.binarySearch(element)
        return if (index < 0) {
            false
        } else {
            data.removeAt(index)
            true
        }
    }

    /**
     * Removes all elements from the queue.
     */
    fun clear() {
        data.clear()
    }

    /**
     * Returns `true` if the queue is empty.
     *
     * @return `true` if the queue is empty, `false` otherwise.
     */
    fun isEmpty(): Boolean = data.isEmpty()
}

/**
 * @see [PriorityQueue.isEmpty]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun PriorityQueue<*>.isNotEmpty(): Boolean = !isEmpty()
