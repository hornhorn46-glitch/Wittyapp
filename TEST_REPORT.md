# Test Report

Date: 2026-07-03

## Commands and Results

```powershell
python tests\validate_project.py
```

Result: Passed. Output: `8 validation tests passed.`

```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_smoke.gd
```

Result: Passed. Output includes `Godot smoke test passed.`

```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_playthrough.gd
```

Result: Passed. Output includes `Godot playthrough simulation passed.`

```powershell
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path . --script tests\visual_capture.gd
```

Result: Passed on Vulkan/Forward+. Russian screenshots saved to `artifacts/screenshots/`.

## Notes

The validator now includes a UTF-8/mojibake guard for Russian localization.

Godot headless and scripted GUI runs still print shutdown cleanup warnings/errors after successful assertions/capture. This is tracked as tooling cleanup noise rather than a gameplay failure.
