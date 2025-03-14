// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Effect
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
     * Function to compare the content of the data.
     *
     * This function is used to determine whether the data is identical to the previous data via [MutationCommand].
     * If the data is considered the same, [MutationState.replyUpdatedAt] is not updated, and the existing reply state is maintained.
     * This can be useful when strict update management is needed, such as when special comparison is necessary,
     * although it is generally not that important.
     *
     * ```kotlin
     * override val contentEquals: MutationContentEquals<SomeType> = { a, b -> a.xx == b.xx }
     * ```
     */
    val contentEquals: MutationContentEquals<T>? get() = null

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
    @Deprecated(
        "Use onMutateEffect instead.",
        ReplaceWith("onMutateEffect(variable, data)")
    )
    fun onQueryUpdate(variable: S, data: T): QueryEffect? = null

    /**
     * Function to handle side effects after the mutation is executed.
     *
     * This is often referred to as ["Pessimistic Updates"](https://redux-toolkit.js.org/rtk-query/usage/manual-cache-updates#pessimistic-updates).
     *
     * ```kotlin
     * override fun onMutateEffect(variable: PostForm, data: Post): Effect = {
     *     queryClient.invalidateQueriesBy(GetPostsKey.Id())
     * }
     * ```
     *
     * @param variable The variable to be mutated.
     * @param data The data returned by the mutation.
     */
    fun onMutateEffect(variable: S, data: T): Effect? = null
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
        @Deprecated(
            """
            This function is deprecated because it does not retain automatically generated values when used within Compose.
            As a result, values are regenerated after configuration changes, leading to different values.
            Consider using an alternative approach that preserves state across recompositions.
        """, ReplaceWith("MutationId(namespace, *tags)", "soil.query.MutationId")
        )
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
