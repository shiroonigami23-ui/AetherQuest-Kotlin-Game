# AetherQuest (Kotlin Android RPG)

A production-oriented Kotlin Android fantasy RPG prototype designed for Play Store evolution.

## Gameplay

- Class-based heroes: Knight, Ranger, Mystic
- Stage progression and boss encounters every 5 stages
- Turn-based combat actions: Attack, Skill, Heal, Defend
- XP, level-ups, coins, potion economy
- Camp systems: rest, potion shop, weapon/armor forging
- Quest tracker + loot rarity drops
- Story chapters with branching choices
- World-region progression map
- Romance/loyalty paths (Nyra vs Crown)
- Multiple endings: Romantic, Heroic, Ruthless, Tragic
- Death/lives system with true game-over ending
- Save/continue progression (SharedPreferences)
- Settings menu: music/SFX toggles + master volume
- In-app legal/privacy/about screen
- Custom rendered battle + world map scene (Canvas)
- Real external assets integrated: enemy sprites, BGM, UI/combat SFX

## Assets

- External sprite sheet: `app/src/main/res/drawable-nodpi/hero_sheet.png`
- Enemy sprite pack: `app/src/main/res/drawable-nodpi/enemy_*.png`
- World backgrounds: `app/src/main/res/drawable-nodpi/region_*.png`
- BGM: `app/src/main/res/raw/bgm_menu.ogg`, `bgm_battle.ogg`
- SFX: `app/src/main/res/raw/sfx_*.wav`
- Source + license details: `ASSET_ATTRIBUTION.md`

## KMP + iOS Scaffold

- KMP shared core scaffold: `kmp-shared/`
- iOS app scaffold: `iosApp/`
- macOS scaffold CI: `.github/workflows/ios-scaffold-macos.yml`

## Play Store Release Ops

- Listing draft + rating + permissions + signing docs:
  - `docs/playstore/store-listing-draft.md`
  - `docs/playstore/content-rating-notes.md`
  - `docs/playstore/permissions-review.md`
  - `docs/playstore/release-signing-pipeline.md`
- Signed release CI template: `.github/workflows/android-release-signed.yml`

## Build

```bash
./gradlew assembleDebug assembleRelease
```

## APK

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

## Next Steps for Play Store

- Replace placeholder-rendered visuals with expanded licensed art/audio packs
- Add accessibility, privacy policy, analytics, and crash reporting
- Add balancing pass + QA test matrix + signed release pipeline

## iOS Note

- Kotlin logic can be shared via KMP, but `.ipa` builds require macOS + Xcode.
- This repo currently builds Android APK on Windows.
