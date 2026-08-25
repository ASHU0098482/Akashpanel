# Mobile Panel v29

Non-root Android file replacement is handled through Shizuku.

1. Install and start Shizuku. On Android 11 or newer, pair it through Wireless debugging.
2. Install `MOBILE_PANEL.apk`.
3. Open Mobile Panel and allow its Shizuku permission request.
4. Open Free Fire MAX once so its real `Android/data/com.dts.freefiremax` directory exists, then close it.
5. Press Launch Panel. The app extracts the exact six-file tree supplied in `com.dts.freefiremax.zip` and merge-overwrites those matching paths only inside the real `Internal Storage/Android/data/com.dts.freefiremax/files` directory. Direct storage, root, Shizuku direct shell, and Shizuku UserService are tried as compatible fallbacks. All six target files are SHA-256 verified before Free Fire MAX is launched; a missing directory or failed verification is reported as an error instead of success. Unrelated game files are preserved.

Minimum supported Android version: Android 7.0 (API 24).
