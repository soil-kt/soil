package soil.query.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

@Composable
fun <T> rememberOptimistic(
    scope: CoroutineScope = rememberCoroutineScope(),
    state: T,
    policy: OptimisticUpdatePolicy = OptimisticUpdatePolicy.Default,
): OptimisticObject<T, T> {

    val passThrough = remember<OptimisticUpdateFn<T, T>> {
        OptimisticUpdateFn { _, optimisticValue ->
            optimisticValue
        }
    }

    return rememberOptimistic(
        scope = scope,
        state = state,
        updateFn = passThrough,
        policy = policy
    )
}

@Composable
fun <T, D> rememberOptimistic(
    scope: CoroutineScope = rememberCoroutineScope(),
    state: T,
    updateFn: OptimisticUpdateFn<T, D>,
    policy: OptimisticUpdatePolicy = OptimisticUpdatePolicy.Default,
): OptimisticObject<T, D> {

    var pendingValues by remember { mutableStateOf(emptyList<Pair<D, Job>>()) }
    val optimisticState = remember(pendingValues) {
        pendingValues.fold(state) { acc, (optimisticValue, _) ->
            updateFn(acc, optimisticValue)
        }
    }

    val addOptimistic = remember<CoroutineScope.(D) -> Unit> {
        { value ->
            val currentJob = coroutineContext[Job] ?: error("No Job in CoroutineScope")
            pendingValues += Pair(value, currentJob)
            currentJob.invokeOnCompletion { err ->
                pendingValues = if (err != null && policy.shouldResetOnError(cause = err)) {
                    emptyList()
                } else {
                    pendingValues.filterNot { (_, job) -> job === currentJob }
                }
            }
        }
    }

    return OptimisticObject(
        scope = scope,
        state = optimisticState,
        add = addOptimistic,
    )
}

@Immutable
data class OptimisticObject<T, D>(
    val scope: CoroutineScope,
    val state: T,
    val add: CoroutineScope.(D) -> Unit,
)

@Stable
fun interface OptimisticUpdateFn<T, D> {
    operator fun invoke(currentState: T, optimisticValue: D): T
}

@Stable
interface OptimisticUpdatePolicy {
    fun shouldResetOnError(cause: Throwable): Boolean

    companion object
}

val OptimisticUpdatePolicy.Companion.Default: OptimisticUpdatePolicy
    get() = DefaultOptimisticUpdatePolicy

private object DefaultOptimisticUpdatePolicy : OptimisticUpdatePolicy {
    override fun shouldResetOnError(cause: Throwable): Boolean {
        return cause is CancellationException
    }
}
