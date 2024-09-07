// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Represents a reply from a query or mutation.
 *
 * [None] indicates that there is no reply yet.
 */
sealed interface Reply<out T> {
    data object None : Reply<Nothing>
    data class Some<out T> internal constructor(val value: T) : Reply<T>

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        internal inline operator fun <T> invoke(value: T): Reply<T> = Some(value)

        fun <T> none(): Reply<T> = None
        fun <T> some(value: T): Reply<T> = Some(value)
    }
}

/**
 * Returns true if the reply is [Reply.None].
 */
val <T> Reply<T>.isNone: Boolean get() = this is Reply.None

/**
 * Returns the value of the [Reply.Some] instance, or throws an error if there is no reply yet ([Reply.None]).
 */
fun <T> Reply<T>.getOrThrow(): T = when (this) {
    is Reply.None -> error("Reply is none.")
    is Reply.Some -> value
}

/**
 * Returns the value of the [Reply.Some] instance, or null if there is no reply yet ([Reply.None]).
 */
fun <T> Reply<T>.getOrNull(): T? = when (this) {
    is Reply.None -> null
    is Reply.Some -> value
}

/**
 * Returns the value of the [Reply.Some] instance, or the result of the [default] function if there is no reply yet ([Reply.None]).
 */
fun <T> Reply<T>.getOrElse(default: () -> T): T = when (this) {
    is Reply.None -> default()
    is Reply.Some -> value
}

/**
 * Returns the value of the [Reply.Some] instance, or the [default] value if there is no reply yet ([Reply.None]).
 */
inline fun <T> Reply<T>?.orNone(): Reply<T> = this ?: Reply.none()

/**
 * Transforms the value of the [Reply.Some] instance using the provided [transform] function,
 * or returns [Reply.None] if there is no reply yet ([Reply.None]).
 */
inline fun <T, R> Reply<T>.map(transform: (T) -> R): Reply<R> = when (this) {
    is Reply.None -> Reply.none()
    is Reply.Some -> Reply.some(transform(value))
}

/**
 * Combines two [Reply] instances using the provided [transform] function.
 * If either [Reply] has no reply yet ([Reply.None]), returns [Reply.None].
 */
inline fun <T1, T2, R> Reply.Companion.combine(
    r1: Reply<T1>,
    r2: Reply<T2>,
    transform: (T1, T2) -> R
): Reply<R> {
    return when {
        r1.isNone || r2.isNone -> none()
        else -> some(
            transform(
                r1.getOrThrow(),
                r2.getOrThrow()
            )
        )
    }
}

/**
 * Combines three [Reply] instances using the provided [transform] function.
 * If any [Reply] has no reply yet ([Reply.None]), returns [Reply.None].
 */
inline fun <T1, T2, T3, R> Reply.Companion.combine(
    r1: Reply<T1>,
    r2: Reply<T2>,
    r3: Reply<T3>,
    transform: (T1, T2, T3) -> R
): Reply<R> {
    return when {
        r1.isNone || r2.isNone || r3.isNone -> none()
        else -> some(
            transform(
                r1.getOrThrow(),
                r2.getOrThrow(),
                r3.getOrThrow()
            )
        )
    }
}
