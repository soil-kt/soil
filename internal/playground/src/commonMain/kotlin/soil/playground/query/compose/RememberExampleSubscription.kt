package soil.playground.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.playground.query.key.ExampleSubscriptionKey
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SubscriptionObject
import soil.query.compose.auto
import soil.query.compose.rememberSubscription
import soil.query.core.Namespace

@OptIn(ExperimentalSoilQueryApi::class)
@Composable
fun rememberExampleSubscription(): SubscriptionObject<String> {
    val auto = Namespace.auto()
    val key = remember { ExampleSubscriptionKey(auto) }
    return rememberSubscription(key)
}
