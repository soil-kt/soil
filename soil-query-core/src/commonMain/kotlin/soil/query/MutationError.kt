package soil.query

import soil.query.core.ErrorRecord
import soil.query.core.UniqueId

/**
 * Mutation error information that can be received via a back-channel.
 */
class MutationError @PublishedApi internal constructor(
    override val exception: Throwable,
    override val key: UniqueId,

    /**
     * The mutation state that caused the error.
     */
    val model: MutationModel<*>
) : ErrorRecord {

    override fun toString(): String {
        return """
            MutationError(
                message=${exception.message},
                key=$key,
                model={
                    dataUpdatedAt=${model.dataUpdatedAt},
                    errorUpdatedAt=${model.errorUpdatedAt},
                    status=${model.status},
                    mutatedCount=${model.mutatedCount},
                    submittedAt=${model.submittedAt},
                }
            )
        """.trimIndent()
    }
}

internal typealias MutationErrorRelay = (MutationError) -> Unit
