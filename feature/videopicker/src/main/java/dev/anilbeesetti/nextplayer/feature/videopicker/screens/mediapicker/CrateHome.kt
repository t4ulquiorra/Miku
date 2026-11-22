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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalLayoutDirection
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
            .background(PrototypeBg),
    ) {
        val density = LocalDensity.current.density
        val layoutDirection = LocalLayoutDirection.current

        val containerWidth = maxWidth
        val containerHeight = maxHeight

        val bottomPadding = contentPadding.calculateBottomPadding()
        val startContentPadding = contentPadding.calculateStartPadding(layoutDirection)
        val endContentPadding = contentPadding.calculateEndPadding(layoutDirection)
        val topContentPadding = contentPadding.calculateTopPadding()

        // 1. Reserved zones
        val reservedRightColumnWidth = 72.dp
        val hintLineHeight = 30.dp
        val hintLineBottomMargin = 8.dp
        val bottomReserved = bottomPadding + hintLineHeight + hintLineBottomMargin
        val topReserved = topContentPadding + 10.dp

        // 2. Stack available area (bounded horizontally and vertically)
        val availableStackWidth = (containerWidth - reservedRightColumnWidth - startContentPadding - endContentPadding - 16.dp)
            .coerceAtLeast(180.dp)
        val availableStackHeight = (containerHeight - bottomReserved - topReserved)
            .coerceAtLeast(160.dp)

        // 3. Tab rise above front card
        val tabHeight = (availableStackHeight * 0.11f).coerceIn(26.dp, 36.dp)
        val visibleCardsBehind = minOf(folders.lastIndex, 4)
        val stepY = if (visibleCardsBehind > 0) {
            (availableStackHeight * 0.05f).coerceIn(7.dp, 22.dp)
        } else {
            0.dp
        }
        val tabRise = tabHeight + (stepY * visibleCardsBehind)

        // 4. Derive card width and height (height wins: if card would not fit, shrink it)
        val maxCardHeight = (availableStackHeight - tabRise).coerceAtLeast(140.dp)
        val targetAspect = if (availableStackWidth >= 540.dp) 2.0f else 1.45f

        val candidateWidth = minOf(availableStackWidth, 820.dp)
        val candidateHeight = candidateWidth / targetAspect

        val cardHeight = minOf(candidateHeight, maxCardHeight)
        val cardWidth = minOf(candidateWidth, cardHeight * targetAspect)

        val tabWidth = (cardWidth * 0.32f).coerceIn(88.dp, 240.dp)

        // 5. Vertical positioning of the front card
        val unusedHeight = (availableStackHeight - (cardHeight + tabRise)).coerceAtLeast(0.dp)
        val frontCardTopY = topReserved + tabRise + (unusedHeight * 0.35f)

        // 6. Horizontal positioning: center in availableStackWidth, reserve right column
        val stackAreaStartX = startContentPadding + 8.dp
        val stackCenterX = stackAreaStartX + (availableStackWidth / 2f)
        val nominalCardHalfWidth = cardWidth / 2f
        val maxRightLeeway = (stackAreaStartX + availableStackWidth) - (stackCenterX + nominalCardHalfWidth)
        val maxAllowedShiftX = maxRightLeeway.coerceAtLeast(0.dp)

        // 7. Limit composition to ~8 cards around current animated position
        val centerIndex = animatedPos.roundToInt().coerceIn(0, folders.lastIndex)
        val minVisible = (centerIndex - 2).coerceAtLeast(0)
        val maxVisible = (centerIndex + 5).coerceAtMost(folders.lastIndex)

        // Draw cards in reverse order (background first, foreground last)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false },
            contentAlignment = Alignment.TopStart,
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
                        frontCardTopY = frontCardTopY,
                        stackCenterX = stackCenterX,
                        stepY = stepY,
                        maxAllowedShiftX = maxAllowedShiftX,
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

        // Right-Edge Stepper with Up/Down Chevrons and Counter (reserved 72dp column)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(reservedRightColumnWidth)
                .padding(end = endContentPadding + 4.dp)
                .zIndex(2000f),
        ) {
            StepperChevronButton(
                isUp = true,
                enabled = safeTarget > 0,
                onClick = {
                    if (safeTarget > 0) targetIndex = safeTarget - 1
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%02d", (animatedPos.roundToInt() + 1).coerceIn(1, folders.size)),
                fontFamily = JetBrainsMono,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = PrototypeInk,
            )
            Text(
                text = "/ ${String.format("%02d", folders.size)}",
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = PrototypeInkMuted,
            )
            Spacer(modifier = Modifier.height(4.dp))
            StepperChevronButton(
                isUp = false,
                enabled = safeTarget < folders.lastIndex,
                onClick = {
                    if (safeTarget < folders.lastIndex) targetIndex = safeTarget + 1
                },
            )
        }

        // Bottom-left subtle interaction cue from prototype (own line outside card area)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = startContentPadding + 20.dp,
                    bottom = bottomPadding + hintLineBottomMargin,
                )
                .zIndex(1500f),
        ) {
            MonoLabel(
                text = "TAP TAB TO SELECT · CHEVRONS TO FLIP",
                color = PrototypeInkDim,
                fontSize = 10.5.sp,
                letterSpacing = 0.14.em,
            )
        }
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
    frontCardTopY: Dp,
    stackCenterX: Dp,
    stepY: Dp,
    maxAllowedShiftX: Dp,
    onTabClick: () -> Unit,
    onBodyClick: () -> Unit,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val d = index - currentPos
    val isFront = abs(d) < 0.55f

    val paperColor = PAPERS[index % PAPERS.size]
    val tabFraction = TAB_SLOTS_FRACTION[index % TAB_SLOTS_FRACTION.size]
    val tabOffsetStart = (cardWidth - tabWidth - 16.dp) * tabFraction + 8.dp

    // 3D Transform calculations matching updateCrateTransforms in prototype.html
    val org = ORGANIC_OFFSETS[index % ORGANIC_OFFSETS.size]
    val ease = if (d > 0f) (d * 1.1f).coerceAtMost(1f) else 0f
    val rawTx = org.x * ease * (cardWidth.value / 820f).coerceIn(0.2f, 1f)
    val tx = rawTx.coerceIn(-maxAllowedShiftX.value, maxAllowedShiftX.value)
    val rz = org.rz * ease

    val ty: Float
    val rx: Float
    val alpha: Float
    val scale: Float

    if (d >= 0f) {
        ty = -d * stepY.value * density
        scale = (1f - d * 0.035f).coerceIn(0.6f, 1f)
        rx = 5f
        alpha = if (d > 3.5f) (1f - (d - 3.5f) * 0.34f).coerceIn(0f, 1f) else 1f
    } else {
        ty = -d * (stepY.value * 6f) * density
        scale = (1f - d * 0.08f).coerceIn(0.6f, 1.2f)
        rx = d * 25f
        alpha = (1f + d * 1.05f).coerceIn(0f, 1f)
    }

    val zIndex = 1000f - d * 10f
    val brightness = (1f - d.coerceAtLeast(0f) * 0.08f).coerceIn(0.45f, 1f)

    val cardLeft = stackCenterX - (cardWidth / 2f)

    Box(
        modifier = modifier
            .offset(x = cardLeft, y = frontCardTopY)
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
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            color = paperColor,
            shadowElevation = 3.dp,
            modifier = Modifier
                .offset(x = tabOffsetStart, y = -tabHeight + 2.dp)
                .size(width = tabWidth, height = tabHeight),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (cardWidth < 360.dp) 8.dp else 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = String.format("%02d", index + 1),
                    fontFamily = JetBrainsMono,
                    fontSize = if (cardHeight < 240.dp) 10.sp else 11.sp,
                    color = PrototypeInkPaper.copy(alpha = 0.7f),
                )
                Text(
                    text = folder.name,
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (cardHeight < 240.dp) 11.5.sp else 13.sp,
                    color = PrototypeInkPaper,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = folder.videosCount.toString(),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (cardHeight < 240.dp) 10.sp else 11.sp,
                    color = PrototypeInkPaper,
                )
            }
        }

        // 2. Folder Main Body
        Surface(
            onClick = onBodyClick,
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 18.dp,
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
                        cardHeight = cardHeight,
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
    cardHeight: Dp,
    onOpenFolder: () -> Unit,
) {
    val accent = LocalAppAccent.current

    val isCompact = cardHeight < 240.dp
    val isVeryShort = cardHeight < 190.dp
    val showWaveform = cardHeight >= 225.dp
    val showPeekCards = cardWidth > 580.dp && cardHeight >= 260.dp

    val hPadding = if (cardWidth > 480.dp) 24.dp else 16.dp
    val vPadding = if (cardHeight > 280.dp) 18.dp else if (cardHeight > 210.dp) 12.dp else 8.dp

    val titleFontSize = when {
        cardHeight >= 340.dp -> 36.sp
        cardHeight >= 250.dp -> 26.sp
        cardHeight >= 200.dp -> 20.sp
        else -> 17.sp
    }
    val titleLineHeight = when {
        cardHeight >= 340.dp -> 38.sp
        cardHeight >= 250.dp -> 28.sp
        cardHeight >= 200.dp -> 22.sp
        else -> 19.sp
    }
    val titleMaxLines = if (cardHeight >= 220.dp) 2 else 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = hPadding, vertical = vPadding),
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
                fontSize = if (isVeryShort) 9.5.sp else 11.sp,
                letterSpacing = 0.12.em,
            )

            if (folder.newVideosCount > 0) {
                Surface(
                    shape = RoundedCornerShape(if (isCompact) 4.dp else 6.dp),
                    border = BorderStroke(if (isCompact) 1.dp else 1.5.dp, PrototypeStamp),
                    color = Color.Transparent,
                    modifier = Modifier.graphicsLayer { rotationZ = -6f },
                ) {
                    Text(
                        text = "${folder.newVideosCount} NEW",
                        fontFamily = JetBrainsMono,
                        fontSize = if (isVeryShort) 9.sp else 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrototypeStamp,
                        letterSpacing = 0.12.em,
                        modifier = Modifier.padding(
                            horizontal = if (isVeryShort) 5.dp else 7.dp,
                            vertical = if (isVeryShort) 1.5.dp else 2.5.dp,
                        ),
                    )
                }
            }
        }

        // Folder Title, Metadata, and Path
        val topTextPadding = if (cardHeight > 280.dp) 28.dp else if (cardHeight > 210.dp) 18.dp else 12.dp
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = topTextPadding)
                .fillMaxWidth(if (showPeekCards) 0.62f else 0.95f),
        ) {
            Text(
                text = folder.name,
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold,
                fontSize = titleFontSize,
                lineHeight = titleLineHeight,
                color = PrototypeInkPaper,
                maxLines = titleMaxLines,
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
                fontSize = if (isVeryShort) 10.sp else 11.5.sp,
                color = PrototypeInkPaper.copy(alpha = 0.8f),
                letterSpacing = 0.08.em,
                modifier = Modifier.padding(top = if (isVeryShort) 2.dp else 4.dp),
            )

            if (folder.path.isNotEmpty() && !isVeryShort) {
                Text(
                    text = folder.path,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = PrototypeInkPaper.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        // 3 Overlapping Peek Thumbnails (shown when ample space is available)
        if (showPeekCards) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-12).dp, y = (-8).dp),
            ) {
                for (k in 0..2) {
                    val rot = (k - 1) * 4.5f
                    val offsetX = (k * 24).dp
                    val offsetY = (-k * 4).dp
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrototypeSurfaceDeep,
                        border = BorderStroke(1.dp, PrototypeBorderInput),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(width = 100.dp, height = 56.dp)
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
                                imageVector = NextIcons.Movie,
                                contentDescription = null,
                                tint = PrototypeInkDim.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        // Waveform spine bars along bottom-left (scaled with cardHeight, hidden on small cards)
        if (showWaveform) {
            val maxBarHeight = (cardHeight * 0.17f).coerceIn(20.dp, 52.dp)
            val barWidth = if (cardWidth < 380.dp) 5.dp else 7.dp
            val barSpacing = if (cardWidth < 380.dp) 3.dp else 4.dp
            val spineLimit = if (cardWidth < 420.dp) 12 else 22
            val spineCount = remember(folder.videosCount, spineLimit) {
                minOf(maxOf(folder.videosCount, 6), spineLimit)
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(barSpacing),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 2.dp),
            ) {
                for (k in 0 until spineCount) {
                    val barHeight = remember(index, k, maxBarHeight) {
                        maxBarHeight * (0.42f + (((index + 3) * 31 + k * 17) % 5) * 0.14f)
                    }
                    val isNew = k < folder.newVideosCount
                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 2.5.dp, topEnd = 2.5.dp))
                            .background(if (isNew) accent.color else PrototypeBorderInput),
                    )
                }
                if (folder.videosCount > spineLimit) {
                    Text(
                        text = "+${folder.videosCount - spineLimit}",
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrototypeInkPaper.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
                    )
                }
            }
        }

        // Tactile "Open folder →" KeycapButton on bottom-right (scaled for compact cards)
        KeycapButton(
            onClick = onOpenFolder,
            isAccent = true,
            shape = RoundedCornerShape(if (isCompact) 12.dp else 16.dp),
            shadowHeight = if (isCompact) 3.5.dp else 5.dp,
            contentPadding = if (isCompact) {
                PaddingValues(horizontal = 14.dp, vertical = 7.dp)
            } else {
                PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = 2.dp),
        ) {
            Text(
                text = "Open folder →",
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 11.5.sp else 13.sp,
            )
        }
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
        modifier = modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(16.dp)) {
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
