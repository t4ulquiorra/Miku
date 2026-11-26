package dev.anilbeesetti.nextplayer.settings.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.CrateCardVideoPreview
import dev.anilbeesetti.nextplayer.core.ui.components.CrateFolderCard
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.JetBrainsMono
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBg
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInk
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkMuted
import kotlin.math.roundToInt

private data class CardSizePreset(
    val label: String,
    val width: Float,
    val height: Float,
)

private val CARD_PRESETS = listOf(
    CardSizePreset("820×410 Wide", 820f, 410f),
    CardSizePreset("640×320 Wide", 640f, 320f),
    CardSizePreset("560×359 Narrow", 560f, 359f),
    CardSizePreset("344×221 Narrow", 344f, 221f),
    CardSizePreset("344×460 Roomy", 344f, 460f),
    CardSizePreset("344×170 Short", 344f, 170f),
    CardSizePreset("280×130 Tiny", 280f, 130f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrateCardPreviewScreen(
    onNavigateUp: () -> Unit,
) {
    var cardWidth by remember { mutableFloatStateOf(344f) }
    var cardHeight by remember { mutableFloatStateOf(221f) }
    var folderIndex by remember { mutableIntStateOf(1) }
    var videosCount by remember { mutableIntStateOf(18) }
    var newVideosCount by remember { mutableIntStateOf(3) }
    var isLastPlayed by remember { mutableStateOf(true) }

    val fakeFolder = remember(folderIndex, videosCount, newVideosCount) {
        Folder(
            name = when (folderIndex % 4) {
                0 -> "Camera"
                1 -> "Bleach"
                2 -> "Screen Recordings"
                else -> "Documentaries"
            },
            path = "/storage/emulated/0/Movies/Anime/Bleach",
            dateModified = 1700000000L,
            totalSize = 4_200_000_000L,
            totalDuration = videosCount * 1_420_000L,
            videosCount = videosCount,
            newVideosCount = newVideosCount.coerceAtMost(videosCount),
        )
    }

    val fakePreviews = remember(videosCount) {
        (1..minOf(videosCount, 9)).map { idx ->
            CrateCardVideoPreview(
                title = "video$idx",
                durationMillis = (15 + idx * 2) * 60_000L + 30_000L,
            )
        }
    }

    val wInt = cardWidth.roundToInt()
    val hInt = cardHeight.roundToInt()
    val variantLabel = if (wInt >= 640) "WIDE" else "NARROW"
    val heightClassLabel = when {
        hInt >= 440 -> "ROOMY"
        hInt >= 200 -> "NORMAL"
        hInt >= 150 -> "SHORT"
        else -> "TINY"
    }

    Scaffold(
        topBar = {
            NextTopAppBar(
                title = "Crate Card Debug",
                navigationIcon = {
                    FilledTonalIconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = "Navigate up",
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PrototypeBg)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "${wInt}×${hInt}dp · $variantLabel · $heightClassLabel · paper=${folderIndex % 4}",
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = PrototypeInkMuted,
            )

            // Presets
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                CARD_PRESETS.forEach { preset ->
                    FilterChip(
                        selected = wInt == preset.width.roundToInt() && hInt == preset.height.roundToInt(),
                        onClick = {
                            cardWidth = preset.width
                            cardHeight = preset.height
                        },
                        label = { Text(preset.label, fontSize = 11.sp) },
                    )
                }
            }

            // Width & Height sliders
            Column {
                Text(
                    text = "Width: ${wInt}dp",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = PrototypeInk,
                )
                Slider(
                    value = cardWidth,
                    onValueChange = { cardWidth = it },
                    valueRange = 220f..860f,
                )
                Text(
                    text = "Height: ${hInt}dp",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = PrototypeInk,
                )
                Slider(
                    value = cardHeight,
                    onValueChange = { cardHeight = it },
                    valueRange = 110f..520f,
                )
            }

            // Data / Style toggles
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                FilterChip(
                    selected = isLastPlayed,
                    onClick = { isLastPlayed = !isLastPlayed },
                    label = { Text("Accent Tab") },
                )
                FilterChip(
                    selected = false,
                    onClick = { folderIndex = (folderIndex + 1) % 4 },
                    label = { Text("Paper #${folderIndex % 4}") },
                )
                listOf(1, 3, 18).forEach { count ->
                    FilterChip(
                        selected = videosCount == count,
                        onClick = { videosCount = count },
                        label = { Text("$count vids") },
                    )
                }
                FilterChip(
                    selected = newVideosCount > 0,
                    onClick = { newVideosCount = if (newVideosCount > 0) 0 else 3 },
                    label = { Text(if (newVideosCount > 0) "3 NEW" else "0 NEW") },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Card stage (horizontally scrollable if cardWidth > screen width)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0B09))
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 60.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
            ) {
                CrateFolderCard(
                    folder = fakeFolder,
                    index = folderIndex,
                    totalFolders = 18,
                    cardWidth = cardWidth.dp,
                    cardHeight = cardHeight.dp,
                    isLastPlayed = isLastPlayed,
                    previewVideos = fakePreviews,
                )
            }
        }
    }
}
