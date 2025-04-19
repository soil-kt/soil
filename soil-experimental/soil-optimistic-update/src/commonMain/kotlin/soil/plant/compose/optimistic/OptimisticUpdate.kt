// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package soil.plant.compose.optimistic

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

/**
 * Function type for updating state with optimistic values.
 *
 * @param T The type of the current state.
 * @param D The type of the optimistic value.
 * @return The updated state after applying the optimistic value.
 */
typealias OptimisticUpdate<T, D> = (currentState: T, optimisticValue: D) -> T

/**
 * Function type for handling completion of optimistic updates.
 *
 * This handle is intended for manual use when you need to explicitly complete an optimistic update.
 * The parameter represents the throwable that caused the completion, null if completed successfully.
 */
typealias OptimisticCompletionHandle = (cause: Throwable?) -> Unit

/**
 * Creates and remembers an optimistic state object where the state and optimistic value are of the same type.
 *
 * This is a convenience overload for when the optimistic value directly replaces the current state.
 *
 * Usage:
 *
 * ```kotlin
 * var counter by remember { mutableStateOf(0) }
 * // Using destructuring declaration for cleaner access
 * val (optimisticState, addOptimistic) = rememberOptimistic(counter)
 * // Inside a coroutine scope - addOptimistic must be called within a coroutine context
 * scope.launch {
 *     // Apply an optimistic update
 *     val completion = addOptimistic(counter + 1)
 *
 *     // Perform the actual operation (e.g. network request)
 *     try {
 *         api.incrementCounter()
 *         counter += 1
 *         // Signal successful completion
 *         completion(null)
 *     } catch (e: Exception) {
 *         // Signal error - resets optimistic state by default
 *         completion(e)
 *     }
 * }
 * ```
 *
 * @param T The type of both the state and optimistic values.
 * @param state The initial state.
 * @param policy The policy that determines how to handle errors in optimistic updates.
 * @return An [OptimisticObject] that manages optimistic updates.
 */
@Composable
inline fun <T> rememberOptimistic(
    state: T,
    policy: OptimisticUpdatePolicy = OptimisticUpdatePolicy,
): OptimisticObject<T, T> {
    return rememberOptimistic(
        state = state,
        updateFn = { _, optimisticValue -> optimisticValue },
        policy = policy
    )
}

/**
 * Creates and remembers an optimistic state object that can track optimistic updates.
 *
 * Optimistic updates allow showing immediate UI feedback for operations that may take time
 * to complete, such as network requests. The UI can be updated optimistically assuming
 * the operation will succeed, then reconciled later if it fails.
 *
 * Usage:
 *
 * ```kotlin
 * // A list that we want to update optimistically
 * var items by remember { mutableStateOf(listOf("A", "B", "C")) }
 *
 * // Create optimistic state with a custom update function
 * val (optimisticItems, addItem) = rememberOptimistic<List<String>, String>(
 *     state = items,
 *     updateFn = { currentList, newItem -> currentList + newItem }
 * )
 *
 * // Inside a coroutine scope - addItem must be called within a coroutine context
 * scope.launch {
 *     // Apply an optimistic update - adds the new item right away
 *     val completion = addItem("D")
 *
 *     // Perform the actual operation
 *     try {
 *         api.addItem("D")
 *         items = items + "D"
 *         completion(null) // Success
 *     } catch (e: Exception) {
 *         completion(e) // Error - reverts the optimistic update
 *     }
 * }
 * ```
 *
 * @param T The type of the state.
 * @param D The type of the optimistic value.
 * @param state The initial state.
 * @param policy The policy that determines how to handle errors in optimistic updates.
 * @param updateFn Function that determines how an optimistic value updates the current state.
 * @return An [OptimisticObject] that manages optimistic updates.
 */
@Composable
fun <T, D> rememberOptimistic(
    state: T,
    policy: OptimisticUpdatePolicy = OptimisticUpdatePolicy,
    updateFn: OptimisticUpdate<T, D>
): OptimisticObject<T, D> {

    var pendingUpdates by remember { mutableStateOf(emptyList<Triple<D, Job, DisposableHandle>>()) }
    val optimisticState = remember(state, pendingUpdates) {
        pendingUpdates.fold(state) { acc, (optimisticValue, _, _) ->
            updateFn(acc, optimisticValue)
        }
    }

    val addOptimistic = remember<CoroutineScope.(D) -> OptimisticCompletionHandle>() {
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

    return OptimisticObject(optimisticState, addOptimistic)
}

/**
 * Data class representing an optimistic state with methods to apply optimistic updates.
 *
 * **This class is designed to be used with destructuring declarations for clean access to its properties:**
 *
 * ```kotlin
 * val (optimisticState, addOptimistic) = rememberOptimistic(initialState)
 * ```
 *
 * @param T The type of the state.
 * @param D The type of the optimistic value.
 * @property optimisticState The current state including any applied optimistic updates.
 * @property addOptimistic A function to add a new optimistic update within a CoroutineScope.
 *             Returns a completion handle that can be used to manually signal completion.
 */
@Immutable
data class OptimisticObject<T, D> internal constructor(
    val optimisticState: T,
    val addOptimistic: CoroutineScope.(D) -> OptimisticCompletionHandle,
)

/**
 * Interface defining a policy for handling errors in optimistic updates.
 *
 * This policy determines whether to reset all pending optimistic updates when an error occurs.
 */
@Stable
interface OptimisticUpdatePolicy {

    /**
     * Determines whether all optimistic updates should be reset when an error occurs.
     *
     * @param cause The throwable that caused the error.
     * @return True if all pending optimistic updates should be reset, false otherwise.
     */
    fun shouldResetOnError(cause: Throwable): Boolean

    /**
     * Default implementation of [OptimisticUpdatePolicy] that resets all updates on any error.
     */
    companion object Default : OptimisticUpdatePolicy {
        override fun shouldResetOnError(cause: Throwable): Boolean = true
    }
}
