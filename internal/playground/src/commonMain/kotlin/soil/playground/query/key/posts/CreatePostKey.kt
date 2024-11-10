package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import soil.playground.query.data.Post
import soil.query.InfiniteQueryId
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.core.Namespace
import soil.query.receivers.ktor.buildKtorMutationKey

class CreatePostKey(auto: Namespace) : MutationKey<Post, PostForm> by buildKtorMutationKey(
    id = MutationId(auto.value),
    mutate = { body ->
        post("https://jsonplaceholder.typicode.com/posts") {
            setBody(body)
        }.body()
    }
) {
    override fun onQueryUpdate(variable: PostForm, data: Post): QueryEffect = {
        invalidateQueriesBy(InfiniteQueryId.forGetPosts())
    }
}

data class PostForm(
    val title: String,
    val body: String,
    val userId: Long
)
