# Qatar4u Mobile device trial

Open the latest successful main build in GitHub Actions and download Qatar4u-Mobile-preview. Extract the ZIP and compare the APK's SHA256 against SHA256SUMS. This is a debug-signed test build, not a store release. A stable signing identity is required before production updates; never clear user data just to work around a signature mismatch.

After authorizing installation, run from Windows platform-tools, adjusting the APK path:

```powershell
.\adb.exe devices -l
.\adb.exe install -r "$env:USERPROFILE\Downloads\Qatar4u-Mobile-preview\Qatar4u-Mobile-1.1.0-preview.apk"
.\adb.exe shell am start -n net.qatar4u.mobile/net.qatar4u.mobile.MainActivity
```

Test Arabic/English and RTL, search, favorites, user/system filters, opening apps and returning from Android settings. Check all nine bundled background choices and custom images, then restart the app to confirm persistence. Select the correct city and verify calculated prayer times against the city's official timetable. Weather is off by default and sends the selected city center to Open-Meteo only when enabled; hourly refresh happens only while the launcher is visible.

Cache cleanup affects this app only. App removal requires confirmation and may delete local data. There is no root, Shizuku, silent uninstall, automatic disabling or RAM booster.

Rollback: Android Settings > Default apps > Home app > choose your original launcher. Keep the original launcher installed. Disable USB debugging after testing, and revoke debugging authorizations if desired.

HTML is a conceptual design preview, not a device screenshot. Unit tests and lint do not validate OEM behavior, real-device performance or visual layout. Device-reported hardware details are not independent hardware certification.
