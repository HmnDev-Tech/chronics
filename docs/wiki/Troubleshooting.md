# Troubleshooting

## The journal stays empty

1. Check **Settings → Access → Notification listener** — it must show **Allowed**.
2. Make sure **Settings → Capture → Capture enabled** is on.
3. Send a test notification from Settings and look for the entry.
4. Some apps post notifications only when the screen is off or the app is in the
   background. Trigger a real message and check again.

## A notification was captured but the text is short

The listener stores what the app provides. If the app puts only a summary in
`text`, enable the **accessibility service** as a fallback, or turn on
**Deep capture** to read the open shade.

## The accessibility service turns itself off

Android disables accessibility services after some system updates or when a
battery optimizer kills them. Re-enable it in **Settings → Accessibility →
Chronics**.

## Shizuku says “not running”

- Open the Shizuku app and start the service.
- Return to Chronics → **Settings → Access → Allow Shizuku** and approve.
- If the request does not appear, restart Shizuku and try again.

## “App not installed” when updating

The APK you are installing is signed with a different key than the installed
app. Uninstall Chronics once and install the new APK. Builds from the same CI
pipeline share a stable key, so this should not happen again.

## Exports are missing from Downloads

- Android 10+: check the Downloads app, folder `Chronics`.
- Android 8–9: the app needs storage permission; grant it in system settings.
- If auto-export is off, use **Settings → Storage → Export everything**.
- Files exported before the rename live in `Downloads/Chronika` and are not
  moved automatically. Copy them into `Downloads/Chronics` if you want them next
  to new exports.

## The language did not change

Changing the language restarts the activity. If the interface stays the same,
close the app from recents and open it again. System-level labels such as the
accessibility service name follow the system language, not the in-app choice.

## The app looks too large or too small

Open **Settings → Screen**. Toggle **Adaptive sizing** off for the reference
layout, or move the **Fine-tuning** slider. The section also shows the detected
screen size and density.

## Search finds nothing

Search matches the title and text of entries. Clear the filter chips above the
timeline and make sure the entry is not archived.
