# iOS Scaffold (AetherQuest)

This is a starter scaffold for an iOS app that will consume shared Kotlin Multiplatform logic.

## What this includes

- SwiftUI starter app files
- Placeholder bridge for shared KMP framework import
- TestFlight-oriented build notes

## Build on macOS

1. Open this folder in Xcode and create `AetherQuestiOS.xcodeproj` using the included Swift files.
2. Add KMP generated framework as dependency.
3. Set bundle ID, signing team, and provisioning profile.
4. Archive and upload to TestFlight from Xcode.

## CI

- `.github/workflows/ios-scaffold-macos.yml` validates macOS/Xcode toolchain and scaffold presence.
