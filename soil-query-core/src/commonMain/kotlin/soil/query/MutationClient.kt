// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Marker

/**
 * A Mutation client, which allows you to make mutations actor and handle [MutationKey].
 */
interface MutationClient {

    /**
     * **Note:** This property is exposed for limited use cases where you may need to call [MutationKey.mutate] manually.
     * It can be useful as an escape hatch or for synchronous invocations within the data layer.
     */
    val mutationReceiver: MutationReceiver

    /**
     * Gets the [MutationRef] by the specified [MutationKey].
     */
    fun <T, S> getMutation(
        key: MutationKey<T, S>,
        marker: Marker = Marker.None
    ): MutationRef<T, S>
}

typealias MutationContentEquals<T> = (oldData: T, newData: T) -> Boolean
typealias MutationOptionsOverride = (MutationOptions) -> MutationOptions
typealias MutationCallback<T> = (Result<T>) -> Unit
