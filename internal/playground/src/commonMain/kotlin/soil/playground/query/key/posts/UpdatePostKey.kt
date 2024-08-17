package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import soil.playground.query.data.Post
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.modifyData
import soil.query.receivers.ktor.buildKtorMutationKey

class UpdatePostKey : MutationKey<Post, Post> by buildKtorMutationKey(
    /* id = MutationId.auto(), */
    mutate = { body ->
        put("https://jsonplaceholder.typicode.com/posts/${body.id}") {
            setBody(body)
        }.body()
    }
) {
    override fun onQueryUpdate(variable: Post, data: Post): QueryEffect = {
        updateQueryData(GetPostKey.Id(data.id)) { data }
        updateInfiniteQueryData(GetPostsKey.Id()) { modifyData({ it.id == data.id }) { data } }
    }
}
