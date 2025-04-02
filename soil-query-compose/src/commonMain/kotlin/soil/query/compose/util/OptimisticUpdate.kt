// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package soil.query.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job

typealias OptimisticUpdate<T, D> = (currentState: T, optimisticValue: D) -> T
typealias OptimisticCompletionHandle = (cause: Throwable?) -> Unit

@Composable
inline fun <T> rememberOptimistic(
    state: T,
    policy: OptimisticUpdatePolicy = OptimisticUpdatePolicy.Default,
): OptimisticObject<T, T> {
    return rememberOptimistic(
        state = state,
        updateFn = { _, optimisticValue -> optimisticValue },
        policy = policy
    )
}

@Composable
fun <T, D> rememberOptimistic(
    state: T,
    policy: OptimisticUpdatePolicy = OptimisticUpdatePolicy.Default,
    updateFn: OptimisticUpdate<T, D>
): OptimisticObject<T, D> {

    var pendingUpdates by remember { mutableStateOf(emptyList<Triple<D, Job, DisposableHandle>>()) }
    val optimisticState = remember(state, pendingUpdates) {
        pendingUpdates.fold(state) { acc, (optimisticValue, _, _) ->
            updateFn(acc, optimisticValue)
        }
    }

    val addOptimistic = remember<CoroutineScope.(D) -> OptimisticCompletionHandle> {
        { value ->
            val currentJob = coroutineContext[Job] ?: error("No Job in CoroutineScope")
            val completionHandle: OptimisticCompletionHandle = { err ->
                if (err != null && policy.shouldResetOnError(cause = err)) {
                    pendingUpdates.forEach { (_, job, handle) ->
                        handle.dispose()
                        job.cancel()
                    }
                    pendingUpdates = emptyList()
                } else {
                    pendingUpdates = pendingUpdates.filterNot { (_, job, _) -> job === currentJob }
                }
            }
            val disposableHandle = currentJob.invokeOnCompletion { err -> completionHandle(err) }
            pendingUpdates += Triple(value, currentJob, disposableHandle)
            completionHandle
        }
    }

    return OptimisticObject(
        state = optimisticState,
        add = addOptimistic,
    )
}

@Immutable
data class OptimisticObject<T, D>(
    val state: T,
    val add: CoroutineScope.(D) -> OptimisticCompletionHandle,
)

@Stable
interface OptimisticUpdatePolicy {
    fun shouldResetOnError(cause: Throwable): Boolean

    companion object Default : OptimisticUpdatePolicy {
        override fun shouldResetOnError(cause: Throwable): Boolean = true
    }
}
