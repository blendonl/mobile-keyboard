# Android System-Level Layout Limitations

## Your Request
You want the keyboard to **literally split** and **take space on the layout of Android screen** so that:
- Home screen is constrained to middle 70%
- ALL apps are constrained to middle 70%
- Left and right keyboard panels physically reserve screen space
- Layout: `[Left Keyboard 15%] | [All Apps 70%] | [Right Keyboard 15%]`

## The Hard Truth: Android Architecture Limitation

**This is IMPOSSIBLE for regular apps without root access or system-level modifications.**

### Why It's Impossible

#### 1. **Window Insets Are System-Privileged**

Android's system UI elements that reserve space (status bar, navigation bar) use:
- Special window types: `TYPE_STATUS_BAR`, `TYPE_NAVIGATION_BAR`
- Require signature-level permission: `INTERNAL_SYSTEM_WINDOW`
- Only available to system apps signed with the platform key
- Cannot be used by regular apps

#### 2. **WindowManager Doesn't Support Horizontal App Constraints**

The Android WindowManager architecture:
- Only supports vertical insets (top/bottom)
- `WindowInsets` provides: `contentTopInsets`, `systemBars`, etc.
- **NO API for horizontal insets** (`contentLeftInsets` doesn't exist)
- Apps can only be pushed up/down, not left/right

#### 3. **TYPE_APPLICATION_OVERLAY Doesn't Reserve Space**

Regular apps can create overlays with `TYPE_APPLICATION_OVERLAY`, but:
- They float **on top** of other apps
- They don't resize or constrain other app windows
- They're purely visual overlays
- Apps underneath don't know they exist

#### 4. **InputMethodService Is Bottom-Only**

The IME (keyboard) framework:
- Only supports keyboards at the **bottom** of the screen
- Can only reserve **vertical** space (push content up)
- No mechanism for side keyboards
- Android 11+ added some flexibility, but still bottom-oriented

### What Would Be Required

To achieve true system-level horizontal space reservation, you would need:

1. **Custom ROM / System Modification**
   - Modify Android framework's `WindowManagerService`
   - Add horizontal inset support
   - Recompile AOSP with changes

2. **Root Access + Xposed/Magisk Module**
   - Hook into WindowManager at runtime
   - Inject horizontal insets
   - Force apps to resize

3. **Device Manufacturer Implementation**
   - Samsung, OnePlus, etc. could add this feature
   - Similar to their custom UI features
   - Requires custom Android build

## What We CAN Do: Best Possible Solution

Since true system-level reservation is impossible, here's the best achievable approach:

### Approach 1: Persistent Overlays + Cooperative Apps ⭐ (IMPLEMENTED)

**What it does:**
- Keyboard creates persistent overlay panels on left and right edges
- Panels are solid (not transparent) and always visible
- Keyboard broadcasts its state when shown/hidden
- **Cooperative apps** listen to broadcasts and resize themselves
- Creates visual effect of side-by-side layout for apps that cooperate

**Files:**
- `KeyboardOverlayService.kt` - Creates solid overlay panels
- `SampleLauncherActivity.kt` - Example app that resizes itself

**Limitations:**
- Non-cooperative apps will still be obscured by keyboard panels
- Not true system enforcement
- Each app must implement the listening logic

**Benefits:**
- Works without root
- Apps you control can fully cooperate
- Best achievable solution on stock Android

### Approach 2: Container Activity (Single-App Solution)

**What it does:**
- Special activity with proper layout: `[Left Keyboard | Content | Right Keyboard]`
- TRUE horizontal layout within that one activity
- Only works for content you control

**Files:**
- `KeyboardContainerActivity.kt`

**Limitations:**
- Only works for your own app content
- Doesn't affect home screen or other apps

**Benefits:**
- True layout (not overlay) within the activity
- Guaranteed to work

### Approach 3: Custom Launcher (Partial Solution)

**What it does:**
- Create your own launcher/home screen
- Launcher respects keyboard space
- Other apps must still cooperate individually

**Limitations:**
- Only affects home screen, not all apps
- Doesn't solve the core Android limitation

## Implementation Status

✅ **Implemented:**
1. Persistent overlay keyboard panels (`KeyboardOverlayService`)
2. Broadcast API for keyboard state (`ACTION_KEYBOARD_STATE_CHANGED`)
3. Sample keyboard-aware app (`SampleLauncherActivity`)
4. Container activity with true layout (`KeyboardContainerActivity`)

## How to Use the Current Solution

### 1. Enable Keyboard Overlays

```kotlin
// From Settings:
Settings → Overlay Mode → Grant Overlay Permission → Show Keyboard

// Or programmatically:
val intent = Intent(context, KeyboardOverlayService::class.java).apply {
    action = KeyboardOverlayService.ACTION_SHOW
}
startForegroundService(intent)
```

### 2. Make Your Apps Keyboard-Aware

```kotlin
class YourActivity : AppCompatActivity() {

    private val keyboardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isVisible = intent?.getBooleanExtra(
                KeyboardOverlayService.EXTRA_IS_VISIBLE, false
            ) ?: false

            val panelWidth = intent?.getIntExtra(
                KeyboardOverlayService.EXTRA_PANEL_WIDTH, 0
            ) ?: 0

            // Update your UI to avoid keyboard areas
            if (isVisible) {
                yourContentView.setPadding(panelWidth, 0, panelWidth, 0)
            } else {
                yourContentView.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(KeyboardOverlayService.ACTION_KEYBOARD_STATE_CHANGED)
        registerReceiver(keyboardReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(keyboardReceiver)
    }
}
```

### 3. Test with Sample App

```
Settings → Overlay Mode → Open Sample Keyboard-Aware App
```

This demonstrates an app that properly avoids keyboard areas.

## Alternative Solutions (Require System Modifications)

### Option A: Build Custom ROM

1. Download AOSP source
2. Modify `frameworks/base/services/core/java/com/android/server/wm/WindowManagerService.java`
3. Add horizontal inset support
4. Build and flash custom ROM

### Option B: Magisk/Xposed Module (Requires Root)

1. Hook `WindowManagerService`
2. Inject custom insets for all app windows
3. Force apps to resize horizontally

### Option C: Convince Google

File a feature request with Android:
https://issuetracker.google.com/issues/new?component=192698

Request: "WindowInsets API for horizontal space reservation by non-system apps"

## Conclusion

The solution implemented provides the **best possible approach** within Android's architectural constraints:
- Solid keyboard overlays that appear to take space
- Broadcast API for cooperative apps
- Sample implementation showing how apps should respond
- Works on all stock Android devices without root

For **true system-level enforcement** where ALL apps are constrained, you would need:
- Custom ROM, or
- Root + system hooks, or
- Device manufacturer implementation

The current implementation is the **maximum achievable on stock Android** for regular apps.

## See Also

- `LAYOUT_SOLUTION.md` - Original technical documentation
- `SampleLauncherActivity.kt` - Reference implementation for keyboard-aware apps
- `KeyboardOverlayService.kt` - Overlay service with broadcast API
