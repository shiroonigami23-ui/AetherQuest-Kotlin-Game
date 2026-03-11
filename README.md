# AetherQuest

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Android-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Android"/>
  <img src="https://img.shields.io/badge/Engine-Custom%20Canvas-0EA5E9?style=for-the-badge" alt="Canvas Engine"/>
  <img src="https://img.shields.io/badge/Genre-Turn--Based%20RPG-10B981?style=for-the-badge" alt="RPG"/>
  <img src="https://img.shields.io/github/license/shiroonigami23-ui/AetherQuest-Kotlin-Game?style=for-the-badge" alt="License"/>
  <img src="https://img.shields.io/github/stars/shiroonigami23-ui/AetherQuest-Kotlin-Game?style=for-the-badge" alt="Stars"/>
  <img src="https://img.shields.io/github/last-commit/shiroonigami23-ui/AetherQuest-Kotlin-Game?style=for-the-badge" alt="Last Commit"/>
</p>

Production-ready Kotlin RPG foundation focused on Android APK delivery, story depth, progression systems, and Play Store scale-up.

## Table of Contents
- [Highlights](#highlights)
- [Core Systems](#core-systems)
- [Project Structure](#project-structure)
- [Run Locally](#run-locally)
- [Build APK](#build-apk)
- [Assets and Licensing](#assets-and-licensing)
- [KMP and iOS Scaffold](#kmp-and-ios-scaffold)
- [Play Store Operations](#play-store-operations)
- [Roadmap](#roadmap)

## Highlights
- 3 hero classes: `Knight`, `Ranger`, `Mystic`
- Difficulty modes: `Story`, `Easy`, `Hard`, `Extreme (1 life)`
- Turn-based combat with skills, defend, heal, and bomb usage
- Camp economy and progression:
  - Potions, elixirs, bombs, chest unlocks, accessory crafting
  - Weapon and armor tiering with named equipment upgrades
- Story arcs with multi-route romance design:
  - 5 romanceable characters (Nyra, Lyra, Sera, Mira, Kaela)
  - Branches for single, throuple, harem, or solo outcomes
- Multiple ending types:
  - Heroic, Ruthless, Romantic, Throuple, Harem, Lone Wolf, Tragic
- Region progression map and boss pacing
- Persistent save/continue state via `SharedPreferences`
- Settings, legal/about screens, and external licensed assets integrated

## Core Systems

| Area | What it Handles |
|---|---|
| Combat Engine | Enemy scaling, class skills, win/lose state, timed events |
| Narrative Engine | Story triggers, branching choices, relationship routes, endings |
| Camp Engine | Economy loop, upgrades, utility items, secrets, chest system |
| Save Manager | Full session serialization and backward-compatible loading |
| Renderer | Custom battle + map rendering with external region/sprite assets |

## Project Structure
```text
app/
  src/main/java/com/shiro/aetherquest/
    BattleEngine.kt
    NarrativeEngine.kt
    CampEngine.kt
    SaveManager.kt
    GameRendererView.kt
  src/main/res/
    drawable-nodpi/   # sprite and region packs
    raw/              # bgm/sfx
docs/playstore/       # store listing + release docs
kmp-shared/           # KMP shared core scaffold
iosApp/               # iOS scaffold
```

## Run Locally
```bash
git clone https://github.com/shiroonigami23-ui/AetherQuest-Kotlin-Game.git
cd AetherQuest-Kotlin-Game
./gradlew clean assembleDebug
```

## Build APK
```bash
./gradlew assembleDebug assembleRelease
```

Generated outputs:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

## Assets and Licensing
- Hero sprite: `app/src/main/res/drawable-nodpi/hero_sheet.png`
- Enemy sprites: `app/src/main/res/drawable-nodpi/enemy_*.png`
- Region backgrounds: `app/src/main/res/drawable-nodpi/region_*.png`
- Audio: `app/src/main/res/raw/`
- Attribution and third-party license details: [`ASSET_ATTRIBUTION.md`](ASSET_ATTRIBUTION.md)

## KMP and iOS Scaffold
- Shared Kotlin module scaffold: `kmp-shared/`
- iOS app scaffold: `iosApp/`
- macOS iOS scaffold workflow: `.github/workflows/ios-scaffold-macos.yml`

Note: iOS `.ipa` packaging/signing requires macOS + Xcode.

## Play Store Operations
- Store and compliance docs:
  - `docs/playstore/store-listing-draft.md`
  - `docs/playstore/content-rating-notes.md`
  - `docs/playstore/permissions-review.md`
  - `docs/playstore/release-signing-pipeline.md`
- Android signed-release workflow template:
  - `.github/workflows/android-release-signed.yml`

## Roadmap
- Add larger licensed asset packs (characters, environments, VFX, SFX/BGM)
- Quest journal, codex, and achievement systems
- Deeper balancing pass and boss mechanics
- Play Store production hardening (ANR/crash analytics, QA matrix, telemetry, accessibility)
