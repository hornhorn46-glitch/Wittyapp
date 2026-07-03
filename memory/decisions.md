# Decisions

## Decision: Select Godot 4.x + GDScript

### Context
The repository contains only the instruction pack and `task_1.md`; there is no existing engine project to preserve.

### Advisor notes
- Product Strategist: Godot can reach a playable milestone without heavy editor-only workflows.
- Game Designer: Godot 3D supports first-person stealth, triggers, simple AI, and tutorial scripting.
- Visual/UI Director: Godot UI and 3D lighting are enough for an intentional atmospheric first milestone.
- Engineering Architect: Text `.tscn` and `.gd` files are suitable for autonomous code generation and review.
- QA / Test Auditor: A lightweight Python project validator plus Godot headless smoke run can cover required checks.
- User Advocate: The task explicitly recommends Godot 4.x when no existing base exists.

### Central Consul verdict
Use Godot 4.x + GDScript.

### Why this is safe
It aligns with the brief, keeps files readable, and avoids inventing a non-game-engine workaround for a 3D game.

### Rejected alternatives
- Unity: no existing Unity base and heavier editor/project generation requirements.
- Browser/Three.js: easier to run in some environments, but it ignores the recommended Godot stack and complicates native game feel.

## Decision: Generate License-Safe Audio And Textures Locally

### Context
The task allows license-safe generated assets and requires the game not to be silent or visually bare.

### Advisor notes
- Product Strategist: Original generated assets reduce licensing delay and keep the milestone moving.
- Game Designer: The required feedback sounds can be purposeful even if minimal.
- Visual/UI Director: Procedural textures/materials can establish a coherent tense thriller style.
- Engineering Architect: Generation scripts create reproducible assets and document origin clearly.
- QA / Test Auditor: Local generation avoids flaky external downloads for asset tests.
- User Advocate: The user asked for real sound in game; generated WAVs satisfy that without unclear licenses.

### Central Consul verdict
Generate original WAV files and simple texture PNGs with Python, document them in `ASSET_CREDITS.md`, and replace later only if stronger CC0 assets are added.

### Why this is safe
The assets are created in-repo, have clear ownership/origin, and can be validated automatically.

### Rejected alternatives
- Random web asset scraping: license ambiguity.
- No audio/placeholders only: explicitly forbidden by the task.
