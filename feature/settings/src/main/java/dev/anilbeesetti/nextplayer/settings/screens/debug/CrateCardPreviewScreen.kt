package dev.anilbeesetti.nextplayer.settings.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.components.CrateCardVideoPreview
import dev.anilbeesetti.nextplayer.core.ui.components.CrateFolderCard
import dev.anilbeesetti.nextplayer.core.ui.components.CrateSolveResult
import dev.anilbeesetti.nextplayer.core.ui.components.CrateStack
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.rememberCrateStackState
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.JetBrainsMono
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBg
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBorderStrong
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInk
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkMuted
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class CratePreviewMode(val label: String) {
    CARD("Card (Phase 1)"),
    STACK("Stack (Phase 2)"),
}

private data class SizePreset(
    val label: String,
    val width: Float,
    val height: Float,
)

private val CARD_PRESETS = listOf(
    SizePreset("820×410 Wide", 820f, 410f),
    SizePreset("640×320 Wide", 640f, 320f),
    SizePreset("560×359 Narrow", 560f, 359f),
    SizePreset("344×221 Narrow", 344f, 221f),
    SizePreset("344×460 Roomy", 344f, 460f),
    SizePreset("344×170 Short", 344f, 170f),
    SizePreset("280×130 Tiny", 280f, 130f),
)

private val STACK_PRESETS = listOf(
    SizePreset("360×640", 360f, 640f),
    SizePreset("412×892", 412f, 892f),
    SizePreset("892×412", 892f, 412f),
    SizePreset("640×360", 640f, 360f),
    SizePreset("884×740", 884f, 740f),
    SizePreset("640×800", 640f, 800f),
    SizePreset("800×1280", 800f, 1280f),
    SizePreset("1280×800", 1280f, 800f),
    SizePreset("1024×768", 1024f, 768f),
    SizePreset("1600×1000", 1600f, 1000f),
)

private val STACK_NAMES = listOf(
    "1080p x264 2.3GB",
    "Bleach",
    "Camera",
    "Creative",
    "Download",
    "Internal Storage",
    "Movies",
    "psldawn",
    "S02 1080p WEB-DL",
    "Screen Recordings",
    "Season 1 · English",
    "Season 1 · 1080p",
    "Season 2 · Hindi",
    "Documentaries",
    "Podcasts",
    "Music Videos",
    "Series",
    "Anime",
)

private val STACK_COUNTS = listOf(1, 275, 24, 3, 9, 2, 1, 3, 9, 31, 26, 1, 7, 11, 6, 15, 19, 48)
private val STACK_NEWS = listOf(0, 3, 1, 0, 2, 0, 0, 4, 0, 1, 2, 1, 0, 0, 1, 0, 2, 0)

private fun debugSeed(n: Int): Double {
    val x = sin(n * 9.13) * 43758.5453
    return x - floor(x)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrateCardPreviewScreen(
    onNavigateUp: () -> Unit,
) {
    var mode by remember { mutableStateOf(CratePreviewMode.STACK) }

    Scaffold(
        topBar = {
            NextTopAppBar(
                title = "Crate Debug",
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
                .padding(innerPadding),
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                CratePreviewMode.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = mode == item,
                        onClick = { mode = item },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = CratePreviewMode.entries.size,
                        ),
                    ) {
                        Text(item.label)
                    }
                }
            }

            when (mode) {
                CratePreviewMode.CARD -> SingleCardDebugPane()
                CratePreviewMode.STACK -> StackDebugPane()
            }
        }
    }
}

