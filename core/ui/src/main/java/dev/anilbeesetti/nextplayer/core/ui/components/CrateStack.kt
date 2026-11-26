package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.theme.LocalAppAccent
import kotlin.math.roundToInt

private val ORG_X = floatArrayOf(
    -18f, 17f, -24f, 21f, -11f, 24f, -20f, 14f, -22f,
    19f, -14f, 23f, -17f, 12f, -21f, 16f, -10f, 18f,
)

private val ORG_R = floatArrayOf(
    -2.1f, 1.9f, -2.7f, 2.4f, -1.3f, 2.8f, -2.3f, 1.6f, -2.5f,
    2.1f, -1.7f, 2.6f, -2.0f, 1.4f, -2.4f, 1.8f, -1.2f, 2.0f,
)

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

data class CrateSolveResult(
    val ok: Boolean,
    val cardWidthDp: Float,
    val cardHeightDp: Float,
    val shape: Float,
    val t: Float,
    val riseDp: Float,
    val tabHeightDp: Float,
    val behindCount: Int,
    val perspectiveP: Float,
    val stepZ: Float,
    val oyK: Float,
    val scaleOfApproved: Float,
    val marginDp: Float,
)

/**
 * Adaptive crate solver from design/CRATE_SPEC.md section 4 & design/crate_adaptive.html.
 */
fun solveCrateLayout(
    availableWidthDp: Float,
    availableHeightDp: Float,
    folderCount: Int,
): CrateSolveResult {
    val aw = availableWidthDp.coerceAtLeast(1f)
    val ah = availableHeightDp.coerceAtLeast(1f)
    val wideScr = aw / ah >= 1.3f
    val a0 = if (wideScr) 2.0f else 1.56f
    val capW = if (wideScr) 820f else 560f
    val m = (aw * 0.03f).coerceIn(10f, 18f)
    val base = if (a0 < 1.7f) minOf(aw * 0.835f, capW) else minOf(aw - 2f * m, capW)
    val nMax = (folderCount - 1).coerceAtLeast(0)

    fun fit(shape: Float, sc: Float, targetBehind: Int): CrateSolveResult {
        val w = base * sc
        val h = w / shape
        val t = ((shape - 1.56f) / (2.0f - 1.56f)).coerceIn(0f, 1f)
        val rise = maxOf(h * lerp(0.245f, 0.117f, t), 22f)
        val wide = w >= 640f
        val tabH = if (wide) {
            minOf(46f, maxOf(30f, h * 0.112f))
        } else {
            minOf(34f, maxOf(26f, w * 0.09f))
        }
        val p = 2f * w
        val z = 0.1f * w
        val oyK = lerp(0.9f, 0.26f, t)
        val oy = -oyK * h
        var n = -1
        for (k in nMax downTo 0) {
            val scl = p / (p + k * z)
            val farTop = oy + ((-k * rise) - oy) * scl - tabH * scl
            if (h - farTop <= ah - 2f * m) {
                n = k
                break
            }
        }
        val minN = minOf(nMax, targetBehind)
        return CrateSolveResult(
            ok = n >= minN && n >= 0,
            cardWidthDp = w,
            cardHeightDp = h,
            shape = shape,
            t = t,
            riseDp = rise,
            tabHeightDp = tabH,
            behindCount = maxOf(n, 0),
            perspectiveP = p,
            stepZ = z,
            oyK = oyK,
            scaleOfApproved = sc,
            marginDp = m,
        )
    }

    // 1) Deep stack first: >= 4 folders behind, card >= 75% of approved size
    var sc0 = 1.0f
    while (sc0 >= 0.749f) {
        val r0 = fit(a0, sc0, 4)
        if (r0.ok) return r0
        sc0 -= 0.02f
    }

    // 2) Otherwise settle for >= 2 behind at full scale
    val r1 = fit(a0, 1.0f, 2)
    if (r1.ok) return r1

    // 3) & 4) Try shapes [A0, A0-.2, A0-.4] and scales down to .62 then .4
    val shapes = if (a0 > 1.7f) {
        listOf(a0, maxOf(1.56f, a0 - 0.2f), maxOf(1.56f, a0 - 0.4f))
    } else {
        listOf(a0)
    }
    val tiers = listOf(
        5 to 0.62f,
        4 to 0.62f,
        3 to 0.62f,
        2 to 0.62f,
        5 to 0.40f,
        4 to 0.40f,
        3 to 0.40f,
        2 to 0.40f,
    )
    var best: CrateSolveResult? = null
    for ((tg, lo) in tiers) {
        for (shape in shapes) {
            var sc = 1.0f
            while (sc >= lo - 0.001f) {
                val c = fit(shape, sc, tg)
                if (c.ok) {
                    if (best == null || c.cardHeightDp > best.cardHeightDp + 1f) {
                        best = c
                    }
                    break
                }
                sc -= 0.02f
            }
        }
        if (best != null) return best
    }

    // 5) Fallback
    return fit(a0, 0.45f, 2)
}

/**
 * Adaptive 3D folder stack ported from design/CRATE_SPEC.md sections 3 & 4
 * and design/crate_adaptive.html.
 */
