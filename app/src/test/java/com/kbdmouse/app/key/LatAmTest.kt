package com.kbdmouse.app.key

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LatAmTest {

    private fun single(c: Char): HidKey = LatAm.map(c)!!.single()
    private fun seq(c: Char): List<HidKey> = LatAm.map(c)!!

    @Test
    fun `letters and uppercase`() {
        assertEquals(HidKey(Hid.A), single('a'))
        assertEquals(HidKey(Hid.A, shift = true), single('A'))
        assertEquals(HidKey(Hid.Z), single('z'))
        assertEquals(HidKey(Hid.Z, shift = true), single('Z'))
    }

    @Test
    fun `ñ is the grave key and Ñ is shift`() {
        assertEquals(HidKey(Hid.GRAVE), single('ñ'))
        assertEquals(HidKey(Hid.GRAVE, shift = true), single('Ñ'))
    }

    @Test
    fun `digits and their shifted symbols`() {
        assertEquals(HidKey(Hid.DIGIT_1), single('1'))
        assertEquals(HidKey(Hid.DIGIT_1, shift = true), single('!'))
        assertEquals(HidKey(Hid.DIGIT_2, shift = true), single('"'))
        assertEquals(HidKey(Hid.DIGIT_7, shift = true), single('/'))
        assertEquals(HidKey(Hid.DIGIT_0), single('0'))
        assertEquals(HidKey(Hid.DIGIT_0, shift = true), single('='))
    }

    @Test
    fun `question and apostrophe swap positions`() {
        assertEquals(HidKey(Hid.LEFT_BRACKET), single('?'))
        assertEquals(HidKey(Hid.LEFT_BRACKET, shift = true), single('\''))
    }

    @Test
    fun `inverted marks`() {
        assertEquals(HidKey(Hid.RIGHT_BRACKET), single('¿'))
        assertEquals(HidKey(Hid.RIGHT_BRACKET, shift = true), single('¡'))
    }

    @Test
    fun `colon is shift plus period`() {
        assertEquals(HidKey(Hid.PERIOD, shift = true), single(':'))
        assertEquals(HidKey(Hid.COMMA, shift = true), single(';'))
    }

    @Test
    fun `brackets on home row`() {
        assertEquals(HidKey(Hid.QUOTE), single('['))
        assertEquals(HidKey(Hid.QUOTE, shift = true), single('{'))
        assertEquals(HidKey(Hid.SLASH), single(']'))
        assertEquals(HidKey(Hid.SLASH, shift = true), single('}'))
    }

    @Test
    fun `pipe and degree`() {
        assertEquals(HidKey(Hid.BACKSLASH), single('|'))
        assertEquals(HidKey(Hid.BACKSLASH, shift = true), single('°'))
    }

    @Test
    fun `iso key for angle brackets`() {
        assertEquals(HidKey(Hid.ISO_102), single('<'))
        assertEquals(HidKey(Hid.ISO_102, shift = true), single('>'))
    }

    @Test
    fun `altgr symbols`() {
        assertEquals(HidKey(Hid.Q, altGr = true), single('@'))
        assertEquals(HidKey(Hid.EQUAL, altGr = true), single('~'))
        assertEquals(HidKey(Hid.QUOTE, altGr = true), single('^'))
        assertEquals(HidKey(Hid.LEFT_BRACKET, altGr = true), single('\\'))
        assertEquals(HidKey(Hid.BACKSLASH, altGr = true), single('¬'))
    }

    @Test
    fun `dead keys`() {
        assertEquals(HidKey(Hid.SEMICOLON), single('´'))
        assertEquals(HidKey(Hid.SEMICOLON, shift = true), single('¨'))
    }

    @Test
    fun `accented vowels use dead key plus vowel`() {
        assertEquals(listOf(HidKey(Hid.SEMICOLON), HidKey(Hid.A)), seq('á'))
        assertEquals(listOf(HidKey(Hid.SEMICOLON), HidKey(Hid.A, shift = true)), seq('Á'))
        assertEquals(listOf(HidKey(Hid.SEMICOLON), HidKey(Hid.O)), seq('ó'))
        assertEquals(listOf(HidKey(Hid.SEMICOLON, shift = true), HidKey(Hid.U)), seq('ü'))
        assertEquals(listOf(HidKey(Hid.SEMICOLON, shift = true), HidKey(Hid.I, shift = true)), seq('Ï'))
    }

    @Test
    fun `space enter tab`() {
        assertEquals(HidKey(Hid.SPACE), single(' '))
        assertEquals(HidKey(Hid.ENTER), single('\n'))
        assertEquals(HidKey(Hid.TAB), single('\t'))
    }

    @Test
    fun `unsupported chars return null`() {
        assertNull(LatAm.map('€'))
        assertNull(LatAm.map('ç'))
        assertNull(LatAm.map('ə'))
    }
}
