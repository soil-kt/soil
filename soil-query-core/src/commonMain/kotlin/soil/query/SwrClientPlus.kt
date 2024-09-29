// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * An enhanced version of [SwrClient] that integrates [SubscriptionClient] into SwrClient.
 */
interface SwrClientPlus : SwrClient, SubscriptionClient {

    /**
     * Removes all queries, mutations and subscriptions from the in-memory stored data.
     *
     * **Note:**
     * If there are any active queries or mutations or subscriptions, they will be stopped as well.
     * This method should only be used for full resets, such as during sign-out.
     */
    override fun purgeAll()
}
