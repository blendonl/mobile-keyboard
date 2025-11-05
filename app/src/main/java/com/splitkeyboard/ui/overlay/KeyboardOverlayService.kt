package com.splitkeyboard.ui.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Insets
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.splitkeyboard.R
import com.splitkeyboard.model.KeyboardConfig
import com.splitkeyboard.ui.settings.SettingsActivity

/**
 * Foreground service that manages overlay windows for left and right keyboard panels
 */
class KeyboardOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var leftPanelView: KeyboardPanelView? = null
    private var rightPanelView: KeyboardPanelView? = null
    private var config: KeyboardConfig? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "keyboard_overlay_channel"
        const val ACTION_SHOW = "com.splitkeyboard.SHOW_OVERLAY"
        const val ACTION_HIDE = "com.splitkeyboard.HIDE_OVERLAY"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        config = KeyboardConfig.load(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                if (canDrawOverlays()) {
                    startForeground(NOTIFICATION_ID, createNotification())
                    showOverlays()
                } else {
                    Toast.makeText(this, "Overlay permission required", Toast.LENGTH_LONG).show()
                    requestOverlayPermission()
                    stopSelf()
                }
            }
            ACTION_HIDE -> {
                hideOverlays()
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                if (canDrawOverlays()) {
                    showOverlays()
                }
            }
        }
        return START_STICKY
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun showOverlays() {
        if (leftPanelView != null || rightPanelView != null) {
            return // Already showing
        }

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val panelWidth = (screenWidth * ((config?.widthPercent ?: 15f) / 100f)).toInt()

        // Create window layout parameters
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Left panel parameters
        val leftParams = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 0
        }

        // Right panel parameters
        val rightParams = WindowManager.LayoutParams(
            panelWidth,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.TOP
            x = 0
            y = 0
        }

        // Create and add views
        leftPanelView = KeyboardPanelView(this, PanelSide.LEFT) { key ->
            // Handle key click - TODO: implement text input
            Toast.makeText(this, "Key: ${key.label}", Toast.LENGTH_SHORT).show()
        }

        rightPanelView = KeyboardPanelView(this, PanelSide.RIGHT) { key ->
            // Handle key click - TODO: implement text input
            Toast.makeText(this, "Key: ${key.label}", Toast.LENGTH_SHORT).show()
        }

        try {
            windowManager.addView(leftPanelView, leftParams)
            windowManager.addView(rightPanelView, rightParams)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to show overlays: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun hideOverlays() {
        leftPanelView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        rightPanelView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        leftPanelView = null
        rightPanelView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Split Keyboard Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows split keyboard overlays on screen edges"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Split Keyboard Active")
                .setContentText("Keyboard panels are visible on screen edges")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Split Keyboard Active")
                .setContentText("Keyboard panels are visible on screen edges")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlays()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
