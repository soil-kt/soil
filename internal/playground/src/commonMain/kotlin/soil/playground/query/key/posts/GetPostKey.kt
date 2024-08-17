package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.get
import soil.playground.query.data.Post
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryPlaceholderData
import soil.query.chunkedData
import soil.query.receivers.ktor.buildKtorQueryKey

class GetPostKey(private val postId: Int) : QueryKey<Post> by buildKtorQueryKey(
    id = Id(postId),
    fetch = {
        get("https://jsonplaceholder.typicode.com/posts/$postId").body()
    }
) {

    override fun onPlaceholderData(): QueryPlaceholderData<Post> = {
        getInfiniteQueryData(GetPostsKey.Id())?.let {
            it.chunkedData.firstOrNull { post -> post.id == postId }
        }
    }

    class Id(postId: Int) : QueryId<Post>(
        namespace = "posts/$postId"
    )
}
