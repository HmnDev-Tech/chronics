# Building and signing

## Continuous integration

Every push to `main` and every pull request runs
[`.github/workflows/android.yml`](https://github.com/HmnDev-Tech/chronics/blob/main/.github/workflows/android.yml):

1. Install Android SDK platform `android-37.0` and build-tools `36.0.0`.
2. Restore the signing key from repository secrets.
3. `./gradlew testDebugUnitTest`
4. `./gradlew assembleDebug`
5. Print the APK certificate fingerprint into the run summary.
6. Upload `chronics-debug-apk` (30 days) and, on failure, the test reports (7 days).

A run takes about three minutes.

## Local build

Requirements: JDK 17 and an Android SDK with platform `android-37.0` and
build-tools `36.0.0`.

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Stable signature

Android refuses to install an update signed by a different key. Chronics therefore
signs every build with one PKCS12 keystore that lives in repository secrets:

| Secret | Meaning |
| --- | --- |
| `CHRONICS_KEYSTORE_BASE64` | Base64 of the keystore file |
| `CHRONICS_STORE_PASSWORD` | Keystore password |
| `CHRONICS_KEY_ALIAS` | Key alias (`chronics`) |
| `CHRONICS_KEY_PASSWORD` | Key password |

`app/build.gradle.kts` reads `CHRONICS_KEYSTORE_PATH` and the passwords from the
environment. CI decodes the secret into `$RUNNER_TEMP` so the key never enters the
checkout.

To build locally with the same key:

```bash
export CHRONICS_KEYSTORE_PATH=/absolute/path/to/chronics.keystore
export CHRONICS_STORE_PASSWORD=...
export CHRONICS_KEY_ALIAS=chronics
export CHRONICS_KEY_PASSWORD=...
./gradlew assembleDebug
```

Without the variables the build falls back to the default debug key. That APK is
installable on a clean device but cannot update an existing install.

## Verifying a signature

```bash
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs app-debug.apk
```

Compare the SHA-256 digest with the one printed in the CI run summary. It must
never change between builds.

## Rotating the key

If the signing key is exposed:

1. Generate a new PKCS12 keystore.
2. Replace all four secrets.
3. Publish a build with a note that a one-time reinstall is required.

## Versioning

`versionCode` and `versionName` live in `app/build.gradle.kts`. Feature pull
requests do not change them; releases bump both.

## Wiki publishing

The wiki pages are kept in `docs/wiki/` so they can be reviewed in pull requests.
Create the first page in the web UI once (see `docs/wiki/README.md`), then run:

```bash
./scripts/publish-wiki.sh
```

The script copies the pages into `chronics.wiki.git` and pushes them.
