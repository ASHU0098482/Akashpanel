# Mobile Panel v26

Non-root Android file replacement is handled through Shizuku.

1. Install and start Shizuku. On Android 11 or newer, pair it through Wireless debugging.
2. Install `MOBILE_PANEL.apk`.
3. Open Mobile Panel and allow its Shizuku permission request.
4. Press Launch Panel. The app uses Shizuku's direct shell process (with UserService only as a compatibility fallback) to mirror the exact embedded eight-file tree into `Android/data/com.dts.freefiremax/files`, overwriting existing files and creating missing payload folders/files. It SHA-256 verifies all eight and launches Free Fire MAX only when every replacement succeeds. Normal Free Fire is skipped.

Minimum supported Android version: Android 7.0 (API 24).
