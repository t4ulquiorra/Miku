package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
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

    // Performance telemetry for the debug readout
    var totalCardRecomps by mutableIntStateOf(0)
        internal set
    var moveRecomps by mutableIntStateOf(0)
        internal set
    var avgFrameMs by mutableFloatStateOf(0f)
        internal set
    var worstFrameMs by mutableFloatStateOf(0f)
        internal set
    internal var isMoving by mutableStateOf(false)

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

    var sc0 = 1.0f
    while (sc0 >= 0.749f) {
        val r0 = fit(a0, sc0, 4)
        if (r0.ok) return r0
        sc0 -= 0.02f
    }

    val r1 = fit(a0, 1.0f, 2)
    if (r1.ok) return r1

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

    return fit(a0, 0.45f, 2)
}

/**
 * Adaptive 3D folder stack ported from design/CRATE_SPEC.md sections 3, 4 & 5
 * and design/crate_adaptive.html.
 *
 * Moving [state.pos] is strictly draw/layer-phase only: [pos] is never read in
 * composition, only inside `graphicsLayer {}`, `drawWithContent {}`, and `Canvas {}`.
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
    val posProvider = remember(state) { { state.pos } }

    // Frame-time telemetry while dragging
    LaunchedEffect(state.isDragging) {
        if (!state.isDragging) return@LaunchedEffect
        state.isMoving = true
        state.moveRecomps = 0
        var lastNanos = 0L
        var sumMs = 0f
        var count = 0
        var worst = 0f
        while (state.isDragging) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val ms = (now - lastNanos) / 1_000_000f
                    sumMs += ms
                    count++
                    if (ms > worst) worst = ms
                    state.avgFrameMs = sumMs / count
                    state.worstFrameMs = worst
                }
                lastNanos = now
            }
        }
    }

    // Spring animation loop + frame-time telemetry: runs only while settling
    LaunchedEffect(state.isDragging, state.target, maxIndex) {
        if (state.isDragging) return@LaunchedEffect
        val needsSpring = abs(state.target - state.pos) > 0.0005f || abs(state.vel) > 0.0005f
        if (!needsSpring) {
            state.isMoving = false
            return@LaunchedEffect
        }
        val wasDragging = state.isMoving
        state.isMoving = true
        if (!wasDragging) {
            state.moveRecomps = 0
        }
        var lastNanos = 0L
        var sumMs = 0f
        var count = 0
        var worst = if (wasDragging) state.worstFrameMs else 0f
        while (abs(state.target - state.pos) > 0.0005f || abs(state.vel) > 0.0005f) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val ms = (now - lastNanos) / 1_000_000f
                    sumMs += ms
                    count++
                    if (ms > worst) worst = ms
                    state.avgFrameMs = sumMs / count
                    state.worstFrameMs = worst

                    val dtFrames = (ms / (1000f / 60f)).coerceIn(0.1f, 3f)
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
        state.isMoving = false
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(perFolderPx, maxIndex) {
                    val velocityTracker = VelocityTracker()
                    var totalDragY = 0f

                    detectVerticalDragGestures(
                        onDragStart = {
                            state.isDragging = true
                            state.vel = 0f
                            totalDragY = 0f
                            velocityTracker.resetTracking()
                        },
                        onDragEnd = {
                            state.isDragging = false
                            val velPxPerSec = velocityTracker.calculateVelocity().y
                            val velFoldersPerSec = velPxPerSec / perFolderPx
                            val projected = state.pos + velFoldersPerSec * 0.24f
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
                            totalDragY += dragAmount
                            velocityTracker.addPosition(change.uptimeMillis, Offset(0f, totalDragY))
                            val nextPos = (state.pos + dragAmount / perFolderPx)
                                .coerceIn(-0.3f, maxIndex + 0.3f)
                            state.pos = nextPos
                            state.target = nextPos
                        },
                    )
                },
        ) {
            // Static back-to-front order (nearest folder always on top, zero zIndex recomposition)
            for (i in folders.lastIndex downTo 0) {
                val folder = folders[i]
                val xoBase = ORG_X[i % ORG_X.size] * kWidth
                val rzBase = ORG_R[i % ORG_R.size]
                val staticZ = (folders.size - i).toFloat()

                Box(
                    modifier = Modifier
                        .offset(x = fx.dp, y = fy.dp)
                        .zIndex(staticZ)
                        .graphicsLayer {
                            val d = i - posProvider()
                            val vis = d > -1.7f && d < g.behindCount + 1.4f
                            if (!vis) {
                                alpha = 0f
                                return@graphicsLayer
                            }
                            val ease = if (d > 0f) minOf(1f, d * 1.1f) else 0f
                            val y: Float
                            val z: Float
                            val rx: Float
                            val o: Float
                            if (d >= 0f) {
                                y = -d * g.riseDp
                                z = -d * g.stepZ
                                rx = 5f
                                o = ((g.behindCount + 0.5f - d) / 0.5f).coerceIn(0f, 1f)
                            } else {
                                y = -d * passY
                                z = -d * g.stepZ * 1.9f
                                rx = d * 30f
                                o = maxOf(0f, 1f + d * 1.05f)
                            }
                            val scl = (g.perspectiveP / (g.perspectiveP - z)).coerceAtLeast(0.05f)
                            val projX = (xoBase * ease) * scl
                            val projY = oy + (y - oy) * scl

                            translationX = projX * density
                            translationY = projY * density
                            scaleX = scl
                            scaleY = scl
                            rotationX = rx
                            rotationZ = rzBase * ease
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            alpha = o
                            cameraDistance = maxOf(8f, g.perspectiveP * 0.05f) * density
                        }
                        .drawWithContent {
                            val d = i - posProvider()
                            if (d > -1.7f && d < g.behindCount + 1.4f) {
                                drawContent()
                            }
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
                        darkenAlphaProvider = {
                            val d = i - posProvider()
                            if (d <= 0.02f) {
                                0f
                            } else {
                                var br = maxOf(0.5f, 1f - d * 0.08f)
                                if (d > lim) {
                                    br *= maxOf(0.2f, 1f - (d - lim) * 0.32f)
                                }
                                (1f - br).coerceIn(0f, 0.9f)
                            }
                        },
                        detailVisibleProvider = {
                            (i - posProvider()) <= 2.5f
                        },
                        previewVideos = previewsForFolder(folder),
                        onOpenFolder = {
                            val front = posProvider().roundToInt().coerceIn(0, maxIndex)
                            if (i == front) {
                                onOpenFolder(i)
                            } else {
                                state.animateTo(i, maxIndex)
                            }
                        },
                        onTabClick = {
                            if (abs(i - posProvider()) > 0.4f) {
                                state.animateTo(i, maxIndex)
                            }
                        },
                        onRecompose = {
                            state.totalCardRecomps++
                            if (state.isMoving) {
                                state.moveRecomps++
                            }
                        },
                    )
                }
            }

            // Right-edge ruler (Canvas draws active tick from posProvider in draw phase)
            if (folders.size >= 2) {
                CrateRuler(
                    totalFolders = folders.size,
                    posProvider = posProvider,
                    maxIndex = maxIndex,
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
    posProvider: () -> Float,
    maxIndex: Int,
    onJumpTo: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = LocalAppAccent.current.color
    val inactiveColor = Color(0xFF4A4231)
    val density = LocalDensity.current.density

    Canvas(
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
        val hDp = size.height / density
        val step = minOf(18f, hDp / (totalFolders - 1)) * density
        val used = step * (totalFolders - 1)
        val y0 = (size.height - used) / 2f
        val activeIndex = posProvider().roundToInt().coerceIn(0, maxIndex)
        val tickH = 2.dp.toPx()
        val r = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        val rightEdge = size.width - 3.dp.toPx()
        val activeW = 15.dp.toPx()
        val inactiveW = 7.dp.toPx()

        for (i in 0 until totalFolders) {
            val isOn = i == activeIndex
            val w = if (isOn) activeW else inactiveW
            drawRoundRect(
                color = if (isOn) accentColor else inactiveColor,
                topLeft = Offset(rightEdge - w, y0 + step * i - tickH / 2f),
                size = Size(w, tickH),
                cornerRadius = r,
            )
        }
    }
}
