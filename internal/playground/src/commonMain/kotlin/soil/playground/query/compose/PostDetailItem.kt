package soil.playground.query.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soil.playground.query.data.Post
import soil.playground.style.withAppTheme

@Composable
fun PostDetailItem(
    post: Post,
    modifier: Modifier = Modifier
) = withAppTheme {
    Box(
        modifier = modifier
            .background(colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Post: ${post.id}",
                style = typography.titleSmall
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Title:", style = typography.titleSmall)
                Text(text = post.title)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Body:", style = typography.titleSmall)
                Text(text = post.body)
            }
        }
    }
}
