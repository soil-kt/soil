package soil.playground.query.key.users

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
class GetUserAlbumsKey(userId: Int) : KeyEquals(), InfiniteQueryKey<Albums, PageParam> by buildKtorInfiniteQueryKey(
    id = InfiniteQueryId.forGetUserAlbums(userId),
    fetch = { param ->
        get("https://jsonplaceholder.typicode.com/users/$userId/albums") {
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

fun InfiniteQueryId.Companion.forGetUserAlbums(userId: Int) = InfiniteQueryId<Albums, PageParam>(
    namespace = "users/$userId/albums/*"
)
