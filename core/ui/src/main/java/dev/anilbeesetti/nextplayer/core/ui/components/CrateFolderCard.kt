package dev.anilbeesetti.nextplayer.core.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.theme.BricolageGrotesque
import dev.anilbeesetti.nextplayer.core.ui.theme.JetBrainsMono
import dev.anilbeesetti.nextplayer.core.ui.theme.LocalAppAccent
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkPaper
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper0
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper1
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper2
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypePaper3
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeStamp
import kotlin.math.floor
import kotlin.math.sin

data class CrateCardVideoPreview(
    val title: String,
    val durationMillis: Long,
)

private enum class CardHeightClass {
    ROOMY,
    NORMAL,
    SHORT,
    TINY,
}

private enum class ThumbSlot {
    LEFT,
    MIDDLE,
    RIGHT,
}

private val CRATE_PAPERS = listOf(
    PrototypePaper0,
    PrototypePaper1,
    PrototypePaper2,
    PrototypePaper3,
)

private val WIDE_TAB_SLOTS = listOf(
    30f / 820f,
    185f / 820f,
    340f / 820f,
    495f / 820f,
)

private val NARROW_TAB_SLOTS = listOf(
    16f / 344f,
    84f / 344f,
    152f / 344f,
)

private fun prototypeSeed(n: Int): Float {
    val x = sin(n * 9.13) * 43758.5453
    return (x - floor(x)).toFloat()
}

