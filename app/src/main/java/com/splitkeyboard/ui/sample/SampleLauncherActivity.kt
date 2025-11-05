package com.splitkeyboard.ui.sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.splitkeyboard.ui.overlay.KeyboardOverlayService
import com.splitkeyboard.ui.theme.SplitKeyboardTheme

/**
 * Sample Launcher/Home Screen Activity that RESPECTS the keyboard space.
 *
 * This demonstrates how apps can listen to keyboard broadcasts and resize themselves
 * to avoid the keyboard panels, achieving the visual effect of:
 * [Left Keyboard] | [App Content] | [Right Keyboard]
 *
 * While Android doesn't force this system-wide, cooperative apps can implement this
 * to work seamlessly with the split keyboard.
 */
class SampleLauncherActivity : ComponentActivity() {

    private var keyboardReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplitKeyboardTheme {
                SampleLauncherScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerKeyboardReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterKeyboardReceiver()
    }

    private fun registerKeyboardReceiver() {
        keyboardReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Keyboard state changed - activity will recompose with new padding
            }
        }

        val filter = IntentFilter(KeyboardOverlayService.ACTION_KEYBOARD_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(keyboardReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(keyboardReceiver, filter)
        }
    }

    private fun unregisterKeyboardReceiver() {
        keyboardReceiver?.let {
            unregisterReceiver(it)
        }
        keyboardReceiver = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleLauncherScreen() {
    // Listen to keyboard state via shared preferences or broadcast
    var leftPadding by remember { mutableStateOf(0.dp) }
    var rightPadding by remember { mutableStateOf(0.dp) }

    // In a real implementation, you'd update these from the broadcast receiver
    // For demo purposes, we'll simulate keyboard being active
    val isKeyboardActive = true // Set this based on actual keyboard state

    LaunchedEffect(isKeyboardActive) {
        if (isKeyboardActive) {
            // Assume 15% keyboard panels
            leftPadding = 80.dp  // Adjust based on actual screen width
            rightPadding = 80.dp
        } else {
            leftPadding = 0.dp
            rightPadding = 0.dp
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sample Launcher (Keyboard-Aware)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = leftPadding, end = rightPadding) // KEY: Avoid keyboard areas
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "This app respects keyboard space",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Content is padded to avoid keyboard panels",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                // Sample app grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(12) { index ->
                        AppIcon(index)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How This Works:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "• Keyboard broadcasts its state when shown/hidden\n" +
                                    "• This app listens to broadcasts\n" +
                                    "• Content is padded to avoid keyboard areas\n" +
                                    "• Creates visual effect of [Keyboard|App|Keyboard]",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppIcon(index: Int) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "App\n${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
