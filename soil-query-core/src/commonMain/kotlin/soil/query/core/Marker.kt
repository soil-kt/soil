// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

// ---------------------------------------------------------------------------- //
// The design of this code draws significant inspiration from CoroutineContext.
// Many thanks to the original authors for their excellent work.
// ---------------------------------------------------------------------------- //

/**
 * An interface for providing additional information based on the caller of a query or mutation.
 *
 * **Note:**
 * You can include different information in the [ErrorRecord] depending on where the key is used.
 * This is useful when you want to differentiate error messages based on the specific use case,
 * even if the same query or mutation is used in multiple places.
 */
interface Marker {

    operator fun <E : Element> get(key: Key<E>): E?

    operator fun plus(other: Marker): Marker {
        if (other === None) return this
        return other.fold(this) { acc, element ->
            val removed = acc.minusKey(element.key)
            if (removed === None) {
                element
            } else {
                CombinedMarker(removed, element)
            }
        }
    }

    fun <R> fold(initial: R, operation: (R, Element) -> R): R

    fun minusKey(key: Key<*>): Marker

    interface Key<E : Element>

    interface Element : Marker {
        val key: Key<*>

        @Suppress("UNCHECKED_CAST")
        override operator fun <E : Element> get(key: Key<E>): E? {
            return if (this.key == key) this as E else null
        }

        override fun <R> fold(initial: R, operation: (R, Element) -> R): R {
            return operation(initial, this)
        }

        override fun minusKey(key: Key<*>): Marker {
            return if (this.key == key) None else this
        }
    }

    companion object None : Marker {
        override fun <E : Element> get(key: Key<E>): E? = null
        override fun <R> fold(initial: R, operation: (R, Element) -> R): R = initial
        override fun plus(other: Marker): Marker = other
        override fun minusKey(key: Key<*>): Marker = this
    }
}

internal class CombinedMarker(
    private val left: Marker,
    private val element: Marker.Element
) : Marker {

    override fun <E : Marker.Element> get(key: Marker.Key<E>): E? {
        var cur = this
        while (true) {
            cur.element[key]?.let { return it }
            val next = cur.left
            if (next is CombinedMarker) {
                cur = next
            } else {
                return next[key]
            }
        }
    }

    override fun <R> fold(initial: R, operation: (R, Marker.Element) -> R): R {
        return operation(left.fold(initial, operation), element)
    }

    override fun minusKey(key: Marker.Key<*>): Marker {
        element[key]?.let { return left }
        val newLeft = left.minusKey(key)
        return when {
            newLeft === left -> this
            newLeft === Marker -> element
            else -> CombinedMarker(newLeft, element)
        }
    }

    private fun size(): Int {
        var cur = this
        var size = 2
        while (true) {
            cur = cur.left as? CombinedMarker ?: return size
            size++
        }
    }

    private fun contains(element: Marker.Element): Boolean {
        return get(element.key) == element
    }

    private fun containsAll(context: CombinedMarker): Boolean {
        var cur = context
        while (true) {
            if (!contains(cur.element)) return false
            val next = cur.left
            if (next is CombinedMarker) {
                cur = next
            } else {
                return contains(next as Marker.Element)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is CombinedMarker && other.size() == size() && other.containsAll(this)
    }

    override fun hashCode(): Int {
        return left.hashCode() + element.hashCode()
    }
}
