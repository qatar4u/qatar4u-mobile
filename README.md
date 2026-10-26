# Qatar4u Mobile

Qatar4u • 1.0.0 preview

Light Android phone launcher forked from Qatar4u TV, with circular icons, Arabic/English, visible-app management and device care. Independent application ID: net.qatar4u.mobile.

## Candidate build

The mobile-build workflow runs tests and lint before uploading a debug-signed candidate APK and SHA256 checksum to Actions artifacts. It does not create a release, update main, or install on any phone. This preview signature is not a production release identity; production signing must be configured before stable distribution.

Build locally with JDK 17 and Android SDK 34:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

See docs/mobile/STATUS.md for implementation and validation limits. docs/mobile/preview.html is a conceptual interactive preview, not a native-device screenshot.

## Mobile features

Four-column circular launcher, saved favorites and language, four static color themes, five bundled Qatar wallpapers and a custom-image picker. Local Umm al-Qura prayer calculation with city selection, next-prayer emphasis, and optional foreground-only hourly weather. Visible user/system app manager, Android-mediated uninstall, internal-cache cleanup, device memory/storage, and system settings shortcuts. No advanced root or Shizuku mode.

[Arabic installation and rollback](docs/mobile/INSTALL_AR.md) · [English installation and rollback](docs/mobile/INSTALL_EN.md).

Derived from qatar4u/qatar4u-tv; upstream device-test claims do not apply to this mobile edition. Physical-device performance, visual verification and OEM compatibility remain pending.
