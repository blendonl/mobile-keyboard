# Layout Solution: True Side-by-Side Keyboard

## Problem
The keyboard was appearing **on top of** apps (overlay) instead of taking actual space in the layout, resulting in:
- Keyboard panels overlaying app content
- App window not resizing to accommodate keyboards
- Not a true `[Left Keyboard | App | Right Keyboard]` layout

## Android Limitation
**Important:** Android's `InputMethodService` (IME) framework **only supports bottom keyboards** that consume vertical space. There is no native way for an IME to:
- Reserve horizontal space
- Force apps to resize their windows horizontally
- Create true side-by-side layouts

The IME `Insets` API only provides:
- `contentTopInsets` - vertical space from top
- `visibleTopInsets` - visible vertical space

There is **no `contentLeftInsets` or `contentRightInsets`**.

## Solution: Three Approaches

### 1. ⭐ Container Mode (RECOMMENDED - True Layout)
**File:** `KeyboardContainerActivity.kt`

**How it works:**
- Creates a proper Android layout with three components in a `LinearLayout`
- True horizontal space allocation: `[Left 15%] | [Content 70%] | [Right 15%]`
- App content (WebView/custom view) is actually constrained to middle area
- Keyboard panels take real layout space

**Benefits:**
- ✅ TRUE side-by-side layout
- ✅ App content truly constrained to middle
- ✅ No overlaying - proper space reservation
- ✅ Works reliably on all Android versions

**Limitations:**
- Only works for apps you control (your own content)
- Not a general keyboard for all apps
- Requires launching the container activity

**Usage:**
```kotlin
// From Settings, click "Launch Container Mode"
// Or programmatically:
val intent = Intent(context, KeyboardContainerActivity::class.java).apply {
    putExtra(KeyboardContainerActivity.EXTRA_CONTENT_URL, "https://example.com")
}
startActivity(intent)
```

### 2. Overlay Mode (Visual Effect)
**File:** `KeyboardOverlayService.kt`

**How it works:**
- Uses `WindowManager` to create overlay windows (`TYPE_APPLICATION_OVERLAY`)
- Windows float on top of apps
- Creates visual appearance of side panels

**Benefits:**
- ✅ Works with any app
- ✅ Can be shown/hidden dynamically
- ✅ Good visual effect

**Limitations:**
- ❌ Overlays on top (doesn't resize app windows)
- ❌ App content may be obscured behind keyboards
- ❌ Not true layout-based positioning

**Usage:**
```kotlin
// Show overlay
val intent = Intent(context, KeyboardOverlayService::class.java).apply {
    action = KeyboardOverlayService.ACTION_SHOW
}
startForegroundService(intent)
```

### 3. IME Mode (Traditional - Visual Only)
**File:** `SplitKeyboardService.kt`

**How it works:**
- Standard Android InputMethodService
- Fullscreen transparent keyboard with side panels
- Touch regions only on sides, middle passes through

**Benefits:**
- ✅ Standard IME integration
- ✅ Automatic text input handling
- ✅ Works as a system keyboard

**Limitations:**
- ❌ Cannot create true horizontal layout (Android limitation)
- ❌ Fullscreen overlay approach
- ❌ App doesn't resize horizontally

## Recommendation

**For your own apps/content:**
- Use **Container Mode** (KeyboardContainerActivity)
- Provides TRUE side-by-side layout as requested

**For general keyboard use:**
- Use **IME Mode** for text input functionality
- Accept that true horizontal layout is not possible due to Android constraints
- Or use **Overlay Mode** for visual effect

## Code Changes Made

### 1. Created Container Activity
```
app/src/main/java/com/splitkeyboard/ui/container/KeyboardContainerActivity.kt
```
- Implements proper LinearLayout: [Left Panel | WebView | Right Panel]
- Handles text input injection
- Configurable content URL

### 2. Updated AndroidManifest
- Added `KeyboardContainerActivity` registration
- Exported for external launching if needed

### 3. Updated Settings UI
- Added "Container Mode" option with star emoji
- Clearly labeled three different approaches
- Updated descriptions to set proper expectations

### 4. Restored IME Service Configuration
- Fullscreen mode for proper overlay
- Touchable regions on sides only
- Transparent middle section

## Testing

1. **Container Mode:**
   ```
   Settings > Launch Container Mode button
   ```
   - Should see three-column layout
   - App content constrained to middle 70%
   - Keyboard panels on sides taking actual space

2. **Overlay Mode:**
   ```
   Settings > Show Keyboard button (after granting overlay permission)
   ```
   - Keyboard panels float on top
   - Visual effect of side panels

3. **IME Mode:**
   ```
   Settings > Enable Keyboard > Select Keyboard
   ```
   - Use in any text field
   - Fullscreen transparent keyboard with side panels

## Future Enhancements

For Container Mode:
- [ ] Replace WebView with custom content provider
- [ ] Add support for different content types (not just WebView)
- [ ] Create intent filters for seamless integration
- [ ] Add configuration for different panel widths

For IME Mode:
- [ ] Explore Android R+ WindowInsets APIs
- [ ] Test with apps that respond to custom insets
- [ ] Document apps known to work well with side keyboards

## Summary

The **Container Mode** solution achieves the requested `[Left Keyboard | App | Right Keyboard]` layout with true space reservation. This is the only way to achieve true horizontal layout constraints on Android without system modifications. For general keyboard use across all apps, Android's limitations mean we can only achieve a visual approximation through overlays.
