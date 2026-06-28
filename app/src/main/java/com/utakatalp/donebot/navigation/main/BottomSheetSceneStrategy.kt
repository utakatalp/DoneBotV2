package com.utakatalp.donebot.navigation.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

@OptIn(ExperimentalMaterial3Api::class)
data class BottomSheetSpec(
    val properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    val skipPartiallyExpanded: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
internal data class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val spec: BottomSheetSpec,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        val lifecycleOwner = rememberLifecycleOwner()
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = spec.skipPartiallyExpanded,
        )
        ModalBottomSheet(
            onDismissRequest = onBack,
            properties = spec.properties,
            sheetState = sheetState,
        ) {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                entry.Content()
            }
        }
    }
}

/**
 * A [SceneStrategy] that displays entries marked with [bottomSheet] metadata
 * as a [ModalBottomSheet] overlaid on top of the previous destination.
 *
 * Register it in [NavDisplay] via `sceneStrategies`. Always add it before any
 * non-overlay strategies so overlay entries are intercepted first.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val spec = lastEntry.metadata[BottomSheetKey] ?: return null
        @Suppress("UNCHECKED_CAST")
        return BottomSheetScene(
            key = lastEntry.contentKey as T,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            entry = lastEntry,
            spec = spec,
            onBack = onBack,
        )
    }

    companion object {
        /**
         * Mark an entry as a bottom sheet destination.
         *
         * Usage inside [entryProvider]:
         * ```
         * entry<AddTask>(metadata = BottomSheetSceneStrategy.bottomSheet(skipPartiallyExpanded = true)) {
         *     AddTaskScreen()
         * }
         * ```
         */
        fun bottomSheet(
            properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
            skipPartiallyExpanded: Boolean = false,
        ) = metadata { put(BottomSheetKey, BottomSheetSpec(properties, skipPartiallyExpanded)) }

        object BottomSheetKey : NavMetadataKey<BottomSheetSpec>
    }
}
