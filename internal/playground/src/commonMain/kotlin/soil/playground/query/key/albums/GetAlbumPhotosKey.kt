package soil.playground.query.key.albums

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.data.PageParam
import soil.playground.query.data.Photos
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.receivers.ktor.buildKtorInfiniteQueryKey

class GetAlbumPhotosKey(albumId: Int) : InfiniteQueryKey<Photos, PageParam> by buildKtorInfiniteQueryKey(
    id = InfiniteQueryId.forGetAlbumPhotos(albumId),
    fetch = { param ->
        get("https://jsonplaceholder.typicode.com/albums/$albumId/photos") {
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
)

fun InfiniteQueryId.Companion.forGetAlbumPhotos(albumId: Int) = InfiniteQueryId<Photos, PageParam>(
    namespace = "albums/$albumId/photos/*"
)
