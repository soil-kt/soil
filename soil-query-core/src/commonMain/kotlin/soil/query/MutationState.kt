// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * State for managing the execution result of [Mutation].
 */
data class MutationState<out T>(
    override val data: T? = null,
    override val dataUpdatedAt: Long = 0,
    override val error: Throwable? = null,
    override val errorUpdatedAt: Long = 0,
    override val status: MutationStatus = MutationStatus.Idle,
    override val mutatedCount: Int = 0
) : MutationModel<T>
