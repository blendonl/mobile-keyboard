package com.splitkeyboard.ui.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.splitkeyboard.model.Key
import com.splitkeyboard.model.KeyboardLayer
import com.splitkeyboard.model.KeyboardLayers

enum class PanelSide {
    LEFT, RIGHT
}

/**
 * View that renders a single keyboard panel (left or right)
 */
class KeyboardPanelView(
    context: Context,
    private val side: PanelSide,
    private val onKeyClick: (Key) -> Unit
) : View(context) {

    private var currentLayer: KeyboardLayer? = null
    private val keys = mutableListOf<KeyBounds>()
    private var pressedKey: KeyBounds? = null

    private val keyPaint = Paint().apply {
        color = Color.parseColor("#2C2C2C")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val keyPressedPaint = Paint().apply {
        color = Color.parseColor("#4A4A4A")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#1A1A1A")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#1A1A1A")
        style = Paint.Style.FILL
    }

    private data class KeyBounds(
        val key: Key,
        val rect: RectF
    )

    init {
        // Set background color for the panel
        setBackgroundColor(Color.parseColor("#1A1A1A"))

        // Load default layer
        val layers = KeyboardLayers.getDefaultLayers()
        setLayer(layers["default"]!!)
    }

    fun setLayer(layer: KeyboardLayer) {
        currentLayer = layer
        calculateKeyBounds()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateKeyBounds()
    }

    private fun calculateKeyBounds() {
        val layer = currentLayer ?: return
        keys.clear()

        val panelWidth = width.toFloat()
        val panelHeight = height.toFloat()
        val keyMargin = 4f
        val keyPadding = 8f

        // Get the appropriate keys for this panel side
        val rows = when (side) {
            PanelSide.LEFT -> layer.leftKeys
            PanelSide.RIGHT -> layer.rightKeys
        }

        calculatePanelKeys(
            rows,
            0f,
            panelWidth,
            panelHeight,
            keyMargin,
            keyPadding,
            keys
        )
    }

    private fun calculatePanelKeys(
        rows: List<List<Key>>,
        startX: Float,
        panelWidth: Float,
        panelHeight: Float,
        keyMargin: Float,
        keyPadding: Float,
        outputList: MutableList<KeyBounds>
    ) {
        val rowCount = rows.size
        val rowHeight = panelHeight / rowCount

        rows.forEachIndexed { rowIndex, keys ->
            val y = rowIndex * rowHeight
            val totalWidth = keys.sumOf { it.width.toDouble() }.toFloat()
            val keyWidth = (panelWidth - (keys.size + 1) * keyMargin) / totalWidth

            var x = startX + keyMargin

            keys.forEach { key ->
                val actualKeyWidth = keyWidth * key.width
                val rect = RectF(
                    x + keyPadding,
                    y + keyMargin + keyPadding,
                    x + actualKeyWidth - keyPadding,
                    y + rowHeight - keyMargin - keyPadding
                )
                outputList.add(KeyBounds(key, rect))
                x += actualKeyWidth + keyMargin
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Set text size based on screen
        textPaint.textSize = height / 30f

        // Draw keys
        keys.forEach { keyBounds ->
            val paint = if (keyBounds == pressedKey) keyPressedPaint else keyPaint

            // Draw key background
            canvas.drawRoundRect(keyBounds.rect, 8f, 8f, paint)

            // Draw key border
            canvas.drawRoundRect(keyBounds.rect, 8f, 8f, borderPaint)

            // Draw key label
            val centerX = keyBounds.rect.centerX()
            val centerY = keyBounds.rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(keyBounds.key.label, centerX, centerY, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val key = findKeyAt(event.x, event.y)
                if (key != null) {
                    pressedKey = key
                    invalidate()
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val key = findKeyAt(event.x, event.y)
                if (key != pressedKey) {
                    pressedKey = key
                    invalidate()
                }
                return pressedKey != null
            }
            MotionEvent.ACTION_UP -> {
                val hadKey = pressedKey != null
                pressedKey?.let { onKeyClick(it.key) }
                pressedKey = null
                invalidate()
                return hadKey
            }
        }
        return false
    }

    private fun findKeyAt(x: Float, y: Float): KeyBounds? {
        return keys.firstOrNull { it.rect.contains(x, y) }
    }
}
