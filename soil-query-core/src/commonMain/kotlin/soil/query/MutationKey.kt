// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.SurrogateKey
import soil.query.core.UniqueId
import soil.query.core.uuid

/**
 * Interface for mutations key.
 *
 * MutationKey is typically used to perform side effects on external resources, such as creating, updating, or deleting data.
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
interface MutationKey<T, S> {

    /**
     * A unique identifier used for managing [MutationKey].
     */
    val id: MutationId<T, S>

    /**
     * Suspending function to mutate the variable.
     *
     * @receiver MutationReceiver You can use a custom MutationReceiver within the mutate function.
     */
    val mutate: suspend MutationReceiver.(variable: S) -> T

    /**
     * Function to configure the [MutationOptions].
     *
     * If unspecified, the default value of [SwrCachePolicy] is used.
     *
     * ```kotlin
     * override fun onConfigureOptions(): MutationOptionsOverride? = { options ->
     *      options.copy(isOneShot = true)
     * }
     * ```
     */
    fun onConfigureOptions(): MutationOptionsOverride? = null

    /**
     * Function to update the query cache after the mutation is executed.
     *
     * This is often referred to as ["Pessimistic Updates"](https://redux-toolkit.js.org/rtk-query/usage/manual-cache-updates#pessimistic-updates).
     *
     * ```kotlin
     * override fun onQueryUpdate(variable: PostForm, data: Post): QueryEffect = {
     *     invalidateQueriesBy(GetPostsKey.Id())
     * }
     * ```
     *
     * @param variable The variable to be mutated.
     * @param data The data returned by the mutation.
     */
    fun onQueryUpdate(variable: S, data: T): QueryEffect? = null
}

/**
 * Unique identifier for [MutationKey].
 */
@Suppress("unused")
open class MutationId<T, S>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MutationId<*, *>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "MutationId(namespace='$namespace', tags=${tags.contentToString()})"
    }

    companion object {

        /**
         * Automatically generates a [MutationId].
         *
         * Generates an ID for one-time use, so it cannot be shared among multiple places of use.
         *
         * FIXME: Since this function is for automatic ID assignment, it might be better not to have arguments.
         */
        fun <T, S> auto(
            namespace: String = "auto/${uuid()}",
            vararg tags: SurrogateKey
        ): MutationId<T, S> {
            return MutationId(namespace, *tags)
        }
    }
}

/**
 * Function for building implementations of [MutationKey] using [Kotlin Delegation](https://kotlinlang.org/docs/delegation.html).
 *
 * **Note:** By implementing through delegation, you can reduce the impact of future changes to [MutationKey] interface extensions.
 *
 * Usage:
 *
 * ```kotlin
 * class CreatePostKey : MutationKey<Post, PostForm> by buildMutationKey(
 *   mutate = { body -> ... }
 * )
 * ```
 */
fun <T, S> buildMutationKey(
    id: MutationId<T, S> = MutationId.auto(),
    mutate: suspend MutationReceiver.(variable: S) -> T
): MutationKey<T, S> {
    return object : MutationKey<T, S> {
        override val id: MutationId<T, S> = id
        override val mutate: suspend MutationReceiver.(S) -> T = mutate
    }
}
