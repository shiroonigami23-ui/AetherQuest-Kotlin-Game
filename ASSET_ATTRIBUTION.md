# Asset Attribution

## Hero Sprite Sheet

- File: `app/src/main/res/drawable-nodpi/hero_sheet.png`
- Source: https://lpc.opengameart.org/sites/default/files/Hero.png
- License: CC0 1.0 (Public Domain)
- Usage: Player/enemy sprite extraction in `GameRendererView.kt`

## Enemy Sprite Pack

- Files:
  - `app/src/main/res/drawable-nodpi/enemy_drone.png`
  - `app/src/main/res/drawable-nodpi/enemy_sentinel.png`
  - `app/src/main/res/drawable-nodpi/enemy_observer.png`
  - `app/src/main/res/drawable-nodpi/enemy_steel_eagle.png`
  - `app/src/main/res/drawable-nodpi/enemy_metal_slug.png`
- Source: https://opengameart.org/content/phantasy-tiny-rpg-mobs-pack-2
- License note in pack: personal/commercial use allowed, credit appreciated, redistribution allowed.
- Original author: Luis Zuno (@ansimuz).

## UI + Combat SFX

- Files:
  - `app/src/main/res/raw/sfx_ui_click.wav`
  - `app/src/main/res/raw/sfx_ui_success.wav`
  - `app/src/main/res/raw/sfx_hit.wav`
  - `app/src/main/res/raw/sfx_switch.wav`
- Sources:
  - Kenney UI Audio: https://github.com/Calinou/kenney-ui-audio (based on https://kenney.nl/assets/ui-audio)
  - Owlish Media SFX pack: https://opengameart.org/content/owlish-media-sound-effects
- Licenses:
  - Kenney: CC0 1.0
  - Owlish Media pack page states free game use with attribution required.

## Background Music

- Files:
  - `app/src/main/res/raw/bgm_menu.ogg`
  - `app/src/main/res/raw/bgm_battle.ogg`
- Source page: https://opengameart.org/content/free-contemplative-fantasy-music-pack
- License on page: Creative Commons Attribution 4.0
- Author: Ekard / Ravi Te

## Notes

- The current build combines external CC0 sprite assets with custom-rendered UI and effects.
- For Play Store release, keep attribution and license references bundled in app/legal section and store listing.
