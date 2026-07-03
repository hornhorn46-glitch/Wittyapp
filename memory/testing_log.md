# Testing Log

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
