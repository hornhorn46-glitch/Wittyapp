# Playtest Report

Date: 2026-07-03

## How it was run

Automated deterministic playthrough:

```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_playthrough.gd
```

Final visual capture:

```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path . --script tests\visual_capture.gd
```

## Checked

- Game starts from main scene into main menu.
- Russian localization renders readable UTF-8 text in final screenshots.
- New Game starts the tutorial level and intro legend.
- Player can pick up and throw a throwable item.
- Thrown item emits a sound event.
- Hostile NPC reacts to the sound event by entering investigate behavior.
- Civilian NPC can be interacted with and enters follow state before rescue.
- Civilian reaches rescued state only after being moved to the safe zone.
- Exit trigger completes the level after rescue.
- Pause menu dims the gameplay screen and renders centered.
- Visual pass includes textured sci-fi UI, round hint/pause icons, flat PBR materials, brick/plaster/concrete/carpet/tile/wood surfaces, 3D characters, furniture/interior props, windows with outside views, local/window lighting, reflection probes, weather/rain, minimap, and pickup props.

## Passed

All automated playthrough assertions passed. Final Russian screenshots were generated:

- `artifacts/screenshots/menu.png`
- `artifacts/screenshots/intro_legend.png`
- `artifacts/screenshots/gameplay_start.png`
- `artifacts/screenshots/patrol_room.png`
- `artifacts/screenshots/rescue_room.png`
- `artifacts/screenshots/pause.png`

## Bugs fixed during playtest

- Opened real level passages instead of leaving doors embedded in solid walls.
- Increased thrown item sound loudness so the patrol reliably investigates in the tutorial layout.
- Moved pause UI to a viewport-sized overlay so it no longer appears pinned to the left edge.
- Replaced over-close white NPC captures with textured character materials and wider scripted screenshot cameras.
- Moved the safe zone away from the civilian start so rescue does not auto-complete on the first follow frame.
- Fixed Russian localization mojibake and added a validator guard.
- Added UI font fallback for Cyrillic text.
- Reduced over-bright safe-zone/window lighting and captured final screenshots with the project Forward+ renderer.

## Remaining issues

See `KNOWN_ISSUES.md`.
