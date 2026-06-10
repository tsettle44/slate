# AGENTS.md

## Cursor Cloud specific instructions

### Product overview

Slate is a Kotlin/Jetpack Compose Android launcher and device-policy app under `android/`. The only runnable product in the repo today is the Android app (`com.slate.phone`). Companion web/bridge services are planned but not implemented.

### Prerequisites (one-time VM setup)

- **JDK 17** — required by `android/app/build.gradle.kts` (`jvmTarget = "17"`). Use `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`.
- **Android SDK** — install command-line tools to `$HOME/Android/Sdk`, accept licenses, then install:
  - `platform-tools`
  - `platforms;android-35`
  - `build-tools;35.0.0`
  - `emulator` + `system-images;android-35;google_apis;x86_64` (optional, for emulator testing)
- **`android/local.properties`** — gitignored; must contain `sdk.dir=<ANDROID_HOME path>`. The update script auto-creates this if `$ANDROID_HOME` (or `$HOME/Android/Sdk`) exists.

### Environment variables

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
```

### Build / lint / test

All commands run from `android/`:

| Task | Command |
|------|---------|
| Debug APK | `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `./gradlew assembleRelease` |
| Lint | `./gradlew lint` |
| Unit tests | `./gradlew test` (no test sources yet; task succeeds with NO-SOURCE) |

First build downloads Gradle 8.9 and Android dependencies; expect ~2 minutes.

### Running on emulator (cloud VM caveats)

This VM has **no KVM** (`/dev/kvm` absent). The emulator runs with software rendering and is very slow (~8–10 min cold boot).

```bash
# Create AVD once
echo "no" | avdmanager create avd -n slate_api35 -k "system-images;android-35;google_apis;x86_64" -d pixel_6 --force

# Start (use -wipe-data on first run or if install fails with storage errors)
emulator -avd slate_api35 -wipe-data -no-audio -no-boot-anim \
  -gpu swiftshader_indirect -accel off -no-snapshot-load -no-snapshot-save &

# Wait for boot, then install and launch
adb wait-for-device
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.slate.phone/.launcher.LauncherActivity
```

**Physical device** is preferred for Device Owner provisioning and full policy enforcement. Without Device Owner, the app runs in dev mode (launcher UI works; enforcement disabled; banner shown).

### Device Owner provisioning (physical device only)

After factory reset with no accounts:

```bash
adb shell dpm set-device-owner com.slate.phone/.admin.SlateDeviceAdminReceiver
```

### Gotchas

- Gradle may auto-install `build-tools;34.0.0` on first build even though compile SDK is 35.
- Emulator without KVM can show "Process system isn't responding" during heavy startup; wait or use `-wipe-data`.
- `local.properties` and `keystore.properties` are gitignored — never commit them.
