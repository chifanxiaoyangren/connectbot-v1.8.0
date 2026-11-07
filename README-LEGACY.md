# ConnectBot v1.8.0 legacy build (Android 2.2.2)

This directory is a separate, patched copy of the ConnectBot `v1.8.0` source
tree. It is **not** part of the current `~/Desktop/connectbot` main repository.
The current repository is a modern ConnectBot build with `minSdk` 24 and does
not contain these legacy changes.

## What this build is

- Upstream source: `git archive v1.8.0` from the ConnectBot repository.
- Build: Gradle 7.6.4, Android Gradle Plugin 7.4.2, JDK 17.
- Android SDK: `compileSdk 33`, `minSdk 4`, `targetSdk 15`.
- Output APK: `connectbot-v180-debug.apk` (debug-signed).
- APK SHA-256:
  `873d68f0c3ae9089a3388a8d8c08a8787a05edcb80569968850ef8badd17d5d5`

## Build

`local.properties` currently points at the SDK used on this machine:

```properties
sdk.dir=/Users/dww/android-build-env/sdk
```

Change it if the SDK is elsewhere, then run:

```sh
JAVA_HOME=/Users/dww/android-build-env/jdk-17.0.21+9/Contents/Home ./gradlew assembleDebug
```

The APK is written to:

```text
build/outputs/apk/debug/connectbot-v180-debug.apk
```

## Changes made for Android 2.2.2

### Build compatibility

- Added a modern Gradle build (`settings.gradle`, `build.gradle`).
- Copied the prebuilt native libraries from `libs/` to `jniLibs/`.
- Fixed `@+drawable/...` references rejected by AAPT2.
- Replaced removed `PixelXorXfermode` with `PorterDuffXfermode`.
- Replaced removed `Canvas.CLIP_SAVE_FLAG` with `Canvas.save()`.
- Replaced removed `Notification.setLatestEventInfo` with a reflective call.
- The APK is 32-bit only (`armeabi`, `x86`), matching the original native libs.

### SSH compatibility

- The server used during testing is OpenSSH 8.6, which offers an ECDSA host key.
- Android 2.2.2 has no `KeyFactory EC` or `SHA256withECDSA`, so ECDSA host keys
  fail with `No EC KeyFactory available`.
- `KexManager` now offers ECDSA only when the platform supports it. On Android
  2.x it falls back to `ssh-rsa`, which works on the tested server.

### Remember password

- Added a `password` BLOB column to the `hosts` database (DB version 23).
- Passwords are encrypted with AES before being stored. The per-install key is
  kept in the app-private `password_store` SharedPreferences.
- The password prompt has a `Remember password` / `记住密码` checkbox. It is
  unchecked by default.
- Saved passwords are tried before keyboard-interactive/password prompts.
- Supports both `password` auth and password-like keyboard-interactive prompts.
- A wrong saved password is cleared automatically.
- The console menu has `Forget saved password` / `忘记已保存的密码`.

Security note: Android 2.2.2 has no hardware-backed Keystore. The encryption is
app-private obfuscation, not strong protection against a rooted device. Use only
on a trusted device.

### Settings

- `Hide status bar` / `隐藏状态栏` (formerly `Full screen`) hides the system
  status bar in the console and the host list.
- `Font size` / `字体大小` adds a 6-24 pt `ListPreference` (default 10).
  Changing it updates all saved hosts and all currently open terminal bridges.
  Per-host font size settings still exist and can be changed afterwards.

### Home / kiosk mode

- `HostListActivity` now declares `android.intent.category.HOME` (plus
  `DEFAULT`) and uses `singleTask`.
- It can be selected as the default home app: press Home and choose ConnectBot.
- Back is ignored while it is acting as home, to avoid accidental exits.
- Full-screen mode applies to the host list as well.
- The host list menu has `System settings` / `系统设置` so the default home can
  be cleared without ADB.

To exit kiosk mode:

```text
Menu -> System settings -> Applications -> Manage applications ->
ConnectBot -> Clear defaults
```

Then press Home and choose the normal launcher.

This is not a security-grade kiosk. Android 2.2 has no screen pinning or device
owner support; notifications, recent apps and system settings can still leave
the app. Hiding the status bar reduces accidental access.

### Soft keyboard

- `ConsoleActivity` uses `stateAlwaysHidden|adjustResize`, so the soft keyboard
  no longer appears automatically. The hardware keyboard works directly; use the
  keyboard icon to show the soft keyboard when needed.

## Limitations / warnings

- Android 2.2.2 and the old Trilead SSH library have outdated cryptography.
- The server must allow `ssh-rsa` host keys (and whichever auth method you use).
- The app is debug-signed; it is not a Play Store release.
- The device was not rooted. System preinstalled apps cannot be removed or
  disabled on this build without root; `pm disable` is denied to the ADB shell.
- This tree includes build output paths only when built; the checked-in APK is
  a convenience copy.
