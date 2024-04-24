import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.playground.query.KtorReceiver
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.SwrCacheScope
import soil.query.WasmJsNetworkConnectivity
import soil.query.WasmJsWindowVisibility
import soil.query.compose.SwrClientProvider

private val ktorReceiver = KtorReceiver(client = createHttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
})

private val swrClient = SwrCache(
    policy = SwrCachePolicy(
        coroutineScope = SwrCacheScope(),
        networkConnectivity = WasmJsNetworkConnectivity(),
        windowVisibility = WasmJsWindowVisibility(),
        queryReceiver = ktorReceiver,
        mutationReceiver = ktorReceiver
    )
)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        SwrClientProvider(client = swrClient) {
            App()
        }
    }
}
