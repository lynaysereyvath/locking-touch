# Floating Lock

A tiny Android app, like iOS AssistiveTouch with a single option: **Lock screen**.
A draggable button floats over every app. Tap it, tap *Lock screen*, the phone locks.

Built with Jetpack Compose. Min SDK 28 (Android 9).

## Run it

1. Open this folder in Android Studio (Ladybug or newer). It will generate the Gradle wrapper and sync.
2. Run on a device or emulator running Android 9+.
3. In the app, tap **Open accessibility settings**, find **Floating Lock**, turn it on.
4. The button appears. Drag it, tap it, choose **Lock screen**.

## How it works

| Piece | Job |
|---|---|
| `LockAccessibilityService` | The engine. Draws the button and calls `performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)`. |
| `OverlayController` | Adds/removes two `TYPE_ACCESSIBILITY_OVERLAY` windows: the bubble and the full-screen menu. Handles edge snapping and rotation. |
| `DragTouchLayout` | Root view of the bubble window. Uses raw screen coordinates so dragging a moving window doesn't jitter. |
| `OverlayLifecycleOwner` | Gives Compose the Lifecycle/SavedState owners it needs when running inside a Service instead of an Activity. |
| `FloatingUi.kt` | The Compose UI for the bubble and the menu. |
| `SetupScreen` | The in-app setup screen: enable the service, toggle the button. |

Because the overlay is owned by the accessibility service:
- no `SYSTEM_ALERT_WINDOW` ("draw over other apps") permission
- no foreground service or persistent notification
- the button lives exactly as long as the service is enabled

The service requests no event types and cannot read window content (see `res/xml/accessibility_service_config.xml`).

## Things to know

- **Sideloaded installs, Android 13+:** the accessibility toggle may be greyed out. App info → ⋮ → *Allow restricted settings*. The setup screen explains this.
- **Google Play:** Play restricts the Accessibility API to apps that need it, and a lock-screen utility has to go through the Accessibility declaration review. If that's a concern, the alternative is a Device Admin receiver calling `DevicePolicyManager.lockNow()` (works on all API levels, needs a different permission flow).
- **The button doesn't appear on the lock screen** or over some secure system screens. That's an Android restriction.
- Some manufacturers aggressively kill background processes. If the button disappears, exclude the app from battery optimisation.
- No launcher icon is included. Add one with *New → Image Asset* in Android Studio.
