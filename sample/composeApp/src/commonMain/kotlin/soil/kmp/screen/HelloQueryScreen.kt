package soil.kmp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.plugins.ResponseException
import soil.playground.Alert
import soil.playground.query.compose.ContentLoading
import soil.playground.query.compose.ContentUnavailable
import soil.playground.query.compose.LoadMoreEffect
import soil.playground.query.compose.PostListItem
import soil.playground.query.compose.rememberGetPostsQuery
import soil.playground.query.data.PageParam
import soil.playground.query.data.Posts
import soil.playground.router.NavLink
import soil.playground.style.withAppTheme
import soil.query.compose.rememberQueriesErrorReset
import soil.query.compose.runtime.Await
import soil.query.compose.runtime.Catch
import soil.query.compose.runtime.ErrorBoundary
import soil.query.compose.runtime.Suspense

@Composable
fun HelloQueryScreen() {
    HelloQueryScreenTemplate {
        HelloQueryContent(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun HelloQueryScreenTemplate(
    content: @Composable () -> Unit
) {
    ErrorBoundary(
        modifier = Modifier.fillMaxSize(),
        fallback = {
            ContentUnavailable(
                error = it.err,
                reset = it.reset,
                modifier = Modifier.matchParentSize()
            )
        },
        onError = { e -> println(e.toString()) },
        onReset = rememberQueriesErrorReset()
    ) {
        Suspense(
            fallback = { ContentLoading(modifier = Modifier.matchParentSize()) },
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
private fun HelloQueryContent(
    modifier: Modifier = Modifier
) = withAppTheme {
    ListSectionContainer { state ->
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.posts, key = { it.id }) { post ->
                NavLink(to = NavScreen.HelloQueryDetail(post.id)) {
                    PostListItem(
                        onClick = it,
                        post = post,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            val pageParam = state.loadMoreParam
            if (state.posts.isNotEmpty() && pageParam != null) {
                item(pageParam, contentType = "loading") {
                    ContentLoading(
                        modifier = Modifier.fillMaxWidth(),
                        size = 20.dp
                    )
                }
            }
        }
        LoadMoreEffect(
            state = lazyListState,
            loadMore = state.loadMore,
            loadMoreParam = state.loadMoreParam
        )
    }
}

// TIPS: Adopting the Presentational-Container Pattern allows for separation of control and presentation.
// https://www.patterns.dev/react/presentational-container-pattern
@Composable
private fun ListSectionContainer(
    content: @Composable (ListSectionState) -> Unit
) {
    val query = rememberGetPostsQuery()
    Await(query) { posts ->
        val state by remember(posts, query.loadMoreParam, query.loadMore) {
            derivedStateOf { ListSectionState(posts, query.loadMoreParam, query.loadMore) }
        }
        content(state)
    }
    Catch(query) { e ->
        // You can also write your own error handling logic.
        // Try testing the error by setting your mobile device to AirPlane mode.
        if (e !is ResponseException) {
            Alert(message = "Unexpected error :(")
            return@Catch
        }
        Throw(e)
    }
}

private data class ListSectionState(
    val posts: Posts,
    val loadMoreParam: PageParam?,
    val loadMore: suspend (PageParam) -> Unit
)
