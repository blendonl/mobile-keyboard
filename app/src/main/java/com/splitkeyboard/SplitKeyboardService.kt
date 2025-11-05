package com.splitkeyboard

import android.graphics.Color
import android.graphics.PixelFormat
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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

    override fun onEvaluateFullscreenMode(): Boolean {
        // Always use fullscreen mode so keyboard overlays the entire screen
        return true
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        // Tell the system that our keyboard doesn't consume any vertical space
        // This prevents the app from being resized or shifted up
        outInsets?.apply {
            contentTopInsets = 0
            visibleTopInsets = 0
            touchableInsets = Insets.TOUCHABLE_INSETS_REGION
            touchableRegion.setEmpty()

            // Add touchable regions for the left and right panels only
            keyboardView?.let { view ->
                val screenWidth = view.width
                val panelWidth = (screenWidth * ((config?.widthPercent ?: 15f) / 100f)).toInt()

                // Left panel touchable region
                touchableRegion.union(
                    android.graphics.Rect(0, 0, panelWidth, view.height)
                )
                // Right panel touchable region
                touchableRegion.union(
                    android.graphics.Rect(screenWidth - panelWidth, 0, screenWidth, view.height)
                )
            }
        }
    }

    override fun onCreateExtractTextView(): View? {
        // Don't show the extract text view in fullscreen mode
        // This allows our transparent keyboard to overlay the app
        return null
    }

    override fun onCreateInputView(): View {
        config = KeyboardConfig.load(this)

        // Configure window to be truly fullscreen and overlay properly
        window?.window?.let { win ->
            win.setBackgroundDrawableResource(android.R.color.transparent)
            win.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            win.setGravity(Gravity.TOP or Gravity.LEFT)

            // Set flags to allow the window to extend over the entire screen
            win.addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            // Allow touches to pass through in areas without keys
            win.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )

            // Make the window cover the full screen
            win.attributes = win.attributes?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP or Gravity.LEFT
                format = PixelFormat.TRANSLUCENT
            }
        }

        // Ensure extract view is not shown in fullscreen mode
        setExtractViewShown(false)

        keyboardView = SplitKeyboardView(
            this,
            config?.widthPercent ?: 15f,
            ::handleKeyClick
        ).apply {
            // Set layout parameters to fill the entire screen
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Add layout change listener to update insets
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                updateInputViewShown()
            }
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
