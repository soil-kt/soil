import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.JsNetworkConnectivity
import soil.query.JsWindowVisibility
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.SwrCacheScope
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.receivers.ktor.httpClient

@OptIn(markerClass = [ExperimentalSoilQueryApi::class])
internal actual val swrClient: SwrCachePlus = SwrCachePlus(
    SwrCachePlusPolicy(
        coroutineScope = SwrCacheScope(),
        networkConnectivity = JsNetworkConnectivity(),
        windowVisibility = JsWindowVisibility()
    ) {
        httpClient = createHttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    })
