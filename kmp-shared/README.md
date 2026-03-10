# AetherQuest KMP Shared Core (Scaffold)

This folder contains a Kotlin Multiplatform scaffold for extracting shared game logic
between Android and iOS.

## Structure

- `shared/`: multiplatform module with common domain models and logic.
- `iosApp/`: iOS scaffold that can consume shared framework on macOS.

## Notes

- Android app currently uses local Android code directly.
- Next migration step is moving battle/narrative logic from `app/` to `shared/`.
- iOS binaries (`.ipa`) require macOS + Xcode build/signing.
