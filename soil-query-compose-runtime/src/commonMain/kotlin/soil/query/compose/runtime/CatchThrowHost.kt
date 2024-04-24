// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

@Stable
interface CatchThrowHost {

    val keys: Set<Any>

    operator fun get(key: Any): Throwable?

    operator fun set(key: Any, error: Throwable)

    fun remove(key: Any)

    companion object Noop : CatchThrowHost {

        override val keys: Set<Any> = emptySet()

        override fun get(key: Any): Throwable? = null

        override fun set(key: Any, error: Throwable) = Unit

        override fun remove(key: Any) = Unit
    }
}

val LocalCatchThrowHost = compositionLocalOf<CatchThrowHost> {
    CatchThrowHost
}
