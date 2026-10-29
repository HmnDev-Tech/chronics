# Privacy

Chronics exists because messages disappear. It is built so that the copy it
keeps belongs to you and nobody else.

## What leaves the device

Nothing. The app declares no `INTERNET` permission, has no analytics, no crash
reporter, no ads and no account. There is no server to send anything to.

## Where data is stored

| Data | Location | Removed by |
| --- | --- | --- |
| Journal entries | App-private SQLite database | **Clear journal**, retention, uninstall |
| JSON/TXT exports | `Downloads/Chronics/` | You, manually |
| Images | `Downloads/Chronics/media/` | You, manually |
| Full backup | `Downloads/Chronics/backup/` | You, manually |
| Settings | App-private preferences | Uninstall |

On Android 10+ files are written through MediaStore, so they appear in the
Downloads app and survive uninstalling Chronics.

## Masking

**Settings → Filters → Mask secrets** rewrites an entry before it is stored:

- OTP and verification codes become `••••`.
- Phone numbers are shortened to the last two digits.
- E-mail addresses lose the local part.
- Card numbers keep the last four digits.

Masking is off by default because it also removes information you may want to
keep. Turn it on if the archive may be read by someone else.

## Retention

**Settings → Storage → Keep entries** deletes entries older than the chosen
period, and **Maximum entries** trims the oldest rows beyond the limit. Files
already exported to Downloads are never deleted automatically.

## Backups

Android backup is disabled for the app (`allowBackup="false"`), so the journal is
not copied into Google backups. Use **Export everything** to create your own
backup in `Downloads/Chronics/backup/`.

## Sharing a bug report

Never paste real notification text, phone numbers, OTP codes, e-mail addresses,
chat names or file paths into an issue. Replace them with placeholders. Security
problems go through [SECURITY.md](https://github.com/HmnDev-Tech/chronics/blob/main/SECURITY.md),
never the public tracker.
