package soil.kmp.integration.voyager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import soil.space.AtomStore

@Composable
expect fun Screen.rememberScreenStore(key: String? = null): AtomStore

@Composable
fun Navigator.rememberNavigatorScreenStore(
    key: String? = null,
): AtomStore {
    val model = rememberNavigatorScreenModel(key) { AtomScreenModel() }
    return remember(model) { model.store }
}
