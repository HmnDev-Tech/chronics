# Chronics

**A local, offline notification archive for Android.** Chronics keeps the full
text of every notification in its own database and in `Downloads/Chronics`, so a
message stays readable after it has been deleted from the chat or wiped from the
shade.

No account, no cloud, no network access.

[![Build](https://github.com/HmnDev-Tech/chronics/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/HmnDev-Tech/chronics/actions/workflows/android.yml)
![Platform](https://img.shields.io/badge/Android-8.0%2B-3DDC84)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF)
![Compose](https://img.shields.io/badge/Compose%20Material%203-Expressive-4285F4)

---

## Why

Chat apps delete messages, notifications disappear, and the text you needed is
gone. Chronics is a paper archive with a pulse: it captures notifications the
moment they arrive and keeps a searchable, exportable copy on your own device.

## Features

| Area | What it does |
| --- | --- |
| **Notification listener** | Title, text, `bigText`, `subText`, conversation, channel, group, priority, actions, attached photos from `EXTRA_PICTURE` and `EXTRA_PICTURE_ICON`, and `MessagingStyle` messages with senders and timestamps. Avatars are not saved. |
| **Accessibility service** | Fallback source: reads notification event text and, optionally, the open System UI shade. |
| **Shizuku** | Grants notification-listener and accessibility access through shell and imports the current shade via `dumpsys notification --noredact`. |
| **Downloads export** | One JSON and one TXT per notification, images under `media/`, full archive backup under `backup/`. MediaStore on Android 10+, direct shared-folder writes on Android 8–9. |
| **Filters** | App blocklist/allowlist, stop words, required words, ignore ongoing and silent notifications, duplicate merging. |
| **Privacy** | Masking of OTP codes, phone numbers, e-mail addresses and card numbers. |
| **Journal** | Day-by-day timeline, search, pinning, archive, and a detail screen with messages, attachments, raw extras and text copying. |
| **Pulse** | Statistics by hour and day, top apps, capture sources, archive fill. |
| **Languages** | Full interface in English, Russian, Kazakh, Spanish, Chinese and Portuguese, switchable in Settings. |
| **Adaptive sizing** | Paddings, cards, hero panel and dock scale from the real screen size and density, with a fine-tuning slider and centered content on wide screens. |

## Install

1. Open the **Actions** tab and pick the latest successful *Build Chronics debug APK* run.
2. Download the `chronics-debug-apk` artifact and unzip it.
3. Install `app-debug.apk`. Android will ask to allow installs from your browser
   or file manager the first time.

Every APK is signed with the same key, so later builds install as an update over
the previous one — no uninstall, no lost data.

> **One-time note:** builds published before the stable key was introduced were
> signed with a throwaway debug key. If you installed one of those, uninstall it
> once; every build after that updates in place.

## First run

1. Allow notifications (Android 13+).
2. Open onboarding and grant **notification access** — this is the main source.
3. Optionally enable the **accessibility service** and **Shizuku**.
4. Tap **Send test notification** and check that the entry appears in the journal.

## Permissions

| Permission | Why |
| --- | --- |
| Notification listener access | Reads the full text of notifications. Can be revoked at any time in system settings. |
| Accessibility service (optional) | Catches text when a notification disappears before the listener reads it. |
| `POST_NOTIFICATIONS` | Shows the test notification and the service status. |
| `QUERY_ALL_PACKAGES` | Resolves app names and icons for the filter picker. No data leaves the device. |
| Storage (Android 8–9 only) | Writes exports directly to the shared Downloads folder. Android 10+ uses MediaStore. |
| Shizuku (optional) | Grants the two accesses above without opening system settings. |

## Where the data lives

| Data | Location |
| --- | --- |
| Journal | App-private SQLite database |
| Exports | `Downloads/Chronics/` |
| Images | `Downloads/Chronics/media/` |
| Full backup | `Downloads/Chronics/backup/` |

Nothing is uploaded anywhere. The app declares no internet permission.

## Build

No local Android SDK is required — CI builds every push to `main`:

1. `./gradlew testDebugUnitTest` — unit tests for text parsing, fingerprints, time formatting and the string catalogs.
2. `./gradlew assembleDebug` — signed debug APK.
3. The APK is published as the `chronics-debug-apk` artifact (kept for 30 days).
4. The run summary prints the signing certificate fingerprint so you can confirm it never changes.

Local build (optional): JDK 17, Android SDK with platform `android-37.0` and
build-tools `36.0.0`, then `./gradlew assembleDebug`.

### Signing

CI restores a PKCS12 keystore from repository secrets and passes it to Gradle:

| Secret | Meaning |
| --- | --- |
| `CHRONICS_KEYSTORE_BASE64` | Base64 of the keystore |
| `CHRONICS_STORE_PASSWORD` | Keystore password |
| `CHRONICS_KEY_ALIAS` | Key alias |
| `CHRONICS_KEY_PASSWORD` | Key password |

Without those secrets the build still succeeds, but it falls back to the default
debug key and the resulting APK will not install over a previous one. See
[CONTRIBUTING.md](CONTRIBUTING.md) for local signing.

Stack: Kotlin 2.2.20, AGP 9.1.1, Gradle 9.3.1, Compose Material 3 1.5.0-beta02
(Expressive), minSdk 26, compileSdk/targetSdk 37, JDK 17.

## Project structure

```
app/src/main/java/com/hmndev/chronika/
  data/       SQLite, repository, settings, models
  i18n/       string catalogs for six languages and the runtime resolver
  service/    notification listener, accessibility, capture engine
  shizuku/    permissions, shell, dumpsys import
  util/       notification parser, export, time, masking
  ui/         Compose screens, components, theme, adaptive sizing
app/src/main/res/
  values*/    launcher and service labels per language
  drawable/   launcher foreground, monochrome icon, status icon
```

## Contributing

Issues and pull requests are welcome. Please read
[CONTRIBUTING.md](CONTRIBUTING.md) and the
[Code of Conduct](CODE_OF_CONDUCT.md) first.

- Bug reports use the issue template and include app version, Android version and device.
- Never paste real notification contents, phone numbers or OTP codes into an issue.
- Security problems go through [SECURITY.md](SECURITY.md), not the public tracker.

## Security

Chronics handles sensitive message text. The threat model, supported versions
and the private reporting channel are described in [SECURITY.md](SECURITY.md).

## License

No license has been chosen yet. Until one is added, the code is all rights
reserved and may not be redistributed. This section will be updated before the
repository becomes public.
