import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.SwrCacheScope
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.WasmJsNetworkConnectivity
import soil.query.WasmJsWindowVisibility
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SwrClientProvider
import soil.query.receivers.ktor.KtorReceiver

private val ktorReceiver = KtorReceiver(client = createHttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
})

@OptIn(ExperimentalSoilQueryApi::class)
private val swrClient = SwrCachePlus(
    policy = SwrCachePlusPolicy(
        coroutineScope = SwrCacheScope(),
        networkConnectivity = WasmJsNetworkConnectivity(),
        windowVisibility = WasmJsWindowVisibility(),
        queryReceiver = ktorReceiver,
        mutationReceiver = ktorReceiver
    )
)

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSoilQueryApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        SwrClientProvider(client = swrClient) {
            App()
        }
    }
}
