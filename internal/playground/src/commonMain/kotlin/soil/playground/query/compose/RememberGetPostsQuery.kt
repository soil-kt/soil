package soil.playground.query.compose

import androidx.compose.runtime.Composable
import soil.playground.query.data.PageParam
import soil.playground.query.data.Posts
import soil.playground.query.key.posts.GetPostsKey
import soil.query.chunkedData
import soil.query.compose.InfiniteQueryConfig
import soil.query.compose.InfiniteQueryObject
import soil.query.compose.rememberInfiniteQuery

typealias GetPostsQueryObject = InfiniteQueryObject<Posts, PageParam>

@Composable
fun rememberGetPostsQuery(
    userId: Int? = null,
    builderBlock: InfiniteQueryConfig.Builder.() -> Unit = {}
): GetPostsQueryObject {
    return rememberInfiniteQuery(
        key = GetPostsKey(userId),
        select = { it.chunkedData },
        config = InfiniteQueryConfig(builderBlock)
    )
}
