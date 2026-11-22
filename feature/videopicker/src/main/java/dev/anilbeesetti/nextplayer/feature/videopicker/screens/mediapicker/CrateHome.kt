package dev.anilbeesetti.nextplayer.feature.videopicker.screens.mediapicker

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.components.KeycapButton
import dev.anilbeesetti.nextplayer.core.ui.components.MonoLabel
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.BricolageGrotesque
import dev.anilbeesetti.nextplayer.core.ui.theme.JetBrainsMono
import dev.anilbeesetti.nextplayer.core.ui.theme.LocalAppAccent
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBg
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBorderInput
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBorderStrong
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInk
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkDim
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkMuted
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkPaper
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper0
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper1
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper2
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper3
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeStamp
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeSurface
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeSurfaceDeep
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeSurfaceKey
import kotlin.math.abs
import kotlin.math.roundToInt

// Predefined static tokens & arrays matching prototype.html
private data class OrganicOffset(val x: Float, val rz: Float)

private val ORGANIC_OFFSETS = arrayOf(
    OrganicOffset(-48f, -2.1f), OrganicOffset(44f, 1.9f), OrganicOffset(-62f, -2.7f), OrganicOffset(56f, 2.4f),
    OrganicOffset(-30f, -1.3f), OrganicOffset(64f, 2.8f), OrganicOffset(-54f, -2.3f), OrganicOffset(36f, 1.6f),
    OrganicOffset(-58f, -2.5f), OrganicOffset(50f, 2.1f), OrganicOffset(-38f, -1.7f), OrganicOffset(60f, 2.6f),
    OrganicOffset(-46f, -2.0f), OrganicOffset(32f, 1.4f), OrganicOffset(-56f, -2.4f), OrganicOffset(42f, 1.8f),
    OrganicOffset(-26f, -1.2f), OrganicOffset(48f, 2.0f),
)

private val PAPERS = arrayOf(
    PrototypePaper0,
    PrototypePaper1,
    PrototypePaper2,
    PrototypePaper3,
)

private val TAB_SLOTS_FRACTION = floatArrayOf(0.04f, 0.28f, 0.52f, 0.74f)

@Composable
fun CrateHome(
    folders: List<Folder>,
    onOpenFolder: (String) -> Unit,
    onPlayLast: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    if (folders.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            MonoLabel(
                text = "NO FOLDERS FOUND",
                color = PrototypeInkMuted,
                fontSize = 13.sp,
            )
        }
        return
    }

    var targetIndex by rememberSaveable { mutableIntStateOf(0) }
    val safeTarget = targetIndex.coerceIn(0, folders.lastIndex)

    val animatedPos by animateFloatAsState(
        targetValue = safeTarget.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioLowBouncy,
        ),
        label = "crate_stack_pos",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PrototypeBg)
            .padding(contentPadding),
    ) {
        val density = LocalDensity.current.density
        val viewportWidth = maxWidth
        val viewportHeight = maxHeight

        // Responsive card dimensions adapting to mobile and tablet screen widths
        val cardWidth = remember(viewportWidth) {
            (viewportWidth - 92.dp).coerceIn(280.dp, 820.dp)
        }
        val cardHeight = remember(cardWidth) {
            (cardWidth * 0.5f).coerceIn(220.dp, 410.dp)
        }
        val tabWidth = remember(cardWidth) {
            (cardWidth * 0.30f).coerceIn(120.dp, 248.dp)
        }
        val tabHeight = 36.dp

        // Stack center positioning (approx 42% from top matching prototype)
        val stackCenterY = remember(viewportHeight) {
            viewportHeight * 0.42f
        }

        // Limit composition to ~8 cards around current animated position
        val centerIndex = animatedPos.roundToInt().coerceIn(0, folders.lastIndex)
        val minVisible = (centerIndex - 2).coerceAtLeast(0)
        val maxVisible = (centerIndex + 5).coerceAtMost(folders.lastIndex)

        // Draw cards in reverse order (background first, foreground last)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false },
            contentAlignment = Alignment.TopCenter,
        ) {
            for (idx in maxVisible downTo minVisible) {
                val folder = folders[idx]
                key(folder.path) {
                    CrateFolderCard(
                        folder = folder,
                        index = idx,
                        totalFolders = folders.size,
                        currentPos = animatedPos,
                        density = density,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        tabWidth = tabWidth,
                        tabHeight = tabHeight,
                        stackCenterY = stackCenterY,
                        onTabClick = { targetIndex = idx },
                        onBodyClick = {
                            if (safeTarget == idx) {
                                onOpenFolder(folder.path)
                            } else {
                                targetIndex = idx
                            }
                        },
                        onOpenFolder = { onOpenFolder(folder.path) },
                    )
                }
            }
        }

        // Right-Edge Stepper with Up/Down Chevrons and Counter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .zIndex(2000f),
        ) {
            StepperChevronButton(
                isUp = true,
                enabled = safeTarget > 0,
                onClick = {
                    if (safeTarget > 0) targetIndex = safeTarget - 1
                },
            )
            Text(
                text = String.format("%02d", (animatedPos.roundToInt() + 1).coerceIn(1, folders.size)),
                fontFamily = JetBrainsMono,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = PrototypeInk,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "/ ${String.format("%02d", folders.size)}",
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = PrototypeInkMuted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            StepperChevronButton(
                isUp = false,
                enabled = safeTarget < folders.lastIndex,
                onClick = {
                    if (safeTarget < folders.lastIndex) targetIndex = safeTarget + 1
                },
            )
        }

        // Bottom-left subtle interaction cue from prototype
        MonoLabel(
            text = "TAP TAB TO SELECT · CHEVRONS TO FLIP",
            color = PrototypeInkDim,
            fontSize = 10.5.sp,
            letterSpacing = 0.14.em,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 20.dp),
        )
    }
}

