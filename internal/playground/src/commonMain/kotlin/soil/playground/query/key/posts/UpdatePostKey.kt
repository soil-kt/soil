package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import soil.playground.query.data.Post
import soil.query.InfiniteQueryId
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.QueryId
import soil.query.core.Auto
import soil.query.modifyData
import soil.query.receivers.ktor.buildKtorMutationKey

class UpdatePostKey(auto: Auto) : MutationKey<Post, Post> by buildKtorMutationKey(
    id = MutationId(auto.namespace),
    mutate = { body ->
        put("https://jsonplaceholder.typicode.com/posts/${body.id}") {
            setBody(body)
        }.body()
    }
) {
    override fun onQueryUpdate(variable: Post, data: Post): QueryEffect = {
        updateQueryData(QueryId.forGetPost(data.id)) { data }
        updateInfiniteQueryData(InfiniteQueryId.forGetPosts()) { modifyData({ it.id == data.id }) { data } }
    }
}
