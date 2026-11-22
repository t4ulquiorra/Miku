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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
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
private val LANDSCAPE_ORGANIC_X = floatArrayOf(
    -48f, 44f, -62f, 56f, -30f, 64f, -54f, 36f, -58f, 50f, -38f, 60f, -46f, 32f, -56f, 42f, -26f, 48f,
)
private val LANDSCAPE_ORGANIC_RZ = floatArrayOf(
    -2.1f, 1.9f, -2.7f, 2.4f, -1.3f, 2.8f, -2.3f, 1.6f, -2.5f, 2.1f, -1.7f, 2.6f, -2.0f, 1.4f, -2.4f, 1.8f, -1.2f, 2.0f,
)

private val PORTRAIT_ORGANIC_X = floatArrayOf(
    -18f, 17f, -24f, 21f, -11f, 24f, -20f, 14f, -22f, 19f, -14f, 23f, -17f, 12f, -21f, 16f, -10f, 18f,
)
private val PORTRAIT_ORGANIC_RZ = floatArrayOf(
    -2.1f, 1.9f, -2.7f, 2.4f, -1.3f, 2.8f, -2.3f, 1.6f, -2.5f, 2.1f, -1.7f, 2.6f, -2f, 1.4f, -2.4f, 1.8f, -1.2f, 2f,
)

private val PAPERS = arrayOf(
    PrototypePaper0, // #D9CBA6
    PrototypePaper1, // #CDBE96
    PrototypePaper2, // #E2D6B8
    PrototypePaper3, // #C4B48A
)

