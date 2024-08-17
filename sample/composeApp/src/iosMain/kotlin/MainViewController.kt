import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.query.IosMemoryPressure
import soil.query.IosWindowVisibility
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
        memoryPressure = IosMemoryPressure(),
        windowVisibility = IosWindowVisibility(),
        queryReceiver = ktorReceiver,
        mutationReceiver = ktorReceiver
    )
)

fun MainViewController() = ComposeUIViewController {
    SwrClientProvider(client = swrClient) {
        App()
    }
}
