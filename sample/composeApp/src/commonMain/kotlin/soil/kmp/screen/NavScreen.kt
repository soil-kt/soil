package soil.kmp.screen

import soil.playground.router.NavRoute

sealed interface NavScreen : NavRoute {

    data object Home : NavScreen

    data object HelloQuery : NavScreen

    data class HelloQueryDetail(val postId: Int) : NavScreen

    data object HelloForm : NavScreen

    data object HelloSpace : NavScreen

    companion object {
        val root: NavScreen = Home
    }
}
