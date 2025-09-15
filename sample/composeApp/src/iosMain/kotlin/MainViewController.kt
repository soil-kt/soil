import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.IosMemoryPressure
import soil.query.IosNetworkConnectivity
import soil.query.IosWindowVisibility
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.SwrCacheScope
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SwrClientProvider
import soil.query.receivers.ktor.httpClient

@OptIn(ExperimentalSoilQueryApi::class)
private val swrClient = SwrCachePlus(
    policy = SwrCachePlusPolicy(
        coroutineScope = SwrCacheScope(),
        memoryPressure = IosMemoryPressure(),
        networkConnectivity = IosNetworkConnectivity(),
        windowVisibility = IosWindowVisibility()
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
    }
)

@OptIn(ExperimentalSoilQueryApi::class)
fun MainViewController() = ComposeUIViewController {
    SwrClientProvider(client = swrClient) {
        App()
    }
}
