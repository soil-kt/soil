package soil.playground.query.key.users

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.KtorReceiver
import soil.playground.query.data.Albums
import soil.playground.query.data.PageParam
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.buildInfiniteQueryKey

class GetUserAlbumsKey(userId: Int) : InfiniteQueryKey<Albums, PageParam> by buildInfiniteQueryKey(
    id = Id(userId),
    fetch = { param ->
        this as KtorReceiver
        client.get("https://jsonplaceholder.typicode.com/users/$userId/albums") {
            parameter("_start", param.offset)
            parameter("_limit", param.limit)
        }.body()
    },
    initialParam = { PageParam() },
    loadMoreParam = { chunks ->
        chunks.lastOrNull()
            ?.takeIf { it.data.isNotEmpty() }
            ?.run { param.copy(offset = param.offset + param.limit) }
    }
) {
    class Id(userId: Int) : InfiniteQueryId<Albums, PageParam>(
        namespace = "users/$userId/albums/*"
    )
}
