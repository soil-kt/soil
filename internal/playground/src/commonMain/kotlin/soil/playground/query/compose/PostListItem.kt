package soil.playground.query.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import soil.playground.query.data.Post
import soil.playground.style.withAppTheme

@Composable
fun PostListItem(
    onClick: () -> Unit,
    post: Post,
    modifier: Modifier = Modifier
) = withAppTheme {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        PostListInnerItem(post)
    }
}

@Composable
fun PostListItem(
    post: Post,
    modifier: Modifier = Modifier
) = withAppTheme {
    Card(
        modifier = modifier
    ) {
        PostListInnerItem(post)
    }
}

@Composable
private fun PostListInnerItem(
    post: Post,
) = withAppTheme {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "Post: ${post.id}", style = typography.titleSmall)
        Text(text = post.title, overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}
