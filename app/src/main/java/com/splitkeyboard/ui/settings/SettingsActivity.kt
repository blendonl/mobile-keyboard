package com.splitkeyboard.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.splitkeyboard.model.KeyboardConfig
import com.splitkeyboard.ui.container.KeyboardContainerActivity
import com.splitkeyboard.ui.overlay.KeyboardOverlayService
import com.splitkeyboard.ui.theme.SplitKeyboardTheme

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplitKeyboardTheme {
                SettingsScreen(
                    onEnableKeyboard = ::openInputMethodSettings,
                    onSelectKeyboard = ::openInputMethodPicker
                )
            }
        }
    }

    private fun openInputMethodSettings() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    private fun openInputMethodPicker() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.showInputMethodPicker()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onEnableKeyboard: () -> Unit,
    onSelectKeyboard: () -> Unit
) {
    val context = LocalContext.current
    var widthPercent by remember { mutableStateOf(15f) }
    var isOverlayActive by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        )
    }

    // Load configuration when the composable is first created
    LaunchedEffect(Unit) {
        // Note: This is a simplified version. In a real app, you'd want to use a ViewModel
        // For now, we'll just use the default value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split Keyboard Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Container Activity Section (TRUE LAYOUT)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Container Mode (True Layout) ⭐",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "TRUE side-by-side layout where the app takes actual space in the middle. Left Keyboard | App | Right Keyboard. Best option!",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            val intent = Intent(context, KeyboardContainerActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Launch Container Mode")
                    }
                }
            }

            // System Overlay Section (NEW METHOD)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Overlay Mode (Visual Effect)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Keyboard overlays on top of apps. Creates visual effect of side panels but doesn't resize the app window.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!hasOverlayPermission) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Overlay Permission")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isOverlayActive) {
                                        val intent = Intent(context, KeyboardOverlayService::class.java).apply {
                                            action = KeyboardOverlayService.ACTION_SHOW
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(intent)
                                        } else {
                                            context.startService(intent)
                                        }
                                        isOverlayActive = true
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isOverlayActive
                            ) {
                                Text("Show Keyboard")
                            }

                            Button(
                                onClick = {
                                    if (isOverlayActive) {
                                        val intent = Intent(context, KeyboardOverlayService::class.java).apply {
                                            action = KeyboardOverlayService.ACTION_HIDE
                                        }
                                        context.startService(intent)
                                        isOverlayActive = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = isOverlayActive,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Hide Keyboard")
                            }
                        }
                    }
                }
            }

            // Setup Section (ORIGINAL IME METHOD)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Traditional IME Setup (Alternative)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Traditional keyboard method. Note: Android limitations prevent true side-by-side layout with this method.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = onEnableKeyboard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable Keyboard")
                    }

                    Button(
                        onClick = onSelectKeyboard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Keyboard")
                    }
                }
            }

            // Configuration Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Keyboard Width",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Adjust the width of each keyboard panel (left and right)",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${widthPercent.toInt()}%",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Slider(
                        value = widthPercent,
                        onValueChange = { widthPercent = it },
                        valueRange = 10f..30f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Range: 10% - 30%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Information Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About Split Keyboard",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = """
                            This keyboard features:
                            • Split layout with adjustable width
                            • Full screen height coverage
                            • Multiple layers (letters, numbers, symbols)
                            • Shift support for uppercase letters
                            • Customizable width for each panel
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
