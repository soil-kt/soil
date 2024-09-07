package soil.playground.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.playground.query.key.ExampleSubscriptionKey
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SubscriptionObject
import soil.query.compose.rememberSubscription

@OptIn(ExperimentalSoilQueryApi::class)
@Composable
fun rememberExampleSubscription(): SubscriptionObject<String> {
    val key = remember { ExampleSubscriptionKey() }
    return rememberSubscription(key)
}
