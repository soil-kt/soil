package soil.playground.query.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T : Any> ContentLoadMore(
    onLoadMore: suspend (param: T) -> Unit,
    pageParam: T
) {
    ContentLoading(
        modifier = Modifier.fillMaxWidth(),
        size = 20.dp
    )
    LaunchedEffect(Unit) {
        onLoadMore(pageParam)
    }
}
