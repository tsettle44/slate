# Slate Android

Minimal launcher and device policy app for Samsung Galaxy S22 (Android 12+).

## Build

```bash
cd android
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Device Owner provisioning

After a factory reset (no accounts on device during provisioning):

```bash
adb shell dpm set-device-owner com.slate.phone/.admin.SlateDeviceAdminReceiver
```

Then set Slate as the default home app in Android settings.

## Dev mode

Without Device Owner, Slate runs in dev mode: the launcher UI works but package hiding and install restrictions are not enforced. A banner is shown at the top of the screen.

## Project structure

See [../docs/technical-spec.md](../docs/technical-spec.md) for architecture, policy model, and sprint plan.
