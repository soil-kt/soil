// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

/**
 * A host to manage the caught errors of a key.
 *
 * @see Catch
 */
@Stable
@Deprecated(
    message = "This implementation is deprecated. Please use the new implementation from soil-reacty module instead.",
    replaceWith = ReplaceWith(
        "CatchThrowHost",
        "soil.plant.compose.reacty.CatchThrowHost"
    ),
    level = DeprecationLevel.WARNING
)
interface CatchThrowHost {

    /**
     * Returns the set of keys that have caught errors.
     */
    val keys: Set<Any>

    /**
     * Returns the caught error of the key.
     */
    operator fun get(key: Any): Throwable?

    /**
     * Sets the caught error of the key.
     */
    operator fun set(key: Any, error: Throwable)

    /**
     * Removes the caught error of the key.
     */
    fun remove(key: Any)

    /**
     * A noop implementation of [CatchThrowHost].
     */
    companion object Noop : CatchThrowHost {

        override val keys: Set<Any> = emptySet()

        override fun get(key: Any): Throwable? = null

        override fun set(key: Any, error: Throwable) = Unit

        override fun remove(key: Any) = Unit
    }
}

/**
 * CompositionLocal for [CatchThrowHost].
 */
@Deprecated(
    message = "This implementation is deprecated. Please use the new implementation from soil-reacty module instead.",
    replaceWith = ReplaceWith(
        "LocalCatchThrowHost",
        "soil.plant.compose.reacty.LocalCatchThrowHost"
    ),
    level = DeprecationLevel.WARNING
)
val LocalCatchThrowHost = compositionLocalOf<CatchThrowHost> {
    CatchThrowHost
}
