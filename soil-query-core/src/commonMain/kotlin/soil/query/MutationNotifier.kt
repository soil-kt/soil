// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * MutationNotifier is used to notify the mutation result.
 */
fun interface MutationNotifier {

    /**
     * Notifies the mutation success
     *
     * Mutation success usually implies data update, causing side effects on related queries.
     * This callback is used as a trigger for re-fetching or revalidating data managed by queries.
     * It invokes with the [QueryEffect] set in [MutationKey.onQueryUpdate].
     *
     * @param sideEffects The side effects of the mutation for related queries.
     */
    fun onMutateSuccess(sideEffects: QueryEffect)
}
