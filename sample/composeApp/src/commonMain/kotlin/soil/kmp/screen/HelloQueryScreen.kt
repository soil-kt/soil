package soil.kmp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.plugins.ResponseException
import soil.playground.Alert
import soil.playground.query.compose.ContentLoadMore
import soil.playground.query.compose.ContentLoading
import soil.playground.query.compose.ContentUnavailable
import soil.playground.query.compose.PostDetailItem
import soil.playground.query.compose.PostListItem
import soil.playground.query.compose.PostUserDetailItem
import soil.playground.query.compose.rememberGetPostQuery
import soil.playground.query.compose.rememberGetPostsQuery
import soil.playground.query.compose.rememberGetUserPostsQuery
import soil.playground.query.compose.rememberGetUserQuery
import soil.playground.query.data.PageParam
import soil.playground.query.data.Post
import soil.playground.query.data.Posts
import soil.playground.query.data.User
import soil.playground.style.withAppTheme
import soil.query.compose.rememberQueriesErrorReset
import soil.query.compose.runtime.Await
import soil.query.compose.runtime.Catch
import soil.query.compose.runtime.ErrorBoundary
import soil.query.compose.runtime.Suspense

class HelloQueryScreen : Screen {

    @Composable
    override fun Content() {
        HelloQueryScreenTemplate {
            val navigator = LocalNavigator.currentOrThrow
            HelloQueryContent(
                onSelect = { navigator.push(PostDetailScreen(postId = it.id)) },
                modifier = Modifier.fillMaxSize()
            )
        }
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
    onSelect: (Post) -> Unit,
    modifier: Modifier = Modifier
) = withAppTheme {
    ListSectionContainer { state ->
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.posts, key = { it.id }) { post ->
                PostListItem(
                    onClick = { onSelect(post) },
                    post = post,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val pageParam = state.loadMoreParam
            if (pageParam != null) {
                item(pageParam, contentType = "loading") {
                    ContentLoadMore(
                        onLoadMore = state.loadMore,
                        pageParam = pageParam
                    )
                }
            }
        }
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

private class PostDetailScreen(
    val postId: Int
) : Screen {
    @Composable
    override fun Content() {
        HelloQueryScreenTemplate {
            PostDetailContent(
                postId = postId,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PostDetailContent(
    postId: Int,
    modifier: Modifier = Modifier
) {
    PostDetailContainer(postId) { post ->
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PostDetailItem(post, modifier = Modifier.fillMaxWidth())
            PostUserDetailContainer(userId = post.userId) { user, posts ->
                PostUserDetailItem(user = user, posts = posts)
            }
        }
    }
}

@Composable
private fun PostDetailContainer(
    postId: Int,
    content: @Composable (Post) -> Unit
) {
    val query = rememberGetPostQuery(postId)
    Await(query) { post ->
        content(post)
    }
    Catch(query)
}

@Composable
private fun PostUserDetailContainer(
    userId: Int,
    content: @Composable (User, Posts) -> Unit
) {
    val userQuery = rememberGetUserQuery(userId)
    val postsQuery = rememberGetUserPostsQuery(userId)
    Suspense(
        fallback = { ContentLoading(modifier = Modifier.matchParentSize()) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Await(userQuery, postsQuery) { user, posts ->
            content(user, posts)
        }
    }
    Catch(userQuery, postsQuery)
}
