package soil.kmp.screen

import kotlinx.serialization.Serializable
import soil.playground.router.NavRoute

@Serializable
sealed interface NavScreen : NavRoute {

    @Serializable
    data object Home : NavScreen

    @Serializable
    data object HelloQuery : NavScreen

    @Serializable
    data class HelloQueryDetail(val postId: Int) : NavScreen

    @Serializable
    data object HelloForm : NavScreen

    @Serializable
    data object HelloSpace : NavScreen

    companion object {
        val root: NavScreen = Home
    }
}
