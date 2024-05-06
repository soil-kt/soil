// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlin.math.max

/**
 * Data model for the state handled by [MutationKey].
 *
 * All data models related to mutations, implement this interface.
 *
 * @param T Type of the return value from the mutation.
 */
interface MutationModel<out T> {

    /**
     * The return value from the mutation.
     */
    val data: T?

    /**
     * The timestamp when the data was updated.
     */
    val dataUpdatedAt: Long

    /**
     * The error that occurred.
     */
    val error: Throwable?

    /**
     * The timestamp when the error occurred.
     */
    val errorUpdatedAt: Long

    /**
     * The status of the mutation.
     */
    val status: MutationStatus

    /**
     * The number of times the mutation has been mutated.
     */
    val mutatedCount: Int

    /**
     * The revision of the currently snapshot.
     */
    val revision: String get() = "d-$dataUpdatedAt/e-$errorUpdatedAt"

    /**
     * The timestamp when the mutation was submitted.
     */
    val submittedAt: Long get() = max(dataUpdatedAt, errorUpdatedAt)

    /**
     * Returns `true` if the mutation is idle, `false` otherwise.
     */
    val isIdle: Boolean get() = status == MutationStatus.Idle

    /**
     * Returns `true` if the mutation is pending, `false` otherwise.
     */
    val isPending: Boolean get() = status == MutationStatus.Pending

    /**
     * Returns `true` if the mutation is successful, `false` otherwise.
     */
    val isSuccess: Boolean get() = status == MutationStatus.Success

    /**
     * Returns `true` if the mutation is a failure, `false` otherwise.
     */
    val isFailure: Boolean get() = status == MutationStatus.Failure

    /**
     * Returns `true` if the mutation has been mutated, `false` otherwise.
     */
    val isMutated: Boolean get() = mutatedCount > 0
}

/**
 * The status of the mutation.
 */
enum class MutationStatus {
    Idle,
    Pending,
    Success,
    Failure
}
