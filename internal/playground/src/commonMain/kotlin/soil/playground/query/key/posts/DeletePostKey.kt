package soil.playground.query.key.posts

import io.ktor.client.request.delete
import soil.query.InfiniteQueryId
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.QueryId
import soil.query.core.Auto
import soil.query.receivers.ktor.buildKtorMutationKey

class DeletePostKey(auto: Auto) : MutationKey<Unit, Int> by buildKtorMutationKey(
    id = MutationId(auto.namespace),
    mutate = { postId ->
        delete("https://jsonplaceholder.typicode.com/posts/$postId")
    }
) {
    override fun onQueryUpdate(variable: Int, data: Unit): QueryEffect = {
        removeQueriesBy(QueryId.forGetPost(variable))
        invalidateQueriesBy(InfiniteQueryId.forGetPosts())
    }
}
