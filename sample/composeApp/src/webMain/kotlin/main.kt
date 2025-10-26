import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.compose.rememberNavController
import soil.query.SwrCachePlus
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SwrClientProvider

@OptIn(ExperimentalSoilQueryApi::class)
internal expect val swrClient: SwrCachePlus

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
