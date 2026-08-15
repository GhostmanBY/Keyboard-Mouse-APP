package com.kbdmouse.app.ui

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kbdmouse.app.MainViewModel
import com.kbdmouse.app.key.Hid

private val KEY_H = 36.dp
private val MOD_H = 34.dp

@Composable
fun KeyboardScreen(
    vm: MainViewModel,
    onHome: () -> Unit,
    onKeyboard: () -> Unit,
) {
    var capsOn by rememberSaveable { mutableStateOf(false) }
    val held = remember { mutableStateListOf<Int>() }

    DisposableEffect(Unit) {
        onDispose {
            held.forEach { vm.keyUp(it) }
            held.clear()
        }
    }

    Column(
        Modifier.fillMaxSize().padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ScreenTopBar(onHome = onHome, onRight = onKeyboard, rightIcon = "✋")
        KeyRow(vm, capsOn, held, onCapsToggle = { capsOn = !capsOn }, keys = FN_ROW)
        KeyRow(vm, capsOn, held, onCapsToggle = { capsOn = !capsOn }, keys = TOP_ROW)
        KeyRow(vm, capsOn, held, onCapsToggle = { capsOn = !capsOn }, keys = QWERTY_ROW)
        KeyRow(vm, capsOn, held, onCapsToggle = { capsOn = !capsOn }, keys = HOME_ROW)
        KeyRow(vm, capsOn, held, onCapsToggle = { capsOn = !capsOn }, keys = BOTTOM_ROW)
        KeyRow(vm, capsOn, held, onCapsToggle = { capsOn = !capsOn }, keys = MOD_ROW)
        KeyRow(vm, capsOn, held, onCapsToggle = { capsOn = !capsOn }, keys = NAV_ROW)
    }
}

private sealed interface KKey {
    val weight: Float

    data class Tap(
        val hid: Int,
        val label: String,
        val shiftLabel: String? = null,
        val altLabel: String? = null,
        val letter: Boolean = false,
        val dead: Boolean = false,
        override val weight: Float = 1f,
    ) : KKey

    data class Repeat(val hid: Int, val label: String, override val weight: Float = 1f) : KKey
    data class Mod(val label: String, val hid: Int, override val weight: Float = 1f) : KKey
    data class Caps(override val weight: Float = 1f) : KKey
    data class Space(override val weight: Float = 3f) : KKey
}

private fun letter(c: Char) = KKey.Tap(
    hid = Hid.A + (c - 'a'),
    label = c.toString(),
    shiftLabel = c.uppercase(),
    letter = true,
)

private fun tap(
    label: String,
    hid: Int,
    shiftLabel: String? = null,
    altLabel: String? = null,
    letter: Boolean = false,
    dead: Boolean = false,
    weight: Float = 1f,
) = KKey.Tap(label = label, hid = hid, shiftLabel = shiftLabel, altLabel = altLabel, letter = letter, dead = dead, weight = weight)

private val FN_ROW: List<KKey> =
    listOf(tap("Esc", Hid.ESC)) +
        (Hid.F1..Hid.F12).map { tap("F${it - Hid.F1 + 1}", it) }

private val TOP_ROW: List<KKey> = listOf(
    tap("|", Hid.GRAVE, shiftLabel = "°", altLabel = "¬"),
    tap("1", Hid.DIGIT_1, shiftLabel = "!"),
    tap("2", Hid.DIGIT_2, shiftLabel = "\""),
    tap("3", Hid.DIGIT_3, shiftLabel = "#"),
    tap("4", Hid.DIGIT_4, shiftLabel = "$"),
    tap("5", Hid.DIGIT_5, shiftLabel = "%"),
    tap("6", Hid.DIGIT_6, shiftLabel = "&"),
    tap("7", Hid.DIGIT_7, shiftLabel = "/"),
    tap("8", Hid.DIGIT_8, shiftLabel = "("),
    tap("9", Hid.DIGIT_9, shiftLabel = ")"),
    tap("0", Hid.DIGIT_0, shiftLabel = "="),
    tap("?", Hid.LEFT_BRACKET, shiftLabel = "'", altLabel = "\\"),
    tap("¿", Hid.RIGHT_BRACKET, shiftLabel = "¡"),
    KKey.Repeat(Hid.BACKSPACE, "⌫", weight = 1.4f),
)

private val QWERTY_ROW: List<KKey> = listOf(
    tap("Tab", Hid.TAB, weight = 1.2f),
) + listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p').map { letter(it) } + listOf(
    tap("´", Hid.SEMICOLON, shiftLabel = "¨", dead = true),
    tap("+", Hid.EQUAL, shiftLabel = "*", altLabel = "~"),
    tap("⏎", Hid.ENTER, weight = 1.4f),
)

private val HOME_ROW: List<KKey> = listOf(
    KKey.Caps(weight = 1.4f),
) + listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l').map { letter(it) } + listOf(
    tap("ñ", Hid.GRAVE, shiftLabel = "Ñ", letter = true),
    tap("[", Hid.QUOTE, shiftLabel = "{", altLabel = "^"),
    tap("]", Hid.SLASH, shiftLabel = "}"),
    tap("⏎", Hid.ENTER, weight = 1.4f),
)

