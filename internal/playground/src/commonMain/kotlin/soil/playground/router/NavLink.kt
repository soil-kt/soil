package soil.playground.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
inline fun NavLink(
    to: String,
    router: NavRouter = LocalNavRouter.current,
    content: @Composable (NavLinkHandle) -> Unit
) {
    val handle: NavLinkHandle = remember(to) { { router.push(to) } }
    content(handle)
}

@Composable
inline fun <T : NavRoute> NavLink(
    to: T,
    router: NavRouter = LocalNavRouter.current,
    content: @Composable (NavLinkHandle) -> Unit
) {
    val handle: NavLinkHandle = remember(to) { { router.push<T>(to) } }
    content(handle)
}

typealias NavLinkHandle = () -> Unit
