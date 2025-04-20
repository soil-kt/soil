// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.reacty

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

/**
 * A host to manage the caught errors of a key.
 *
 * @see Catch
 */
@Stable
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
val LocalCatchThrowHost = compositionLocalOf<CatchThrowHost> {
    CatchThrowHost
}
