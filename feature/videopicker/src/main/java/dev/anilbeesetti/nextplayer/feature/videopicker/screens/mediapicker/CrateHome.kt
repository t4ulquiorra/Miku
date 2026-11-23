package dev.anilbeesetti.nextplayer.feature.videopicker.screens.mediapicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.ui.components.KeycapButton
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.BricolageGrotesque
import dev.anilbeesetti.nextplayer.core.ui.theme.JetBrainsMono
import dev.anilbeesetti.nextplayer.core.ui.theme.LocalAppAccent
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeBorderInput
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkDim
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeInkPaper
import dev.anilbeesetti.nextplayer.core.ui.theme.PrototypeStamp

// JS constants copied directly from ~/docs/prototype.html (lines 2222-2238)
private val PAPERS = listOf(
    Color(0xFFD9CBA6), // #D9CBA6
    Color(0xFFCDBE96), // #CDBE96
    Color(0xFFE2D6B8), // #E2D6B8
    Color(0xFFC4B48A), // #C4B48A
)

private val TAB_SLOTS = listOf(30.dp, 185.dp, 340.dp, 495.dp)

private data class OrganicOffset(val x: Float, val rz: Float)

private val ORGANIC_OFFSETS = listOf(
    OrganicOffset(-48f, -2.1f),
    OrganicOffset(44f, 1.9f),
    OrganicOffset(-62f, -2.7f),
    OrganicOffset(56f, 2.4f),
    OrganicOffset(-30f, -1.3f),
    OrganicOffset(64f, 2.8f),
    OrganicOffset(-54f, -2.3f),
    OrganicOffset(36f, 1.6f),
    OrganicOffset(-58f, -2.5f),
    OrganicOffset(50f, 2.1f),
    OrganicOffset(-38f, -1.7f),
    OrganicOffset(60f, 2.6f),
    OrganicOffset(-46f, -2.0f),
    OrganicOffset(32f, 1.4f),
    OrganicOffset(-56f, -2.4f),
    OrganicOffset(42f, 1.8f),
    OrganicOffset(-26f, -1.2f),
    OrganicOffset(48f, 2.0f),
)

/**
 * LANDSCAPE 3D Folder Crate Viewport (1184 x 800)
 * Ported directly from ~/docs/prototype.html:
 * .crate-viewport, .folder-wrap, .folder-tab-btn, .folder-body-btn,
 * .folder-name-big, .folder-stamp, .folder-spines, .open-folder-cta,
 * TAB_SLOTS, PAPERS, ORGANIC_OFFSETS, and updateCrateTransforms.
 */
@Composable
fun CrateHome(
    folders: List<Folder>,
    recentlyPlayedFolder: Folder? = null,
    onOpenFolder: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (folders.isEmpty()) return

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val designWidth = 1184.dp
        val designHeight = 800.dp
        val scale = minOf(
            maxWidth / designWidth,
            maxHeight / designHeight,
        )

        // .crate-viewport: 1184px x 800px, scaled uniformly to fit screen
        Box(
            modifier = Modifier
                .size(designWidth, designHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin.Center
                },
        ) {
            val total = folders.size
            val frontIndex = 0
            val maxVisible = minOf(folders.lastIndex, frontIndex + 6)

            // Render back-to-front (highest d down to 0) so front card is on top
            for (index in maxVisible downTo frontIndex) {
                val folder = folders[index]
                val d = (index - frontIndex).toFloat()
                val isFront = index == frontIndex
                val isLast = recentlyPlayedFolder?.path == folder.path

                CrateFolderWrap(
                    folder = folder,
                    index = index,
                    d = d,
                    totalFolders = total,
                    isFront = isFront,
                    isLastPlayed = isLast,
                    onOpenFolder = { onOpenFolder(folder.path) },
                )
            }
        }
    }
}

