// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

@Stable
interface AwaitHost {

    val keys: Set<Any>

    operator fun get(key: Any): Boolean

    operator fun set(key: Any, isAwaited: Boolean)

    fun remove(key: Any)

    companion object Noop : AwaitHost {

        override val keys: Set<Any> = emptySet()

        override fun get(key: Any): Boolean = false

        override fun set(key: Any, isAwaited: Boolean) = Unit

        override fun remove(key: Any) = Unit
    }
}

val LocalAwaitHost = compositionLocalOf<AwaitHost> {
    AwaitHost
}
