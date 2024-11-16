package soil.playground.query.key.users

import androidx.compose.runtime.Stable
import io.ktor.client.call.body
import io.ktor.client.request.get
import soil.playground.query.data.User
import soil.query.InfiniteQueryId
import soil.query.QueryId
import soil.query.QueryInitialData
import soil.query.QueryKey
import soil.query.chunkedData
import soil.query.core.KeyEquals
import soil.query.receivers.ktor.buildKtorQueryKey

@Stable
class GetUserKey(private val userId: Int) : KeyEquals(), QueryKey<User> by buildKtorQueryKey(
    id = QueryId.forGetUser(userId),
    fetch = {
        get("https://jsonplaceholder.typicode.com/users/$userId").body()
    }
) {
    override fun onInitialData(): QueryInitialData<User> = {
        getInfiniteQueryData(InfiniteQueryId.forGetUsers())?.let {
            it.chunkedData.firstOrNull { user -> user.id == userId }
        }
    }
}

fun QueryId.Companion.forGetUser(userId: Int) = QueryId<User>(
    namespace = "users/$userId"
)
