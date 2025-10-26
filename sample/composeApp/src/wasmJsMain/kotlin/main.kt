import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.compose.rememberNavController
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.SwrCacheScope
import soil.query.WasmJsNetworkConnectivity
import soil.query.WasmJsWindowVisibility
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SwrClientProvider
import soil.query.receivers.ktor.httpClient

@OptIn(ExperimentalSoilQueryApi::class)
private val swrClient = SwrCachePlus(
    policy = SwrCachePlusPolicy(
        coroutineScope = SwrCacheScope(),
        networkConnectivity = WasmJsNetworkConnectivity(),
        windowVisibility = WasmJsWindowVisibility()
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSoilQueryApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport {
        SwrClientProvider(client = swrClient) {
            val navController = rememberNavController()
            App(navController)
            LaunchedEffect(Unit) {
                navController.bindToBrowserNavigation()
            }
        }
    }
}
