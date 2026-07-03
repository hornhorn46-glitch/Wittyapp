# Silent Exit

Silent Exit / Тихий выход is a first playable Godot 4.x milestone for a tense, non-graphic 3D stealth-survival tutorial.

## Requirements

- Godot 4.2.x or newer Godot 4.x.
- Python 3.12+ for asset generation and project validation.

This repository includes a local portable Godot runner at `tools/godot/Godot_v4.2.2-stable_win64_console.exe` when downloaded during setup.

## Run

```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path .
```

If you have Godot in PATH:

```powershell
godot --path .
```

## Test

```powershell
python tests\validate_project.py
$env:APPDATA=(Resolve-Path '.godot_user\AppData\Roaming').Path
$env:LOCALAPPDATA=(Resolve-Path '.godot_user\AppData\Local').Path
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_smoke.gd
tools\godot\Godot_v4.2.2-stable_win64_console.exe --headless --path . --script tests\godot_playthrough.gd
```

The local `APPDATA`/`LOCALAPPDATA` assignments keep Godot test settings/cache inside the project workspace.

## Controls

- WASD: move
- Mouse: look
- Ctrl: crouch/sneak
- E: interact/pick up/guide civilian
- Left mouse: throw held item
- Right mouse: drop held item
- Esc: pause

## Current Milestone

The current milestone includes an atmospheric main menu, settings with persistence, EN/RU localization, generated license-safe audio, a 3D tutorial level with side rooms, a hostile NPC, a rescued civilian NPC, throwable distraction items, tutorial prompts, minimap, pause flow, textured sci-fi UI, curated CC0/PBR visual assets, furniture/interior props, windows with outside views, weather, local/window lighting, reflection probes, automated validation, and reporting files.
