package com.splitkeyboard.model

import android.content.Context
import android.content.SharedPreferences

/**
 * Configuration class for the split keyboard
 */
data class KeyboardConfig(
    val widthPercent: Float = 15f,  // Width of each keyboard panel as percentage of screen
    val currentLayer: String = "default"
) {
    companion object {
        private const val PREFS_NAME = "keyboard_config"
        private const val KEY_WIDTH_PERCENT = "width_percent"
        private const val KEY_CURRENT_LAYER = "current_layer"

        fun load(context: Context): KeyboardConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return KeyboardConfig(
                widthPercent = prefs.getFloat(KEY_WIDTH_PERCENT, 15f),
                currentLayer = prefs.getString(KEY_CURRENT_LAYER, "default") ?: "default"
            )
        }
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(KEY_WIDTH_PERCENT, widthPercent)
            putString(KEY_CURRENT_LAYER, currentLayer)
            apply()
        }
    }
}
