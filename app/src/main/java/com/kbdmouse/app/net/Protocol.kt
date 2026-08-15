package com.kbdmouse.app.net

import java.io.EOFException

/**
 * Protocolo binario TCP entre el cliente (teléfono) y el servidor.
 *
 * Frame: magic "KM"(2) | version(1) | type(1) | length BE(4) | payload
 * Espejo de kbdmouse/protocol.py del repositorio del servidor.
 * Todos los enteros son big-endian.
 */
object MsgType {
    const val HELLO = 0x01        // cliente -> servidor: handshake
    const val HELLO_ACK = 0x02    // servidor -> cliente: confirmación de versión
    const val KEY_DOWN = 0x10     // tecla presionada (HID usage, 1 byte)
    const val KEY_UP = 0x11       // tecla liberada (HID usage, 1 byte)
    const val MOUSE_MOVE = 0x20   // deltas relativos (dx int16, dy int16)
    const val MOUSE_BTN = 0x21    // botón (btn 1 byte, state 1 byte)
    const val SCROLL = 0x22       // (dx int16, dy int16)
    const val PING = 0x30         // servidor -> cliente (seq u32 + t0 u64 ms)
    const val PONG = 0x31         // cliente -> servidor (eco del payload)
    const val DISCONNECT = 0x40   // aviso ordenado de cierre
}

object MouseBtn {
    const val LEFT = 0
    const val RIGHT = 1
    const val MIDDLE = 2
}

object BtnState {
    const val UP = 0
    const val DOWN = 1
}

const val PROTO_MAGIC = "KM"
const val PROTO_VERSION = 0x01
const val PROTO_HEADER_SIZE = 8
const val PROTO_MAX_PAYLOAD = 65535

class ProtocolException(message: String) : Exception(message)

data class Frame(val type: Int, val payload: ByteArray) {
    override fun equals(other: Any?): Boolean =
        other is Frame && other.type == type && other.payload.contentEquals(payload)

    override fun hashCode(): Int = type * 31 + payload.contentHashCode()
}

object Protocol {

    fun buildFrame(type: Int, payload: ByteArray = ByteArray(0)): ByteArray {
        require(payload.size <= PROTO_MAX_PAYLOAD) {
            "payload demasiado grande: ${payload.size}"
        }
        return ByteArray(PROTO_HEADER_SIZE + payload.size).also { out ->
            out[0] = 'K'.code.toByte()
            out[1] = 'M'.code.toByte()
            out[2] = PROTO_VERSION.toByte()
            out[3] = type.toByte()
            val len = payload.size
            out[4] = (len ushr 24).toByte()
            out[5] = (len ushr 16).toByte()
            out[6] = (len ushr 8).toByte()
            out[7] = len.toByte()
            payload.copyInto(out, PROTO_HEADER_SIZE)
        }
    }

    fun parseFrame(data: ByteArray): Frame? {
        if (data.size < PROTO_HEADER_SIZE) return null
        if (data[0] != 'K'.code.toByte() || data[1] != 'M'.code.toByte()) {
            throw ProtocolException("magic inválido")
        }
        val version = data[2].toInt() and 0xFF
        if (version != PROTO_VERSION) {
            throw ProtocolException("versión de protocolo no soportada: $version")
        }
        val type = data[3].toInt() and 0xFF
        val length = u32(data, 4)
        if (length > PROTO_MAX_PAYLOAD) {
            throw ProtocolException("largo de payload inválido: $length")
        }
        if (data.size < PROTO_HEADER_SIZE + length) return null
        return Frame(type, data.copyOfRange(PROTO_HEADER_SIZE, PROTO_HEADER_SIZE + length))
    }

    fun hello(clientVersion: Int = 1): ByteArray =
        buildFrame(MsgType.HELLO, byteArrayOf(clientVersion.toByte()))

    fun keyDown(hid: Int): ByteArray = buildFrame(MsgType.KEY_DOWN, byteArrayOf(hid.toByte()))

    fun keyUp(hid: Int): ByteArray = buildFrame(MsgType.KEY_UP, byteArrayOf(hid.toByte()))

    fun mouseMove(dx: Int, dy: Int): ByteArray =
        buildFrame(MsgType.MOUSE_MOVE, int16Pair(clamp16(dx), clamp16(dy)))

