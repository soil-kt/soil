// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.SubscriptionRef
import soil.query.SubscriptionState
import soil.query.SubscriptionStatus
import soil.query.core.map

/**
 * A mapper that converts [SubscriptionState] to [SubscriptionObject].
 */
interface SubscriptionObjectMapper {

    /**
     * Converts the given [SubscriptionState] to [SubscriptionObject].
     *
     * @param subscription The subscription reference.
     * @param select A function that selects the object from the reply.
     * @return The converted object.
     */
    fun <T, U> SubscriptionState<T>.toObject(
        subscription: SubscriptionRef<T>,
        select: (T) -> U
    ): SubscriptionObject<U>

    companion object
}

/**
 * The default [SubscriptionObjectMapper].
 */
val SubscriptionObjectMapper.Companion.Default: SubscriptionObjectMapper
    get() = DefaultSubscriptionObjectMapper

private object DefaultSubscriptionObjectMapper : SubscriptionObjectMapper {
    override fun <T, U> SubscriptionState<T>.toObject(
        subscription: SubscriptionRef<T>,
        select: (T) -> U
    ): SubscriptionObject<U> = when (status) {
        SubscriptionStatus.Pending -> SubscriptionLoadingObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            restartedAt = restartedAt,
            reset = subscription::reset
        )

        SubscriptionStatus.Success -> SubscriptionSuccessObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            restartedAt = restartedAt,
            reset = subscription::reset
        )

        SubscriptionStatus.Failure -> SubscriptionErrorObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = checkNotNull(error),
            errorUpdatedAt = errorUpdatedAt,
            restartedAt = restartedAt,
            reset = subscription::reset
        )
    }
}
