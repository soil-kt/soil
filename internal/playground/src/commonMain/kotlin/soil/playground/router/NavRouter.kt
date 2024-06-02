package soil.playground.router

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
interface NavRouter {
    fun push(route: String)

    fun <T : NavRoute> push(route: T)

    fun back(): Boolean

    fun canBack(): Boolean
}

interface NavRoute

private val noRouter = object : NavRouter {
    override fun push(route: String) = Unit
    override fun <T : NavRoute> push(route: T) = Unit
    override fun back() = false
    override fun canBack() = false
}

val LocalNavRouter = staticCompositionLocalOf<NavRouter> {
    noRouter
}