@Composable
fun CrateHome(
    folders: List<Folder>,
    onOpenFolder: (String) -> Unit,
    onPlayLast: () -> Unit,
    modifier: Modifier = Modifier,
    recentlyPlayedFolder: Folder? = null,
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
        val fontScale = LocalDensity.current.fontScale
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

        // 3. Approach: Exact Design Sizing per layout scaled uniformly by s
        val isLandscape = availableStackWidth >= 600.dp
        val designW = if (isLandscape) 820.dp else 344.dp
        val designH = if (isLandscape) 410.dp else 220.dp
        val designTabRise = if (isLandscape) 36.dp else 30.dp
        val stackRoom = if (isLandscape) 64.dp else 46.dp

        // Compute uniform scale factor s capped at 1.4
        val s = minOf(
            availableStackWidth / (designW + 40.dp),
            availableStackHeight / (designH + designTabRise + stackRoom),
        ).coerceAtMost(1.4f)

        // Actual dimensions on screen in normal dp
        val actualCardWidth = designW * s
        val actualCardHeight = designH * s
        val actualTabRise = designTabRise * s

        // 4. Tab rise and stack spacing (kept as before)
        val visibleCardsBehind = minOf(folders.lastIndex, 4)
        val stepY = if (visibleCardsBehind > 0) {
            (actualTabRise * 0.55f).coerceIn(7.dp, 22.dp)
        } else {
            0.dp
        }
        val totalStackRise = actualTabRise + (stepY * visibleCardsBehind)

        // 5. Vertical positioning of the front card
        val unusedHeight = (availableStackHeight - (actualCardHeight + totalStackRise)).coerceAtLeast(0.dp)
        val frontCardTopY = topReserved + totalStackRise + (unusedHeight * 0.35f)

        // 6. Horizontal positioning: center in availableStackWidth, reserve right column
        val stackAreaStartX = startContentPadding + 8.dp
        val stackCenterX = stackAreaStartX + (availableStackWidth / 2f)
        val maxRightLeeway = (stackAreaStartX + availableStackWidth) - (stackCenterX + (actualCardWidth / 2f))
        val maxAllowedShiftX = maxRightLeeway.coerceAtLeast(0.dp)

        // 7. Limit composition to ~10 cards around current animated position (up to 7 behind)
        val centerIndex = animatedPos.roundToInt().coerceIn(0, folders.lastIndex)
        val minVisible = (centerIndex - 2).coerceAtLeast(0)
        val maxVisible = (centerIndex + 7).coerceAtMost(folders.lastIndex)

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
                        fontScale = fontScale,
                        s = s,
                        isLandscape = isLandscape,
                        designW = designW,
                        designH = designH,
                        actualCardWidth = actualCardWidth,
                        actualCardHeight = actualCardHeight,
                        frontCardTopY = frontCardTopY,
                        stackCenterX = stackCenterX,
                        maxAllowedShiftX = maxAllowedShiftX,
                        isLastPlayed = folder.path == recentlyPlayedFolder?.path,
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
    fontScale: Float,
    s: Float,
    isLandscape: Boolean,
    designW: Dp,
    designH: Dp,
    actualCardWidth: Dp,
    actualCardHeight: Dp,
    frontCardTopY: Dp,
    stackCenterX: Dp,
    maxAllowedShiftX: Dp,
    isLastPlayed: Boolean,
    onTabClick: () -> Unit,
    onBodyClick: () -> Unit,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val d = index - currentPos
    val isFront = abs(d) < 0.55f

    val accent = LocalAppAccent.current
    val paperColor = PAPERS[index % PAPERS.size]
    val tabBg = if (isLastPlayed) accent.color else paperColor

    val orgIdx = ((index % 18) + 18) % 18
    val orgX = if (isLandscape) LANDSCAPE_ORGANIC_X[orgIdx] else PORTRAIT_ORGANIC_X[orgIdx]
    val orgRz = if (isLandscape) LANDSCAPE_ORGANIC_RZ[orgIdx] else PORTRAIT_ORGANIC_RZ[orgIdx]

    val ease = if (d > 0f) minOf(1f, d * 1.1f) else 0f
    val rawTx = orgX * ease * s * density
    val tx = rawTx.coerceIn(-maxAllowedShiftX.value * density, maxAllowedShiftX.value * density)
    val rz = orgRz * ease

    val riseDesignDp = if (isLandscape) 48f else 54f
    val p = if (isLandscape) 1600f else 700f
    val z = if (isLandscape) 80f else 34f

    val ty: Float
    val rx: Float
    val alpha: Float
    val scale: Float

    if (d >= 0f) {
        ty = -d * riseDesignDp * s * density
        scale = p / (p + d * z)
        rx = 5f
        val limit = if (isLandscape) 3.5f else 4.4f
        val decay = if (isLandscape) 0.34f else 0.36f
        alpha = if (d <= limit) 1f else (1f - (d - limit) * decay).coerceAtLeast(0f)
    } else {
        ty = -d * (riseDesignDp * 6.5f) * s * density
        scale = (1f - d * 0.08f).coerceIn(0.6f, 1.2f)
        rx = d * 25f
        alpha = (1f + d * 1.05f).coerceIn(0f, 1f)
    }

    val zIndex = 1000f - d * 10f
    val brightness = (1f - d.coerceAtLeast(0f) * 0.08f).coerceAtLeast(0.5f)

    val cardLeft = stackCenterX - (actualCardWidth / 2f)

    Box(
        modifier = modifier
            .offset(x = cardLeft, y = frontCardTopY)
            .size(width = actualCardWidth, height = actualCardHeight)
            .zIndex(zIndex)
            .graphicsLayer {
                translationX = tx
                translationY = ty
                rotationX = rx
                rotationZ = rz
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                cameraDistance = 24f * density
                transformOrigin = TransformOrigin(0.5f, 0f)
                clip = false
            },
    ) {
        // Uniformly scale all design dp and sp by s
        val scaledDensity = remember(density, s, fontScale) {
            Density(density = density * s, fontScale = fontScale)
        }

        CompositionLocalProvider(LocalDensity provides scaledDensity) {
            if (isLandscape) {
                LandscapeCardContent(
                    folder = folder,
                    index = index,
                    totalFolders = totalFolders,
                    isFront = isFront,
                    isLastPlayed = isLastPlayed,
                    paperColor = paperColor,
                    tabBg = tabBg,
                    brightness = brightness,
                    onTabClick = onTabClick,
                    onBodyClick = onBodyClick,
                    onOpenFolder = onOpenFolder,
                )
            } else {
                PortraitCardContent(
                    folder = folder,
                    index = index,
                    totalFolders = totalFolders,
                    isFront = isFront,
                    isLastPlayed = isLastPlayed,
                    paperColor = paperColor,
                    tabBg = tabBg,
                    brightness = brightness,
                    onTabClick = onTabClick,
                    onBodyClick = onBodyClick,
                    onOpenFolder = onOpenFolder,
                )
            }
        }
    }
}

