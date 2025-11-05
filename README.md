# Split Keyboard for Android

A custom Android keyboard with a unique split design optimized for one-handed or two-handed thumb typing. Features true side-by-side panels with the app content in the middle.

## Features

- **True Split Layout**: Two keyboard panels positioned at the left and right edges with app content in the middle
- **System Overlay Mode** (Recommended): Creates actual side panels that make the app window narrower
- **Traditional IME Mode**: Alternative fullscreen overlay approach
- **Configurable Width**: Adjust panel width from 10% to 30% of screen width (default: 15%)
- **Full Height**: Keyboard spans 100% of screen height for easy access
- **Multiple Layers**:
  - Default: Lowercase letters (a-z)
  - Shift: Uppercase letters (A-Z)
  - Numbers: Digits (0-9) and common symbols
  - Symbols: Special characters and symbols
- **Modern UI**: Built with Jetpack Compose for settings
- **Customizable**: Easy to extend with additional layers and key configurations

## Two Implementation Methods

### System Overlay Mode (Recommended)

Creates true side panels where the app window is actually narrower and positioned in the middle:

```
┌─────────┬─────────────────────┬─────────┐
│  LEFT   │                     │  RIGHT  │
│ PANEL   │    APP WINDOW       │ PANEL   │
│ (15%)   │   (Narrower, 70%)   │ (15%)   │
└─────────┴─────────────────────┴─────────┘
```

**Pros:**
- True side-by-side layout (left panel | app | right panel)
- App window is actually resized and narrower
- Clear visual separation between keyboard and app

**Cons:**
- Requires `SYSTEM_ALERT_WINDOW` permission
- Text input requires additional implementation
- User must manually show/hide keyboard

### Traditional IME Mode (Alternative)

Uses Android's InputMethodService framework:

**Pros:**
- Native keyboard integration
- Automatic text input handling
- Standard keyboard behavior

**Cons:**
- Android limitation: IME windows can only be positioned at bottom
- App is overlaid but not truly resized horizontally
- Cannot achieve true side-by-side layout

## Architecture

The keyboard is built with the following components:

### Data Models
- `KeyboardConfig`: Stores user preferences (width, current layer)
- `Key`: Represents individual keys with labels, types, and actions
- `KeyboardLayer`: Defines key layouts for each layer (default, shift, numbers, symbols)
- `KeyboardLayers`: Factory for creating default keyboard layers

### UI Components
- `SplitKeyboardView`: Custom View that renders the split keyboard (used in IME mode)
- `KeyboardPanelView`: Custom View that renders a single panel (used in overlay mode)
- `SettingsActivity`: Compose-based settings UI for configuration

### Services
- `SplitKeyboardService`: InputMethodService for traditional IME mode
- `KeyboardOverlayService`: Foreground service that manages system overlay windows

## Setup

### System Overlay Mode (Recommended)

1. Install the app on an Android device (API 31+)
2. Open the app
3. Tap "Grant Overlay Permission" in the first card
4. Enable the permission in Android settings
5. Return to the app
6. Tap "Show Keyboard" to display the side panels
7. Adjust width settings as desired (requires hiding and re-showing keyboard)
8. Tap "Hide Keyboard" when done

**Note:** The keyboard will remain visible as overlays until you hide it. The service runs in the foreground and shows a notification.

### Traditional IME Mode (Alternative)

1. Install the app on an Android device (API 31+)
2. Open the app
3. Scroll to "Traditional IME Setup" card
4. Tap "Enable Keyboard"
5. Enable "Split Keyboard" in system settings
6. Tap "Select Keyboard" and choose "Split Keyboard"
7. Open any text field to activate the keyboard
8. Adjust width settings as desired

**Note:** Due to Android limitations, this mode cannot achieve true side-by-side layout.

## Building

```bash
./gradlew assembleDebug
```

## Requirements

- Android 12 (API 31) or higher
- Kotlin 1.9.20
- Jetpack Compose

### Permissions

- `VIBRATE`: For haptic feedback (optional)
- `SYSTEM_ALERT_WINDOW`: Required for overlay mode to draw over other apps
- `FOREGROUND_SERVICE`: Required for overlay service
- `FOREGROUND_SERVICE_SPECIAL_USE`: Android 14+ requirement for foreground service

## Customization

### Adding New Layers

To add custom keyboard layers:

1. Define the layer in `KeyboardLayers.kt`:
```kotlin
private fun createCustomLayer(): KeyboardLayer {
    val leftKeys = listOf(
        listOf(Key("a"), Key("b"), Key("c")),
        // ... more rows
    )
    val rightKeys = listOf(
        listOf(Key("d"), Key("e"), Key("f")),
        // ... more rows
    )
    return KeyboardLayer("custom", leftKeys, rightKeys)
}
```

2. Add to the layers map in `getDefaultLayers()`
3. Add layer switch logic in `SplitKeyboardService.handleKeyClick()`

### Customizing Appearance

Modify colors and styles in `SplitKeyboardView.kt` (IME mode) or `KeyboardPanelView.kt` (overlay mode):
- `keyPaint`: Normal key background color (#2C2C2C)
- `keyPressedPaint`: Pressed key background color (#4A4A4A)
- `textPaint`: Key label text color (WHITE)
- `borderPaint`: Key border color (#1A1A1A)
- `backgroundPaint`: Panel background color (#1A1A1A, overlay mode only)

### Implementing Text Input for Overlay Mode

The overlay mode currently shows Toast messages for key presses. To implement actual text input, you have several options:

1. **Accessibility Service**: Use AccessibilityService to inject text into focused fields
2. **Clipboard Injection**: Copy text to clipboard and simulate paste
3. **App Integration**: Integrate directly with specific apps

Example using AccessibilityService would require:
- Creating an AccessibilityService class
- Requesting accessibility permission
- Using `dispatchGesture()` to simulate typing

This is more complex than IME but necessary for the overlay approach.

## Known Limitations

### Overlay Mode
- Text input not yet implemented (currently shows toast messages)
- User must manually show/hide keyboard
- Requires explicit overlay permission grant
- May conflict with other overlay apps

### IME Mode
- Cannot achieve true side-by-side layout due to Android framework limitations
- InputMethodService windows are always positioned at the bottom of the screen
- App content is overlaid rather than resized

## Future Improvements

- [ ] Implement text input for overlay mode using AccessibilityService
- [ ] Add auto-hide/show triggers for overlay mode
- [ ] Add haptic feedback for key presses
- [ ] Implement key long-press actions
- [ ] Add support for swipe gestures
- [ ] Add theme customization options
- [ ] Persist overlay state across app restarts
- [ ] Add quick settings tile to toggle overlay
- [ ] Optimize performance and memory usage
- [ ] Add support for landscape orientation

## License

This project is open source and available for educational purposes.
