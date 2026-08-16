package com.kbdmouse.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kbdmouse.app.MainViewModel
import com.kbdmouse.app.net.BtnState
import com.kbdmouse.app.net.MouseBtn
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TrackpadScreen(
    vm: MainViewModel,
    onHome: () -> Unit,
    onKeyboard: () -> Unit,
) {
    val connected = vm.isConnected
    val scrollSensitivity = vm.scrollSensitivity
    val currentScroll by rememberUpdatedState(scrollSensitivity)

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp)) {
        ScreenTopBar(onHome = onHome, onRight = onKeyboard, rightIcon = "⌨")

        val trackpadShape = RoundedCornerShape(20.dp)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(trackpadShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, trackpadShape)
                .pointerInput(connected) {
                    if (!connected) return@pointerInput
                    awaitEachGesture {
                        var lastCentroid: Offset? = null
                        var prevCount = 0
                        var downFingers = 0
                        var moved = false
                        var accX = 0f
                        var accY = 0f
                        var scrollX = 0f
                        var scrollY = 0f

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val pressed = event.changes.filter { it.pressed }
                            val count = pressed.size

                            if (prevCount == 0 && count > 0) {
                                downFingers = count
                                moved = false
                            }

                            when (count) {
                                1 -> {
                                    val delta = pressed[0].position - pressed[0].previousPosition
                                    if (delta != Offset.Zero) {
                                        if (abs(delta.x) + abs(delta.y) > MOVE_THRESHOLD) moved = true
                                        accX += delta.x
                                        accY += delta.y
                                        val ix = accX.roundToInt()
                                        val iy = accY.roundToInt()
                                        if (ix != 0 || iy != 0) {
                                            vm.mouseMove(ix, iy)
                                            accX -= ix
                                            accY -= iy
                                        }
                                    }
                                    lastCentroid = null
                                }
                                2 -> {
                                    val centroid = pressed.map { it.position }
                                        .reduce(Offset::plus) / 2f
                                    val prev = lastCentroid
                                    if (prev != null) {
                                        val dx = centroid.x - prev.x
                                        val dy = centroid.y - prev.y
                                        if (abs(dx) + abs(dy) > MOVE_THRESHOLD) moved = true
                                        scrollX += dx * currentScroll
                                        scrollY += dy * currentScroll
                                        val sx = scrollX.roundToInt()
                                        val sy = scrollY.roundToInt()
                                        if (sx != 0 || sy != 0) {
                                            vm.scroll(sx, sy)
                                            scrollX -= sx
                                            scrollY -= sy
                                        }
                                    }
                                    lastCentroid = centroid
                                }
                                else -> lastCentroid = null
                            }

                            if (count == 0 && prevCount > 0) {
                                if (!moved) {
                                    if (downFingers >= 2) {
                                        vm.mouseBtn(MouseBtn.RIGHT, BtnState.DOWN)
                                        vm.mouseBtn(MouseBtn.RIGHT, BtnState.UP)
                                    } else {
                                        vm.mouseBtn(MouseBtn.LEFT, BtnState.DOWN)
                                        vm.mouseBtn(MouseBtn.LEFT, BtnState.UP)
                                    }
                                }
                                accX = 0f
                                accY = 0f
                                scrollX = 0f
                                scrollY = 0f
                            }

                            prevCount = count
                            event.changes.forEach { it.consume() }
                        }
                    }
                },
        ) {
            Text(
                if (connected) {
                    "Arrastrá = mover · Tap = clic izq\n2 dedos: tap = clic der · arrastre = scroll"
                } else {
                    "Conectate al servidor para usar el trackpad"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MouseButton("Izquierdo", MouseBtn.LEFT, vm, Modifier.weight(1f))
            MouseButton("Medio", MouseBtn.MIDDLE, vm, Modifier.weight(1f))
            MouseButton("Derecho", MouseBtn.RIGHT, vm, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MouseButton(
    label: String,
    btn: Int,
    vm: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = {
            vm.mouseBtn(btn, BtnState.DOWN)
            vm.mouseBtn(btn, BtnState.UP)
        },
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(48.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private const val MOVE_THRESHOLD = 12f

