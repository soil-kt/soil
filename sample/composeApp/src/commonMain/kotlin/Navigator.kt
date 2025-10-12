import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import soil.kmp.screen.HelloFormScreen
import soil.kmp.screen.HelloQueryDetailScreen
import soil.kmp.screen.HelloQueryScreen
import soil.kmp.screen.HelloSpaceScreen
import soil.kmp.screen.HomeScreen
import soil.kmp.screen.NavScreen
import soil.playground.router.LocalNavRouter
import soil.playground.router.NavRoute
import soil.playground.router.NavRouter
import soil.space.compose.rememberViewModelStore

@Stable
class Navigator(
    val navController: NavHostController
) : NavRouter {
    override fun push(route: String) {
        navController.navigate(route)
    }

    override fun <T : NavRoute> push(route: T) {
        navController.navigate(route)
    }

    override fun back(): Boolean {
        return navController.popBackStack()
    }

    override fun canBack(): Boolean {
        return navController.previousBackStackEntry != null
    }
}

@Composable
fun NavRouterHost(
    navigator: Navigator,
    modifier: Modifier
) {
    CompositionLocalProvider(LocalNavRouter provides navigator) {
        NavHost(
            navController = navigator.navController,
            startDestination = NavScreen.root,
            modifier = modifier
        ) {
            composable<NavScreen.Home> {
                HomeScreen()
            }
            composable<NavScreen.HelloQuery> {
                HelloQueryScreen()
            }
            composable<NavScreen.HelloQueryDetail> {
                val screen = it.toRoute<NavScreen.HelloQueryDetail>()
                HelloQueryDetailScreen(postId = screen.postId)
            }
            composable<NavScreen.HelloForm> {
                HelloFormScreen()
            }
            composable<NavScreen.HelloSpace> {
                val rootEntry = navigator.navController.getBackStackEntry<NavScreen.Home>()
                HelloSpaceScreen(
                    navStore = rememberViewModelStore(rootEntry)
                )
            }
        }
    }
}
