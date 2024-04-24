package soil.playground.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.playground.query.data.PageParam
import soil.playground.query.data.Posts
import soil.playground.query.key.posts.GetPostsKey
import soil.query.chunkedData
import soil.query.compose.InfiniteQueryObject
import soil.query.compose.rememberInfiniteQuery

typealias GetPostsQueryObject = InfiniteQueryObject<Posts, PageParam>

@Composable
fun rememberGetPostsQuery(userId: Int? = null): GetPostsQueryObject {
    val key = remember(userId) { GetPostsKey(userId) }
    return rememberInfiniteQuery(key, select = { it.chunkedData })
}
