package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import soil.playground.query.KtorReceiver
import soil.playground.query.data.Post
import soil.query.MutationKey
import soil.query.QueryEffect
import soil.query.buildMutationKey

class CreatePostKey : MutationKey<Post, PostForm> by buildMutationKey(
    /* id = MutationId.auto(), */
    mutate = { body ->
        this as KtorReceiver
        client.post("https://jsonplaceholder.typicode.com/posts") {
            setBody(body)
        }.body()
    }
) {
    override fun onQueryUpdate(variable: PostForm, data: Post): QueryEffect= {
        invalidateQueriesBy(GetPostsKey.Id())
    }
}

data class PostForm(
    val title: String,
    val body: String,
    val userId: Long
)
