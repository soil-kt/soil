import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.SwrCacheScope
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SwrClientProvider
import soil.query.receivers.ktor.httpClient

@OptIn(ExperimentalSoilQueryApi::class)
private val swrClient = SwrCachePlus(
    policy = SwrCachePlusPolicy(
        coroutineScope = SwrCacheScope()
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
fun main() = application {
    SwrClientProvider(client = swrClient) {
        Window(onCloseRequest = ::exitApplication, title = "soil") {
            App()
        }
    }
}
