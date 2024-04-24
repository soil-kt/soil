package soil.playground.query.key.albums

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.KtorReceiver
import soil.playground.query.data.PageParam
import soil.playground.query.data.Photos
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.buildInfiniteQueryKey

class GetAlbumPhotosKey(albumId: Int) : InfiniteQueryKey<Photos, PageParam> by buildInfiniteQueryKey(
    id = Id(albumId),
    fetch = { param ->
        this as KtorReceiver
        client.get("https://jsonplaceholder.typicode.com/albums/$albumId/photos") {
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
    class Id(albumId: Int) : InfiniteQueryId<Photos, PageParam>(
        namespace = "albums/$albumId/photos/*"
    )
}
