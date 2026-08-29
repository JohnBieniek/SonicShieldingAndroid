# Store release guide

The project produces a standard Android App Bundle (`.aab`) for Google Play and an APK for stores that accept APK uploads.

## Before any public release

1. Replace the temporary vector launcher artwork with final production artwork.
2. Create and securely retain a release signing key. Never commit the keystore or passwords.
3. Configure Gradle signing from local or CI secrets.
4. Test output-mix equalizer behavior on representative Samsung, Google, Motorola, and Amazon devices.
5. Add store screenshots, feature graphics, support contact details, and a hosted copy of `PRIVACY.md`.
6. Complete each store's current audio-effect, MediaProjection foreground-service, playback-capture, and data-safety declarations accurately. Google Play requires a foreground-service declaration and demonstration video for the media-projection use case.

## Distribution targets

- **Google Play:** Upload the signed AAB to Play Console. Use internal testing before production.
- **Amazon Appstore:** Upload a signed APK or supported bundle and test on Fire OS; audio-effect behavior can differ from standard Android.
- **Samsung Galaxy Store:** Upload a signed release through Seller Portal and complete Samsung device review.
- **F-Droid:** The source is suitable for an open-source submission once reproducible release signing/build metadata is prepared.

Store requirements change. Review the current official documentation immediately before submission.
