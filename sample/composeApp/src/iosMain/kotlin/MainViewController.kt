import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.playground.createHttpClient
import soil.playground.query.KtorReceiver
import soil.query.IosMemoryPressure
import soil.query.IosWindowVisibility
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.SwrCacheScope
import soil.query.compose.SwrClientProvider

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
