package com.kbdmouse.app.net

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val host: String, val port: Int) : ConnectionState
    data class Reconnecting(val attempt: Int, val delayMs: Long) : ConnectionState
}

/** Gestiona la conexión al servidor con auto-reconexión y backoff. */
object ConnectionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private var client: TcpClient? = null
    private var reconnectJob: Job? = null
    private var userRequested = false
    private var attempt = 0
    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("kbdmouse", Context.MODE_PRIVATE)
            scrollSensitivity = prefs?.getFloat("scroll_sensitivity", 0.3f) ?: 0.3f
        }
    }

    var scrollSensitivity by mutableStateOf(0.3f)

    fun updateScrollSensitivity(v: Float) {
        scrollSensitivity = v
        prefs?.edit()?.putFloat("scroll_sensitivity", v)?.apply()
    }

    var lastHost: String
        get() = prefs?.getString("host", "") ?: ""
        set(value) {
            prefs?.edit()?.putString("host", value)?.apply()
        }

    var lastPort: Int
        get() = prefs?.getInt("port", 8765) ?: 8765
        set(value) {
            prefs?.edit()?.putInt("port", value)?.apply()
        }

    fun connect(host: String, port: Int) {
        userRequested = true
        reconnectJob?.cancel()
        attempt = 0
        connectOnce(host, port)
    }

    fun disconnect() {
        userRequested = false
        reconnectJob?.cancel()
        client?.close()
        client = null
        _state.value = ConnectionState.Disconnected
    }

    fun sendKey(hid: Int, down: Boolean) {
        client?.send(if (down) Protocol.keyDown(hid) else Protocol.keyUp(hid))
    }

    fun sendMouseMove(dx: Int, dy: Int) {
        client?.send(Protocol.mouseMove(dx, dy))
    }

    fun sendMouseBtn(btn: Int, state: Int) {
        client?.send(Protocol.mouseBtn(btn, state))
    }

    fun sendScroll(dx: Int, dy: Int) {
        client?.send(Protocol.scroll(dx, dy))
    }

    private fun connectOnce(host: String, port: Int) {
        client?.close()
        _state.value = ConnectionState.Connecting
        val c = TcpClient(
            onConnected = {
                _state.value = ConnectionState.Connected(host, port)
                attempt = 0
            },
            onClosed = {
                if (userRequested) scheduleReconnect(host, port) else _state.value = ConnectionState.Disconnected
            },
            onProtocolError = {
                if (userRequested) scheduleReconnect(host, port)
            },
        )
        client = c
        c.connect(host, port)
    }

    private fun scheduleReconnect(host: String, port: Int) {
        if (!userRequested) return
        reconnectJob?.cancel()
        val delayMs = minOf(2000L * (1 shl attempt), 30_000L)
        attempt++
        _state.value = ConnectionState.Reconnecting(attempt, delayMs)
        reconnectJob = scope.launch {
            delay(delayMs)
            if (userRequested) connectOnce(host, port)
        }
    }
}
