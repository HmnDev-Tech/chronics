# Chronics Wiki

Chronics is an offline notification archive for Android. It captures the full
text of notifications and keeps a searchable copy in a local database and in
`Downloads/Chronics`, so messages survive deletion from the chat or the shade.

## Start here

- [Installation](Installation) — get the APK and keep updates working.
- [Permissions](Permissions) — what each access does and how to revoke it.
- [Privacy](Privacy) — where data lives and what leaves the device (nothing).
- [Troubleshooting](Troubleshooting) — common problems and fixes.
- [Building and signing](Building) — CI, local builds, stable signature.

## At a glance

| | |
| --- | --- |
| Platform | Android 8.0 (API 26) and newer |
| Language | Kotlin, Jetpack Compose, Material 3 Expressive |
| Storage | App-private SQLite + `Downloads/Chronics` |
| Network | None. The app declares no internet permission |
| Languages | English, Russian, Kazakh, Spanish, Chinese, Portuguese |
| License | GNU GPL v3.0 |

## How capture works

1. **Notification listener** is the primary source. It reads title, text,
   `bigText`, conversation, channel, actions and attachments.
2. **Accessibility service** is a fallback. It catches text when a notification
   disappears before the listener sees it.
3. **Shizuku** is optional. It grants the two accesses above from a shell and can
   import whatever is currently in the shade.

Entries are filtered, optionally masked, written to SQLite, and exported as JSON
and TXT when auto-export is on.

## Repository

- Source, issues and pull requests: <https://github.com/HmnDev-Tech/chronics>
- APK builds: Actions tab → latest *Build Chronics debug APK* run
- Security reports: [SECURITY.md](https://github.com/HmnDev-Tech/chronics/blob/main/SECURITY.md)
