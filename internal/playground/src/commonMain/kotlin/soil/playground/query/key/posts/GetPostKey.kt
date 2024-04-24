package soil.playground.query.key.posts

import io.ktor.client.call.body
import io.ktor.client.request.get
import soil.playground.query.KtorReceiver
import soil.playground.query.data.Post
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryPlaceholderData
import soil.query.buildQueryKey
import soil.query.chunkedData

class GetPostKey(private val postId: Int) : QueryKey<Post> by buildQueryKey(
    id = Id(postId),
    fetch = {
        this as KtorReceiver
        client.get("https://jsonplaceholder.typicode.com/posts/$postId").body()
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
