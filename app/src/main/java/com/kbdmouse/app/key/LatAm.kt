package com.kbdmouse.app.key

/**
 * Pulsación HID: tecla + modificadores necesarios.
 * @param shift requiere Shift (mayúscula o símbolo superior).
 * @param altGr requiere AltGr = Ctrl izquierdo + Alt derecho (símbolos LatAm como @ ~ ^).
 */
data class HidKey(val hid: Int, val shift: Boolean = false, val altGr: Boolean = false)

/**
 * Mapa carácter → HID para el layout Español (Latinoamérica).
 * El servidor envía posición física; la letra final depende del layout de Windows.
 * Los acentos precompuestos (á é í ó ú ü) se envían como tecla muerta + vocal:
 * Windows (LatAm) los combina.
 */
object LatAm {

    private const val DEAD_ACUTE = Hid.SEMICOLON      // ´
    private const val DEAD_DIAERESIS = Hid.SEMICOLON  // ¨ (con shift)

    fun map(c: Char): List<HidKey>? {
        if (c in 'a'..'z') return listOf(HidKey(Hid.A + (c - 'a')))
        if (c in 'A'..'Z') return listOf(HidKey(Hid.A + (c - 'A'), shift = true))
        if (c in '1'..'9') return listOf(HidKey(Hid.DIGIT_1 + (c - '1')))

        val simple = when (c) {
            '0' -> HidKey(Hid.DIGIT_0)
            ' ' -> HidKey(Hid.SPACE)
            '\n', '\r' -> HidKey(Hid.ENTER)
            '\t' -> HidKey(Hid.TAB)
            'ñ' -> HidKey(Hid.GRAVE)
            'Ñ' -> HidKey(Hid.GRAVE, shift = true)
            '-' -> HidKey(Hid.MINUS)
            '_' -> HidKey(Hid.MINUS, shift = true)
            '+' -> HidKey(Hid.EQUAL)
            '*' -> HidKey(Hid.EQUAL, shift = true)
            '?' -> HidKey(Hid.LEFT_BRACKET)
            '\'' -> HidKey(Hid.LEFT_BRACKET, shift = true)
            '¿' -> HidKey(Hid.RIGHT_BRACKET)
            '¡' -> HidKey(Hid.RIGHT_BRACKET, shift = true)
            '|' -> HidKey(Hid.BACKSLASH)
            '°' -> HidKey(Hid.BACKSLASH, shift = true)
            '´' -> HidKey(Hid.SEMICOLON)
            '¨' -> HidKey(Hid.SEMICOLON, shift = true)
            '[' -> HidKey(Hid.QUOTE)
            '{' -> HidKey(Hid.QUOTE, shift = true)
            ']' -> HidKey(Hid.SLASH)
            '}' -> HidKey(Hid.SLASH, shift = true)
            ',' -> HidKey(Hid.COMMA)
            ';' -> HidKey(Hid.COMMA, shift = true)
            '.' -> HidKey(Hid.PERIOD)
            ':' -> HidKey(Hid.PERIOD, shift = true)
            '<' -> HidKey(Hid.ISO_102)
            '>' -> HidKey(Hid.ISO_102, shift = true)
            '!' -> HidKey(Hid.DIGIT_1, shift = true)
            '"' -> HidKey(Hid.DIGIT_2, shift = true)
            '#' -> HidKey(Hid.DIGIT_3, shift = true)
            '$' -> HidKey(Hid.DIGIT_4, shift = true)
            '%' -> HidKey(Hid.DIGIT_5, shift = true)
            '&' -> HidKey(Hid.DIGIT_6, shift = true)
            '/' -> HidKey(Hid.DIGIT_7, shift = true)
            '(' -> HidKey(Hid.DIGIT_8, shift = true)
            ')' -> HidKey(Hid.DIGIT_9, shift = true)
            '=' -> HidKey(Hid.DIGIT_0, shift = true)
            '@' -> HidKey(Hid.Q, altGr = true)
            '~' -> HidKey(Hid.EQUAL, altGr = true)
            '^' -> HidKey(Hid.QUOTE, altGr = true)
            '\\' -> HidKey(Hid.LEFT_BRACKET, altGr = true)
            '¬' -> HidKey(Hid.BACKSLASH, altGr = true)
            else -> null
        }
        return simple?.let { listOf(it) } ?: accent(c)
    }

    /** Acentos precompuestos → tecla muerta (´ o ¨) + vocal. */
    private fun accent(c: Char): List<HidKey>? {
        val vowel = when (c) {
            'á' -> Hid.A; 'Á' -> Hid.A
            'é' -> Hid.E; 'É' -> Hid.E
            'í' -> Hid.I; 'Í' -> Hid.I
            'ó' -> Hid.O; 'Ó' -> Hid.O
            'ú' -> Hid.U; 'Ú' -> Hid.U
            'ü' -> Hid.U; 'Ü' -> Hid.U
            'ï' -> Hid.I; 'Ï' -> Hid.I
            else -> return null
        }
        val diaeresis = c in "üÜïÏ"
        val uppercase = c.isUpperCase()
        return listOf(
            HidKey(DEAD_ACUTE, shift = diaeresis),
            HidKey(vowel, shift = uppercase),
        )
    }
}
