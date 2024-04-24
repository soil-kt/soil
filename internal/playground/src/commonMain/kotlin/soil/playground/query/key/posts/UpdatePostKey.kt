package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import soil.playground.query.KtorReceiver
import soil.playground.query.data.Post
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.buildMutationKey
import soil.query.modifyData

class UpdatePostKey : MutationKey<Post, Post> by buildMutationKey(
    /* id = MutationId.auto(), */
    mutate = { body ->
        this as KtorReceiver
        client.put("https://jsonplaceholder.typicode.com/posts/${body.id}") {
            setBody(body)
        }.body()
    }
) {
    override fun onQueryUpdate(variable: Post, data: Post): QueryEffect = {
        updateQueryData(GetPostKey.Id(data.id)) { data }
        updateInfiniteQueryData(GetPostsKey.Id()) { modifyData({ it.id == data.id }) { data } }
    }
}
