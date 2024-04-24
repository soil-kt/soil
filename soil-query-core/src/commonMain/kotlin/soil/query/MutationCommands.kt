// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.vvv

sealed class MutationCommands<T> : MutationCommand<T> {
    data class Mutate<T, S>(
        val key: MutationKey<T, S>,
        val variable: S,
        val revision: String
    ) : MutationCommands<T>() {

        override suspend fun handle(ctx: MutationCommand.Context<T>) {
            if (!ctx.shouldMutate(revision)) {
                ctx.options.vvv(key.id) { "skip mutation(shouldMutate=false)" }
                return
            }
            ctx.dispatch(MutationAction.Mutating)
            ctx.dispatchMutateResult(key, variable)
        }
    }

    class Reset<T> : MutationCommands<T>() {

        override suspend fun handle(ctx: MutationCommand.Context<T>) {
            ctx.dispatch(MutationAction.Reset)
        }
    }
}