    fun mouseBtn(btn: Int, state: Int): ByteArray =
        buildFrame(MsgType.MOUSE_BTN, byteArrayOf(btn.toByte(), state.toByte()))

    fun scroll(dx: Int, dy: Int): ByteArray =
        buildFrame(MsgType.SCROLL, int16Pair(clamp16(dx), clamp16(dy)))

    /** PONG: eco exacto del payload del PING (seq u32 + t0 u64). */
    fun pongEcho(pingPayload: ByteArray): ByteArray =
        buildFrame(MsgType.PONG, pingPayload)

    fun pingPayload(seq: Long, t0Ms: Long): ByteArray = ByteArray(12).apply {
        this[0] = (seq ushr 24).toByte()
        this[1] = (seq ushr 16).toByte()
        this[2] = (seq ushr 8).toByte()
        this[3] = seq.toByte()
        var i = 4
        for (shift in 56 downTo 0 step 8) {
            this[i++] = (t0Ms ushr shift).toByte()
        }
    }

    /** Devuelve (seq, t0_ms) de un payload de PING/PONG. */
    fun parsePingPong(payload: ByteArray): Pair<Long, Long> {
        require(payload.size == 12) { "payload de ping/pong inválido" }
        val seq = ((payload[0].toLong() and 0xFF) shl 24) or
            ((payload[1].toLong() and 0xFF) shl 16) or
            ((payload[2].toLong() and 0xFF) shl 8) or
            (payload[3].toLong() and 0xFF)
        var t0 = 0L
        for (i in 4 until 12) {
            t0 = (t0 shl 8) or (payload[i].toLong() and 0xFF)
        }
        return seq to t0
    }

    fun disconnect(): ByteArray = buildFrame(MsgType.DISCONNECT)

    private fun clamp16(v: Int): Int = maxOf(-32768, minOf(32767, v))

    private fun int16Pair(a: Int, b: Int): ByteArray = ByteArray(4).apply {
        this[0] = (a ushr 8).toByte()
        this[1] = a.toByte()
        this[2] = (b ushr 8).toByte()
        this[3] = b.toByte()
    }

    private fun u32(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 24) or
            ((data[offset + 1].toInt() and 0xFF) shl 16) or
            ((data[offset + 2].toInt() and 0xFF) shl 8) or
            (data[offset + 3].toInt() and 0xFF)
}

/**
 * Acumula bytes de red y entrega frames completos. Si encuentra un frame
 * corrupto, se resincroniza buscando el siguiente magic "KM".
 */
class FrameBuffer {
    private var data = ByteArray(0)

    fun feed(chunk: ByteArray): List<Frame> {
        data = data + chunk
        val frames = mutableListOf<Frame>()
        while (true) {
            val frame = try {
                Protocol.parseFrame(data)
            } catch (e: ProtocolException) {
                val idx = indexOfMagic(data, 1)
                if (idx > 0) {
                    data = data.copyOfRange(idx, data.size)
                    continue
                }
                data = ByteArray(0)
                throw e
            } ?: break
            frames.add(frame)
            data = data.copyOfRange(PROTO_HEADER_SIZE + frame.payload.size, data.size)
        }
        return frames
    }

    private fun indexOfMagic(bytes: ByteArray, from: Int): Int {
        var i = from
        while (i + 1 < bytes.size) {
            if (bytes[i] == 'K'.code.toByte() && bytes[i + 1] == 'M'.code.toByte()) return i
            i++
        }
        return -1
    }
}

/** Utilidades de red. */
internal object Net {
    /** Lee un frame completo desde el stream o lanza EOFException. */
    fun readFrame(input: java.io.InputStream): ByteArray {
        val header = ByteArray(PROTO_HEADER_SIZE)
        readFully(input, header)
        val length = ((header[4].toInt() and 0xFF) shl 24) or
            ((header[5].toInt() and 0xFF) shl 16) or
            ((header[6].toInt() and 0xFF) shl 8) or
            (header[7].toInt() and 0xFF)
        if (length > PROTO_MAX_PAYLOAD) throw ProtocolException("largo de payload inválido: $length")
        val payload = ByteArray(length)
        readFully(input, payload)
        return header + payload
    }

    private fun readFully(input: java.io.InputStream, out: ByteArray) {
        var read = 0
        while (read < out.size) {
            val n = input.read(out, read, out.size - read)
            if (n < 0) throw EOFException("EOF del servidor")
            read += n
        }
    }
}
