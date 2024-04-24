package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.KtorReceiver
import soil.playground.query.data.PageParam
import soil.playground.query.data.Posts
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.buildInfiniteQueryKey

// NOTE: userId
// Filtering resources
// ref. https://jsonplaceholder.typicode.com/guide/
class GetPostsKey(userId: Int? = null) : InfiniteQueryKey<Posts, PageParam> by buildInfiniteQueryKey(
    id = Id(userId),
    fetch = { param ->
        this as KtorReceiver
        client.get("https://jsonplaceholder.typicode.com/posts") {
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
) {
    class Id(userId: Int? = null) : InfiniteQueryId<Posts, PageParam>(
        namespace = "posts/*",
        "userId" to userId
    )
}
