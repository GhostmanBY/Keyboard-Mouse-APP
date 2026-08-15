package com.kbdmouse.app.key

/**
 * Códigos USB HID (usage codes, 1 byte) según USB HID Usage Tables,
 * sección Keyboard/Keypad. Referencia usada por docs/protocol.md y keymap.py.
 */
object Hid {
    const val A = 0x04
    const val B = 0x05
    const val C = 0x06
    const val D = 0x07
    const val E = 0x08
    const val F = 0x09
    const val G = 0x0A
    const val H = 0x0B
    const val I = 0x0C
    const val J = 0x0D
    const val K = 0x0E
    const val L = 0x0F
    const val M = 0x10
    const val N = 0x11
    const val O = 0x12
    const val P = 0x13
    const val Q = 0x14
    const val R = 0x15
    const val S = 0x16
    const val T = 0x17
    const val U = 0x18
    const val V = 0x19
    const val W = 0x1A
    const val X = 0x1B
    const val Y = 0x1C
    const val Z = 0x1D

    const val DIGIT_1 = 0x1E
    const val DIGIT_2 = 0x1F
    const val DIGIT_3 = 0x20
    const val DIGIT_4 = 0x21
    const val DIGIT_5 = 0x22
    const val DIGIT_6 = 0x23
    const val DIGIT_7 = 0x24
    const val DIGIT_8 = 0x25
    const val DIGIT_9 = 0x26
    const val DIGIT_0 = 0x27

    const val ENTER = 0x28
    const val ESC = 0x29
    const val BACKSPACE = 0x2A
    const val TAB = 0x2B
    const val SPACE = 0x2C
    const val MINUS = 0x2D
    const val EQUAL = 0x2E
    const val LEFT_BRACKET = 0x2F
    const val RIGHT_BRACKET = 0x30
    const val BACKSLASH = 0x31
    const val SEMICOLON = 0x33
    const val QUOTE = 0x34
    const val GRAVE = 0x35
    const val COMMA = 0x36
    const val PERIOD = 0x37
    const val SLASH = 0x38
    const val CAPS_LOCK = 0x39

    const val F1 = 0x3A
    const val F2 = 0x3B
    const val F3 = 0x3C
    const val F4 = 0x3D
    const val F5 = 0x3E
    const val F6 = 0x3F
    const val F7 = 0x40
    const val F8 = 0x41
    const val F9 = 0x42
    const val F10 = 0x43
    const val F11 = 0x44
    const val F12 = 0x45

    const val PRINT_SCREEN = 0x46
    const val SCROLL_LOCK = 0x47
    const val PAUSE = 0x48
    const val INSERT = 0x49
    const val HOME = 0x4A
    const val PAGE_UP = 0x4B
    const val DELETE = 0x4C
    const val END = 0x4D
    const val PAGE_DOWN = 0x4E
    const val RIGHT_ARROW = 0x4F
    const val LEFT_ARROW = 0x50
    const val DOWN_ARROW = 0x51
    const val UP_ARROW = 0x52

    const val NUMPAD_LOCK = 0x53
    const val NUMPAD_DIVIDE = 0x54
    const val NUMPAD_MULTIPLY = 0x55
    const val NUMPAD_SUBTRACT = 0x56
    const val NUMPAD_ADD = 0x57
    const val NUMPAD_ENTER = 0x58
    const val NUMPAD_1 = 0x59
    const val NUMPAD_2 = 0x5A
    const val NUMPAD_3 = 0x5B
    const val NUMPAD_4 = 0x5C
    const val NUMPAD_5 = 0x5D
    const val NUMPAD_6 = 0x5E
    const val NUMPAD_7 = 0x5F
    const val NUMPAD_8 = 0x60
    const val NUMPAD_9 = 0x61
    const val NUMPAD_0 = 0x62
    const val NUMPAD_DECIMAL = 0x63

    /** Tecla ISO "< >" (non-US #, 0x64). */
    const val ISO_102 = 0x64

    const val LEFT_CTRL = 0xE0
    const val LEFT_SHIFT = 0xE1
    const val LEFT_ALT = 0xE2
    const val LEFT_SUPER = 0xE3
    const val RIGHT_CTRL = 0xE4
    const val RIGHT_SHIFT = 0xE5
    const val RIGHT_ALT = 0xE6
    const val RIGHT_SUPER = 0xE7

    val modifiers = listOf(
        LEFT_CTRL, LEFT_SHIFT, LEFT_ALT, LEFT_SUPER,
        RIGHT_CTRL, RIGHT_SHIFT, RIGHT_ALT, RIGHT_SUPER,
    )
}
