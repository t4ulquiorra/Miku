package dev.anilbeesetti.nextplayer.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.anilbeesetti.nextplayer.settings.screens.debug.CrateCardPreviewScreen
import kotlinx.serialization.Serializable

@Serializable
object CrateCardPreviewRoute : NavKey

fun NavBackStack<NavKey>.navigateToCrateCardPreview() {
    add(element = CrateCardPreviewRoute)
}

fun EntryProviderScope<NavKey>.crateCardPreviewEntry(
    onNavigateUp: () -> Unit,
) {
    entry<CrateCardPreviewRoute> {
        CrateCardPreviewScreen(onNavigateUp = onNavigateUp)
    }
}
