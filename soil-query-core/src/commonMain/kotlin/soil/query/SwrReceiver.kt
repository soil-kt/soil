// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiverBase
import soil.query.core.ContextReceiverBuilderBase

/**
 * **Note:** This interface is used internally to pass the same property settings to [QueryReceiver] and [MutationReceiver].
 *
 * @see SwrCachePolicy
 */
internal interface SwrReceiver : QueryReceiver, MutationReceiver

/**
 * Builder for creating a [QueryReceiver] and [MutationReceiver].
 *
 * **Note:** This interface is only used in the [SwrCachePolicy] function to pass the same property settings to [QueryReceiver] and [MutationReceiver].
 * For defining extensions for common [ContextPropertyKey], please use [soil.query.core.ContextReceiverBuilder] and [soil.query.core.ContextReceiver].
 * Adding extension definitions to this interface will not make them accessible from the receivers within [QueryKey] or [MutationKey].
 *
 * @see SwrCachePolicy
 */
interface SwrReceiverBuilder : QueryReceiverBuilder, MutationReceiverBuilder

internal class SwrReceiverImpl(
    context: Map<ContextPropertyKey<*>, Any>
) : ContextReceiverBase(context), SwrReceiver

internal class SwrReceiverBuilderImpl : ContextReceiverBuilderBase(), SwrReceiverBuilder {
    override fun build(): SwrReceiver = SwrReceiverImpl(context)
}
