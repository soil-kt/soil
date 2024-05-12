// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.MutationModel
import soil.query.MutationStatus

/**
 * A MutationObject represents [MutationModel]s interface for mutating data.
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Stable
sealed interface MutationObject<out T, S> : MutationModel<T> {

    /**
     * Mutates the variable.
     */
    val mutate: suspend (variable: S) -> T

    /**
     * Mutates the variable asynchronously.
     */
    val mutateAsync: suspend (variable: S) -> Unit

    /**
     * Resets the mutation state.
     */
    val reset: suspend () -> Unit
}

/**
 * A MutationIdleObject represents the initial idle state of the [MutationObject].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Immutable
data class MutationIdleObject<T, S>(
    override val data: T?,
    override val dataUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Idle
}

/**
 * A Mutation Loading Object represents the waiting execution result state of the [Mutation Object].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Immutable
data class MutationLoadingObject<T, S>(
    override val data: T?,
    override val dataUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Pending
}

/**
 * A MutationErrorObject represents the error state of the [MutationObject].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Immutable
data class MutationErrorObject<T, S>(
    override val data: T?,
    override val dataUpdatedAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Failure
}

/**
 * A MutationSuccessObject represents the successful state of the [MutationObject].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Immutable
data class MutationSuccessObject<T, S>(
    override val data: T,
    override val dataUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Success
}
