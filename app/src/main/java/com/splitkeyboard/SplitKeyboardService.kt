package com.splitkeyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.splitkeyboard.model.Key
import com.splitkeyboard.model.KeyType
import com.splitkeyboard.model.KeyboardConfig
import com.splitkeyboard.model.KeyboardLayer
import com.splitkeyboard.model.KeyboardLayers
import com.splitkeyboard.ui.keyboard.SplitKeyboardView

/**
 * Main Input Method Service for the split keyboard
 */
class SplitKeyboardService : InputMethodService() {

    private var keyboardView: SplitKeyboardView? = null
    private var config: KeyboardConfig? = null
    private val layers = KeyboardLayers.getDefaultLayers()
    private var currentLayerName = "default"
    private var isShifted = false

    override fun onCreateInputView(): View {
        config = KeyboardConfig.load(this)

        keyboardView = SplitKeyboardView(
            this,
            config?.widthPercent ?: 15f,
            ::handleKeyClick
        ).apply {
            // Set layout parameters to fill the entire screen height
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Set initial layer
        switchToLayer(config?.currentLayer ?: "default")

        return keyboardView!!
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // Reset to default layer when starting new input
        switchToLayer("default")
        isShifted = false
    }

    override fun onFinishInput() {
        super.onFinishInput()
        // Save current state
        config?.copy(currentLayer = currentLayerName)?.save(this)
    }

    private fun handleKeyClick(key: Key) {
        when (key.type) {
            KeyType.CHARACTER -> {
                currentInputConnection?.commitText(key.outputText, 1)
                // Auto-shift after character if shift was on
                if (isShifted) {
                    isShifted = false
                    switchToLayer("default")
                }
            }
            KeyType.BACKSPACE -> {
                currentInputConnection?.deleteSurroundingText(1, 0)
            }
            KeyType.ENTER -> {
                currentInputConnection?.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
                currentInputConnection?.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER)
                )
            }
            KeyType.SPACE -> {
                currentInputConnection?.commitText(" ", 1)
            }
            KeyType.SHIFT -> {
                isShifted = !isShifted
                switchToLayer(if (isShifted) "shift" else "default")
            }
            KeyType.LAYER_SWITCH -> {
                when (key.label) {
                    "123" -> switchToLayer("numbers")
                    "ABC" -> switchToLayer("default")
                    "#+=" -> switchToLayer("symbols")
                    "#+" -> switchToLayer("symbols")
                    else -> switchToLayer("default")
                }
            }
            KeyType.SPECIAL -> {
                // Handle special keys if needed
            }
        }
    }

    private fun switchToLayer(layerName: String) {
        layers[layerName]?.let { layer ->
            currentLayerName = layerName
            keyboardView?.setLayer(layer)
        }
    }

    /**
     * Update the keyboard width (call this when settings change)
     */
    fun updateWidth(widthPercent: Float) {
        config = config?.copy(widthPercent = widthPercent)
        config?.save(this)
        // Recreate the view with new width
        setInputView(onCreateInputView())
    }
}
