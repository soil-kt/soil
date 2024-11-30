package soil.playground.query.key.posts

import androidx.compose.runtime.Stable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import soil.playground.query.data.Post
import soil.query.InfiniteQueryId
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.core.Effect
import soil.query.core.KeyEquals
import soil.query.core.Namespace
import soil.query.queryClient
import soil.query.receivers.ktor.buildKtorMutationKey

@Stable
class CreatePostKey(auto: Namespace) : KeyEquals(), MutationKey<Post, PostForm> by buildKtorMutationKey(
    id = MutationId(auto.value),
    mutate = { body ->
        post("https://jsonplaceholder.typicode.com/posts") {
            setBody(body)
        }.body()
    }
) {

    override fun onMutateEffect(variable: PostForm, data: Post): Effect = {
        queryClient.invalidateQueriesBy(InfiniteQueryId.forGetPosts())
    }
}

data class PostForm(
    val title: String,
    val body: String,
    val userId: Long
)
