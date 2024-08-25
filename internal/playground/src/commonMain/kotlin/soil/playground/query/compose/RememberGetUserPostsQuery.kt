package soil.playground.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.playground.query.data.PageParam
import soil.playground.query.data.Posts
import soil.playground.query.key.users.GetUserPostsKey
import soil.query.chunkedData
import soil.query.compose.InfiniteQueryConfig
import soil.query.compose.InfiniteQueryObject
import soil.query.compose.rememberInfiniteQuery

typealias GetUserPostsQueryObject = InfiniteQueryObject<Posts, PageParam>

@Composable
fun rememberGetUserPostsQuery(
    userId: Int,
    builderBlock: InfiniteQueryConfig.Builder.() -> Unit = {}
): GetUserPostsQueryObject {
    val key = remember(userId) { GetUserPostsKey(userId) }
    return rememberInfiniteQuery(key = key, select = { it.chunkedData }, config = InfiniteQueryConfig(builderBlock))
}
