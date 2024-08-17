package soil.kmp

import android.app.Application
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.AndroidMemoryPressure
import soil.query.AndroidNetworkConnectivity
import soil.query.AndroidWindowVisibility
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.SwrCacheScope
import soil.query.SwrClient
import soil.query.receivers.ktor.KtorReceiver

class SoilApplication : Application(), SwrClientFactory {

    override val queryClient: SwrClient by lazy {
        SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = SwrCacheScope(),
                memoryPressure = AndroidMemoryPressure(this),
                networkConnectivity = AndroidNetworkConnectivity(this),
                windowVisibility = AndroidWindowVisibility(),
                queryReceiver = ktorReceiver,
                mutationReceiver = ktorReceiver
            )
        )
    }

    private val ktorReceiver: KtorReceiver by lazy {
        KtorReceiver(client = createHttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        })
    }
}
