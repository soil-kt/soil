package soil.playground.query.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
inline fun <T : Any> LoadMoreEffect(
    state: LazyListState,
    noinline loadMore: suspend (T) -> Unit,
    loadMoreParam: T?,
    crossinline predicate: (totalCount: Int, lastIndex: Int) -> Boolean = { totalCount, lastIndex ->
        totalCount > 0 && lastIndex > totalCount - 5
    }
) {
    LaunchedEffect(state, loadMore, loadMoreParam) {
        if (loadMoreParam == null) return@LaunchedEffect
        snapshotFlow {
            val totalCount = state.layoutInfo.totalItemsCount
            val lastIndex = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            predicate(totalCount, lastIndex)
        }
            .debounce(250.milliseconds)
            .filter { it }
            .collect {
                loadMore(loadMoreParam)
            }
    }
}
