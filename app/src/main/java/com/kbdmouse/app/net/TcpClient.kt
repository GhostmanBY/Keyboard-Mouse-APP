package com.kbdmouse.app.net

import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Cliente TCP de baja latencia: Socket crudo con TCP_NODELAY, HELLO al
 * conectar y respuesta inmediata a PING con PONG. Las escrituras se
 * serializan con un Channel para no mezclar frames.
 */
class TcpClient(
    private val onConnected: () -> Unit,
    private val onClosed: (Exception?) -> Unit,
    private val onProtocolError: (String) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val outbox = Channel<ByteArray>(Channel.BUFFERED)
    @Volatile private var socket: Socket? = null
    @Volatile private var closed = false
    @Volatile private var reported = false

    fun connect(host: String, port: Int) {
        scope.launch {
            try {
                val sock = Socket().apply { tcpNoDelay = true }
                sock.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                socket = sock
                closed = false
                reported = false

                sock.getOutputStream().write(Protocol.hello())
                sock.getOutputStream().flush()
                report { onConnected() }

                writerJob(sock)
                readLoop(sock)
            } catch (e: SocketTimeoutException) {
                report { onClosed(e) }
            } catch (e: Exception) {
                report { onClosed(e) }
            }
        }
    }

    fun send(frame: ByteArray) {
        if (!closed) outbox.trySend(frame)
    }

    fun close() {
        closed = true
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        scope.cancel()
    }

    private inline fun report(block: () -> Unit) {
        if (reported) return
        reported = true
        block()
    }

    private fun writerJob(sock: Socket) {
        scope.launch {
            try {
                val out = sock.getOutputStream()
                for (frame in outbox) {
                    out.write(frame)
                    out.flush()
                }
            } catch (e: Exception) {
                if (!closed) report { onClosed(e) }
            }
        }
    }

    private fun readLoop(sock: Socket) {
        val buffer = FrameBuffer()
        val chunk = ByteArray(4096)
        try {
            while (!closed) {
                val n = sock.getInputStream().read(chunk)
                if (n < 0) {
                    report { onClosed(java.io.EOFException("EOF del servidor")) }
                    break
                }
                if (n == 0) continue

                val frames = try {
                    buffer.feed(chunk.copyOf(n))
                } catch (e: ProtocolException) {
                    report { onProtocolError(e.message ?: "frame inválido") }
                    break
                }
                for (frame in frames) handleFrame(frame)
            }
        } catch (e: Exception) {
            if (!closed) report { onClosed(e) }
        }
    }

    private fun handleFrame(frame: Frame) {
        when (frame.type) {
            MsgType.PING -> send(Protocol.pongEcho(frame.payload))
            MsgType.DISCONNECT -> {
                send(Protocol.disconnect())
                close()
            }
            MsgType.HELLO_ACK -> Unit // el estado conectado se marca al establecer TCP
            else -> Unit
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 5000
    }
}