private val BOTTOM_ROW: List<KKey> = listOf(
    KKey.Mod("Shift", Hid.LEFT_SHIFT, weight = 1.5f),
    tap("<", Hid.ISO_102, shiftLabel = ">"),
) + listOf('z', 'x', 'c', 'v', 'b', 'n', 'm').map { letter(it) } + listOf(
    tap(",", Hid.COMMA, shiftLabel = ";"),
    tap(".", Hid.PERIOD, shiftLabel = ":"),
    tap("-", Hid.MINUS, shiftLabel = "_"),
    KKey.Mod("Shift", Hid.LEFT_SHIFT, weight = 1.5f),
)

private val MOD_ROW: List<KKey> = listOf(
    KKey.Mod("Ctrl", Hid.LEFT_CTRL),
    KKey.Mod("Win", Hid.LEFT_SUPER),
    KKey.Mod("Alt", Hid.LEFT_ALT),
    KKey.Space(weight = 4f),
    KKey.Mod("Alt", Hid.LEFT_ALT),
    KKey.Mod("Win", Hid.LEFT_SUPER),
    KKey.Mod("Ctrl", Hid.LEFT_CTRL),
)

private val NAV_ROW: List<KKey> = listOf(
    tap("←", Hid.LEFT_ARROW),
    tap("↓", Hid.DOWN_ARROW),
    tap("↑", Hid.UP_ARROW),
    tap("→", Hid.RIGHT_ARROW),
    tap("Inicio", Hid.HOME),
    tap("Fin", Hid.END),
    tap("RePág", Hid.PAGE_UP),
    tap("AvPág", Hid.PAGE_DOWN),
    tap("Supr", Hid.DELETE, weight = 1.2f),
)

@Composable
private fun KeyRow(
    vm: MainViewModel,
    capsOn: Boolean,
    held: MutableList<Int>,
    onCapsToggle: () -> Unit,
    keys: List<KKey>,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (key in keys) {
            when (key) {
                is KKey.Space -> SpaceKey(vm, Modifier.weight(key.weight))
                is KKey.Caps -> CapsKey(capsOn, onCapsToggle, Modifier.weight(key.weight))
                is KKey.Mod -> ModKey(key.label, key.hid, vm, held, Modifier.weight(key.weight))
                else -> TapKey(key, vm, capsOn, held, Modifier.weight(key.weight))
            }
        }
    }
}

@Composable
private fun TapKey(
    key: KKey,
    vm: MainViewModel,
    capsOn: Boolean,
    held: List<Int>,
    modifier: Modifier = Modifier,
) {
    val currentCaps by rememberUpdatedState(capsOn)

    val pressModifier = when (key) {
        is KKey.Tap -> Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    val shiftDown = held.contains(Hid.LEFT_SHIFT) || held.contains(Hid.RIGHT_SHIFT)
                    val useShift = key.shiftLabel != null && (shiftDown || (currentCaps && key.letter))
                    if (useShift) vm.tapWithModifiers(key.hid, shift = true) else vm.tapKey(key.hid)
                },
                onLongPress = {
                    if (key.altLabel != null) vm.tapWithModifiers(key.hid, altGr = true)
                },
            )
        }
        is KKey.Repeat -> Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                vm.tapKey(key.hid)
                var last = SystemClock.uptimeMillis()
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.changes.all { !it.pressed }) break
                    val now = SystemClock.uptimeMillis()
                    if (now - last >= 60) {
                        vm.tapKey(key.hid)
                        last = now
                    }
                    event.changes.forEach { it.consume() }
                }
            }
        }
        else -> Modifier
    }

    val shape = RoundedCornerShape(6.dp)
    val label = when (key) {
        is KKey.Tap -> if (key.letter && currentCaps) key.shiftLabel ?: key.label else key.label
        is KKey.Repeat -> key.label
        else -> ""
    }
    val bg = when (key) {
        is KKey.Tap if key.dead -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val fg = when (key) {
        is KKey.Tap if key.dead -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier
            .height(KEY_H)
            .clip(shape)
            .background(bg)
            .then(pressModifier),
        contentAlignment = Alignment.Center,
    ) {
        if (key is KKey.Tap && key.altLabel != null) {
            Text(
                key.altLabel,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 3.dp, top = 1.dp),
                color = MaterialTheme.colorScheme.outline,
            )
        }
        if (key is KKey.Tap && key.shiftLabel != null && !(key.letter && currentCaps)) {
            Text(
                key.shiftLabel,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 3.dp, top = 1.dp),
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ModKey(
    label: String,
    hid: Int,
    vm: MainViewModel,
    held: MutableList<Int>,
    modifier: Modifier = Modifier,
) {
    val active = held.contains(hid)
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier
            .height(MOD_H)
            .clip(shape)
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
            )
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    if (hid in held) {
                        tryAwaitRelease()
                    } else {
                        vm.keyDown(hid)
                        held.add(hid)
                        tryAwaitRelease()
                        vm.keyUp(hid)
                        held.remove(hid)
                    }
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (active) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CapsKey(
    capsOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier
            .height(KEY_H)
            .clip(shape)
            .background(
                if (capsOn) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onToggle() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (capsOn) "CAPS" else "caps",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (capsOn) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SpaceKey(
    vm: MainViewModel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(KEY_H)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { vm.tapKey(Hid.SPACE) })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "␣",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
