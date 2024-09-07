// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.MutationRef
import soil.query.MutationState
import soil.query.MutationStatus

/**
 * A mapper that converts [MutationState] to [MutationObject].
 */
interface MutationObjectMapper {

    /**
     * Converts the given [MutationState] to [MutationObject].
     *
     * @param mutation The mutation reference.
     * @return The converted object.
     */
    fun <T, S> MutationState<T>.toObject(
        mutation: MutationRef<T, S>,
    ): MutationObject<T, S>

    companion object
}

/**
 * The default [MutationObjectMapper].
 */
val MutationObjectMapper.Companion.Default: MutationObjectMapper
    get() = DefaultMutationObjectMapper

private object DefaultMutationObjectMapper : MutationObjectMapper {
    override fun <T, S> MutationState<T>.toObject(
        mutation: MutationRef<T, S>
    ): MutationObject<T, S> = when (status) {
        MutationStatus.Idle -> MutationIdleObject(
            reply = reply,
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )

        MutationStatus.Pending -> MutationLoadingObject(
            reply = reply,
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )

        MutationStatus.Success -> MutationSuccessObject(
            reply = reply,
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )

        MutationStatus.Failure -> MutationErrorObject(
            reply = reply,
            replyUpdatedAt = replyUpdatedAt,
            error = checkNotNull(error),
            errorUpdatedAt = errorUpdatedAt,
            mutatedCount = mutatedCount,
            mutate = mutation::mutate,
            mutateAsync = mutation::mutateAsync,
            reset = mutation::reset
        )
    }
}
