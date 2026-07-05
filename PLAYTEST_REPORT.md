# Playtest Report

## 2026-07-05 Door, Security, and Audio Pass

Run modes:

```powershell
python .\tests\validate_project.py
tools\godot\Godot_v4.2.2-stable_win64_console.exe --quiet --headless --path . --script tests\godot_audio_system.gd
tools\godot\Godot_v4.2.2-stable_win64_console.exe --quiet --headless --path . --script tests\godot_security_terminal.gd
tools\godot\Godot_v4.2.2-stable_win64_console.exe --quiet --headless --path . --script tests\godot_input_playtest.gd
tools\godot\Godot_v4.2.2-stable_win64_console.exe --quiet --path . --script tests\room_showcase_capture.gd
```

Result: passed.

Checked:

- Door panels were widened and aligned to the real trimmed openings; the first-door route still opens and passes.
- The oversized imported doorway meshes were removed; clean rounded trims, threshold plates, and jamb shadow strips replace them.
- Added an interactable security terminal; using it disables the security loop and reduces patrol vision/suspicion gain.
- Added a real audio layer: menu music, gameplay music bed, communal room ambience, radiator knocks, water noise, phone ringing, hostile phone argument, softer door/throw sounds, and concrete footstep variants.
- Hostile phone event is scheduled every 180-240 seconds and can be triggered in test; it shows a phone prop and creates spatial phone/argument audio.
- Full input route still completes after the geometry and audio changes.
- No Godot process remained after the final validation pass.

Screenshots generated:

- `artifacts/screenshots/showcase_01_start_room.png`
- `artifacts/screenshots/showcase_02_door_hinge_handle.png`
- `artifacts/screenshots/showcase_03_security_terminal.png`
- `artifacts/screenshots/showcase_03_records_room.png`
- `artifacts/screenshots/showcase_04_patrol_guard_scale.png`
- `artifacts/screenshots/showcase_05_rescue_room_entry.png`
- `artifacts/screenshots/showcase_06_rescue_npc_corner.png`
- `artifacts/screenshots/showcase_07_exit_stairwell.png`

## 2026-07-05 Godot Polish Pass

Run modes:

```powershell
python .\tests\validate_project.py
tools\godot\Godot_v4.2.2-stable_win64.exe --headless --path . --script res://tests/godot_input_playtest.gd
tools\godot\Godot_v4.2.2-stable_win64.exe --display-driver windows --rendering-driver opengl3 --path . --script res://tests/godot_mouse_picture_check.gd
tools\godot\Godot_v4.2.2-stable_win64.exe --display-driver windows --rendering-driver opengl3 --path . --script res://tests/room_showcase_capture.gd
```

Result: passed.

Checked:

- Mouse-look changes both player yaw/pitch and the rendered image: `yaw=0.3821`, `pitch=0.1029`, `image_delta=0.1279`.
- Full input route still completes: pickup, badge, door prompts, locked door, rescue interaction, civilian follow, safe zone, exit.
- No Godot process remained after the final validation/capture pass.
- Visual polish pass adds smoother fallback NPCs, rounded visual props for bedding/cushions/exit door details, a real mug prop, softer indirect light settings, smaller decorative window GLBs, and cleaner showcase camera angles.

Screenshots generated:

- `artifacts/screenshots/showcase_01_start_room.png`
- `artifacts/screenshots/showcase_02_door_hinge_handle.png`
- `artifacts/screenshots/showcase_03_records_room.png`
- `artifacts/screenshots/showcase_04_patrol_guard_scale.png`
- `artifacts/screenshots/showcase_05_rescue_room_entry.png`
- `artifacts/screenshots/showcase_06_rescue_npc_corner.png`
- `artifacts/screenshots/showcase_07_exit_stairwell.png`

## 2026-07-05 Micro Detail Polish Pass

Run modes:

```powershell
python .\tests\validate_project.py
tools\godot\Godot_v4.2.2-stable_win64.exe --headless --path . --script res://tests/godot_input_playtest.gd
tools\godot\Godot_v4.2.2-stable_win64.exe --display-driver windows --rendering-driver opengl3 --path . --script res://tests/godot_mouse_picture_check.gd
tools\godot\Godot_v4.2.2-stable_win64.exe --display-driver windows --rendering-driver opengl3 --path . --script res://tests/room_showcase_capture.gd
```

