# Progress

## 2026-07-03
- Read the full operating pack and `task_1.md`.
- Confirmed the repository has no existing game code.
- Locked the first playable milestone in `memory/task_lock.md`.
- Selected Godot 4.x + GDScript and generated license-safe assets as the initial implementation path.
- Created the Godot project skeleton, managers, localization files, UI screens, gameplay scripts, and first tutorial-level composition.
- Generated original WAV/PNG assets and documented them in `ASSET_CREDITS.md`.
- Fixed runtime issues found by Godot smoke tests: typed patrol point arrays, tension light setup timing, real room passages, and pause layout.
- Added and ran Python validation, Godot smoke, deterministic playthrough, and GUI visual capture.
- Final screenshots saved in `artifacts/screenshots/`.
- Added the first external visual asset pass: ambientCG CC0 textures, Kenney CC0 Factory Kit props, Kenney CC0 Blocky Characters, and Kenney CC0 sci-fi UI textures/font.
- Upgraded the tutorial level with textured walls/floors/ceilings, real 3D NPC models, pickup prop models, windows, lamps, weather/rain visuals, side rooms based on floor-plan logic, minimap, intro legend, pause dimming, scuffs, and factory dressing.
- Replaced calculator-like buttons with textured sci-fi UI assets.
- Added the second external visual pass: flat ambientCG PBR maps for brick, painted plaster, concrete, carpet, tile, and wood; Kenney Furniture Kit props for desks, chairs, benches, sofas, bookcases, lamps, plants, electronics, rugs, and boxes.
- Added transparent glass/window dressing, a surrounding outside world sphere, street/building silhouettes visible through windows, window spotlights, warm lamp lights, room reflection probes, SSAO, glow, and filmic tonemapping for a more natural lighting pass.
- Added round contextual UI icons for hints and pause.
- Fixed Russian localization mojibake, added a UTF-8 validator guard, and added UI font fallback so Cyrillic text renders readably.
- Moved the safe zone away from the civilian spawn so rescue requires guiding the NPC instead of auto-completing.
- Tuned down over-bright window/safe-zone lighting and captured final Russian screenshots on Vulkan/Forward+.
- Latest validation, Godot smoke, deterministic playthrough, and final visual capture passed after the material/light/localization pass.
