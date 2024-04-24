package soil.playground.query.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soil.playground.query.data.Post
import soil.playground.query.data.Posts
import soil.playground.query.data.User
import soil.playground.style.withAppTheme

@Composable
fun PostUserDetailItem(
    user: User,
    posts: Posts,
    modifier: Modifier = Modifier
) = withAppTheme {
    Box(modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Author:", style = typography.titleSmall)
                Text(text = "${user.username} - ${user.email}")
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Author's post:", style = typography.titleSmall)

                posts.forEach { post ->
                    PostListItem(
                        post = post,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