@Composable
fun CrateStack(
    folders: List<Folder>,
    pos: Float,
    modifier: Modifier = Modifier,
    lastPlayedFolderPath: String? = null,
    previewsForFolder: (Folder) -> List<CrateCardVideoPreview> = { emptyList() },
    onOpenFolder: (Folder) -> Unit = {},
    onSolveChanged: ((CrateSolveResult) -> Unit)? = null,
) {
    if (folders.isEmpty()) return
    val density = LocalDensity.current.density

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val aw = maxWidth.value
        val ah = maxHeight.value
        val g = solveCrateLayout(aw, ah, folders.size)
        LaunchedEffect(g) {
            onSolveChanged?.invoke(g)
        }

        val cw = g.cardWidthDp
        val ch = g.cardHeightDp
        val oy = -g.oyK * ch
        val farScale = g.perspectiveP / (g.perspectiveP + g.behindCount * g.stepZ)
        val farTop = oy + ((-g.behindCount * g.riseDp) - oy) * farScale - g.tabHeightDp * farScale
        val ext = ch - farTop
        val frontTop = (ah - ext) / 2f - farTop
        val fx = (aw - cw) / 2f
        val fy = frontTop

        val passY = ch * lerp(1.36f, 0.78f, g.t)
        val lim = maxOf(2f, g.behindCount - 2.5f)
        val kWidth = cw / 344f

        // Visible window: -1.7 < d < n + 1.4, ordered back-to-front so nearest is on top
        val visibleIndices = folders.indices
            .map { i -> i to (i - pos) }
            .filter { (_, d) -> d > -1.7f && d < g.behindCount + 1.4f }
            .sortedByDescending { (_, d) -> d }

        Box(modifier = Modifier.fillMaxSize()) {
            visibleIndices.forEach { (i, d) ->
                val folder = folders[i]
                val ease = if (d > 0f) minOf(1f, d * 1.1f) else 0f

                val y: Float
                val z: Float
                val rx: Float
                val opacity: Float
                if (d >= 0f) {
                    y = -d * g.riseDp
                    z = -d * g.stepZ
                    rx = 5f
                    opacity = ((g.behindCount + 0.5f - d) / 0.5f).coerceIn(0f, 1f)
                } else {
                    y = -d * passY
                    z = -d * g.stepZ * 1.9f
                    rx = d * 30f
                    opacity = maxOf(0f, 1f + d * 1.05f)
                }

                val xo = ORG_X[i % ORG_X.size] * kWidth * ease
                val rz = ORG_R[i % ORG_R.size] * ease

                // Perspective projection matching CSS perspective(P) with perspective-origin (50%, oy)
                val scl = (g.perspectiveP / (g.perspectiveP - z)).coerceAtLeast(0.05f)
                val projX = xo * scl
                val projY = oy + (y - oy) * scl

                var br = maxOf(0.5f, 1f - maxOf(0f, d) * 0.08f)
                if (d > lim) {
                    br *= maxOf(0.2f, 1f - (d - lim) * 0.32f)
                }
                val darkenAlpha = if (d > 0.02f) (1f - br).coerceIn(0f, 0.9f) else 0f

                val zIndexVal = if (d >= 0f) {
                    500f - (d * 10f).roundToInt()
                } else {
                    600f - d
                }

                Box(
                    modifier = Modifier
                        .offset(x = fx.dp, y = fy.dp)
                        .zIndex(zIndexVal)
                        .graphicsLayer {
                            translationX = projX * density
                            translationY = projY * density
                            scaleX = scl
                            scaleY = scl
                            rotationX = rx
                            rotationZ = rz
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            alpha = opacity
                            cameraDistance = maxOf(8f, g.perspectiveP * 0.05f) * density
                            if (darkenAlpha > 0f) {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                        }
                        .then(
                            if (darkenAlpha > 0f) {
                                Modifier.drawWithContent {
                                    drawContent()
                                    drawRect(
                                        color = Color.Black.copy(alpha = darkenAlpha),
                                        blendMode = BlendMode.SrcAtop,
                                    )
                                }
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    CrateFolderCard(
                        folder = folder,
                        index = i,
                        totalFolders = folders.size,
                        cardWidth = cw.dp,
                        cardHeight = ch.dp,
                        tabHeight = g.tabHeightDp.dp,
                        isLastPlayed = lastPlayedFolderPath == folder.path ||
                            (lastPlayedFolderPath == null && i == 1),
                        previewVideos = previewsForFolder(folder),
                        onOpenFolder = { onOpenFolder(folder) },
                    )
                }
            }

            // Right-edge ruler
            if (folders.size >= 2) {
                CrateRuler(
                    totalFolders = folders.size,
                    activeIndex = pos.roundToInt().coerceIn(0, folders.lastIndex),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp, top = 6.dp, bottom = 6.dp)
                        .fillMaxHeight()
                        .zIndex(900f),
                )
            }
        }
    }
}

@Composable
private fun CrateRuler(
    totalFolders: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppAccent.current
    BoxWithConstraints(
        modifier = modifier.width(22.dp),
    ) {
        val h: Dp = maxHeight
        val step = minOf(18.dp, h / (totalFolders - 1))
        val used = step * (totalFolders - 1)
        val y0 = (h - used) / 2f

        for (i in 0 until totalFolders) {
            val isOn = i == activeIndex
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = y0 + step * i - 1.dp)
                    .width(if (isOn) 15.dp else 7.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isOn) accent.color else Color(0xFF4A4231)),
            )
        }
    }
}
