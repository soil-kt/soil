// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.MutationModel
import soil.query.MutationStatus

@Stable
sealed interface MutationObject<out T, S> : MutationModel<T> {
    val mutate: suspend (variable: S) -> T
    val mutateAsync: suspend (variable: S) -> Unit
    val reset: suspend () -> Unit
}

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
