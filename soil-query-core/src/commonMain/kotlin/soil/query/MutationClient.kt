// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * A Mutation client, which allows you to make mutations actor and handle [MutationKey].
 */
interface MutationClient {

    /**
     * The default mutation options.
     */
    val defaultMutationOptions: MutationOptions

    /**
     * Gets the [MutationRef] by the specified [MutationKey].
     */
    fun <T, S> getMutation(
        key: MutationKey<T, S>
    ): MutationRef<T, S>
}

typealias MutationOptionsOverride = (MutationOptions) -> MutationOptions
typealias MutationCallback<T> = (Result<T>) -> Unit