/**
 * LANDSCAPE DESIGN (820 x 410)
 * All dimensions below are EXACT DESIGN VALUES in dp / sp.
 */
@Composable
private fun LandscapeCardContent(
    folder: Folder,
    index: Int,
    totalFolders: Int,
    isFront: Boolean,
    isLastPlayed: Boolean,
    paperColor: Color,
    tabBg: Color,
    brightness: Float,
    onTabClick: () -> Unit,
    onBodyClick: () -> Unit,
    onOpenFolder: () -> Unit,
) {
    val accent = LocalAppAccent.current

    // Slots left: index%4 -> 30, 185, 340, 495
    val tabSlotLeft = when (index % 4) {
        0 -> 30.dp
        1 -> 185.dp
        2 -> 340.dp
        else -> 495.dp
    }

    // Card radii: TL 6, TR 20, BR 16, BL 16
    val cardShape = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 20.dp,
        bottomEnd = 16.dp,
        bottomStart = 16.dp,
    )

    // Upward shadow casting onto the card behind (landscape: y -12, blur 28, black 40%)
    Box(
        modifier = Modifier
            .offset(x = 0.dp, y = (-36).dp)
            .size(width = 820.dp, height = 36.dp)
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.4f to Color.Black.copy(alpha = 0.12f),
                    0.75f to Color.Black.copy(alpha = 0.28f),
                    1.0f to Color.Black.copy(alpha = 0.40f),
                ),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 24.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp,
                ),
            ),
    )

    // 1. Tab: 248 x 46, sits 36 above card top edge, top corners 12, bottom 0, padding 9 top / 14 sides
    Surface(
        onClick = onTabClick,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        color = tabBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .offset(x = tabSlotLeft, y = (-36).dp)
            .size(width = 248.dp, height = 46.dp)
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(12.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width - 12.dp.toPx(), 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp, top = 9.dp),
            ) {
                Text(
                    text = String.format("%02d", index + 1),
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = PrototypeInkPaper.copy(alpha = 0.70f),
                )
                Text(
                    text = folder.name,
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = PrototypeInkPaper,
                )
                Text(
                    text = folder.videosCount.toString(),
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = PrototypeInkPaper,
                )
            }
            if (brightness < 0.999f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (1f - brightness) * 0.75f)),
                )
            }
        }
    }

    // 2. Card Body: 820 x 410, top highlight follows rounded corners (TL 6, TR 20, 2dp white 40%)
    Surface(
        onClick = onBodyClick,
        shape = cardShape,
        color = paperColor,
        shadowElevation = 0.dp,
        modifier = Modifier
            .size(width = 820.dp, height = 410.dp)
            .drawBehind {
                val strokeWidthPx = 2.dp.toPx()
                val strokeHalf = strokeWidthPx / 2f
                val rTL = 6.dp.toPx()
                val rTR = 20.dp.toPx()
                val left = strokeHalf
                val top = strokeHalf
                val right = size.width - strokeHalf

                val highlightPath = Path().apply {
                    moveTo(left, top + rTL)
                    arcTo(
                        rect = Rect(left, top, left + 2f * rTL, top + 2f * rTL),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(right - rTR, top)
                    arcTo(
                        rect = Rect(right - 2f * rTR, top, right, top + 2f * rTR),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                }
                drawPath(
                    path = highlightPath,
                    color = Color.White.copy(alpha = 0.40f),
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                    ),
                )
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Label "FOLDER 02 / 18": mono 12sp, spacing 0.14em, 65%, at (30, 30) - drawn on ALL cards
            Text(
                text = "FOLDER ${String.format("%02d", index + 1)} / ${String.format("%02d", totalFolders)}",
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                letterSpacing = 0.14.em,
                color = PrototypeInkPaper.copy(alpha = 0.65f),
                modifier = Modifier.offset(x = 30.dp, y = 30.dp),
            )

            // Title: left 30, top 62, bold 54sp, letter-spacing -1.5, max 2 lines, #1A150C - drawn on ALL cards
            Text(
                text = folder.name,
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold,
                fontSize = 54.sp,
                lineHeight = 56.sp,
                letterSpacing = (-1.5).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1A150C),
                modifier = Modifier
                    .offset(x = 30.dp, y = 62.dp)
                    .widthIn(max = 480.dp),
            )

            // Stamp: right 34, top 34, rotated -6 - drawn on ALL cards
            if (folder.newVideosCount > 0) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.5.dp, PrototypeStamp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 34.dp, top = 34.dp)
                        .graphicsLayer { rotationZ = -6f },
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

            // FRONT ONLY elements (meta row, thumbnails, waveform bars, CTA button):
            if (isFront) {
                // "LAST PLAYED" mono 12sp spacing 0.14em, right 34, top 98 (last-played folder only)
                if (isLastPlayed) {
                    Text(
                        text = "LAST PLAYED",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        letterSpacing = 0.14.em,
                        color = PrototypeInkPaper.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 34.dp, top = 98.dp),
                    )
                }

                // Meta (count + duration + path): left 32, top 196, right inset 360, mono 13sp, spacing 0.1em
                Column(
                    modifier = Modifier
                        .offset(x = 32.dp, y = 196.dp)
                        .width((820 - 32 - 360).dp),
                ) {
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
                        fontSize = 13.sp,
                        letterSpacing = 0.1.em,
                        color = PrototypeInkPaper.copy(alpha = 0.80f),
                    )
                    if (folder.path.isNotEmpty()) {
                        Text(
                            text = folder.path,
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            letterSpacing = 0.04.em,
                            color = PrototypeInkPaper.copy(alpha = 0.60f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                // 3 Overlapping Peek Thumbnails (keep current placeholders)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-34).dp, y = 10.dp),
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
                                            listOf(PrototypeSurfaceKey, PrototypeSurfaceDeep),
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

                // Bars: bottom-left, max 26 bars, width 12, gap 5
                val spineCount = remember(folder.videosCount) {
                    minOf(maxOf(folder.videosCount, 6), 26)
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 32.dp, bottom = 28.dp),
                ) {
                    for (k in 0 until spineCount) {
                        val barHeight = remember(index, k) {
                            46.dp + (((index + 3) * 31 + k * 17) % 5).dp * 20
                        }
                        val isNew = k < folder.newVideosCount
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(if (isNew) accent.color else Color(0xFF3A3220)),
                        )
                    }
                    if (folder.videosCount > 26) {
                        Text(
                            text = "+${folder.videosCount - 26}",
                            fontFamily = JetBrainsMono,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.06.em,
                            color = PrototypeInkPaper,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 12.dp),
                        )
                    }
                }

                // Open folder key: right 30, bottom 28, height 56, radius 18
                KeycapButton(
                    onClick = onOpenFolder,
                    isAccent = true,
                    shape = RoundedCornerShape(18.dp),
                    shadowHeight = 5.dp,
                    contentPadding = PaddingValues(horizontal = 26.dp, vertical = 14.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 30.dp, bottom = 28.dp)
                        .height(56.dp),
                ) {
                    Text(
                        text = "Open folder →",
                        fontFamily = BricolageGrotesque,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }

            // Darkening falloff with depth (prototype brightness filter applied to whole card)
            if (brightness < 0.999f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (1f - brightness) * 0.75f)),
                )
            }
        }
    }
}

