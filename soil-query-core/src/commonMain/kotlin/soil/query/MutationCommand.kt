// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.RetryCallback
import soil.query.internal.RetryFn
import soil.query.internal.UniqueId
import soil.query.internal.exponentialBackOff
import soil.query.internal.vvv
import kotlin.coroutines.cancellation.CancellationException

interface MutationCommand<T> {

    suspend fun handle(ctx: Context<T>)

    interface Context<T> {
        val receiver: MutationReceiver
        val options: MutationOptions
        val state: MutationState<T>
        val dispatch: MutationDispatch<T>
        val notifier: MutationNotifier
    }
}

fun <T> MutationCommand.Context<T>.shouldMutate(revision: String): Boolean {
    if (options.isOneShot && state.isMutated) {
        return false
    }
    if (options.isStrictMode && state.revision != revision) {
        return false
    }
    return !state.isPending
}


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

suspend inline fun <T, S> MutationCommand.Context<T>.dispatchMutateResult(
    key: MutationKey<T, S>,
    variable: S
) {
    mutate(key, variable)
        .onSuccess { data ->
            dispatch(MutationAction.MutateSuccess(data))
            key.onQueryUpdate(variable, data)?.let(notifier::onMutateSuccess)
        }
        .onFailure { dispatch(MutationAction.MutateFailure(it)) }
}

internal fun <T> MutationCommand.Context<T>.onRetryCallback(
    id: UniqueId,
): RetryCallback? {
    options.logger ?: return null
    return { err, count, nextBackOff ->
        options.vvv(id) { "retry(count=$count next=$nextBackOff error=${err.message})" }
    }
}
