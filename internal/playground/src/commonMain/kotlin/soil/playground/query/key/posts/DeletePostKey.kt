package soil.playground.query.key.posts

import io.ktor.client.request.delete
import soil.playground.query.KtorReceiver
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.buildMutationKey

class DeletePostKey : MutationKey<Unit, Int> by buildMutationKey(
    /* id = MutationId.auto(), */
    mutate = { postId ->
        this as KtorReceiver
        client.delete("https://jsonplaceholder.typicode.com/posts/$postId")
    }
) {
    override fun onQueryUpdate(variable: Int, data: Unit): QueryEffect = {
        removeQueriesBy(GetPostKey.Id(variable))
        invalidateQueriesBy(GetPostsKey.Id())
    }
}
