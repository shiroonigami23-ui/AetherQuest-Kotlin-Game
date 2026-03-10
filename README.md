# AetherQuest (Kotlin Android RPG)

A production-oriented Kotlin Android fantasy RPG prototype designed for Play Store evolution.

## Gameplay

- Class-based heroes: Knight, Ranger, Mystic
- Stage progression and boss encounters every 5 stages
- Turn-based combat actions: Attack, Skill, Heal, Defend
- XP, level-ups, coins, potion economy
- Save/continue progression (SharedPreferences)
- Custom rendered battle + world map scene (Canvas)

## Build

```bash
./gradlew assembleDebug assembleRelease
```

## APK

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

## Next Steps for Play Store

- Replace placeholder-rendered visuals with licensed art/audio packs
- Add accessibility, privacy policy, analytics, and crash reporting
- Add balancing pass + QA test matrix + signed release pipeline
