package soil.playground.query.key.users

import io.ktor.client.call.body
import io.ktor.client.request.get
import soil.playground.query.KtorReceiver
import soil.playground.query.data.User
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryPlaceholderData
import soil.query.buildQueryKey
import soil.query.chunkedData

class GetUserKey(private val userId: Int) : QueryKey<User> by buildQueryKey(
    id = Id(userId),
    fetch = {
        this as KtorReceiver
        client.get("https://jsonplaceholder.typicode.com/users/$userId").body()
    }
) {

    override fun onPlaceholderData(): QueryPlaceholderData<User> = {
        getInfiniteQueryData(GetUsersKey.Id())?.let {
            it.chunkedData.firstOrNull { user -> user.id == userId }
        }
    }

    class Id(userId: Int) : QueryId<User>(
        namespace = "users/$userId"
    )
}
