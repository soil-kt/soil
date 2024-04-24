// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlin.math.max

interface MutationModel<out T> {
    val data: T?
    val dataUpdatedAt: Long
    val error: Throwable?
    val errorUpdatedAt: Long
    val status: MutationStatus
    val mutatedCount: Int

    val revision: String get() = "d-$dataUpdatedAt/e-$errorUpdatedAt"
    val submittedAt: Long get() = max(dataUpdatedAt, errorUpdatedAt)
    val isIdle: Boolean get() = status == MutationStatus.Idle
    val isPending: Boolean get() = status == MutationStatus.Pending
    val isSuccess: Boolean get() = status == MutationStatus.Success
    val isFailure: Boolean get() = status == MutationStatus.Failure
    val isMutated: Boolean get() = mutatedCount > 0
}

enum class MutationStatus {
    Idle,
    Pending,
    Success,
    Failure
}
