package soil.playground.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.playground.query.data.Post
import soil.playground.query.key.posts.GetPostKey
import soil.query.compose.QueryObject
import soil.query.compose.rememberQuery

typealias GetPostQueryObject = QueryObject<Post>

@Composable
fun rememberGetPostQuery(postId: Int): GetPostQueryObject {
    val key = remember(postId) { GetPostKey(postId) }
    return rememberQuery(key)
}
