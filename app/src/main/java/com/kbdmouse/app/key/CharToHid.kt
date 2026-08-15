package com.kbdmouse.app.key

/**
 * Mapea un carácter a su código HID y si requiere Shift (en teclado US).
 * Devuelve (hidUsage, needsShift) o null si el carácter no está soportado.
 */
object CharToHid {

    fun map(c: Char): Pair<Int, Boolean>? {
        if (c in 'a'..'z') return Hid.A + (c - 'a') to false
        if (c in 'A'..'Z') return Hid.A + (c - 'A') to true
        if (c in '1'..'9') return Hid.DIGIT_1 + (c - '1') to false

        return when (c) {
            '0' -> Hid.DIGIT_0 to false
            ' ' -> Hid.SPACE to false
            '\n', '\r' -> Hid.ENTER to false
            '\t' -> Hid.TAB to false
            '-' -> Hid.MINUS to false
            '=' -> Hid.EQUAL to false
            '[' -> Hid.LEFT_BRACKET to false
            ']' -> Hid.RIGHT_BRACKET to false
            '\\' -> Hid.BACKSLASH to false
            ';' -> Hid.SEMICOLON to false
            '\'' -> Hid.QUOTE to false
            '`' -> Hid.GRAVE to false
            ',' -> Hid.COMMA to false
            '.' -> Hid.PERIOD to false
            '/' -> Hid.SLASH to false
            '!' -> Hid.DIGIT_1 to true
            '@' -> Hid.DIGIT_2 to true
            '#' -> Hid.DIGIT_3 to true
            '$' -> Hid.DIGIT_4 to true
            '%' -> Hid.DIGIT_5 to true
            '^' -> Hid.DIGIT_6 to true
            '&' -> Hid.DIGIT_7 to true
            '*' -> Hid.DIGIT_8 to true
            '(' -> Hid.DIGIT_9 to true
            ')' -> Hid.DIGIT_0 to true
            '_' -> Hid.MINUS to true
            '+' -> Hid.EQUAL to true
            '{' -> Hid.LEFT_BRACKET to true
            '}' -> Hid.RIGHT_BRACKET to true
            '|' -> Hid.BACKSLASH to true
            ':' -> Hid.SEMICOLON to true
            '"' -> Hid.QUOTE to true
            '~' -> Hid.GRAVE to true
            '<' -> Hid.COMMA to true
            '>' -> Hid.PERIOD to true
            '?' -> Hid.SLASH to true
            else -> null
        }
    }
}
