package com.splitkeyboard.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitkeyboard.model.KeyboardConfig
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
    var widthPercent by remember { mutableStateOf(15f) }

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
            // Setup Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Setup",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "To use the Split Keyboard, you need to enable it in your system settings and select it as your input method.",
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
