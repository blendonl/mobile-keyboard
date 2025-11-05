package com.splitkeyboard.ui.container

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.splitkeyboard.model.Key
import com.splitkeyboard.model.KeyboardConfig
import com.splitkeyboard.model.KeyboardLayers
import com.splitkeyboard.ui.overlay.KeyboardPanelView
import com.splitkeyboard.ui.overlay.PanelSide

/**
 * Container activity that provides TRUE side-by-side layout:
 * [Left Keyboard] | [App Content] | [Right Keyboard]
 *
 * This solves the Android limitation where IME keyboards cannot reserve horizontal space.
 * Use this activity when you want the app content to be truly constrained to the middle
 * with keyboard panels taking actual layout space on the sides.
 */
class KeyboardContainerActivity : AppCompatActivity() {

    private var leftPanel: KeyboardPanelView? = null
    private var rightPanel: KeyboardPanelView? = null
    private var contentView: WebView? = null
    private var config: KeyboardConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = KeyboardConfig.load(this)

        // Create horizontal linear layout for side-by-side arrangement
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Calculate dimensions
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val panelWidthPercent = config?.widthPercent ?: 15f
        val panelWidth = (screenWidth * (panelWidthPercent / 100f)).toInt()
        val contentWidth = screenWidth - (2 * panelWidth)

        // Left keyboard panel
        leftPanel = KeyboardPanelView(this, PanelSide.LEFT) { key ->
            handleKeyInput(key)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(panelWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        // Content area (WebView example - replace with your app content)
        contentView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(contentWidth, ViewGroup.LayoutParams.MATCH_PARENT)
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true

            // Load initial content
            val initialUrl = intent.getStringExtra(EXTRA_CONTENT_URL) ?: "https://www.example.com"
            loadUrl(initialUrl)
        }

        // Right keyboard panel
        rightPanel = KeyboardPanelView(this, PanelSide.RIGHT) { key ->
            handleKeyInput(key)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(panelWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        // Add views to layout in order: left | content | right
        rootLayout.addView(leftPanel)
        rootLayout.addView(contentView)
        rootLayout.addView(rightPanel)

        setContentView(rootLayout)
    }

    private fun handleKeyInput(key: Key) {
        // Inject key input into the content view
        contentView?.let { webView ->
            when (key.type) {
                com.splitkeyboard.model.KeyType.CHARACTER -> {
                    // Inject text into focused element
                    webView.evaluateJavascript(
                        "document.activeElement.value += '${key.outputText}';",
                        null
                    )
                }
                com.splitkeyboard.model.KeyType.BACKSPACE -> {
                    webView.evaluateJavascript(
                        """
                        var el = document.activeElement;
                        if (el.value) {
                            el.value = el.value.slice(0, -1);
                        }
                        """.trimIndent(),
                        null
                    )
                }
                com.splitkeyboard.model.KeyType.ENTER -> {
                    webView.evaluateJavascript(
                        "document.activeElement.value += '\\n';",
                        null
                    )
                }
                com.splitkeyboard.model.KeyType.SPACE -> {
                    webView.evaluateJavascript(
                        "document.activeElement.value += ' ';",
                        null
                    )
                }
                else -> {
                    // Handle other key types as needed
                }
            }
        }
    }

    companion object {
        const val EXTRA_CONTENT_URL = "content_url"
    }
}