Result: passed.

Checked:

- Full route still completes after adding non-blocking micro-detail props.
- Mouse-look remains visually verified: `yaw=0.3821`, `pitch=0.1029`, `image_delta=0.1333`.
- Added non-blocking room details: outlets, light switches, vents, blinds, radiators, cable bundles, loose papers, wall frames, and soft bounce lights.
- Fallback humanoids now include extra read details: chest panel, belt, buckle, and knee pads.
- No Godot process remained after the capture and validation pass.

## 2026-07-04 Interior Scale and Door Pass

Run modes:

```powershell
python .\tests\validate_project.py
tools\godot\Godot_v4.2.2-stable_win64.exe --headless --path . --script res://tests/godot_input_playtest.gd
tools\godot\Godot_v4.2.2-stable_win64.exe --headless --path . --script res://tests/human_playtest_capture.gd
tools\godot\Godot_v4.2.2-stable_win64.exe --path . --script res://tests/room_showcase_capture.gd
```

Result: passed.

Screenshots generated:

- `artifacts/screenshots/showcase_01_start_room.png`
- `artifacts/screenshots/showcase_02_door_hinge_handle.png`
- `artifacts/screenshots/showcase_03_rescue_room_entry.png`
- `artifacts/screenshots/showcase_04_rescue_living_corner.png`

Issues found and fixed:

- Door handles were visually near the hinge side, making the door read as if it opened from the handle. Door geometry now has a clear hinge side, latch-side handles, latch plates, and inset panels.
- Chairs were still reading too small after the prior collision fix. Decorative chair scale was increased while keeping those chairs visual-only so they no longer block the route.
- The rescue room read too much like loose props on a floor. It now has a more room-like composition with a nightstand lamp, bed/mattress/pillow, coffee-table clutter, ottoman, wall photos/curtains, smaller rug placement, and furniture pushed away from the critical rescue path.
- The civilian start point was moved out of the central entry sightline while staying far enough from the safe zone to avoid instant rescue.
- Added `tests/room_showcase_capture.gd` for readable human-height visual screenshots, separate from the route-playthrough screenshots.

## 2026-07-04 Human-Style Playtest

Run modes:

```powershell
python .\tests\validate_project.py
tools\godot\Godot_v4.2.2-stable_win64.exe --headless --path . --script res://tests/godot_input_playtest.gd
tools\godot\Godot_v4.2.2-stable_win64.exe --path . --script res://tests/human_playtest_capture.gd
```

Result: passed.

Screenshots generated:

- `artifacts/screenshots/human_01_start_chairs.png`
- `artifacts/screenshots/human_02_badge_counter.png`
- `artifacts/screenshots/human_03_first_door.png`
- `artifacts/screenshots/human_04_corridor_locked_door.png`
- `artifacts/screenshots/human_05_patrol_entry.png`
- `artifacts/screenshots/human_06_patrol_crossed.png`
- `artifacts/screenshots/human_07_rescue_room_entry.png`
- `artifacts/screenshots/human_08_rescue_furniture_path.png`
- `artifacts/screenshots/human_09_result.png`

Issues found and fixed:

- Chairs were over-scaled relative to the room and could feel like oversized blockers. Reduced the chair scale boost and made decorative chairs/rugs/lamps/plants/books visual-only so they no longer create invisible navigation snags.
- Rescue-room furniture read as clutter in the route to the civilian and safe zone. Moved the coffee table and sofa to the wall side, reduced sofa/table scale, and removed the problematic `chairModernCushion` pair from the central path.
- Tutorial hint could remain stuck on the pickup/throw step if the player chose a quiet route without throwing. Tutorial advancement now accepts later milestone events, so rescue/exit guidance updates even if the optional distraction step is skipped.
- Added a first-person human-style playtest capture script that walks the actual route, interacts with objects, rescues the civilian, reaches the exit, and saves screenshots for review.

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
