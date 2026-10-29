# Security Policy

Chronics reads and stores the full text of your notifications, so a security
problem here can expose private messages. Please report issues privately.

## Supported versions

Only the latest build from the `main` branch is supported. Chronics has no
long-term release branches; fixes ship as a new APK artifact.

| Version | Supported |
| --- | --- |
| Latest `main` build | Yes |
| Older builds | No |

## Reporting a vulnerability

**Do not open a public issue for a security problem.**

1. Use GitHub's private reporting:
   **Security** tab → **Advisories** → **Report a vulnerability**.
2. If that is unavailable, contact the maintainer through the GitHub profile
   linked on the repository page.

Please include:

- Affected build (commit or artifact date) and Android version.
- Steps to reproduce, and the impact you expect.
- Whether the issue needs a malicious app, a physical device, or only a file.

You can expect an acknowledgement within a few days. Please give a reasonable
window for a fix before public disclosure; credit is given in the advisory
unless you prefer otherwise.

## Scope

In scope:

- Reading, storing or exporting notification data beyond what the UI promises.
- Any network transmission of captured data.
- Bypass of the filters, masking, or the opt-in permission flow.
- Path traversal or overwriting files outside `Downloads/Chronics`.
- Leaks through exported files, logs, intents, providers or the Shizuku bridge.
- Weaknesses in how the APK is signed or updated.

Out of scope:

- Data that is already on the device and readable by a rooted user or by the
  device owner.
- Attacks that require the accessibility service to be enabled by the user and
  a second malicious app with the same privileges.
- Denial of service through extremely large notification payloads without a
  concrete memory-safety impact.
- Reports produced only by a generic scanner without a working proof of concept.
- Missing hardening flags that have no demonstrable impact on Android 8+.

## Design promises

These are guarantees, and a violation is a security bug:

- The app declares no `INTERNET` permission and makes no network requests.
- Captured text is stored in the app-private database and in
  `Downloads/Chronics`, nowhere else.
- Masking, when enabled, removes OTP codes, phone numbers, e-mail addresses and
  card numbers before an entry is written.
- Notification access and the accessibility service are opt-in and can be
  revoked in system settings at any time.
- `QUERY_ALL_PACKAGES` is used only to resolve labels and icons for the filter
  picker.
- The APK is signed with a single stable key; the fingerprint is printed in the
  CI run summary and must not change.

## Handling secrets

Never include keystores, passwords or `keystore.properties` in a commit, issue
or pull request. If a signing key is exposed, rotate it and publish a new
artifact with a note that a one-time reinstall is required.
