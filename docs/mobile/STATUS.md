# Qatar4u Mobile — basic manager

Qatar4u. Native Android launcher fork of Qatar4u TV.

Implemented in source: circular launch icons, Arabic/English, visible launcher-app search and user/system filtering, system app-info navigation, user-app uninstall request with a prior warning and Android confirmation, internal-cache cleanup with confirmation, RAM/storage report, system storage/battery/developer/default-app/accessibility links. Refresh app list on resume after returning from Android settings.

No privileged mode, root, Shizuku integration, silent uninstall, automated disabling, or edits to another app's data. Android package visibility limits the list. Full management opens Android settings. Uninstall may delete local data; reinstall is not a data backup. System classification is not a recommendation to remove an app.

preview.html is an illustrative interactive design preview with explicitly fictional packages. It does not connect to the phone or execute Android operations. The native UI is implemented separately in MobileHome.kt; this is not a native screenshot.

Validation: source diff and XML reviewed. Gradle build and lint attempted, blocked before compilation by network access to services.gradle.org (Gradle 8.9 unavailable). No APK generated, no install, no device runtime test. Device owner's supplied readings: Android reports API 30 / Android 11, ARMv7, approximately 2 GB RAM, 720x1612; these are reported data, not independent hardware certification.

Build when Android SDK 34 and JDK 17 are available: ./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest

Installation is pending preview approval. Restore home screen using Android Settings > Default apps > Home app. No need to remove the old launcher.

Added persisted favorites, four-column launcher, four static theme backgrounds, five bundled Qatar backgrounds, and a custom-image document picker with persisted read access and sampled image decoding. Application ID updated before first installation to net.qatar4u.mobile. Repository: qatar4u/qatar4u-mobile. Native screenshot/device QA still pending successful compilation.

Server build review: initial SDK setup failure corrected; mobile-only manifest removes Leanback feature. Arabic/English device-trial guides added. Final server build outcome is recorded in GitHub Actions.

Validated candidate: commit 250ee3316420c2d5c4d975d482b344710bf0f0c4, Actions run 40284146672. testDebugUnitTest, lintDebug and assembleDebug all passed. Downloaded APK SHA256 matches generated SHA256SUMS: a16dbfec96c087f18ccaefd4ee037ad00bf4e3bd69d84d39afa7e77a564cf815. APK size 18,309,429 bytes. Native runtime/visual/performance checks on the physical phone remain pending. Earlier local-build failure notes describe the prior environment; the successful server build supersedes the compilation blocker.

Qatar theme 1.1.0: maroon #8A1538, snow white #F5F8FA, gold borders #C6A15B; light cards, circular outlined icons, Qatar-inspired nine-point header detail and redesigned navigation. Text gold uses a darker shade on snow for readability. Preview remains illustrative; native runtime screenshots pending.
