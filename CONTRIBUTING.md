# Contributing to Chronics

Thanks for taking the time to help. Chronics is a small, privacy-first Android
app, so the rules below are mostly about keeping it predictable and keeping user
data out of the public record.

By participating you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to help

- Report a bug with the **Bug report** issue form.
- Propose a feature with the **Feature request** form.
- Improve a translation or add a new language.
- Send a pull request for an open issue.

## Before you open an issue

1. Search existing issues, including closed ones.
2. Update to the latest APK from the Actions tab — the bug may already be fixed.
3. Reproduce with the **Send test notification** button if possible.

### Issue rules

- One problem per issue. Do not bundle unrelated bugs.
- Use the templates. Fill in app version, Android version and device model.
- **Never paste real notification contents**, phone numbers, OTP codes, e-mail
  addresses, chat names or file paths containing personal data. Replace them
  with `123456`, `+1 555 0100`, `example@example.com`.
- Attach logs as text, not screenshots of logs.
- Security vulnerabilities must not be filed as issues — see [SECURITY.md](SECURITY.md).
- Issues that ask for cloud sync, analytics or uploading data will be closed:
  they contradict the purpose of the app.

## Development setup

Requirements:

- JDK 17
- Android SDK with platform `android-37.0` and build-tools `36.0.0`
- No Android Studio required, but it works

```bash
git clone https://github.com/HmnDev-Tech/chronics.git
cd chronika
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

CI runs the same two commands on every push and pull request, so a green local
test run is the minimum bar for a pull request.

### Local signing

CI signs APKs with a stable key stored in repository secrets. For a local build
that installs over a CI build, export the same key before running Gradle:

```bash
export CHRONICS_KEYSTORE_PATH=/absolute/path/to/chronics.keystore
export CHRONICS_STORE_PASSWORD=...
export CHRONICS_KEY_ALIAS=chronics
export CHRONICS_KEY_PASSWORD=...
./gradlew assembleDebug
```

Without these variables Gradle falls back to the default debug key, which is
fine for testing but produces an APK that cannot update an existing install.

Do not commit keystores, passwords or `keystore.properties`. `*.keystore`,
`*.jks` and `keystore.properties` are ignored by git.

## Code style

- Kotlin, four-space indentation, trailing commas in multi-line argument lists.
- Compose first: screens are stateless where possible and read settings from the
  view model.
- No new dependencies without a reason in the pull request description.
- No network calls, analytics, crash reporters or ad SDKs. Ever.
- Keep the notification parser pure and unit-testable.
- Comments and identifiers in English.

### Strings and translations

All user-visible text lives in `app/src/main/java/com/hmndev/chronika/i18n/`.
`StringsEn.kt` is the source of truth.

- Add a key to **all six** catalogs (`StringsEn`, `StringsRu`, `StringsKk`,
  `StringsEs`, `StringsZh`, `StringsPt`).
- Keep `%d`, `%s`, `%.1f` and `%%` placeholders in the same order as English.
- `StringsTest` fails the build if a key, a blank value or a placeholder is
  missing, so run `./gradlew testDebugUnitTest` after editing.
- Launcher and service labels live in `app/src/main/res/values*/strings.xml`;
  update every locale directory when they change.

## Pull requests

- Branch from `main`, keep the branch focused, and rebase instead of merging
  `main` into it.
- Commit messages in English, imperative mood, one logical change per commit
  (`fix:`, `feat:`, `docs:`, `ci:`, `refactor:`, `test:`).
- Describe the user-visible effect and how you tested it.
- Include a screenshot or recording for UI changes.
- Update `README.md` and the Wiki when behaviour or setup changes.
- Do not bump `versionCode`/`versionName` in a feature PR; releases are handled
  separately.
- Do not include generated files, APKs, keystores or personal data.

A pull request is ready when:

- [ ] `./gradlew testDebugUnitTest` passes
- [ ] `./gradlew assembleDebug` produces an installable APK
- [ ] New behaviour is covered by a test where practical
- [ ] All six string catalogs are updated
- [ ] No secrets, keystores or personal notification data are included
- [ ] The description explains the change and the testing performed

Maintainers may ask for changes, squash commits, or close a pull request that
conflicts with the privacy-first scope of the project.
