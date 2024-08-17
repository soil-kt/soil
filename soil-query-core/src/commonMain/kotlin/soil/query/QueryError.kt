// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ErrorRecord
import soil.query.core.UniqueId

/**
 * Query error information that can be received via a back-channel.
 */
class QueryError @PublishedApi internal constructor(
    override val exception: Throwable,
    override val key: UniqueId,

    /**
     * The query model that caused the error.
     */
    val model: QueryModel<*>
) : ErrorRecord {

    override fun toString(): String {
        return """
            QueryError(
                message=${exception.message},
                key=$key,
                model={
                    replyUpdatedAt=${model.replyUpdatedAt},
                    errorUpdatedAt=${model.errorUpdatedAt},
                    staleAt=${model.staleAt},
                    status=${model.status},
                    isInvalidated=${model.isInvalidated}
                }
            )
        """.trimIndent()
    }
}

internal typealias QueryErrorRelay = (QueryError) -> Unit
