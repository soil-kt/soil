package soil.playground.query.key.posts

import androidx.compose.runtime.Stable
import io.ktor.client.request.delete
import soil.query.InfiniteQueryId
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryId
import soil.query.core.Effect
import soil.query.core.KeyEquals
import soil.query.core.Namespace
import soil.query.receivers.ktor.buildKtorMutationKey
import soil.query.withQuery

@Stable
class DeletePostKey(auto: Namespace) : KeyEquals(), MutationKey<Unit, Int> by buildKtorMutationKey(
    id = MutationId(auto.value),
    mutate = { postId ->
        delete("https://jsonplaceholder.typicode.com/posts/$postId")
    }
) {
    override fun onMutateEffect(variable: Int, data: Unit): Effect = {
        withQuery {
            removeQueriesBy(QueryId.forGetPost(variable))
            invalidateQueriesBy(InfiniteQueryId.forGetPosts())
        }
    }
}
