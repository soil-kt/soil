// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.MutationModel
import soil.query.MutationStatus
import soil.query.core.Reply
import soil.query.core.getOrNull
import soil.query.core.getOrThrow

/**
 * A MutationObject represents [MutationModel]s interface for mutating data.
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Stable
sealed interface MutationObject<out T, S> : MutationModel<T> {

    /**
     * The return value from the data source. (Backward compatibility with MutationModel)
     */
    val data: T?

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
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Idle
    override val data: T? get() = reply.getOrNull()
}

/**
 * A Mutation Loading Object represents the waiting execution result state of the [Mutation Object].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Immutable
data class MutationLoadingObject<T, S>(
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Pending
    override val data: T? get() = reply.getOrNull()
}

/**
 * A MutationErrorObject represents the error state of the [MutationObject].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Immutable
data class MutationErrorObject<T, S>(
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Failure
    override val data: T? get() = reply.getOrNull()
}

/**
 * A MutationSuccessObject represents the successful state of the [MutationObject].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
@Immutable
data class MutationSuccessObject<T, S>(
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val mutatedCount: Int,
    override val mutate: suspend (S) -> T,
    override val mutateAsync: suspend (S) -> Unit,
    override val reset: suspend () -> Unit
) : MutationObject<T, S> {
    override val status: MutationStatus = MutationStatus.Success
    override val data: T get() = reply.getOrThrow()
}
