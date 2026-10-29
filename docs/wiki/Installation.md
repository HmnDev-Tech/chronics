# Installation

## Requirements

- Android 8.0 (API 26) or newer.
- About 20 MB of free space plus room for the archive.
- No account, no Google Play, no network.

## Install from Releases

1. Open [Releases](https://github.com/HmnDev-Tech/chronics/releases).
2. Pick the latest release — currently **v1.0.0-beta01**.
3. Download `app-release.apk`.
4. Open the file. If Android blocks it, allow installs from your browser or file
   manager once, then tap **Install**.

## Install from GitHub Actions

Every successful `main` build also publishes the `chronics-release-apk` artifact:

1. Open the [Actions](https://github.com/HmnDev-Tech/chronics/actions) tab.
2. Pick the latest successful **Build Chronics APK** run.
3. Scroll to **Artifacts** and download `chronics-release-apk`.
4. Unzip it — the APK is `app-release.apk`.

## Updates

Every APK is signed with the same stable key, so a newer build installs as an
update over the old one. Settings, journal entries and exports are kept.

> **One-time reinstall.** Builds published before the stable key was introduced
> used a throwaway debug signature. If you installed one of those, uninstall the
> app once. Every build from then on updates in place.

If Android says *“App not installed”* or *“Package conflicts with an existing
package”*, the signatures differ. Uninstall the old copy, install the new one,
and check the certificate fingerprint printed in the CI run summary to confirm
future builds match.

## After installing

1. Allow notifications when asked (Android 13+).
2. Complete onboarding: grant **notification access** first.
3. Optionally enable the **accessibility service** and **Shizuku**.
4. Tap **Send test notification** and confirm the entry appears in the journal.

## Uninstalling

Uninstalling removes the app, the local database and the app-private images.
Files exported to `Downloads/Chronics` stay on the device — delete that folder
manually if you no longer need it.

## Version and build information

The app version is shown in **Settings → About**. The exact build corresponds to
the commit in the Actions run you downloaded.
