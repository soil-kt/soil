// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * A type used to handle side effects.
 */
typealias Effect = suspend EffectContext.() -> Unit

/**
 * An interface to access contextual information within an [Effect] operation.
 */
interface EffectContext {
    operator fun <T : Any> get(key: EffectPropertyKey<T>): T
}

/**
 * Creates an [EffectContext] with the specified properties.
 */
fun EffectContext(vararg pairs: Pair<EffectPropertyKey<*>, Any>): EffectContext {
    return EffectContextImpl(pairs.toMap())
}

/**
 * A class representing one of the property types referenced within an [Effect] operation.
 *
 * ```kotlin
 * val queryEffectClientPropertyKey = EffectPropertyKey<QueryEffectClient>()
 * ```
 */
class EffectPropertyKey<T : Any>(val errorDescription: String)

internal class EffectContextImpl(
    private val context: Map<EffectPropertyKey<*>, Any>
) : EffectContext {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: EffectPropertyKey<T>): T {
        return checkNotNull(context[key]) { key.errorDescription } as T
    }
}
