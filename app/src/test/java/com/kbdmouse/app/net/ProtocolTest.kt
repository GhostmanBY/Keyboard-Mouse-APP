package com.kbdmouse.app.net

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolTest {

    private fun hex(bytes: ByteArray): String =
        bytes.joinToString(" ") { "%02X".format(it) }

    @Test
    fun `hello frame coincides with server vector`() {
        val expected = byteArrayOf(
            0x4B, 0x4D, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01,
        )
        assertArrayEquals(expected, Protocol.hello(0x01))
    }

    @Test
    fun `mouse move coincides with server vector`() {
        // 4B 4D 01 20 00 00 00 04 00 64 FF CE
        val expected = byteArrayOf(
            0x4B, 0x4D, 0x01, 0x20, 0x00, 0x00, 0x00, 0x04, 0x00, 0x64, 0xFF.toByte(), 0xCE.toByte(),
        )
        assertArrayEquals(expected, Protocol.mouseMove(100, -50))
    }

    @Test
    fun `parse frame round trip`() {
        val frame = Protocol.mouseMove(100, -50)
        val parsed = Protocol.parseFrame(frame)
        assertEquals(MsgType.MOUSE_MOVE, parsed!!.type)
        assertArrayEquals(byteArrayOf(0x00, 0x64, 0xFF.toByte(), 0xCE.toByte()), parsed.payload)
    }

    @Test
    fun `mouse move clamps to int16`() {
        val frame = Protocol.mouseMove(100_000, -100_000)
        val parsed = Protocol.parseFrame(frame)!!
        assertEquals(32767, s16(parsed.payload, 0))
        assertEquals(-32768, s16(parsed.payload, 2))
    }

    private fun s16(b: ByteArray, offset: Int): Int {
        val v = ((b[offset].toInt() and 0xFF) shl 8) or (b[offset + 1].toInt() and 0xFF)
        return if (v and 0x8000 != 0) v - 0x10000 else v
    }

    @Test
    fun `key down and up`() {
        assertEquals(0x10, Protocol.keyDown(0x04)[3].toInt())
        assertEquals(0x11, Protocol.keyUp(0x04)[3].toInt())
        assertEquals(0xE1, Protocol.keyDown(0xE1).last().toInt() and 0xFF)
    }

    @Test
    fun `mouse button payload`() {
        val frame = Protocol.mouseBtn(MouseBtn.RIGHT, BtnState.DOWN)
        val parsed = Protocol.parseFrame(frame)!!
        assertEquals(MsgType.MOUSE_BTN, parsed.type)
        assertArrayEquals(byteArrayOf(0x01, 0x01), parsed.payload)
    }

    @Test
    fun `scroll payload is big endian int16`() {
        val parsed = Protocol.parseFrame(Protocol.scroll(1, -2))!!
        assertEquals(MsgType.SCROLL, parsed.type)
        assertArrayEquals(byteArrayOf(0x00, 0x01, 0xFF.toByte(), 0xFE.toByte()), parsed.payload)
    }

    @Test
    fun `ping payload parse round trip`() {
        val payload = Protocol.pingPayload(0xDEADBEEF, 1_700_000_000_000L)
        assertEquals(12, payload.size)
        val (seq, t0) = Protocol.parsePingPong(payload)
        assertEquals(0xDEADBEEFL, seq)
        assertEquals(1_700_000_000_000L, t0)
    }

    @Test
    fun `pong echoes exact ping payload`() {
        val ping = Protocol.buildFrame(MsgType.PING, Protocol.pingPayload(7, 123456L))
        val pingPayload = Protocol.parseFrame(ping)!!.payload
        val pong = Protocol.pongEcho(pingPayload)
        val parsed = Protocol.parseFrame(pong)!!
        assertEquals(MsgType.PONG, parsed.type)
        assertArrayEquals(pingPayload, parsed.payload)
    }

    @Test
    fun `parse frame returns null when incomplete`() {
        val full = Protocol.mouseMove(1, 1)
        assertNull(Protocol.parseFrame(full.copyOfRange(0, 5)))
        assertNull(Protocol.parseFrame(full.copyOfRange(0, 11)))
    }

    @Test
    fun `parse frame rejects bad magic and version`() {
        val badMagic = byteArrayOf(0x41, 0x42, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00)
        assertThrows(ProtocolException::class.java) { Protocol.parseFrame(badMagic) }

        val badVersion = byteArrayOf(0x4B, 0x4D, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00)
        assertThrows(ProtocolException::class.java) { Protocol.parseFrame(badVersion) }
    }

    @Test
    fun `frame buffer handles partial chunks`() {
        val buffer = FrameBuffer()
        val frame = Protocol.mouseMove(100, -50)
        assertTrue(buffer.feed(frame.copyOfRange(0, 5)).isEmpty())
        val frames = buffer.feed(frame.copyOfRange(5, frame.size))
        assertEquals(1, frames.size)
        assertEquals(MsgType.MOUSE_MOVE, frames[0].type)
    }

    @Test
    fun `frame buffer handles multiple frames in one chunk`() {
        val buffer = FrameBuffer()
        val a = Protocol.keyDown(0x04)
        val b = Protocol.keyUp(0x04)
        val frames = buffer.feed(a + b)
        assertEquals(2, frames.size)
        assertEquals(MsgType.KEY_DOWN, frames[0].type)
        assertEquals(MsgType.KEY_UP, frames[1].type)
    }

    @Test
    fun `frame buffer resyncs after corrupt data`() {
        val buffer = FrameBuffer()
        val frame = Protocol.pingPayload(1, 2).let { Protocol.buildFrame(MsgType.PING, it) }
        val garbage = byteArrayOf(0x12, 0x34, 0x56, 0x78)
        val frames = buffer.feed(garbage + frame)
        assertEquals(1, frames.size)
        assertEquals(MsgType.PING, frames[0].type)
    }
}
