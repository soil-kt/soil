// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * Extension receiver for referencing external instances needed when receiving subscription.
 *
 * Usage:
 *
 * ```kotlin
 * class SubscriptionReceiver(
 *     val client: SubscriptionClient
 * ) : SubscriptionReceiver
 * ```
 */
interface SubscriptionReceiver {

    /**
     * Default implementation for [SubscriptionReceiver].
     */
    companion object : SubscriptionReceiver
}
