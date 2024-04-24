// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

// TODO: Significantly slower than java.util.PriorityQueue.
//  Consider rewriting the implementation later.
class PriorityQueue<E>(
    capacity: Int
) where E : Any, E : Comparable<E> {

    private val data = ArrayList<E>(capacity)

    fun push(element: E) {
        val index = data.binarySearch(element)
        if (index < 0) {
            data.add(-index - 1, element)
        } else {
            data.add(index, element)
        }
    }

    fun peek(): E? {
        return data.firstOrNull()
    }

    fun pop(): E? {
        return data.removeFirstOrNull()
    }

    fun remove(element: E): Boolean {
        val index = data.binarySearch(element)
        return if (index < 0) {
            false
        } else {
            data.removeAt(index)
            true
        }
    }

    fun clear() {
        data.clear()
    }

    fun isEmpty(): Boolean = data.isEmpty()
}

@Suppress("NOTHING_TO_INLINE")
inline fun PriorityQueue<*>.isNotEmpty(): Boolean = !isEmpty()
