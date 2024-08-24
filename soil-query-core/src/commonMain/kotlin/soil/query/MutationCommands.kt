// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.vvv
import kotlin.coroutines.cancellation.CancellationException

object MutationCommands {

    /**
     * Executes the [mutate][MutationKey.mutate] function of the specified [MutationKey].
     *
     * **Note:** The mutation is not executed if the revision is different.
     *
     * @param key Instance of a class implementing [MutationKey].
     * @param variable The variable to be mutated.
     * @param revision The revision of the mutation state.
     */
    class Mutate<T, S>(
        val key: MutationKey<T, S>,
        val variable: S,
        val revision: String,
        val callback: MutationCallback<T>? = null
    ) : MutationCommand<T> {

        override suspend fun handle(ctx: MutationCommand.Context<T>) {
            if (!ctx.shouldMutate(revision)) {
                ctx.options.vvv(key.id) { "skip mutation(shouldMutate=false)" }
                callback?.invoke(Result.failure(CancellationException("skip mutation")))
                return
            }
            ctx.dispatch(MutationAction.Mutating)
            ctx.dispatchMutateResult(key, variable, callback)
        }
    }

    /**
     * Resets the mutation state.
     */
    class Reset<T> : MutationCommand<T> {

        override suspend fun handle(ctx: MutationCommand.Context<T>) {
            ctx.dispatch(MutationAction.Reset)
        }
    }
}
