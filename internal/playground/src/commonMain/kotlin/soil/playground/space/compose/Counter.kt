package soil.playground.space.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soil.playground.style.withAppTheme

@Composable
fun Counter(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) = withAppTheme {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onDecrement) {
            Text("-")
        }
        Text(
            text = value.toString(),
            style = typography.displayLarge
        )
        OutlinedButton(onClick = onIncrement) {
            Text("+")
        }
    }
}