@Composable
private fun CrateFolderWrap(
    folder: Folder,
    index: Int,
    d: Float,
    totalFolders: Int,
    isFront: Boolean,
    isLastPlayed: Boolean,
    onOpenFolder: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val density = LocalDensity.current.density

    // JS updateCrateTransforms logic:
    // const scatter = 0.75;
    // const ease = d > 0 ? Math.min(1, d * 1.1) * scatter : 0;
    // const tx = org.x * ease;
    // const rz = org.rz * ease;
    // let y, z, rx, o;
    // y = -d * 48; z = -d * 80; rx = 5;
    // o = d > 3.5 ? Math.max(0, 1 - (d - 3.5) * 0.34) : 1;
    // const b = Math.max(0.5, 1 - Math.max(0, d) * 0.08);
    // const zi = 1000 - Math.round(d * 10);
    val org = ORGANIC_OFFSETS[index % ORGANIC_OFFSETS.size]
    val scatter = 0.75f
    val ease = if (d > 0f) minOf(1f, d * 1.1f) * scatter else 0f
    val tx = org.x * ease
    val rz = org.rz * ease

    val y = -d * 48f
    val rx = 5f
    // CSS translateZ(-d * 80px) projected under perspective: 1600px
    val scale3d = 1600f / (1600f + d * 80f)
    val opacity = if (d > 3.5f) (1f - (d - 3.5f) * 0.34f).coerceAtLeast(0f) else 1f
    val brightness = (1f - maxOf(0f, d) * 0.08f).coerceAtLeast(0.5f)
    val zIndexVal = 1000f - d * 10f

    val paperColor = PAPERS[index % PAPERS.size]
    val tabBg = if (isLastPlayed) accent.color else paperColor

    // .folder-wrap: left 182px, top 330px, width 820px, height 410px, transform-origin: 50% 0%
    Box(
        modifier = Modifier
            .offset(x = 182.dp, y = 330.dp)
            .size(width = 820.dp, height = 410.dp)
            .zIndex(zIndexVal)
            .graphicsLayer {
                translationX = tx * density
                translationY = y * density
                scaleX = scale3d
                scaleY = scale3d
                rotationX = rx
                rotationZ = rz
                transformOrigin = TransformOrigin(0.5f, 0f)
                alpha = opacity
                cameraDistance = 1600f * density
            },
    ) {
        // Upward shadow above card: y -12, blur 28, black 40% (box-shadow: 0 -12px 28px rgba(0, 0, 0, 0.4))
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

        // .folder-tab-btn: top -36px, width 248px, height 46px, border-radius 12px 12px 0 0, padding 9px 14px 0
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            color = tabBg,
            shadowElevation = 2.dp,
            modifier = Modifier
                .offset(x = TAB_SLOTS[index % 4], y = (-36).dp)
                .size(width = 248.dp, height = 46.dp)
                .drawBehind {
                    // inset 0 1px 0 rgba(255, 255, 255, 0.35)
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(14.dp.toPx(), 1.dp.toPx()),
                        end = Offset(size.width - 14.dp.toPx(), 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )
                },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.Top,
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
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        text = folder.name,
                        fontFamily = BricolageGrotesque,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = PrototypeInkPaper,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = folder.videosCount.toString(),
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = PrototypeInkPaper,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                // Brightness filter falloff
                if (brightness < 0.999f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = (1f - brightness) * 0.75f)),
                    )
                }
            }
        }

        // .folder-body-btn: 820px x 410px, border-radius 6px 20px 16px 16px
        Surface(
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 20.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp,
            ),
            color = paperColor,
            shadowElevation = 0.dp,
            modifier = Modifier
                .size(width = 820.dp, height = 410.dp)
                .drawBehind {
                    // inset 0 2px 0 rgba(255, 255, 255, 0.4) following top-left (6) and top-right (20) rounded corners
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
                // Header Label: FOLDER 01 / 18, left 30px, top 30px, font 12px, letter-spacing .14em, opacity .65
                Text(
                    text = "FOLDER ${String.format("%02d", index + 1)} / ${String.format("%02d", totalFolders)}",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    letterSpacing = 0.14.em,
                    color = PrototypeInkPaper.copy(alpha = 0.65f),
                    modifier = Modifier.offset(x = 30.dp, y = 30.dp),
                )

                // Big Title: .folder-name-big, left 30px, top 62px, 54px bold, letter-spacing -1.5px, line-height 1.02, max-width 560px
                Text(
                    text = folder.name,
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp,
                    lineHeight = 55.sp,
                    letterSpacing = (-1.5).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1A150C),
                    modifier = Modifier
                        .offset(x = 30.dp, y = 62.dp)
                        .widthIn(max = 560.dp),
                )

                // Stamp: .folder-stamp, right 34px, top 34px, padding 8px 14px, 2px border, rotate -6deg, radius 6px
                if (folder.newVideosCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, PrototypeStamp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 34.dp, top = 34.dp)
                            .graphicsLayer { rotationZ = -6f },
                    ) {
                        Text(
                            text = "${folder.newVideosCount} NEW",
                            fontFamily = JetBrainsMono,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrototypeStamp,
                            letterSpacing = 0.12.em,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }

                // "LAST PLAYED": right 34px, top 98px, mono 12px, letter-spacing .14em
                if (isLastPlayed) {
                    Text(
                        text = "LAST PLAYED",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        letterSpacing = 0.14.em,
                        color = PrototypeInkPaper,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 34.dp, top = 98.dp),
                    )
                }

                // Meta line & path: left 32px, top 196px, right 360px, mono 13px, letter-spacing .1em, line-height 1.55
                Column(
                    modifier = Modifier
                        .offset(x = 32.dp, y = 196.dp)
                        .width((820 - 32 - 360).dp),
                ) {
                    val countPart = "${folder.videosCount} ${if (folder.videosCount == 1) "VIDEO" else "VIDEOS"}"
                    val metaText = if (folder.totalDuration > 0) {
                        "$countPart · ${Utils.formatDurationMillis(folder.totalDuration)}"
                    } else {
                        countPart
                    }
                    Text(
                        text = metaText,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        letterSpacing = 0.1.em,
                        lineHeight = 20.sp,
                        color = PrototypeInkPaper,
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
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                // .peek thumbnails area: right 44px, top 134px, width 300px, height 120px
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 44.dp, top = 134.dp)
                        .size(width = 300.dp, height = 120.dp),
                ) {
                    for (k in 0..2) {
                        // .peek-c: left calc(var(--k) * 56px), top calc(var(--k) * -5px), width 148px, 16:9, radius 6px
                        // transform: rotate(calc((var(--k) - 1) * 4deg)), box-shadow: 0 10px 22px rgba(0, 0, 0, 0.42)
                        val rot = (k - 1) * 4f
                        val offsetX = (k * 56).dp
                        val offsetY = (-k * 5).dp
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF221E17), // Flat dark paper tile, not black
                            border = BorderStroke(1.dp, PrototypeBorderInput),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .offset(x = offsetX, y = offsetY)
                                .size(width = 148.dp, height = 83.dp)
                                .graphicsLayer { rotationZ = rot }
                                .zIndex((k + 1).toFloat()),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Icon(
                                    imageVector = NextIcons.Movie,
                                    contentDescription = null,
                                    tint = PrototypeInkDim.copy(alpha = 0.6f),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }

                // .folder-spines: left 30px, bottom 34px, height 150px, display flex, align-items flex-end, gap 5px
                val spineCount = minOf(folder.videosCount, 26)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 30.dp, y = (-34).dp)
                        .height(150.dp),
                ) {
                    for (k in 0 until spineCount) {
                        // height: 46 + (((i + 3) * 31 + k * 17) % 5) * 20
                        val barHeight = (46 + (((index + 3) * 31 + k * 17) % 5) * 20).dp
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

                // Brightness filter falloff
                if (brightness < 0.999f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = (1f - brightness) * 0.75f)),
                    )
                }
            }
        }

        // .open-folder-cta: right 30px, bottom 28px, height 56px, padding 0 24px, radius 18px, background accent, box-shadow 0 5px 0 accent-shadow
        // Only visible on front card
        if (isFront) {
            KeycapButton(
                onClick = onOpenFolder,
                isAccent = true,
                shape = RoundedCornerShape(18.dp),
                shadowHeight = 5.dp,
                contentPadding = PaddingValues(horizontal = 24.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-30).dp, y = (-28).dp)
                    .height(56.dp)
                    .zIndex(3f),
            ) {
                Text(
                    text = "Open folder →",
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0D0C0A),
                )
            }
        }
    }
}
