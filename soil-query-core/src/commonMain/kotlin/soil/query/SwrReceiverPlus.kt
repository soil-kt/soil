// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiverBase
import soil.query.core.ContextReceiverBuilderBase

/**
 * **Note:** This interface is used internally to pass the same property settings to [QueryReceiver], [MutationReceiver], and [SubscriptionReceiver].
 *
 * @see SwrCachePlusPolicy
 */
internal interface SwrReceiverPlus : SwrReceiver, SubscriptionReceiver

/**
 * Builder for creating a [QueryReceiver], [MutationReceiver] and [SubscriptionReceiver].
 *
 * **Note:** This interface is only used in the [SwrCachePlusPolicy] function to pass the same property settings to [QueryReceiver], [MutationReceiver], and [SubscriptionReceiver].
 * For defining extensions for common [ContextPropertyKey], please use [soil.query.core.ContextReceiverBuilder] and [soil.query.core.ContextReceiver].
 * Adding extension definitions to this interface will not make them accessible from the receivers within [QueryKey], [MutationKey] or [SubscriptionKey].
 *
 * @see SwrCachePlusPolicy
 */
interface SwrReceiverBuilderPlus : SwrReceiverBuilder, SubscriptionReceiverBuilder

internal class SwrReceiverPlusImpl(
    context: Map<ContextPropertyKey<*>, Any>
) : ContextReceiverBase(context), SwrReceiverPlus

internal class SwrReceiverBuilderPlusImpl : ContextReceiverBuilderBase(), SwrReceiverBuilderPlus {
    override fun build(): SwrReceiverPlus = SwrReceiverPlusImpl(context)
}
