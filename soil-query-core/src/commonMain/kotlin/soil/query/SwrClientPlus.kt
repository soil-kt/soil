// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * An enhanced version of [SwrClient] that integrates [SubscriptionClient] into SwrClient.
 */
interface SwrClientPlus : SwrClient, SubscriptionClient
