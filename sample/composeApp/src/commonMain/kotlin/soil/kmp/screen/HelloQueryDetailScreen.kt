package soil.kmp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soil.playground.query.compose.ContentLoading
import soil.playground.query.compose.ContentUnavailable
import soil.playground.query.compose.PostDetailItem
import soil.playground.query.compose.PostUserDetailItem
import soil.playground.query.compose.rememberExampleSubscription
import soil.playground.query.compose.rememberGetPostQuery
import soil.playground.query.compose.rememberGetUserPostsQuery
import soil.playground.query.compose.rememberGetUserQuery
import soil.playground.query.data.Post
import soil.playground.query.data.Posts
import soil.playground.query.data.User
import soil.query.compose.rememberQueriesErrorReset
import soil.query.compose.runtime.Await
import soil.query.compose.runtime.Catch
import soil.query.compose.runtime.ErrorBoundary
import soil.query.compose.runtime.Suspense
import soil.query.core.getOrElse

@Composable
fun HelloQueryDetailScreen(postId: Int) {
    HelloQueryScreenDetailTemplate {
        PostDetailContent(
            postId = postId,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HelloQueryScreenDetailTemplate(
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
private fun PostDetailContent(
    postId: Int,
    modifier: Modifier = Modifier
) {
    val foo = rememberExampleSubscription()
    PostDetailContainer(postId) { post ->
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = foo.reply.getOrElse { "" })
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
