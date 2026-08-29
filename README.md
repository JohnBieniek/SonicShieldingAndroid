# Sonic Shielding for Android

Sonic Shielding for Android is a native, local-only audio comfort app inspired by the Sonic Shielding browser extension.

## Controls

- Tap the launcher icon to toggle your remembered protection profile on or off. This does not open Android's playback-capture prompt.
- Long-press the launcher icon and choose **Comfort profile** to edit the nine-band EQ.
- Add the **Sonic Shielding** Quick Settings tile for a visible on/off control.
- Tapping the launcher icon saves the active blocker combination and turns all blockers off; tapping again restores that exact combination.
- Test each clearly labeled frequency range with a quiet, one-second representative tone.
- Configure beep blocking, speech protection, aggressive alarm blocking, sudden-sound reduction, and the permanent comfort EQ using the original Sonic Shielding profile model.

## Android capability and limitations

The app uses Android's `Equalizer` audio effect on output-mix session `0`. That is the broadest local, store-compatible output control exposed to third-party apps, but support is device/manufacturer-dependent. Some Android builds block or ignore global effects.

On Android 10 and newer, the app can use Android's user-approved playback-capture workflow to analyze eligible media and game audio in memory. Stable prominent tones receive temporary output-EQ notches, and strict multi-peak alarm signatures can receive broader temporary attenuation. Captured samples are immediately discarded and are never saved or uploaded. Normal audio receives no beep/alarm filtering between detections; only the explicitly enabled Comfort EQ is permanent.

Playback capture cannot inspect protected audio, audio from apps that opt out, calls, alarms, notifications, or content in another Android user profile. Android shows its own consent dialog for every new capture session and requires an ongoing notification. Adaptive attenuation still depends on the manufacturer exposing an output-mix equalizer.

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

1. Tap the Sonic Shielding icon once to turn your remembered profile on.
2. Long-press the icon and choose **Comfort profile**.
3. Press **Start playback capture** only if you want adaptive beep and alarm detection, then approve Android's prompt. Android labels this screen sharing, but Sonic Shielding requests playback audio only and does not capture screen images or video.
4. Playback capture is optional. Without it, Beep Blocker uses a continuous upper-frequency fallback that provides attenuation but can muffle normal speech and music. With capture, it switches to adaptive beep and alarm detection.
5. Optionally enable and adjust **Permanent comfort EQ** to suit your comfort profile.
6. Optionally add Sonic Shielding from the Quick Settings tile editor.

Android requires an ongoing notification whenever adaptive playback capture is active. **Keep protection running** separately controls whether Comfort EQ remains active after the app closes.

See [STORE_RELEASES.md](STORE_RELEASES.md) for multi-store release guidance and [PRIVACY.md](PRIVACY.md) for the privacy policy.
