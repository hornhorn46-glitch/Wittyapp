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

## 2026-07-12
- Started a new presentation polish pass from the existing Godot vertical slice.
- Updated `memory/task_lock.md` with the current polish target: less blocky characters, better furniture scale, denser real-room dressing, clearer UI/interactions, and stable runnable tests.
- Added a Central Consul decision in `memory/decisions.md` to keep the pass focused on the existing first level instead of migrating engines or adding a second level.
- Refined stylized character proportions: smaller heads, slimmer rounded limbs, shorter hostile/civilian scale, brighter hostile clothing for readability.
- Increased chair/bench/lamp scale and applied a unified muted PBR carpet material to rug models so furniture reads less toy-like.
- Added a room finish layer: ceiling tile grids, access panels, water stains, overhead conduit, grime around door frames, traffic wear, small round floor stains, door plaques, electrical boxes, mugs, folded soft props, door stops, and extra soft bounce/window glare lights.
- Tightened HUD composition and changed the focused interaction indicator to `E` for direct readability.
- Removed noisy WAV loader errors by skipping Godot `load()` for unimported generated WAV files and using the existing manual WAV loader directly.
- Captured fresh Forward+ showcase screenshots in `artifacts/screenshots/`.
- Validation, Godot smoke, input-driven playthrough, audio-system test, and Forward+ visual capture passed after the polish pass.
