package soil.playground.query.key.posts

import androidx.compose.runtime.Stable
import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import soil.playground.query.data.Post
import soil.query.InfiniteQueryId
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryId
import soil.query.core.Effect
import soil.query.core.KeyEquals
import soil.query.core.Namespace
import soil.query.modifyData
import soil.query.receivers.ktor.buildKtorMutationKey
import soil.query.withQuery

@Stable
class UpdatePostKey(auto: Namespace) : KeyEquals(), MutationKey<Post, Post> by buildKtorMutationKey(
    id = MutationId(auto.value),
    mutate = { body ->
        put("https://jsonplaceholder.typicode.com/posts/${body.id}") {
            setBody(body)
        }.body()
    }
) {

    override fun onMutateEffect(variable: Post, data: Post): Effect = {
        withQuery {
            updateQueryData(QueryId.forGetPost(data.id)) { data }
            updateInfiniteQueryData(InfiniteQueryId.forGetPosts()) { modifyData({ it.id == data.id }) { data } }
        }
    }
}
