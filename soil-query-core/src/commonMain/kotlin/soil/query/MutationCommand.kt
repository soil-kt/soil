// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import soil.query.internal.RetryCallback
import soil.query.internal.RetryFn
import soil.query.internal.UniqueId
import soil.query.internal.exponentialBackOff
import soil.query.internal.vvv
import kotlin.coroutines.cancellation.CancellationException

/**
 * Mutation command to handle mutation.
 *
 * @param T Type of the return value from the mutation.
 */
interface MutationCommand<T> {

    /**
     * Handles the mutation.
     */
    suspend fun handle(ctx: Context<T>)

    /**
     * Context for mutation command.
     *
     * @param T Type of the return value from the mutation.
     */
    interface Context<T> {
        val receiver: MutationReceiver
        val options: MutationOptions
        val state: MutationModel<T>
        val dispatch: MutationDispatch<T>
        val notifier: MutationNotifier
    }
}

/**
 * Determines whether a mutation operation is necessary based on the current state.
 *
 * @return `true` if mutation operation is allowed, `false` otherwise.
 */
fun <T> MutationCommand.Context<T>.shouldMutate(revision: String): Boolean {
    if (options.isOneShot && state.isMutated) {
        return false
    }
    if (options.isStrictMode && state.revision != revision) {
        return false
    }
    return !state.isPending
}

/**
 * Mutates the data.
 *
 * @param key Instance of a class implementing [MutationKey].
 * @param variable The variable to be mutated.
 * @param retryFn The retry function.
 * @return The result of the mutation.
 */
suspend fun <T, S> MutationCommand.Context<T>.mutate(
    key: MutationKey<T, S>,
    variable: S,
    retryFn: RetryFn<T> = options.exponentialBackOff(onRetry = onRetryCallback(key.id))
): Result<T> {
    return try {
        val value = retryFn.withRetry { with(key) { receiver.mutate(variable) } }
        Result.success(value)
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        Result.failure(t)
    }
}

/**
 * Dispatches the mutation result.
 *
 * @param key Instance of a class implementing [MutationKey].
 * @param variable The variable to be mutated.
 */
suspend inline fun <T, S> MutationCommand.Context<T>.dispatchMutateResult(
    key: MutationKey<T, S>,
    variable: S
) {
    mutate(key, variable)
        .onSuccess { data ->
            val job = key.onQueryUpdate(variable, data)?.let(notifier::onMutate)
            withContext(NonCancellable) {
                if (job != null && options.shouldExecuteEffectSynchronously) {
                    job.join()
                }
                dispatch(MutationAction.MutateSuccess(data))
            }
        }
        .onFailure { dispatch(MutationAction.MutateFailure(it)) }
        .onFailure { options.onError?.invoke(it, state, key.id) }
}

internal fun <T> MutationCommand.Context<T>.onRetryCallback(
    id: UniqueId,
): RetryCallback? {
    options.logger ?: return null
    return { err, count, nextBackOff ->
        options.vvv(id) { "retry(count=$count next=$nextBackOff error=${err.message})" }
    }
}
