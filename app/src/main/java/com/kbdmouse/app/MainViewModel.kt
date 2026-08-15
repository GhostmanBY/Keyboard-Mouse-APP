package com.kbdmouse.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.kbdmouse.app.key.Hid
import com.kbdmouse.app.key.LatAm
import com.kbdmouse.app.net.ConnectionManager
import com.kbdmouse.app.net.ConnectionState
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val state: StateFlow<ConnectionState> = ConnectionManager.state

    var host by mutableStateOf(ConnectionManager.lastHost)
    var port by mutableStateOf(ConnectionManager.lastPort.toString())

    val isConnected: Boolean
        get() = state.value is ConnectionState.Connected

    val scrollSensitivity: Float
        get() = ConnectionManager.scrollSensitivity

    fun updateScrollSensitivity(v: Float) = ConnectionManager.updateScrollSensitivity(v)

    fun toggleConnection() {
        if (isConnected) {
            ConnectionManager.disconnect()
        } else {
            val p = port.toIntOrNull() ?: 8765
            ConnectionManager.lastHost = host.trim()
            ConnectionManager.lastPort = p
            ConnectionManager.connect(host.trim(), p)
        }
    }

    fun disconnect() = ConnectionManager.disconnect()

    fun keyDown(hid: Int) = ConnectionManager.sendKey(hid, down = true)

    fun keyUp(hid: Int) = ConnectionManager.sendKey(hid, down = false)

    fun tapKey(hid: Int) {
        keyDown(hid)
        keyUp(hid)
    }

    fun tapWithModifiers(hid: Int, shift: Boolean = false, altGr: Boolean = false) {
        if (shift) keyDown(Hid.LEFT_SHIFT)
        if (altGr) {
            keyDown(Hid.LEFT_CTRL)
            keyDown(Hid.RIGHT_ALT)
        }
        tapKey(hid)
        if (altGr) {
            keyUp(Hid.RIGHT_ALT)
            keyUp(Hid.LEFT_CTRL)
        }
        if (shift) keyUp(Hid.LEFT_SHIFT)
    }

    fun typeChar(c: Char) {
        LatAm.map(c)?.forEach { k ->
            tapWithModifiers(k.hid, shift = k.shift, altGr = k.altGr)
        }
    }

    fun releaseAllModifiers() {
        Hid.modifiers.forEach { keyUp(it) }
    }

    fun mouseMove(dx: Int, dy: Int) = ConnectionManager.sendMouseMove(dx, dy)

    fun mouseBtn(btn: Int, state: Int) = ConnectionManager.sendMouseBtn(btn, state)

    fun scroll(dx: Int, dy: Int) = ConnectionManager.sendScroll(dx, dy)
}
