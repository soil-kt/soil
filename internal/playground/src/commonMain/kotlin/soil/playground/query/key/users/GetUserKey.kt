package soil.playground.query.key.users

import io.ktor.client.call.body
import io.ktor.client.request.get
import soil.playground.query.data.User
import soil.query.QueryId
import soil.query.QueryInitialData
import soil.query.QueryKey
import soil.query.chunkedData
import soil.query.receivers.ktor.buildKtorQueryKey

class GetUserKey(private val userId: Int) : QueryKey<User> by buildKtorQueryKey(
    id = Id(userId),
    fetch = {
        get("https://jsonplaceholder.typicode.com/users/$userId").body()
    }
) {

    override fun onInitialData(): QueryInitialData<User> = {
        getInfiniteQueryData(GetUsersKey.Id())?.let {
            it.chunkedData.firstOrNull { user -> user.id == userId }
        }
    }

    class Id(userId: Int) : QueryId<User>(
        namespace = "users/$userId"
    )
}
