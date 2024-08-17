package soil.playground.query.key.users

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import soil.playground.query.data.PageParam
import soil.playground.query.data.Users
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.receivers.ktor.buildKtorInfiniteQueryKey

class GetUsersKey : InfiniteQueryKey<Users, PageParam> by buildKtorInfiniteQueryKey(
    id = Id(),
    fetch = { param ->
        get("https://jsonplaceholder.typicode.com/users") {
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
    class Id : InfiniteQueryId<Users, PageParam>(
        namespace = "users/*"
    )
}