/**
 * PORTRAIT DESIGN (344 x 220)
 * All dimensions below are EXACT DESIGN VALUES in dp / sp.
 */
@Composable
private fun PortraitCardContent(
    folder: Folder,
    index: Int,
    totalFolders: Int,
    isFront: Boolean,
    isLastPlayed: Boolean,
    paperColor: Color,
    tabBg: Color,
    brightness: Float,
    onTabClick: () -> Unit,
    onBodyClick: () -> Unit,
    onOpenFolder: () -> Unit,
) {
    val accent = LocalAppAccent.current

    // Slots: index%3 -> 16, 84, 152 (tab must stay inside the card width)
    val tabSlotLeft = when (index % 3) {
        0 -> 16.dp
        1 -> 84.dp
        else -> 152.dp
    }

    // Card radii: TL 4, TR 12, BR 10, BL 10
    val cardShape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 12.dp,
        bottomEnd = 10.dp,
        bottomStart = 10.dp,
    )

    // Upward shadow casting onto the card behind (portrait: y -10, blur 20, black 45-50%)
    Box(
        modifier = Modifier
            .offset(x = 0.dp, y = (-28).dp)
            .size(width = 344.dp, height = 28.dp)
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.4f to Color.Black.copy(alpha = 0.15f),
                    0.75f to Color.Black.copy(alpha = 0.32f),
                    1.0f to Color.Black.copy(alpha = 0.45f),
                ),
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp,
                ),
            ),
    )

    // 1. Tab: 156 x 31, sits 30 above card edge, top corners 12, padding 8 top / 11 sides
    Surface(
        onClick = onTabClick,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        color = tabBg,
        shadowElevation = 3.dp,
        modifier = Modifier
            .offset(x = tabSlotLeft, y = (-30).dp)
            .size(width = 156.dp, height = 31.dp)
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(10.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width - 10.dp.toPx(), 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 11.dp, end = 11.dp, top = 8.dp),
            ) {
                Text(
                    text = String.format("%02d", index + 1),
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = PrototypeInkPaper.copy(alpha = 0.70f),
                )
                Text(
                    text = folder.name,
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = PrototypeInkPaper,
                )
                Text(
                    text = folder.videosCount.toString(),
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = PrototypeInkPaper,
                )
            }
            if (brightness < 0.999f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (1f - brightness) * 0.75f)),
                )
            }
        }
    }

    // 2. Card Body: 344 x 220, top highlight 1.5dp white 45%, shadow y -9 blur 24 black 50%
    Surface(
        onClick = onBodyClick,
        shape = cardShape,
        color = paperColor,
        shadowElevation = 0.dp,
        modifier = Modifier
            .size(width = 344.dp, height = 220.dp)
            .drawBehind {
                val strokeWidthPx = 1.5.dp.toPx()
                val strokeHalf = strokeWidthPx / 2f
                val rTL = 4.dp.toPx()
                val rTR = 12.dp.toPx()
                val left = strokeHalf
                val top = strokeHalf
                val right = size.width - strokeHalf

                val highlightPath = Path().apply {
                    moveTo(left, top + rTL)
                    arcTo(
                        rect = Rect(left, top, left + 2f * rTL, top + 2f * rTL),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(right - rTR, top)
                    arcTo(
                        rect = Rect(right - 2f * rTR, top, right, top + 2f * rTR),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                }
                drawPath(
                    path = highlightPath,
                    color = Color.White.copy(alpha = 0.45f),
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                    ),
                )
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Label: mono 10.5sp, spacing 0.17em, 58%, at (16, 11) - drawn on ALL cards
            Text(
                text = "FOLDER ${String.format("%02d", index + 1)} / ${String.format("%02d", totalFolders)}",
                fontFamily = JetBrainsMono,
                fontSize = 10.5.sp,
                letterSpacing = 0.17.em,
                color = PrototypeInkPaper.copy(alpha = 0.58f),
                modifier = Modifier.offset(x = 16.dp, y = 11.dp),
            )

            // Stamp: right 16, top 10, 2dp border, radius 5, mono 10.5sp - drawn on ALL cards
            if (folder.newVideosCount > 0) {
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(2.dp, PrototypeStamp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 10.dp)
                        .graphicsLayer { rotationZ = -6f },
                ) {
                    Text(
                        text = "${folder.newVideosCount} NEW",
                        fontFamily = JetBrainsMono,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrototypeStamp,
                        letterSpacing = 0.12.em,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            // Title: top 34, left 18, bold 40sp (33sp if name > 12 chars, 27sp if > 18), letter-spacing -1.3, ONE line ellipsis - drawn on ALL cards
            val titleSize = when {
                folder.name.length > 18 -> 27.sp
                folder.name.length > 12 -> 33.sp
                else -> 40.sp
            }
            Text(
                text = folder.name,
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold,
                fontSize = titleSize,
                letterSpacing = (-1.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1A150C),
                modifier = Modifier
                    .offset(x = 18.dp, y = 34.dp)
                    .widthIn(max = (344 - 36).dp),
            )

            // FRONT ONLY elements (meta row, thumbnails, Open folder key):
            if (isFront) {
                // Meta row 9 below title: left count+duration mono 11sp (path 9sp under it); right 14 bars, width 5, gap 3, max height 26
                Row(
                    modifier = Modifier
                        .offset(x = 18.dp, y = 83.dp)
                        .width((344 - 36).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left: count + duration (path 9sp under it)
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp),
                    ) {
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
                            fontSize = 11.sp,
                            color = PrototypeInkPaper.copy(alpha = 0.85f),
                            letterSpacing = 0.08.em,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (folder.path.isNotEmpty()) {
                            Text(
                                text = folder.path,
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                color = PrototypeInkPaper.copy(alpha = 0.55f),
                                letterSpacing = 0.04.em,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }

                    // Right: 14 bars, width 5, gap 3, max height 26
                    val spineCount = remember(folder.videosCount) {
                        minOf(maxOf(folder.videosCount, 6), 14)
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.height(26.dp),
                    ) {
                        for (k in 0 until spineCount) {
                            val barHeight = remember(index, k) {
                                (12.dp + (((index + 3) * 31 + k * 17) % 5).dp * 3.5f).coerceAtMost(26.dp)
                            }
                            val isNew = k < folder.newVideosCount
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp))
                                    .background(if (isNew) accent.color else Color(0xFF3A3220)),
                            )
                        }
                    }
                }

                // Bottom row (padding bottom 16, sides 18): thumbnails group 170 x 56 on the left (keep current placeholders), "Open folder" key weight 1, height 48, radius 15
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Thumbnails group 170 x 56 on the left (keep current placeholders)
                    Box(
                        modifier = Modifier.size(width = 170.dp, height = 56.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        for (k in 0..2) {
                            val rot = (k - 1) * 4.5f
                            val offsetX = (k * 22).dp
                            val offsetY = (-k * 3).dp
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = PrototypeSurfaceDeep,
                                border = BorderStroke(1.dp, PrototypeBorderInput),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .offset(x = offsetX, y = offsetY)
                                    .size(width = 84.dp, height = 48.dp)
                                    .graphicsLayer { rotationZ = rot },
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(PrototypeSurfaceKey, PrototypeSurfaceDeep),
                                            ),
                                        ),
                                    ) {
                                        Icon(
                                            imageVector = NextIcons.Movie,
                                            contentDescription = null,
                                            tint = PrototypeInkDim.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                            }
                        }
                    }

                    // "Open folder" key weight 1, height 48, radius 15
                    KeycapButton(
                        onClick = onOpenFolder,
                        isAccent = true,
                        shape = RoundedCornerShape(15.dp),
                        shadowHeight = 4.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = "Open folder →",
                            fontFamily = BricolageGrotesque,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Darkening falloff with depth (prototype brightness filter)
            if (brightness < 0.999f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (1f - brightness) * 0.75f)),
                )
            }
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
