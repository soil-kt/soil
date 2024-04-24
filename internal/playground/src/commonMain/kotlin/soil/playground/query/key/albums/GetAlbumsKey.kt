package soil.playground.query.key.albums

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.KtorReceiver
import soil.playground.query.data.Albums
import soil.playground.query.data.PageParam
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.buildInfiniteQueryKey

class GetAlbumsKey : InfiniteQueryKey<Albums, PageParam> by buildInfiniteQueryKey(
    id = Id(),
    fetch = { param ->
        this as KtorReceiver
        client.get("https://jsonplaceholder.typicode.com/albums") {
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
    class Id : InfiniteQueryId<Albums, PageParam>(
        namespace = "albums/*"
    )
}