private fun formatMinutesSeconds(millis: Long): String {
    val totalSec = (millis / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return "$mins:${secs.toString().padStart(2, '0')}"
}

/**
 * Single adaptive folder card with its protruding index tab, ported from
 * design/crate_adaptive.html & design/CRATE_SPEC.md section 2.
 *
 * All internal sizes derive proportionally from [cardWidth] and [cardHeight].
 * The tab protrudes above the top edge by ([tabHeight] - 1.dp).
 */
@Composable
fun CrateFolderCard(
    folder: Folder,
    index: Int,
    totalFolders: Int,
    cardWidth: Dp,
    cardHeight: Dp,
    modifier: Modifier = Modifier,
    tabHeight: Dp? = null,
    isLastPlayed: Boolean = false,
    darkenAlpha: Float = 0f,
    previewVideos: List<CrateCardVideoPreview> = emptyList(),
    onOpenFolder: () -> Unit = {},
    onTabClick: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
) {
    val accent = LocalAppAccent.current
    val density = LocalDensity.current

    fun Dp.toCardSp(): TextUnit = with(density) { this@toCardSp.toSp() }
    fun cqw(percent: Float): Dp = cardWidth * (percent / 100f)
    fun cqh(percent: Float): Dp = cardHeight * (percent / 100f)

    val wide = cardWidth >= 640.dp
    val heightClass = when {
        cardHeight >= 440.dp -> CardHeightClass.ROOMY
        cardHeight >= 200.dp -> CardHeightClass.NORMAL
        cardHeight >= 150.dp -> CardHeightClass.SHORT
        else -> CardHeightClass.TINY
    }
    val isVeryNarrow = cardWidth < 300.dp

    val resolvedTabH = tabHeight ?: if (wide) {
        (cardHeight * 0.112f).coerceIn(30.dp, 46.dp)
    } else {
        (cardWidth * 0.09f).coerceIn(26.dp, 34.dp)
    }
    val tabW = if (wide) {
        minOf(248.dp, cardWidth * 0.30f)
    } else {
        minOf(156.dp, maxOf(104.dp, cardWidth * 0.45f))
    }
    val tabFontDp = (if (wide) 14.5.dp else 12.5.dp) *
        minOf(1f, resolvedTabH / (if (wide) 46.dp else 31.dp))
    val rawTabLeft = if (wide) {
        minOf(
            cardWidth * WIDE_TAB_SLOTS[index % WIDE_TAB_SLOTS.size],
            cardWidth - tabW - cardWidth * 0.035f,
        )
    } else {
        minOf(
            cardWidth * NARROW_TAB_SLOTS[index % NARROW_TAB_SLOTS.size],
            cardWidth - tabW - cardWidth * 0.04f,
        )
    }
    val tabLeft = maxOf(cardWidth * 0.03f, rawTabLeft)
    val tabRadius = resolvedTabH * 0.26f

    val paperColor = CRATE_PAPERS[index % CRATE_PAPERS.size]
    val tabBg = if (isLastPlayed) accent.color else paperColor
    val tabInk = if (isLastPlayed) Color(0xFF1A0B04) else PrototypeInkPaper

    val cardRadiusTL = if (wide) 6.dp else 4.dp
    val cardRadiusTR = if (wide) 20.dp else 12.dp
    val cardRadiusBottom = if (wide) 16.dp else 10.dp
    val cardShape = RoundedCornerShape(
        topStart = cardRadiusTL,
        topEnd = cardRadiusTR,
        bottomEnd = cardRadiusBottom,
        bottomStart = cardRadiusBottom,
    )

    val shadowPaint = remember { Paint().apply { isAntiAlias = true } }

    Box(
        modifier = modifier.size(width = cardWidth, height = cardHeight),
    ) {
        // Protruding index tab above the card top edge (zIndex above body seam)
        Box(
            modifier = Modifier
                .offset(x = tabLeft, y = -(resolvedTabH - 1.dp))
                .size(width = tabW, height = resolvedTabH)
                .zIndex(2f)
                .drawBehind {
                    // Real Gaussian blur upward shadow: 0 -4 12 rgba(0,0,0,.35)
                    val blurPx = 12.dp.toPx()
                    val offsetYPx = (-4).dp.toPx()
                    val rPx = tabRadius.toPx()
                    shadowPaint.color = android.graphics.Color.argb((0.35f * 255).toInt(), 0, 0, 0)
                    shadowPaint.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            RectF(0f, offsetYPx, size.width, size.height + offsetYPx),
                            rPx,
                            rPx,
                            shadowPaint,
                        )
                    }
                }
                .clip(RoundedCornerShape(topStart = tabRadius, topEnd = tabRadius))
                .background(tabBg)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.07f),
                            Color.Black.copy(alpha = 0.20f),
                        ),
                    ),
                )
                .drawBehind {
                    // Inset 0 1px 0 rgba(255,255,255,.35)
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(tabRadius.toPx() * 0.5f, 0.5.dp.toPx()),
                        end = Offset(size.width - tabRadius.toPx() * 0.5f, 0.5.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                .then(
                    if (onTabClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTabClick,
                        )
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (darkenAlpha > 0f) {
                        Modifier.drawWithContent {
                            drawContent()
                            drawRect(Color.Black.copy(alpha = darkenAlpha))
                        }
                    } else {
                        Modifier
                    },
                )
                .padding(
                    top = resolvedTabH * 0.2f,
                    start = resolvedTabH * 0.3f,
                    end = resolvedTabH * 0.3f,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = (index + 1).toString().padStart(2, '0'),
                    fontFamily = JetBrainsMono,
                    fontSize = (tabFontDp * 0.78f).toCardSp(),
                    color = tabInk.copy(alpha = 0.70f),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = folder.name,
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = tabFontDp.toCardSp(),
                    letterSpacing = (-0.1).sp,
                    color = tabInk,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = folder.videosCount.toString(),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = (tabFontDp * 0.78f).toCardSp(),
                    color = tabInk,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        // Card body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Real Gaussian blur upward shadow: 0 -12 28 rgba(0,0,0,.4)
                    val blurPx = 28.dp.toPx()
                    val offsetYPx = (-12).dp.toPx()
                    val rPx = cardRadiusTR.toPx()
                    shadowPaint.color = android.graphics.Color.argb((0.40f * 255).toInt(), 0, 0, 0)
                    shadowPaint.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            RectF(0f, offsetYPx, size.width, size.height + offsetYPx),
                            rPx,
                            rPx,
                            shadowPaint,
                        )
                    }
                }
                .clip(cardShape)
                .background(paperColor)
                .then(
                    if (onCardClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCardClick,
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            val pad = if (wide) minOf(cqw(3.7f), cqh(7.3f)) else minOf(cqw(5.2f), cqh(8f))

            // Face content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = pad * 0.8f,
                        start = pad,
                        end = pad,
                        bottom = pad,
                    ),
            ) {
                // Header label
                if (heightClass != CardHeightClass.TINY) {
                    val labelDp = when {
                        heightClass == CardHeightClass.SHORT ->
                            maxOf(7.dp, minOf(cqw(3f), cqh(9f)))
                        wide -> maxOf(8.5.dp, minOf(cqw(1.5f), cqh(3f)))
                        else -> cqw(3.05f).coerceIn(7.dp, 11.dp)
                    }
                    Text(
                        text = "FOLDER ${(index + 1).toString().padStart(2, '0')} / ${totalFolders.toString().padStart(2, '0')}",
                        fontFamily = JetBrainsMono,
                        fontSize = labelDp.toCardSp(),
                        letterSpacing = 0.15.em,
                        color = PrototypeInkPaper.copy(alpha = 0.62f),
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                // Big title
                val titleDp = when {
                    heightClass == CardHeightClass.SHORT && !wide ->
                        minOf(cqw(11f), cqh(26f))
                    heightClass == CardHeightClass.SHORT && wide ->
                        minOf(cqw(6f), cqh(28f))
                    wide -> minOf(cqw(6.6f), cqh(13.2f))
                    else -> minOf(cqw(11.5f), cqh(18f))
                }
                val titleTopMargin = when {
                    heightClass == CardHeightClass.SHORT || heightClass == CardHeightClass.TINY -> 0.dp
                    wide -> minOf(cqw(2.4f), cqh(5f))
                    else -> minOf(cqw(2.4f), cqh(3f))
                }
                Text(
                    text = folder.name,
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleDp.toCardSp(),
                    lineHeight = (titleDp * 1.02f).toCardSp(),
                    letterSpacing = (if (wide) -0.028f else -0.033f).em,
                    color = PrototypeInkPaper,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = titleTopMargin)
                        .padding(bottom = titleDp * 0.06f),
                )

                // Optional roomy 9-line video list
                if (heightClass == CardHeightClass.ROOMY && folder.videosCount > 0) {
                    val vListItems = (0 until minOf(folder.videosCount, 9)).map { k ->
                        previewVideos.getOrNull(k) ?: CrateCardVideoPreview(
                            title = "video${k + 1}",
                            durationMillis = (folder.totalDuration / folder.videosCount.coerceAtLeast(1))
                                .coerceAtLeast(60_000L),
                        )
                    }
                    val vListFontDp = if (wide) {
                        minOf(cqw(1.5f), cqh(2.6f))
                    } else {
                        cqw(3f).coerceIn(8.dp, 12.dp)
                    }
                    val vListMargin = minOf(cqw(2f), cqh(3f))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = vListMargin)
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0.0f to Color.Black,
                                        0.70f to Color.Black,
                                        1.0f to Color.Transparent,
                                    ),
                                    blendMode = BlendMode.DstIn,
                                )
                            },
                    ) {
                        if (wide) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(cqw(3f)),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                val split = (vListItems.size + 1) / 2
                                Column(modifier = Modifier.weight(1f)) {
                                    vListItems.take(split).forEach { item ->
                                        VideoListRow(item, vListFontDp.toCardSp())
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    vListItems.drop(split).forEach { item ->
                                        VideoListRow(item, vListFontDp.toCardSp())
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                vListItems.forEach { item ->
                                    VideoListRow(item, vListFontDp.toCardSp())
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Bottom block
                CardBottomBlock(
                    folder = folder,
                    folderIndex = index,
                    wide = wide,
                    heightClass = heightClass,
                    isVeryNarrow = isVeryNarrow,
                    previewVideos = previewVideos,
                    cqw = ::cqw,
                    cqh = ::cqh,
                    toCardSp = { this.toCardSp() },
                    onOpenFolder = onOpenFolder,
                )
            }

            // Optional rotated "N NEW" stamp
            if (folder.newVideosCount > 0 && heightClass != CardHeightClass.TINY && !isVeryNarrow) {
                val stampRight = if (wide) cqw(4.1f) else cqw(4.6f)
                val stampTop = if (wide) cqh(8.3f) else cqh(4.5f)
                val stampFontDp = if (wide) {
                    minOf(cqw(1.45f), cqh(3.4f))
                } else {
                    cqw(3f).coerceIn(6.dp, 10.5.dp)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = stampRight, top = stampTop)
                        .graphicsLayer { rotationZ = -6f }
                        .border(2.dp, PrototypeStamp, RoundedCornerShape(stampFontDp * 0.4f))
                        .padding(
                            horizontal = stampFontDp * 0.7f,
                            vertical = stampFontDp * 0.35f,
                        ),
                ) {
                    Text(
                        text = "${folder.newVideosCount} NEW",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium,
                        fontSize = stampFontDp.toCardSp(),
                        letterSpacing = 0.12.em,
                        color = PrototypeStamp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            // Seam: 2dp inner white highlight along top edge tracing TL & TR corner arcs
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f)
                    .drawBehind {
                        val strokeWidthPx = 2.dp.toPx()
                        val strokeHalf = strokeWidthPx / 2f
                        val rTL = cardRadiusTL.toPx()
                        val rTR = cardRadiusTR.toPx()
                        val left = strokeHalf
                        val top = strokeHalf
                        val right = size.width - strokeHalf

                        val seamPath = Path().apply {
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
                            path = seamPath,
                            color = Color.White.copy(alpha = 0.40f),
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                        )
                    },
            )

            // Depth darkening overlay (covers the whole card body cleanly without clipping tab)
            if (darkenAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(4f)
                        .background(Color.Black.copy(alpha = darkenAlpha)),
                )
            }
        }
    }
}

