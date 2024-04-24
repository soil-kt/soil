package soil.kmp.integration.voyager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import soil.space.AtomStore

@Composable
actual fun Screen.rememberScreenStore(key: String?): AtomStore {
    val model = rememberScreenModel(tag = key) { AtomScreenModel() }
    return remember(model) { model.store }
}
