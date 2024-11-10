package soil.playground.query.key

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.buildSubscriptionKey
import soil.query.core.Namespace

class ExampleSubscriptionKey(auto: Namespace) : SubscriptionKey<String> by buildSubscriptionKey(
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