@Composable
private fun VideoListRow(
    video: CrateCardVideoPreview,
    fontSize: TextUnit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = PrototypeInkPaper.copy(alpha = 0.22f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                )
            }
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = "▸ ${video.title}",
            fontFamily = JetBrainsMono,
            fontSize = fontSize,
            color = PrototypeInkPaper.copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
        )
        Text(
            text = formatMinutesSeconds(video.durationMillis),
            fontFamily = JetBrainsMono,
            fontSize = fontSize,
            color = PrototypeInkPaper.copy(alpha = 0.78f),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun CardBottomBlock(
    folder: Folder,
    folderIndex: Int,
    wide: Boolean,
    heightClass: CardHeightClass,
    isVeryNarrow: Boolean,
    previewVideos: List<CrateCardVideoPreview>,
    cqw: (Float) -> Dp,
    cqh: (Float) -> Dp,
    toCardSp: Dp.() -> TextUnit,
    onOpenFolder: () -> Unit,
) {
    val showThumbs = heightClass != CardHeightClass.SHORT &&
        heightClass != CardHeightClass.TINY &&
        folder.videosCount > 0
    val showBars = heightClass != CardHeightClass.SHORT &&
        heightClass != CardHeightClass.TINY &&
        !isVeryNarrow &&
        folder.videosCount >= 3

    val rowGap = if (wide) minOf(cqw(1.8f), cqh(3.6f)) else minOf(cqw(3.5f), cqh(5.5f))
    val colGap = if (wide) cqw(3f) else minOf(cqw(4f), cqh(6f))

    if (heightClass == CardHeightClass.SHORT || heightClass == CardHeightClass.TINY) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(colGap),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MetaBlock(
                    folder = folder,
                    wide = wide,
                    heightClass = heightClass,
                    cqw = cqw,
                    cqh = cqh,
                    toCardSp = toCardSp,
                )
            }
            OpenFolderKey(
                wide = wide,
                heightClass = heightClass,
                cqw = cqw,
                cqh = cqh,
                toCardSp = toCardSp,
                onClick = onOpenFolder,
            )
        }
    } else if (wide) {
        Column(
            verticalArrangement = Arrangement.spacedBy(rowGap),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(colGap),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MetaBlock(
                        folder = folder,
                        wide = true,
                        heightClass = heightClass,
                        cqw = cqw,
                        cqh = cqh,
                        toCardSp = toCardSp,
                    )
                }
                if (showThumbs) {
                    ThumbsCluster(
                        folder = folder,
                        folderIndex = folderIndex,
                        wide = true,
                        previewVideos = previewVideos,
                        cqw = cqw,
                        cqh = cqh,
                        toCardSp = toCardSp,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(colGap),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (showBars) {
                        WaveformBars(
                            folder = folder,
                            folderIndex = folderIndex,
                            wide = true,
                            cqw = cqw,
                            cqh = cqh,
                        )
                    }
                }
                OpenFolderKey(
                    wide = true,
                    heightClass = heightClass,
                    cqw = cqw,
                    cqh = cqh,
                    toCardSp = toCardSp,
                    onClick = onOpenFolder,
                )
            }
        }
    } else {
        val leftColWidth = minOf(cqw(49f), cqh(76f))
        Column(
            verticalArrangement = Arrangement.spacedBy(rowGap),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(colGap),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.width(leftColWidth)) {
                    MetaBlock(
                        folder = folder,
                        wide = false,
                        heightClass = heightClass,
                        cqw = cqw,
                        cqh = cqh,
                        toCardSp = toCardSp,
                    )
                }
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.weight(1f),
                ) {
                    if (showBars) {
                        WaveformBars(
                            folder = folder,
                            folderIndex = folderIndex,
                            wide = false,
                            cqw = cqw,
                            cqh = cqh,
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(colGap),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.width(leftColWidth)) {
                    if (showThumbs) {
                        ThumbsCluster(
                            folder = folder,
                            folderIndex = folderIndex,
                            wide = false,
                            previewVideos = previewVideos,
                            cqw = cqw,
                            cqh = cqh,
                            toCardSp = toCardSp,
                        )
                    }
                }
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.weight(1f),
                ) {
                    OpenFolderKey(
                        wide = false,
                        heightClass = heightClass,
                        cqw = cqw,
                        cqh = cqh,
                        toCardSp = toCardSp,
                        onClick = onOpenFolder,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaBlock(
    folder: Folder,
    wide: Boolean,
    heightClass: CardHeightClass,
    cqw: (Float) -> Dp,
    cqh: (Float) -> Dp,
    toCardSp: Dp.() -> TextUnit,
) {
    val metaDp = when {
        heightClass == CardHeightClass.TINY -> cqw(3.2f).coerceIn(6.5.dp, 9.dp)
        wide -> maxOf(9.dp, minOf(cqw(1.6f), cqh(3.3f)))
        else -> cqw(3.2f).coerceIn(7.dp, 12.dp)
    }
    val singleLineMeta = heightClass == CardHeightClass.SHORT || heightClass == CardHeightClass.TINY
    val countLabel = "${folder.videosCount} ${if (folder.videosCount == 1) "VIDEO" else "VIDEOS"}"
    val metaLine = "$countLabel · ${Utils.formatDurationMillis(folder.totalDuration)}"

    Column {
        Text(
            text = metaLine,
            fontFamily = JetBrainsMono,
            fontSize = metaDp.toCardSp(),
            lineHeight = (metaDp * 1.55f).toCardSp(),
            letterSpacing = 0.1.em,
            color = PrototypeInkPaper,
            maxLines = if (singleLineMeta) 1 else 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (heightClass != CardHeightClass.TINY && folder.path.isNotEmpty()) {
            Text(
                text = folder.path,
                fontFamily = JetBrainsMono,
                fontSize = metaDp.toCardSp(),
                lineHeight = (metaDp * 1.55f).toCardSp(),
                letterSpacing = 0.02.em,
                color = PrototypeInkPaper.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun WaveformBars(
    folder: Folder,
    folderIndex: Int,
    wide: Boolean,
    cqw: (Float) -> Dp,
    cqh: (Float) -> Dp,
) {
    val bn = minOf(folder.videosCount, if (wide) 26 else 14)
    val accent = LocalAppAccent.current
    val barsH = if (wide) minOf(cqh(14f), cqw(9f)) else minOf(cqh(11.8f), cqw(10f))
    val barW = if (wide) minOf(cqw(1.46f), cqh(3.2f)) else minOf(cqw(1.5f), cqh(2.6f))
    val barGap = if (wide) cqw(0.6f) else cqw(0.9f)
    val newCount = minOf(folder.newVideosCount, bn)

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(barGap),
        modifier = Modifier.height(barsH),
    ) {
        for (k in 0 until bn) {
            val hFrac = 0.28f + 0.72f * prototypeSeed(folderIndex * 31 + k)
            val isNew = k < newCount
            Box(
                modifier = Modifier
                    .width(barW)
                    .fillMaxHeight(hFrac)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isNew) accent.color else Color(0xFF352C1A)),
            )
        }
    }
}

@Composable
private fun ThumbsCluster(
    folder: Folder,
    folderIndex: Int,
    wide: Boolean,
    previewVideos: List<CrateCardVideoPreview>,
    cqw: (Float) -> Dp,
    cqh: (Float) -> Dp,
    toCardSp: Dp.() -> TextUnit,
) {
    val clusterW = if (wide) minOf(cqw(30f), cqh(60f)) else minOf(cqw(49f), cqh(76f))
    val clusterH = if (wide) minOf(cqw(9f), cqh(18f)) else minOf(cqw(16f), cqh(25f))
    val thumbFontDp = if (wide) minOf(cqw(1.3f), cqh(2.6f)) else minOf(cqw(3f), cqh(4.6f))
    val tileW = clusterW * (if (wide) 0.42f else 0.49f)

    val tn = minOf(3, folder.videosCount)
    val slots = when (tn) {
        1 -> listOf(ThumbSlot.MIDDLE)
        2 -> listOf(ThumbSlot.LEFT, ThumbSlot.RIGHT)
        else -> listOf(ThumbSlot.LEFT, ThumbSlot.MIDDLE, ThumbSlot.RIGHT)
    }

    Box(
        modifier = Modifier.size(width = clusterW, height = clusterH),
    ) {
        slots.forEachIndexed { k, slot ->
            val vi = tn - 1 - k
            val rot = when (slot) {
                ThumbSlot.LEFT -> -5f
                ThumbSlot.MIDDLE -> 0f
                ThumbSlot.RIGHT -> 4.5f
            }
            val align = when (slot) {
                ThumbSlot.LEFT -> Alignment.BottomStart
                ThumbSlot.MIDDLE -> Alignment.BottomCenter
                ThumbSlot.RIGHT -> Alignment.BottomEnd
            }
            val preview = previewVideos.getOrNull(vi)
            val label = if (preview != null) {
                "${preview.title} · ${formatMinutesSeconds(preview.durationMillis)}"
            } else {
                val avgDur = (folder.totalDuration / folder.videosCount.coerceAtLeast(1))
                    .coerceAtLeast(60_000L)
                val dur = (avgDur * (0.7f + prototypeSeed(vi + folderIndex) * 0.6f)).toLong()
                "video${vi + 1} · ${formatMinutesSeconds(dur)}"
            }

            Box(
                modifier = Modifier
                    .align(align)
                    .width(tileW)
                    .aspectRatio(16f / 9f)
                    .zIndex((k + 1).toFloat())
                    .graphicsLayer { rotationZ = rot }
                    .clip(RoundedCornerShape(thumbFontDp * 0.4f))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2C2519), Color(0xFF1B1710)),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.Black.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(thumbFontDp * 0.4f),
                    )
                    .padding(
                        horizontal = thumbFontDp * 0.45f,
                        vertical = thumbFontDp * 0.35f,
                    ),
            ) {
                // Play dot in center
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = -tileW * 0.04f)
                        .size(tileW * 0.22f)
                        .clip(CircleShape)
                        .background(Color(0xFFECE4D2).copy(alpha = 0.16f)),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize(0.45f)) {
                        val tri = Path().apply {
                            moveTo(size.width * 0.25f, 0f)
                            lineTo(size.width, size.height * 0.5f)
                            lineTo(size.width * 0.25f, size.height)
                            close()
                        }
                        drawPath(tri, Color(0xFFECE4D2).copy(alpha = 0.75f))
                    }
                }
                Text(
                    text = label,
                    fontFamily = JetBrainsMono,
                    fontSize = (thumbFontDp * 0.62f).toCardSp(),
                    letterSpacing = 0.05.em,
                    color = Color(0xFFD9CBA6).copy(alpha = 0.85f),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart),
                )
            }
        }
    }
}

