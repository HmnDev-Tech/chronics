# Permissions

Chronics asks for the minimum it needs. Nothing is granted by default and every
access can be revoked later.

| Access | Required | What it does | How to revoke |
| --- | --- | --- | --- |
| Notification access | Yes, for capture | Reads title, text, `bigText`, conversation, channel, actions and attachments of notifications. | Settings → Apps → Special access → Notification access |
| Accessibility service | Optional | Fallback capture: reads notification event text and, if **deep capture** is on, the open shade. | Settings → Accessibility → Chronics |
| `POST_NOTIFICATIONS` | Yes, on Android 13+ | Shows the test notification and the service status. | Settings → Apps → Chronics → Notifications |
| `QUERY_ALL_PACKAGES` | Yes | Resolves app labels and icons for the filter picker. Does not read app data. | Not revocable; uninstall to remove |
| Storage (Android 8–9) | Only on old versions | Writes exports to the shared Downloads folder. Android 10+ uses MediaStore instead. | Settings → Apps → Chronics → Permissions |
| Shizuku | Optional | Grants the two accesses above from a shell and imports the current shade. | Shizuku app → Chronics → revoke |

## Granting notification access

1. Open Chronics → **Settings → Access → Notification listener**.
2. Tap **Grant listener**.
3. In the system list, enable **Chronics**.

Without this the journal stays empty: the listener is the only source of full
notification text.

## Enabling the accessibility service

The accessibility service is a safety net for notifications that disappear
before the listener reads them.

1. Open **Settings → Access → Accessibility service**.
2. Tap **Enable accessibility**.
3. Confirm the system warning and enable **Chronics · deep capture**.

**Deep capture** additionally reads the text of the open notification shade. It
is experimental, slower, and reads more than the default mode. Leave it off
unless you need it.

## Shizuku

[Shizuku](https://shizuku.rikka.app/) runs privileged shell commands without
root. Chronics uses it for two conveniences:

- Grant notification-listener and accessibility access in one tap.
- Import the notifications currently in the shade with
  `dumpsys notification --noredact`.

Shizuku must be installed and running. If the app reports *“Shizuku is not
running”*, start Shizuku first, then tap **Allow Shizuku** and approve the
request.

## Why `QUERY_ALL_PACKAGES`

The filter screen lists installed apps so you can block or allow them by name and
icon. Android requires this permission to enumerate launchable apps. Chronics
does not read app contents, and the list never leaves the device.
