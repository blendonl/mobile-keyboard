package com.splitkeyboard.model

/**
 * Represents a single key on the keyboard
 */
data class Key(
    val label: String,
    val outputText: String = label,
    val code: Int = -1,  // Special key code (e.g., backspace, enter)
    val width: Float = 1f,  // Relative width (1.0 = normal key)
    val type: KeyType = KeyType.CHARACTER
)

enum class KeyType {
    CHARACTER,
    BACKSPACE,
    ENTER,
    SPACE,
    SHIFT,
    LAYER_SWITCH,
    SPECIAL
}