@Composable
private fun OpenFolderKey(
    wide: Boolean,
    heightClass: CardHeightClass,
    cqw: (Float) -> Dp,
    cqh: (Float) -> Dp,
    toCardSp: Dp.() -> TextUnit,
    onClick: () -> Unit,
) {
    val keyHeight = when {
        heightClass == CardHeightClass.TINY -> minOf(cqh(26f), 40.dp)
        heightClass == CardHeightClass.SHORT -> minOf(cqh(30f), 48.dp)
        wide -> minOf(cqw(6.8f), cqh(13.7f))
        else -> minOf(cqw(14f), cqh(22f))
    }
    val keyRadius = if (wide) minOf(cqw(2.2f), cqh(4.4f)) else minOf(cqw(4.4f), cqh(6.6f))
    val keyFontDp = if (wide) minOf(cqw(1.8f), cqh(3.7f)) else cqw(4.2f).coerceIn(10.dp, 16.dp)
    val keyPadH = when {
        heightClass == CardHeightClass.TINY -> 14.dp
        heightClass == CardHeightClass.SHORT -> minOf(cqw(5f), 22.dp)
        wide -> minOf(cqw(4.4f), cqh(9f))
        else -> 12.dp
    }
    val minWidth = when {
        heightClass == CardHeightClass.SHORT || heightClass == CardHeightClass.TINY -> 0.dp
        wide -> minOf(cqw(20f), cqh(40f))
        else -> 0.dp
    }

    val accent = LocalAppAccent.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowHeight = 5.dp
    val pressOffsetY = if (isPressed) shadowHeight else 0.dp
    val keyShape = RoundedCornerShape(keyRadius)

    Box(
        modifier = Modifier
            .padding(bottom = shadowHeight)
            .height(keyHeight)
            .widthIn(min = minWidth),
    ) {
        // Hard 5dp bottom edge (#7A2A12)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = shadowHeight)
                .clip(keyShape)
                .background(accent.shadow),
        )

        // Full orange keycap with centered label
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(min = minWidth)
                .offset(y = pressOffsetY)
                .clip(keyShape)
                .background(accent.color)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = keyPadH),
        ) {
            Text(
                text = "Open folder →",
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold,
                fontSize = keyFontDp.toCardSp(),
                color = Color(0xFF0D0C0A),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}
