# Chronics

A local notification archive for Android. Chronics stores the full text of every
notification in its own database and in the `Downloads/Chronics` folder, so a
message stays available even after it has been deleted from the chat or wiped
from the notification shade.

The app sends nothing to the network and requires no account.

## Features

- **Notification listener** — title, text, `bigText`, `subText`, conversation,
  channel, group, priority, actions, attachments (`largeIcon`, `picture`), and
  `MessagingStyle` messages with senders and timestamps.
- **Accessibility service** — a fallback source: reads notification event text
  and, optionally, the open System UI shade.
- **Shizuku** — grants notification-listener and accessibility access through
  shell, and imports the current shade via `dumpsys notification --noredact`.
- **Downloads export** — a JSON and TXT file per notification, images under
  `media/`, and a full archive backup under `backup/`. MediaStore is used on
  Android 10+, direct writes to the shared folder on Android 8–9.
- **Filters** — app blocklist/allowlist, stop words, required words, ignoring
  ongoing and silent notifications, duplicate merging.
- **Privacy** — masking of OTP codes, phone numbers, e-mail addresses, and card
  numbers.
- **Journal** — day-by-day timeline, search, pinning, archive, an entry screen
  with messages, attachments, raw extras, and text copying.
- **Pulse** — statistics by hour and day, top apps, sources, archive fill.
- **Languages** — the whole interface is translated into English, Russian,
  Kazakh, Spanish, Chinese, and Portuguese. The language can be changed in
  Settings and is applied immediately.
- **Adaptive sizing** — paddings, cards, the hero panel, and the floating dock
  are scaled from the real screen size and density of the phone, with a
  fine-tuning slider in Settings. Wide screens get centered content.
- **Settings** — language, adaptive sizing, theme, dynamic color, retention
  period, entry limit, folder, auto-export, masking.

## Build

No local build is required. Every push to `main` starts GitHub Actions:

1. `./gradlew testDebugUnitTest` — unit tests for text parsing, fingerprints,
   and time formatting.
2. `./gradlew assembleDebug` — debug APK.
3. The APK is published as the `chronics-debug-apk` artifact (kept for 30 days).

Download: the **Actions** tab → latest run → **Artifacts** →
`chronics-debug-apk`.

Stack: Kotlin 2.2.20, AGP 9.1.1, Gradle 9.3.1, Compose Material 3 1.5.0-beta02
(Expressive), minSdk 26, compileSdk/targetSdk 37, JDK 17.

## First run

1. Install the APK.
2. Allow notifications (Android 13+).
3. Open onboarding and grant **notification access**.
4. Optionally enable the **accessibility service** and **Shizuku**.
5. Tap “Send test notification” — an entry should appear in the journal.

## Structure

```
app/src/main/java/com/hmndev/chronika/
  data/       SQLite, repository, settings, models
  i18n/       string catalogs for six languages and the runtime resolver
  service/    notification listener, accessibility, capture engine
  shizuku/    permissions, shell, dumpsys import
  util/       notification parser, export, time, masking
  ui/         Compose screens, components, theme, adaptive sizing
```

## Privacy

All data stays on the device. The `QUERY_ALL_PACKAGES` permission is used only
to show the app list in filters. The app needs no network access.
