// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import soil.query.MutationClient
import soil.query.MutationKey
import soil.query.MutationRef
import soil.query.MutationState
import soil.query.MutationStatus

@Composable
fun <T, S> rememberMutation(
    key: MutationKey<T, S>,
    client: MutationClient = LocalSwrClient.current
): MutationObject<T, S> {
    val mutation = remember(key) { client.getMutation(key) }
    val state by mutation.state.collectAsState()
    LaunchedEffect(mutation) {
        mutation.start(this)
    }
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
