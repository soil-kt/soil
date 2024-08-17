import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.SwrCacheScope
import soil.query.compose.SwrClientProvider
import soil.query.receivers.ktor.KtorReceiver

private val ktorReceiver: KtorReceiver = KtorReceiver(client = createHttpClient {
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
        mutationReceiver = ktorReceiver,
        queryReceiver = ktorReceiver
    )
)

fun main() = application {
    SwrClientProvider(client = swrClient) {
        Window(onCloseRequest = ::exitApplication, title = "soil") {
            App()
        }
    }
}
