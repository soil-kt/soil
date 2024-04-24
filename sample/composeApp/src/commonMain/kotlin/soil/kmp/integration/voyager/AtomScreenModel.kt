package soil.kmp.integration.voyager

import cafe.adriel.voyager.core.model.ScreenModel
import soil.space.compose.AtomSaveableStore
import soil.space.AtomStore

class AtomScreenModel(
    val store: AtomStore = AtomSaveableStore()
) : ScreenModel
