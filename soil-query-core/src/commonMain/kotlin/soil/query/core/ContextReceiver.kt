// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Receiver for referencing external instances needed when executing query, mutation and more.
 *
 * Usage:
 *
 * ```kotlin
 * val ContextReceiver.customClient: CustomClient?
 *     get() = get(customClientKey)
 * ```
 *
 * @see ContextPropertyKey
 */
interface ContextReceiver {
    operator fun <T : Any> get(key: ContextPropertyKey<T>): T?
}

/**
 * Builder for setting external instances needed when executing query, mutation and more.
 *
 * Usage:
 *
 * ```kotlin
 * var ContextReceiverBuilder.customClient: CustomClient
 *     get() = error("You cannot retrieve a builder property directly.")
 *     set(value) = set(customClientKey, value)
 * ```
 *
 * @see ContextPropertyKey
 */
interface ContextReceiverBuilder {
    operator fun <T : Any> set(key: ContextPropertyKey<T>, value: T)
}

/**
 * Key for referencing external instances needed when executing query, mutation and more.
 *
 * ```kotlin
 * internal val customClientKey = ContextPropertyKey<CustomClient>()
 * ```
 */
class ContextPropertyKey<T : Any>

internal abstract class ContextReceiverBase(
    protected val context: Map<ContextPropertyKey<*>, Any>
) : ContextReceiver {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: ContextPropertyKey<T>): T? = context[key] as T?
}

internal abstract class ContextReceiverBuilderBase(
    protected val context: MutableMap<ContextPropertyKey<*>, Any> = mutableMapOf()
) : ContextReceiverBuilder {

    override fun <T : Any> set(key: ContextPropertyKey<T>, value: T) {
        context[key] = value
    }

    abstract fun build(): ContextReceiver
}
