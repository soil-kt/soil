package soil.playground.query.key.posts

import androidx.compose.runtime.Stable
import io.ktor.client.call.body
import io.ktor.client.request.get
import soil.playground.query.data.Post
import soil.query.InfiniteQueryId
import soil.query.QueryId
import soil.query.QueryInitialData
import soil.query.QueryKey
import soil.query.chunkedData
import soil.query.core.KeyEquals
import soil.query.receivers.ktor.buildKtorQueryKey

@Stable
class GetPostKey(private val postId: Int) : KeyEquals(), QueryKey<Post> by buildKtorQueryKey(
    id = QueryId.forGetPost(postId),
    fetch = {
        get("https://jsonplaceholder.typicode.com/posts/$postId").body()
    }
) {
    override fun onInitialData(): QueryInitialData<Post> = {
        getInfiniteQueryData(InfiniteQueryId.forGetPosts())?.let {
            it.chunkedData.firstOrNull { post -> post.id == postId }
        }
    }
}

fun QueryId.Companion.forGetPost(postId: Int) = QueryId<Post>(
    namespace = "posts/$postId"
)
