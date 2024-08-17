package soil.playground.query.key.posts

import io.ktor.client.request.delete
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.receivers.ktor.buildKtorMutationKey

class DeletePostKey : MutationKey<Unit, Int> by buildKtorMutationKey(
    /* id = MutationId.auto(), */
    mutate = { postId ->
        delete("https://jsonplaceholder.typicode.com/posts/$postId")
    }
) {
    override fun onQueryUpdate(variable: Int, data: Unit): QueryEffect = {
        removeQueriesBy(GetPostKey.Id(variable))
        invalidateQueriesBy(GetPostsKey.Id())
    }
}
