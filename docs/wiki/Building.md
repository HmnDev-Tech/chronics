# Building and signing

## Continuous integration

Every push to `main`, every tag and every pull request runs
[`.github/workflows/android.yml`](https://github.com/HmnDev-Tech/chronics/blob/main/.github/workflows/android.yml):

1. Install Android SDK platform `android-37.0` and build-tools `36.0.0`.
2. Restore the signing key from repository secrets.
3. `./gradlew testDebugUnitTest`
4. `./gradlew assembleDebug`
5. `./gradlew assembleRelease` on `main` and on tags.
6. Print both APK certificate fingerprints into the run summary.
7. Upload `chronics-debug-apk` and `chronics-release-apk` (30 days) and, on failure, the test reports (7 days).

A run takes about three to five minutes. A `v*` tag additionally starts the
release job, which verifies the release APK signature and publishes a GitHub
prerelease with `app-release.apk` attached.

## Releasing

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Add `docs/release-notes/v<version>.md`.
3. Commit and push `main`, then push the tag:

   ```bash
   git tag v1.0.0-beta01
   git push origin v1.0.0-beta01
   ```

4. Wait for the **Publish GitHub release** job and check the attached APK.

## Local build

Requirements: JDK 17 and an Android SDK with platform `android-37.0` and
build-tools `36.0.0`.

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease
```

The APKs land in `app/build/outputs/apk/debug/app-debug.apk` and
`app/build/outputs/apk/release/app-release.apk`.

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
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs app-release.apk
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
