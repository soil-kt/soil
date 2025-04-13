// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Effect

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
    id: MutationId<T, S>,
    mutate: suspend MutationReceiver.(variable: S) -> T
): MutationKey<T, S> {
    return object : MutationKey<T, S> {
        override val id: MutationId<T, S> = id
        override val mutate: suspend MutationReceiver.(S) -> T = mutate
    }
}
