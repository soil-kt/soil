// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

/**
 * A host to manage the await state of a key.
 *
 * @see Await
 */
@Stable
@Deprecated(
    message = "This implementation is deprecated. Please use the new implementation from soil-reacty module instead.",
    replaceWith = ReplaceWith(
        "AwaitHost",
        "soil.plant.compose.reacty.AwaitHost"
    ),
    level = DeprecationLevel.WARNING
)
interface AwaitHost {

    /**
     * Returns the set of keys that are awaited.
     */
    val keys: Set<Any>

    /**
     * Returns `true` if the key is awaited.
     */
    operator fun get(key: Any): Boolean

    /**
     * Sets the key to be awaited or not.
     */
    operator fun set(key: Any, isAwaited: Boolean)

    /**
     * Removes the key from the awaited set.
     */
    fun remove(key: Any)

    /**
     * A noop implementation of [AwaitHost].
     */
    companion object Noop : AwaitHost {

        override val keys: Set<Any> = emptySet()

        override fun get(key: Any): Boolean = false

        override fun set(key: Any, isAwaited: Boolean) = Unit

        override fun remove(key: Any) = Unit
    }
}

/**
 * CompositionLocal for [AwaitHost].
 */
@Deprecated(
    message = "This implementation is deprecated. Please use the new implementation from soil-reacty module instead.",
    replaceWith = ReplaceWith(
        "LocalAwaitHost",
        "soil.plant.compose.reacty.LocalAwaitHost"
    ),
    level = DeprecationLevel.WARNING
)
val LocalAwaitHost = compositionLocalOf<AwaitHost> {
    AwaitHost
}
