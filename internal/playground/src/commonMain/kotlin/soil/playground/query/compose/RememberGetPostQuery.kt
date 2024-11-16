package soil.playground.query.compose

import androidx.compose.runtime.Composable
import soil.playground.query.data.Post
import soil.playground.query.key.posts.GetPostKey
import soil.query.compose.QueryConfig
import soil.query.compose.QueryObject
import soil.query.compose.rememberQuery

typealias GetPostQueryObject = QueryObject<Post>

@Composable
fun rememberGetPostQuery(
    postId: Int,
    builderBlock: QueryConfig.Builder.() -> Unit = {}
): GetPostQueryObject {
    return rememberQuery(
        key = GetPostKey(postId),
        config = QueryConfig(builderBlock)
    )
}
