package soil.playground.query.key.albums

import androidx.compose.runtime.Stable
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.data.Albums
import soil.playground.query.data.PageParam
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.core.KeyEquals
import soil.query.receivers.ktor.buildKtorInfiniteQueryKey

@Stable
class GetAlbumsKey : KeyEquals(), InfiniteQueryKey<Albums, PageParam> by buildKtorInfiniteQueryKey(
    id = InfiniteQueryId.forGetAlbums(),
    fetch = { param ->
        get("https://jsonplaceholder.typicode.com/albums") {
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

fun InfiniteQueryId.Companion.forGetAlbums() = InfiniteQueryId<Albums, PageParam>(
    namespace = "albums/*"
)
