import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
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
        when (val screen = route as NavScreen) {
            is NavScreen.Home -> push(NavScreenDestination.Home())
            is NavScreen.HelloQuery -> push(NavScreenDestination.HelloQuery())
            is NavScreen.HelloQueryDetail -> push(NavScreenDestination.HelloQueryDetail(screen.postId))
            is NavScreen.HelloForm -> push(NavScreenDestination.HelloForm())
            is NavScreen.HelloSpace -> push(NavScreenDestination.HelloSpace())
        }
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
    val startDestination = remember(NavScreen.root) { NavScreen.root.destination.route }
    CompositionLocalProvider(LocalNavRouter provides navigator) {
        NavHost(
            navController = navigator.navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(
                route = NavScreenDestination.Home.route
            ) {
                HomeScreen()
            }
            composable(
                route = NavScreenDestination.HelloQuery.route
            ) {
                HelloQueryScreen()
            }
            composable(
                route = NavScreenDestination.HelloQueryDetail.route,
                arguments = NavScreenDestination.HelloQueryDetail.arguments
            ) {
                val id = checkNotNull(it.arguments?.read { getInt(NavScreenDestination.HelloQueryDetail.id.name) })
                HelloQueryDetailScreen(postId = id)
            }
            composable(
                route = NavScreenDestination.HelloForm.route
            ) {
                HelloFormScreen()
            }
            composable(
                route = NavScreenDestination.HelloSpace.route
            ) {
                val rootEntry = navigator.navController.getBackStackEntry(startDestination)
                HelloSpaceScreen(
                    navStore = rememberViewModelStore(rootEntry)
                )
            }
        }
    }
}

private sealed class NavScreenDestination(
    val route: String
) {
    data object Home : NavScreenDestination("/home") {
        operator fun invoke() = route
    }

    data object HelloQuery : NavScreenDestination("/helloQuery") {
        operator fun invoke() = route
    }

    data object HelloQueryDetail : NavScreenDestination("/helloQuery/{id}") {
        val arguments get() = listOf(id)
        val id: NamedNavArgument
            get() = navArgument("id") {
                type = NavType.IntType
            }

        operator fun invoke(postId: Int) = "/helloQuery/$postId"
    }

    data object HelloForm : NavScreenDestination("/helloForm") {
        operator fun invoke() = route
    }

    data object HelloSpace : NavScreenDestination("/helloSpace") {
        operator fun invoke() = route
    }
}

private val NavScreen.destination: NavScreenDestination
    get() = when (this) {
        is NavScreen.Home -> NavScreenDestination.Home
        is NavScreen.HelloQuery -> NavScreenDestination.HelloQuery
        is NavScreen.HelloQueryDetail -> NavScreenDestination.HelloQueryDetail
        is NavScreen.HelloForm -> NavScreenDestination.HelloForm
        is NavScreen.HelloSpace -> NavScreenDestination.HelloSpace
    }
