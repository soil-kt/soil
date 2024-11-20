// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Marker
import soil.query.core.vvv

object SubscriptionCommands {

    /**
     * Handles the result received from the subscription source.
     *
     * @param key Instance of a class implementing [SubscriptionKey].
     * @param result The received result.
     * @param revision The revision of the subscription state.
     * @param marker The marker with additional information based on the caller of a subscription.
     */
    class Receive<T>(
        private val key: SubscriptionKey<T>,
        private val result: Result<T>,
        private val revision: String,
        private val marker: Marker = Marker.None,
    ) : SubscriptionCommand<T> {

        override suspend fun handle(ctx: SubscriptionCommand.Context<T>) {
            if (ctx.state.revision != revision) {
                ctx.options.vvv(key.id) { "skip receive(revision is not matched)" }
                return
            }
            ctx.dispatchResult(key, result, marker)
        }
    }

    /**
     * Resets the state and re-executes the subscription process.
     *
     * @param key Instance of a class implementing [SubscriptionKey].
     * @param revision The revision of the subscription state.
     */
    class Reset<T>(
        private val key: SubscriptionKey<T>,
        private val revision: String
    ) : SubscriptionCommand<T> {
        override suspend fun handle(ctx: SubscriptionCommand.Context<T>) {
            if (ctx.state.revision != revision) {
                ctx.options.vvv(key.id) { "skip receive(revision is not matched)" }
                return
            }
            ctx.dispatch(SubscriptionAction.Reset)
            ctx.restart()
        }
    }
}
