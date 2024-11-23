package soil.playground.query.compose

import androidx.compose.runtime.Composable
import soil.playground.query.key.ExampleSubscriptionKey
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SubscriptionObject
import soil.query.compose.rememberSubscription
import soil.query.compose.util.autoCompositionKeyHash
import soil.query.core.Namespace

@OptIn(ExperimentalSoilQueryApi::class)
@Composable
fun rememberExampleSubscription(): SubscriptionObject<String> {
    return rememberSubscription(
        key = ExampleSubscriptionKey(Namespace.autoCompositionKeyHash)
    )
}
