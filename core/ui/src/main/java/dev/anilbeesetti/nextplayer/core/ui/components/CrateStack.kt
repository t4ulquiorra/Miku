package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.theme.LocalAppAccent
import kotlin.math.abs
import kotlin.math.pow
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

@Stable
class CrateStackState(initialPos: Float = 0f) {
    var pos by mutableFloatStateOf(initialPos)
        internal set
    var target by mutableFloatStateOf(initialPos)
        internal set
    internal var vel by mutableFloatStateOf(0f)
    internal var isDragging by mutableStateOf(false)

    fun snapTo(newPos: Float, maxIndex: Int) {
        val clamped = newPos.coerceIn(0f, maxIndex.coerceAtLeast(0).toFloat())
        isDragging = false
        vel = 0f
        pos = clamped
        target = clamped
    }

    fun animateTo(index: Int, maxIndex: Int) {
        isDragging = false
        target = index.coerceIn(0, maxIndex.coerceAtLeast(0)).toFloat()
    }
}

@Composable
fun rememberCrateStackState(initialPos: Float = 0f): CrateStackState =
    remember { CrateStackState(initialPos) }

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
 * Adaptive 3D folder stack ported from design/CRATE_SPEC.md sections 3, 4 & 5
 * and design/crate_adaptive.html.
 */
@Composable
fun CrateStack(
    folders: List<Folder>,
    state: CrateStackState = rememberCrateStackState(),
    modifier: Modifier = Modifier,
    lastPlayedFolderPath: String? = null,
    previewsForFolder: (Folder) -> List<CrateCardVideoPreview> = { emptyList() },
    onOpenFolder: (Int) -> Unit = {},
    onSolveChanged: ((CrateSolveResult) -> Unit)? = null,
) {
    if (folders.isEmpty()) return
    val density = LocalDensity.current.density
    val maxIndex = folders.lastIndex

    // Spring animation loop: runs only while moving and not dragging
    LaunchedEffect(state.isDragging, state.target, maxIndex) {
        if (state.isDragging) return@LaunchedEffect
        var lastNanos = 0L
        while (abs(state.target - state.pos) > 0.0005f || abs(state.vel) > 0.0005f) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dtFrames = ((now - lastNanos) / (1_000_000_000f / 60f)).coerceIn(0.1f, 3f)
                    val dv = state.target - state.pos
                    val decay = 0.74f.pow(dtFrames)
                    state.vel = (state.vel + dv * 0.13f * dtFrames) * decay
                    state.pos += state.vel * dtFrames
                }
                lastNanos = now
            }
        }
        if (state.pos != state.target) {
            state.pos = state.target
        }
        state.vel = 0f
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        val aw = maxWidth.value
        val ah = maxHeight.value

        // Only recompute layout when available area or folder count changes
        val g = remember(aw, ah, folders.size) {
            solveCrateLayout(aw, ah, folders.size)
        }
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
        val perFolderPx = maxOf(40f, g.riseDp * 2.8f) * density

        val currentPos = state.pos
        val frontRounded = currentPos.roundToInt().coerceIn(0, maxIndex)

        // Visible window: -1.7 < d < n + 1.4, ordered back-to-front so nearest is on top
        val visibleIndices = folders.indices
            .map { i -> i to (i - currentPos) }
            .filter { (_, d) -> d > -1.7f && d < g.behindCount + 1.4f }
            .sortedByDescending { (_, d) -> d }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(perFolderPx, maxIndex) {
                    var dragStartPos = 0f
                    var lastTimeMs = 0L
                    var lastPos = 0f
                    var velPerSec = 0f

                    detectVerticalDragGestures(
                        onDragStart = {
                            state.isDragging = true
                            state.vel = 0f
                            dragStartPos = state.pos
                            lastPos = state.pos
                            lastTimeMs = System.nanoTime() / 1_000_000L
                            velPerSec = 0f
                        },
                        onDragEnd = {
                            state.isDragging = false
                            val projected = state.pos + velPerSec * 0.24f
                            state.target = projected.roundToInt().coerceIn(0, maxIndex).toFloat()
                            state.vel = 0f
                        },
                        onDragCancel = {
                            state.isDragging = false
                            state.target = state.pos.roundToInt().coerceIn(0, maxIndex).toFloat()
                            state.vel = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val deltaFolders = dragAmount / perFolderPx
                            val nextPos = (state.pos + deltaFolders).coerceIn(-0.3f, maxIndex + 0.3f)
                            val nowMs = System.nanoTime() / 1_000_000L
                            val dtMs = (nowMs - lastTimeMs).coerceAtLeast(1L)
                            velPerSec = ((nextPos - lastPos) / dtMs.toFloat()) * 1000f
                            lastPos = nextPos
                            lastTimeMs = nowMs
                            state.pos = nextPos
                            state.target = nextPos
                        },
                    )
                },
        ) {
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
                        },
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
                        darkenAlpha = darkenAlpha,
                        previewVideos = previewsForFolder(folder),
                        onOpenFolder = {
                            if (i == frontRounded) {
                                onOpenFolder(i)
                            } else {
                                state.animateTo(i, maxIndex)
                            }
                        },
                        onTabClick = {
                            if (abs(i - state.pos) > 0.4f) {
                                state.animateTo(i, maxIndex)
                            }
                        },
                    )
                }
            }

            // Right-edge ruler (tap or drag to jump through folders)
            if (folders.size >= 2) {
                CrateRuler(
                    totalFolders = folders.size,
                    activeIndex = frontRounded,
                    onJumpTo = { idx -> state.animateTo(idx, maxIndex) },
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
    onJumpTo: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppAccent.current
    val density = LocalDensity.current.density

    BoxWithConstraints(
        modifier = modifier
            .width(22.dp)
            .pointerInput(totalFolders) {
                val hDp = size.height / density
                val step = minOf(18f, hDp / (totalFolders - 1))
                val used = step * (totalFolders - 1)
                val y0 = (hDp - used) / 2f

                fun jumpAtY(py: Float) {
                    val yDp = py / density
                    val idx = ((yDp - y0) / step).roundToInt().coerceIn(0, totalFolders - 1)
                    onJumpTo(idx)
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    jumpAtY(down.position.y)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        change.consume()
                        jumpAtY(change.position.y)
                    }
                }
            },
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
