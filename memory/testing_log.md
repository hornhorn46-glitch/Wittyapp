# Testing Log

## Test run: 2026-07-12 presentation polish pass

Command:
```powershell
python tests\validate_project.py
```

Result:
- Passed/failed: Passed
- Output: `8 validation tests passed.`
- Fixes covered: project structure, required assets, localization, and updated script references.
- Remaining risks: static validation does not evaluate rendered composition.

Command:
```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_smoke.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Godot smoke test passed.`
- Fixes covered: GDScript compile warnings in the new finish-pass functions and generated WAV loader log noise.
- Remaining risks: headless dummy renderer still prints `mesh_get_surface_count` cleanup noise after successful assertions.

Command:
```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_input_playtest.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Input-driven playtest passed.`
- Fixes covered: mouse-look, E prompts/interactions, badge pickup, both doors, rescue NPC follow, safe-zone rescue, and exit completion after furniture scale changes.
- Remaining risks: deterministic route validates the main route, not every free-roam camera angle.

Command:
```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_audio_system.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Audio system test passed.`
- Fixes covered: audio manager still loads and plays expected sound streams after WAV loader cleanup.
- Remaining risks: automated audio test checks stream/system behavior, not subjective mix quality.

Command:
```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path . --script tests\room_showcase_capture.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Room showcase capture passed.`
- Fixes covered: Forward+ rendered screenshots after the presentation polish pass.
- Remaining risks: screenshots are review aids; final art direction still needs manual production-art iteration.

## Test run: 2026-07-03 final PBR/light/localization pass

Command:
```powershell
python tests\validate_project.py
```

Result:
- Passed/failed: Passed
- Output: `8 validation tests passed.`
- Fixes covered: added Russian UTF-8/mojibake guard after fixing `localization/ru.json`.
- Remaining risks: static validation checks structure and key content, not full rendered appearance.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_smoke.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Godot smoke test passed.`
- Remaining risks: Godot headless prints dummy renderer cleanup warnings after successful quit.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_playthrough.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Godot playthrough simulation passed.`
- Fixes covered: safe zone moved away from civilian start so follow and rescue are separate states.
- Remaining risks: deterministic route validates systems but not full manual exploration.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path . --script tests\visual_capture.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Visual captures saved.`
- Fixes covered: final Russian visual capture on Vulkan/Forward+ after PBR, furniture, lighting, window, outside-world, and UI icon pass.
- Remaining risks: screenshots are visual smoke checks, not a full manual art-review pass.

## Test run: 2026-07-03 final validation

Command:
```powershell
python tests\validate_project.py
```

Result:
- Passed/failed: Passed
- Failures: none
- Fixes applied: none after final run
- Remaining risks: static validator complements but does not replace engine execution.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_smoke.gd
```

Result:
- Passed/failed: Passed
- Failures: none in assertions
- Fixes applied: fixed patrol typed array, tension light look_at timing, and real room passages before final run
- Remaining risks: Godot headless prints dummy renderer cleanup warnings after successful quit.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_playthrough.gd
```

Result:
- Passed/failed: Passed
- Failures: none after final run
- Fixes applied: increased thrown item sound event loudness so the hostile NPC reliably investigates
- Remaining risks: scripted playthrough validates systems deterministically, not full human navigation skill.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path . --rendering-driver opengl3 --script tests\visual_capture.gd
```

Result:
- Passed/failed: Passed
- Failures: none after final run
- Fixes applied: moved pause menu to viewport-sized overlay and corrected level camera/path composition
- Remaining risks: screenshots are smoke checks, not a full manual art pass.

## Test run: 2026-07-03 visual asset pass

Command:
```powershell
python tests\validate_project.py
```

Result:
- Passed/failed: Passed
- Output: `7 validation tests passed.`
- Remaining risks: static validation checks presence and key source tokens, not full rendered appearance.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_smoke.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Godot smoke test passed.`
- Remaining risks: dummy renderer cleanup errors still print after successful test shutdown.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_playthrough.gd
```

Result:
- Passed/failed: Passed
- Output includes: `Godot playthrough simulation passed.`
- Remaining risks: deterministic route validates systems but not full manual exploration.

Command:
```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path . --rendering-driver opengl3 --script tests\visual_capture.gd
```

Result:
- Passed/failed: Superseded
- Note: This earlier capture attempt was superseded by the final PBR/light/localization Forward+ visual capture above.
- Remaining risks: see the latest final pass entry.
