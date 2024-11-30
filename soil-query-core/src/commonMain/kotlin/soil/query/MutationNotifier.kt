// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.Job
import soil.query.core.Effect

/**
 * MutationNotifier is used to notify the mutation result.
 */
fun interface MutationNotifier {

    /**
     * Notifies the mutation success
     *
     * Mutation success usually implies data update, causing side effects on related queries and subscriptions.
     * This callback is used as a trigger for re-fetching or revalidating data managed by queries.
     * It invokes with the [Effect] set in [MutationKey.onMutateEffect].
     *
     * @param effect The side effects of the mutation for related queries and subscriptions.
     */
    fun onMutate(effect: Effect): Job
}
