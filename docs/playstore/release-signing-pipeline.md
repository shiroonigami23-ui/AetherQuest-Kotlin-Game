# Signed Release Pipeline

## Local Signing (Current)
- Keystore: `keystore/aetherquest-release.jks` (gitignored)
- Properties: `keystore.properties` (gitignored)
- Build command:
  - `./gradlew assembleRelease`
- Output:
  - `app/build/outputs/apk/release/app-release.apk` (signed)

## CI Signing (Recommended)
Use GitHub Actions with repository secrets:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## Security Rules
- Never commit keystore or credentials
- Back up keystore securely (offline + encrypted vault)
- Rotate keys only with clear migration plan

## App Bundle Migration
- For Play Store production, prefer `.aab`:
  - `./gradlew bundleRelease`
