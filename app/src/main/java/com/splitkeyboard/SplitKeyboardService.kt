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
        // Use fullscreen mode to overlay entire screen
        // With transparent middle section, this creates the visual effect of side keyboards
        return true
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        // Always show the keyboard when requested, don't let Android auto-hide it
        return true
    }

    override fun onEvaluateInputViewShown(): Boolean {
        // Always keep the keyboard view shown when active
        return true
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)

        outInsets?.apply {
            keyboardView?.let { view ->
                val screenHeight = view.height
                val screenWidth = view.width
                val panelWidth = (screenWidth * ((config?.widthPercent ?: 15f) / 100f)).toInt()

                // Don't consume vertical space since our keyboard is on the sides
                // This allows the app to use the full vertical space
                contentTopInsets = 0
                visibleTopInsets = 0

                // Only the keyboard panels are touchable, middle area passes through to app
                touchableInsets = Insets.TOUCHABLE_INSETS_REGION
                touchableRegion.setEmpty()

                // Left panel touchable region (full height)
                touchableRegion.union(
                    android.graphics.Rect(0, 0, panelWidth, screenHeight)
                )
                // Right panel touchable region (full height)
                touchableRegion.union(
                    android.graphics.Rect(screenWidth - panelWidth, 0, screenWidth, screenHeight)
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

        // Configure window for fullscreen overlay with side panels
        window?.window?.let { win ->
            win.setBackgroundDrawableResource(android.R.color.transparent)
            win.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            win.setGravity(Gravity.TOP or Gravity.LEFT)

            // Set flags for fullscreen overlay
            win.addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            // Allow touches to pass through in the middle area
            win.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )

            win.attributes = win.attributes?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP or Gravity.LEFT
                format = PixelFormat.TRANSLUCENT
            }
        }

        // Disable extract view
        setExtractViewShown(false)

        keyboardView = SplitKeyboardView(
            this,
            config?.widthPercent ?: 15f,
            ::handleKeyClick
        ).apply {
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
