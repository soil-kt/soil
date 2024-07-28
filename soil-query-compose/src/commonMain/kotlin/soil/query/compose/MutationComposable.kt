// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.MutationClient
import soil.query.MutationKey
import soil.query.MutationRef
import soil.query.MutationState
import soil.query.MutationStatus

/**
 * Remember a [MutationObject] and subscribes to the mutation state of [key].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 * @param key The [MutationKey] for managing [mutation][soil.query.Mutation] associated with [id][soil.query.MutationId].
 * @param client The [MutationClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 * @return A [MutationObject] each the mutation state changed.
 */
@Composable
fun <T, S> rememberMutation(
    key: MutationKey<T, S>,
    client: MutationClient = LocalSwrClient.current
): MutationObject<T, S> {
    val scope = rememberCoroutineScope()
    val mutation = remember(key) { client.getMutation(key).also { it.launchIn(scope) } }
    val state by mutation.state.collectAsState()
    return remember(mutation, state) {
        state.toObject(mutation = mutation)
    }
}

private fun <T, S> MutationState<T>.toObject(
    mutation: MutationRef<T, S>,
): MutationObject<T, S> {
    return when (status) {
        MutationStatus.Idle -> MutationIdleObject(
            data = data,
            dataUpdatedAt = dataUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )

        MutationStatus.Pending -> MutationLoadingObject(
            data = data,
            dataUpdatedAt = dataUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )

        MutationStatus.Success -> MutationSuccessObject(
            data = data!!,
            dataUpdatedAt = dataUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )

        MutationStatus.Failure -> MutationErrorObject(
            data = data,
            dataUpdatedAt = dataUpdatedAt,
            error!!,
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )
    }
}
