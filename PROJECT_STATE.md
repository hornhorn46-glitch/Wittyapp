# Project State

Status: first playable milestone implemented, visually upgraded, and validated.

Current milestone: first playable Godot 4.x milestone for Silent Exit.

See `memory/task_lock.md` for the locked scope.

Run command:

```powershell
tools\godot\Godot_v4.2.2-stable_win64_console.exe --path .
```

Validation summary:

- Python project validator: passed.
- Godot smoke test: passed.
- Godot deterministic playthrough: passed.
- Visual capture: passed on Vulkan/Forward+ with Russian localization; screenshots in `artifacts/screenshots/`.

Current visual state:

- Curated CC0/PBR materials for brick, painted plaster, concrete, carpet, tile, and wood.
- Kenney furniture/interior props, factory props, character models, textured UI, round hint/pause icons.
- Transparent windows, outside-world sphere/building silhouettes, weather/rain, window lights, warm local lights, reflection probes, SSAO/glow/filmic environment settings.