@Composable
private fun CrateFolderCard(
    folder: Folder,
    index: Int,
    totalFolders: Int,
    currentPos: Float,
    density: Float,
    cardWidth: Dp,
    cardHeight: Dp,
    tabWidth: Dp,
    tabHeight: Dp,
    stackCenterY: Dp,
    onTabClick: () -> Unit,
    onBodyClick: () -> Unit,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val d = index - currentPos
    val isFront = abs(d) < 0.55f

    val paperColor = PAPERS[index % PAPERS.size]
    val tabFraction = TAB_SLOTS_FRACTION[index % TAB_SLOTS_FRACTION.size]
    val tabOffsetStart = (cardWidth - tabWidth - 20.dp) * tabFraction + 10.dp

    // 3D Transform calculations matching updateCrateTransforms in prototype.html
    val org = ORGANIC_OFFSETS[index % ORGANIC_OFFSETS.size]
    val ease = if (d > 0f) (d * 1.1f).coerceAtMost(1f) else 0f
    val tx = org.x * ease * (cardWidth.value / 820f).coerceIn(0.5f, 1f)
    val rz = org.rz * ease

    val ty: Float
    val rx: Float
    val alpha: Float
    val scale: Float

    if (d >= 0f) {
        ty = -d * 42f * density
        scale = (1f - d * 0.035f).coerceIn(0.6f, 1f)
        rx = 5f
        alpha = if (d > 3.5f) (1f - (d - 3.5f) * 0.34f).coerceIn(0f, 1f) else 1f
    } else {
        ty = -d * 280f * density
        scale = (1f - d * 0.08f).coerceIn(0.6f, 1.2f)
        rx = d * 25f
        alpha = (1f + d * 1.05f).coerceIn(0f, 1f)
    }

    val zIndex = 1000f - d * 10f
    val brightness = (1f - d.coerceAtLeast(0f) * 0.08f).coerceIn(0.45f, 1f)

    Box(
        modifier = modifier
            .offset(y = stackCenterY)
            .size(width = cardWidth, height = cardHeight)
            .zIndex(zIndex)
            .graphicsLayer {
                translationX = tx * density
                translationY = ty
                rotationX = rx
                rotationZ = rz
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                cameraDistance = 24f * density
            },
    ) {
        // 1. Top Folder Tab
        Surface(
            onClick = onTabClick,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = paperColor,
            shadowElevation = 3.dp,
            modifier = Modifier
                .offset(x = tabOffsetStart, y = -tabHeight + 2.dp)
                .size(width = tabWidth, height = tabHeight),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = String.format("%02d", index + 1),
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = PrototypeInkPaper.copy(alpha = 0.7f),
                )
                Text(
                    text = folder.name,
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrototypeInkPaper,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = folder.videosCount.toString(),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PrototypeInkPaper,
                )
            }
        }

        // 2. Folder Main Body
        Surface(
            onClick = onBodyClick,
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 20.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp,
            ),
            color = paperColor,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isFront) {
                    FrontCardFace(
                        folder = folder,
                        index = index,
                        totalFolders = totalFolders,
                        cardWidth = cardWidth,
                        onOpenFolder = onOpenFolder,
                    )
                }

                // Darkening falloff with depth (prototype brightness filter)
                if (brightness < 0.98f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = (1f - brightness) * 0.65f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun FrontCardFace(
    folder: Folder,
    index: Int,
    totalFolders: Int,
    cardWidth: Dp,
    onOpenFolder: () -> Unit,
) {
    val accent = LocalAppAccent.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        // Top row: "FOLDER 02 / 18" and "N NEW" stamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            MonoLabel(
                text = "FOLDER ${String.format("%02d", index + 1)} / ${String.format("%02d", totalFolders)}",
                color = PrototypeInkPaper.copy(alpha = 0.65f),
                fontSize = 11.sp,
                letterSpacing = 0.14.em,
            )

            if (folder.newVideosCount > 0) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.5.dp, PrototypeStamp),
                    color = Color.Transparent,
                    modifier = Modifier.graphicsLayer { rotationZ = -6f },
                ) {
                    Text(
                        text = "${folder.newVideosCount} NEW",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrototypeStamp,
                        letterSpacing = 0.12.em,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }

        // Folder Title, Metadata, and Path
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 30.dp)
                .fillMaxWidth(if (cardWidth > 540.dp) 0.65f else 0.95f),
        ) {
            Text(
                text = folder.name,
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold,
                fontSize = if (cardWidth > 500.dp) 40.sp else 26.sp,
                lineHeight = if (cardWidth > 500.dp) 42.sp else 28.sp,
                color = PrototypeInkPaper,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            val metaText = remember(folder.videosCount, folder.totalDuration) {
                val countPart = "${folder.videosCount} ${if (folder.videosCount == 1) "VIDEO" else "VIDEOS"}"
                if (folder.totalDuration > 0) {
                    "$countPart · ${Utils.formatDurationMillis(folder.totalDuration)}"
                } else {
                    countPart
                }
            }

            Text(
                text = metaText,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = PrototypeInkPaper.copy(alpha = 0.8f),
                letterSpacing = 0.08.em,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (folder.path.isNotEmpty()) {
                Text(
                    text = folder.path,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.5.sp,
                    color = PrototypeInkPaper.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        // 3 Overlapping Peek Thumbnails (shown when ample space is available)
        if (cardWidth > 560.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-12).dp, y = (-10).dp),
            ) {
                for (k in 0..2) {
                    val rot = (k - 1) * 4.5f
                    val offsetX = (k * 26).dp
                    val offsetY = (-k * 4).dp
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrototypeSurfaceDeep,
                        border = BorderStroke(1.dp, PrototypeBorderInput),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(width = 108.dp, height = 61.dp)
                            .graphicsLayer { rotationZ = rot },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrototypeSurfaceKey,
                                            PrototypeSurfaceDeep,
                                        ),
                                    ),
                                ),
                        ) {
                            Icon(
                                imageVector = NextIcons.LocalMovies,
                                contentDescription = null,
                                tint = PrototypeInkDim.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        // Waveform spine bars along bottom-left
        val spineCount = remember(folder.videosCount) {
            minOf(maxOf(folder.videosCount, 6), 22)
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 6.dp),
        ) {
            for (k in 0 until spineCount) {
                val barHeight = remember(index, k) {
                    20.dp + (((index + 3) * 31 + k * 17) % 5).dp * 7
                }
                val isNew = k < folder.newVideosCount
                Box(
                    modifier = Modifier
                        .width(7.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(if (isNew) accent.color else PrototypeBorderInput),
                )
            }
            if (folder.videosCount > 22) {
                Text(
                    text = "+${folder.videosCount - 22}",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrototypeInkPaper.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                )
            }
        }

        // Tactile "Open folder →" KeycapButton on bottom-right
        KeycapButton(
            text = "Open folder →",
            isAccent = true,
            onClick = onOpenFolder,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = 4.dp),
        )
    }
}

@Composable
private fun StepperChevronButton(
    isUp: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = PrototypeSurface,
        border = BorderStroke(1.dp, PrototypeBorderStrong),
        shadowElevation = 2.dp,
        modifier = modifier.size(46.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(18.dp)) {
                val strokeWidth = 2.dp.toPx()
                val color = if (enabled) PrototypeInk else PrototypeInkDim
                val path = Path().apply {
                    if (isUp) {
                        moveTo(size.width * 0.22f, size.height * 0.65f)
                        lineTo(size.width * 0.50f, size.height * 0.35f)
                        lineTo(size.width * 0.78f, size.height * 0.65f)
                    } else {
                        moveTo(size.width * 0.22f, size.height * 0.35f)
                        lineTo(size.width * 0.50f, size.height * 0.65f)
                        lineTo(size.width * 0.78f, size.height * 0.35f)
                    }
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}
