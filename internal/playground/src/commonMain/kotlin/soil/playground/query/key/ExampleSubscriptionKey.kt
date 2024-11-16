package soil.playground.query.key

import androidx.compose.runtime.Stable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.buildSubscriptionKey
import soil.query.core.KeyEquals
import soil.query.core.Namespace

@Stable
class ExampleSubscriptionKey(auto: Namespace) : KeyEquals(), SubscriptionKey<String> by buildSubscriptionKey(
    id = SubscriptionId(auto.value),
    subscribe = {
        flow {
            delay(1000)
            emit("Hello, World!")
            delay(1000)
            emit("Hello, Compose!")
            delay(1000)
            emit("Hello, Soil!")
        }
    }
)
