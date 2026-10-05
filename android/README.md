# Youth Orchestra Android app

Android 8.0 or newer. This is a signed installable Android application, with all tuning and pitch-player assets bundled for first-launch offline use. The matching website is https://a440-studio-tuner.k-lee-augsburg-2018.chatgpt.site.

Install the APK on Android and allow microphone permission when requested. YouTube guides and the website link require internet. Start with low device volume for reference tones. Built as a release APK with debugging disabled and Android SDK R8 shrinking and obfuscation for the native Java wrapper. JavaScript bundled in any client app remains recoverable; this is not enterprise RASP or theft-proof protection. No advertising or analytics SDKs are included. Only the bundled origin can request microphone access; other WebView requests are blocked. External allowlisted links open in the device's YouTube app or browser.

Version 1.0.1 adds adjustable tone volume and increases default gain from 0.08 to 0.4. The maximum gain is 0.8.

To rebuild, install JDK 17, official Android build-tools 35.0.0 and platform android-35. Download Eclipse ECJ 3.39.0 from Maven Central and place it as ecj.jar at the toolchain root. Set ANDROID_TOOLCHAIN to the toolchain directory with `tools/android-15` and `platform/android-35`, then run `python3 build-local.py`. The script generates an app signing key on first build. Preserve the original release key to ship updates that install over this APK. Never commit private signing material to a public repository.

The UI and synthetic pitch detector were checked. No Android device or emulator was available for end-to-end microphone testing; validate on hardware before distributing to students. Website sharing remains private until the owner changes its access settings.