@Composable
private fun SingleCardDebugPane() {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            listOf(1, 3, 9, 18).forEach { count ->
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

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C0B09))
                .horizontalScroll(rememberScrollState())
                .padding(top = 60.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
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

@Composable
private fun StackDebugPane() {
    var winW by remember { mutableFloatStateOf(1280f) }
    var winH by remember { mutableFloatStateOf(800f) }
    val stackState = rememberCrateStackState(initialPos = 1f)
    var statusBar by remember { mutableStateOf(false) }
    var controlsExpanded by remember { mutableStateOf(true) }
    var lastSolve by remember { mutableStateOf<CrateSolveResult?>(null) }
    var openedFolderName by remember { mutableStateOf<String?>(null) }

    val folders = remember {
        STACK_NAMES.mapIndexed { i, name ->
            val c = STACK_COUNTS[i]
            val sec = (c * (1500.0 + debugSeed(i) * 1900.0)).roundToInt()
            Folder(
                name = name,
                path = "/storage/emulated/0/$name",
                dateModified = 1700000000L,
                totalSize = c * 150_000_000L,
                totalDuration = sec * 1000L,
                videosCount = c,
                newVideosCount = STACK_NEWS[i],
            )
        }
    }

    val wInt = winW.roundToInt()
    val hInt = winH.roundToInt()
    val sbDp = if (statusBar) 26f else 0f
    val crateH = (winH - sbDp).coerceAtLeast(100f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Presets
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            STACK_PRESETS.forEach { preset ->
                FilterChip(
                    selected = wInt == preset.width.roundToInt() && hInt == preset.height.roundToInt(),
                    onClick = {
                        winW = preset.width
                        winH = preset.height
                    },
                    label = { Text(preset.label, fontSize = 11.sp) },
                )
            }
        }

        // Info line + perf readout + open feedback + collapsible toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lastSolve?.let { r ->
                        val cw = r.cardWidthDp.roundToInt()
                        val ch = r.cardHeightDp.roundToInt()
                        val pct = (r.scaleOfApproved * 100f).roundToInt()
                        val behind = r.behindCount
                        "Area ${wInt}×${crateH.roundToInt()} → card ${cw}×${ch} (${"%.2f".format(r.shape)}, ${pct}%) · $behind behind · pos=${"%.2f".format(stackState.pos)}"
                    } ?: "Area ${wInt}×${crateH.roundToInt()}",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = PrototypeInk,
                )
                Text(
                    text = "perf: avg ${"%.1f".format(stackState.avgFrameMs)}ms · worst ${"%.1f".format(stackState.worstFrameMs)}ms · move recomps: ${stackState.moveRecomps} (total ${stackState.totalCardRecomps})",
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = PrototypeInkMuted,
                )
                openedFolderName?.let { name ->
                    Text(
                        text = "OPEN: $name",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = Color(0xFFFF5A2E),
                    )
                }
            }
            FilterChip(
                selected = controlsExpanded,
                onClick = { controlsExpanded = !controlsExpanded },
                label = { Text(if (controlsExpanded) "Hide ▲" else "Controls ▼", fontSize = 10.sp) },
            )
        }

        if (controlsExpanded) {
            // Compact W / H / pos sliders + status bar checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "W: ${wInt}dp",
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        color = PrototypeInkMuted,
                    )
                    Slider(
                        value = winW,
                        onValueChange = { winW = it },
                        valueRange = 280f..1600f,
                        modifier = Modifier.height(28.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "H: ${hInt}dp",
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        color = PrototypeInkMuted,
                    )
                    Slider(
                        value = winH,
                        onValueChange = { winH = it },
                        valueRange = 280f..1400f,
                        modifier = Modifier.height(28.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = statusBar,
                        onCheckedChange = { statusBar = it },
                    )
                    Text(
                        text = "SB 26dp",
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        color = PrototypeInk,
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "pos: ${"%.2f".format(stackState.pos)}",
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                color = PrototypeInk,
            )
            Slider(
                value = stackState.pos.coerceIn(0f, folders.lastIndex.toFloat()),
                onValueChange = { stackState.snapTo(it, folders.lastIndex) },
                valueRange = 0f..(folders.lastIndex).toFloat(),
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
            )
        }

        // Simulated window scaled down only if larger than the device viewport
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0908))
                .border(1.dp, PrototypeBorderStrong, RoundedCornerShape(12.dp))
                .padding(8.dp),
        ) {
            val viewScale = minOf(
                maxWidth.value / winW,
                maxHeight.value / winH,
                1f,
            )
            val scaledW = (winW * viewScale).dp
            val scaledH = (winH * viewScale).dp

            Box(
                modifier = Modifier
                    .size(scaledW, scaledH)
                    .graphicsLayer {
                        scaleX = viewScale
                        scaleY = viewScale
                        transformOrigin = TransformOrigin(0f, 0f)
                    },
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                        .size(winW.dp, winH.dp)
                        .background(Color(0xFF0C0B09))
                        .border(1.dp, Color(0xFF3A3220)),
                ) {
                    if (statusBar) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(Color(0xFF17150F))
                                .padding(horizontal = 12.dp),
                        ) {
                            Text(
                                text = "STATUS BAR (26dp)",
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                color = PrototypeInkMuted,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        CrateStack(
                            folders = folders,
                            state = stackState,
                            lastPlayedFolderPath = folders.getOrNull(1)?.path,
                            onOpenFolder = { idx ->
                                openedFolderName = folders.getOrNull(idx)?.name
                            },
                            onSolveChanged = { lastSolve = it },
                        )
                    }
                }
            }
        }
    }
}
