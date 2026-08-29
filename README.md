# Sonic Shielding for Android

Sonic Shielding for Android is a native, local-only audio comfort app inspired by the Sonic Shielding browser extension.

## Controls

- Tap the launcher icon to toggle protection on or off.
- Long-press the launcher icon and choose **Comfort profile** to edit the nine-band EQ.
- Add the **Sonic Shielding** Quick Settings tile for a visible on/off control.
- Use the ongoing notification to turn protection off or open the profile.

## Android capability and limitations

The app uses Android's `Equalizer` audio effect on output-mix session `0`. That is the broadest local, store-compatible output control exposed to third-party apps, but support is device/manufacturer-dependent. Some Android builds block or ignore global effects.

Android does **not** allow an ordinary store app to capture and rewrite every other app's audio. Consequently, the browser extension's adaptive beep detection and whole-sound peak limiter cannot operate globally in this app. The Android app implements the permanent comfort EQ and reports when the platform effect is unavailable. It requests no microphone or media-projection permission.

Sonic Shielding is a comfort tool, not a medical device, hearing test, or guarantee that symptoms will be prevented.

## Build

Requirements: JDK 17 and Android SDK 35.

```powershell
.\gradlew.bat test lint assembleDebug bundleRelease
```

Outputs:

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release bundle: `app/build/outputs/bundle/release/app-release.aab`

## Install a local debug build

Enable Developer options and USB debugging on the phone, connect it, then run:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Alternatively, copy the APK to the phone, open it, and approve **Install unknown apps** for the file manager when Android asks.

After installation:

1. Tap the Sonic Shielding icon once to turn protection on.
2. Approve notification permission so Android can keep the foreground protection service visible.
3. Long-press the icon and choose **Comfort profile**.
4. Adjust the enabled **Permanent comfort EQ** bands to suit your comfort profile.
5. Optionally add Sonic Shielding from the Quick Settings tile editor.

See [STORE_RELEASES.md](STORE_RELEASES.md) for multi-store release guidance and [PRIVACY.md](PRIVACY.md) for the privacy policy.
