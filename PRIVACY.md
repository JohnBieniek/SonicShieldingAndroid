# Privacy policy

Sonic Shielding for Android stores the on/off state and comfort-profile settings locally on the user's device. The app does not collect, transmit, sell, or share personal information.

When the user explicitly starts adaptive protection, Android grants the app a temporary MediaProjection session for eligible playback audio. Sonic Shielding analyzes that playback in memory to detect tonal peaks and immediately discards the samples. It does not save, transmit, or share audio, and it does not open the device microphone as an audio source. Android requires the `RECORD_AUDIO` permission for playback capture even though this workflow analyzes device playback rather than microphone input. Protected content and apps that disallow playback capture are not accessible to Sonic Shielding.

Android may include the local preferences in the user's encrypted device backup, according to the user's Android backup settings.

Questions can be submitted through the repository's GitHub issue tracker.
