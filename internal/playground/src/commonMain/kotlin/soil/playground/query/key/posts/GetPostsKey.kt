package soil.playground.query.key.posts

import androidx.compose.runtime.Stable
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.data.PageParam
import soil.playground.query.data.Posts
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.core.KeyEquals
import soil.query.receivers.ktor.buildKtorInfiniteQueryKey

// NOTE: userId
// Filtering resources
// ref. https://jsonplaceholder.typicode.com/guide/
@Stable
class GetPostsKey(
    val userId: Int? = null
) : KeyEquals(), InfiniteQueryKey<Posts, PageParam> by buildKtorInfiniteQueryKey(
    id = InfiniteQueryId.forGetPosts(userId),
    fetch = { param ->
        get("https://jsonplaceholder.typicode.com/posts") {
            parameter("_start", param.offset)
            parameter("_limit", param.limit)
            if (userId != null) {
                parameter("userId", userId)
            }
        }.body()
    },
    initialParam = { PageParam(limit = 20) },
    loadMoreParam = { chunks ->
        chunks.lastOrNull()
            ?.takeIf { it.data.isNotEmpty() }
            ?.run { param.copy(offset = param.offset + param.limit) }
    }
)

fun InfiniteQueryId.Companion.forGetPosts(userId: Int? = null) = InfiniteQueryId<Posts, PageParam>(
    namespace = "posts/*",
    "userId" to userId
)
