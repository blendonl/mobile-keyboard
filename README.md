# Split Keyboard for Android

A custom Android keyboard with a unique split design optimized for one-handed or two-handed thumb typing.

## Features

- **Split Layout**: Two keyboard panels positioned at the left and right edges of the screen
- **Configurable Width**: Adjust panel width from 10% to 30% of screen width (default: 15%)
- **Full Height**: Keyboard spans 100% of screen height for easy access
- **Multiple Layers**:
  - Default: Lowercase letters (a-z)
  - Shift: Uppercase letters (A-Z)
  - Numbers: Digits (0-9) and common symbols
  - Symbols: Special characters and symbols
- **Modern UI**: Built with Jetpack Compose for settings
- **Customizable**: Easy to extend with additional layers and key configurations

## Architecture

The keyboard is built with the following components:

### Data Models
- `KeyboardConfig`: Stores user preferences (width, current layer)
- `Key`: Represents individual keys with labels, types, and actions
- `KeyboardLayer`: Defines key layouts for each layer (default, shift, numbers, symbols)

### UI Components
- `SplitKeyboardView`: Custom View that renders the split keyboard with touch handling
- `SettingsActivity`: Compose-based settings UI for configuration

### Service
- `SplitKeyboardService`: Main InputMethodService that manages the keyboard lifecycle

## Setup

1. Install the app on an Android device (API 31+)
2. Open the app and tap "Enable Keyboard"
3. Enable "Split Keyboard" in system settings
4. Tap "Select Keyboard" and choose "Split Keyboard"
5. Adjust width settings as desired

## Building

```bash
./gradlew assembleDebug
```

## Requirements

- Android 12 (API 31) or higher
- Kotlin 1.9.20
- Jetpack Compose

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

Modify colors and styles in `SplitKeyboardView.kt`:
- `keyPaint`: Normal key background color
- `keyPressedPaint`: Pressed key background color
- `textPaint`: Key label text color
- `borderPaint`: Key border color

## License

This project is open source and available for educational purposes.
