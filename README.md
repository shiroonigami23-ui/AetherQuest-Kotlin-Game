# AetherQuest (Kotlin Android RPG)

A production-oriented Kotlin Android fantasy RPG prototype designed for Play Store evolution.

## Gameplay

- Class-based heroes: Knight, Ranger, Mystic
- Stage progression and boss encounters every 5 stages
- Turn-based combat actions: Attack, Skill, Heal, Defend
- XP, level-ups, coins, potion economy
- Camp systems: rest, potion shop, weapon/armor forging
- Quest tracker + loot rarity drops
- Save/continue progression (SharedPreferences)
- Custom rendered battle + world map scene (Canvas)

## Assets

- External sprite sheet: `app/src/main/res/drawable-nodpi/hero_sheet.png`
- Source + license details: `ASSET_ATTRIBUTION.md`

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
