# Sonic Shielding for Android

Sonic Shielding for Android is a native, local-only audio comfort app inspired by the Sonic Shielding browser extension.

## Controls

- Tap the launcher icon to toggle protection on or off.
- Long-press the launcher icon and choose **Comfort profile** to edit the nine-band EQ.
- Add the **Sonic Shielding** Quick Settings tile for a visible on/off control.
- Tapping the launcher icon saves the active blocker combination and turns all blockers off; tapping again restores that exact combination.
- Test each clearly labeled frequency range with a quiet, one-second representative tone.
- Configure beep blocking, speech protection, aggressive alarm blocking, sudden-sound reduction, and the permanent comfort EQ using the original Sonic Shielding profile model.
- The protection-strength scale is calibrated so 25% matches the original maximum attenuation, 50% matches the previously doubled maximum, and 100% requests four equal DSP stages on supported devices.

## Android capability and limitations

The app uses Android's `Equalizer` audio effect on output-mix session `0`. That is the broadest local, store-compatible output control exposed to third-party apps, but support is device/manufacturer-dependent. Some Android builds block or ignore global effects.

Android does **not** allow an ordinary store app to capture and rewrite every other app's audio. The Android edition maps the original protection profile to Android's output-mix equalizer where a device exposes it; adaptive detection settings are retained for compatible audio paths, but cannot be guaranteed across every app. It reports when the platform effect is unavailable and requests no microphone or media-projection permission.

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
2. Long-press the icon and choose **Comfort profile**.
3. Optionally enable and adjust **Permanent comfort EQ** to suit your comfort profile.
4. Optionally add Sonic Shielding from the Quick Settings tile editor.

By default, the app shows an ongoing notification because Android requires one to keep protection running reliably. **Keep protection running** in Comfort Profile explains and controls it. Turning that setting off removes the notification, but Android may release the audio effects when it reclaims the app process; tap the icon to restore them.

See [STORE_RELEASES.md](STORE_RELEASES.md) for multi-store release guidance and [PRIVACY.md](PRIVACY.md) for the privacy policy.
