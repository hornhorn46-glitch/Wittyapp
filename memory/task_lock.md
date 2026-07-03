# Task Lock

## User goal
Build the first major playable milestone of a 3D Windows-runnable game named Silent Exit / Тихий выход: a tense, humane stealth-survival tutorial experience with menus, settings, localization, sound, gameplay systems, tests, and completion reports.

## Playable milestone
A user can run the project through the chosen runner, open an atmospheric main menu, change audio/display/language settings, start a 3D tutorial level, learn movement/sneak/interact/pick-up/throw, distract a hostile NPC with a thrown item, rescue a civilian NPC, reach the exit, see win/fail flows, use pause/restart/menu flows, and verify the implementation with automated tests plus documented playtest results.

## Concrete deliverables
- Godot 4.x project using text-based scenes/scripts.
- Main menu with New Game, Settings, Credits, Exit.
- Settings for master/music/SFX volume, fullscreen/window mode, resolution, and EN/RU language with persistence.
- Localization system for all visible UI/tutorial/level strings.
- Sound system with license-safe generated audio assets for UI, footsteps, doors, throw/drop, distant voice, ambient loop, and menu music loop.
- 3D tutorial level with 3-4 distinct areas, doors, cover, safe zone, exit trigger, lighting, fog/post-processing, props, and materials/textures.
- First-person player controller with movement, looking, crouch/sneak, interact, pick up, throw/drop, and pause.
- Hostile NPC with idle/patrol/investigate/return/detect/fail behavior.
- Civilian rescue NPC with idle/fear/follow/rescued states.
- Throwable objects that emit sound events and distract the hostile NPC.
- Tutorial prompts in EN/RU.
- Pause menu and credits.
- Automated tests and at least one deterministic smoke/playthrough simulation where possible.
- README, project state docs, asset credits, testing/playtest reports, known issues, and final report.

## Explicit user constraints
- Do not build only a shallow prototype.
- Use Godot 4.x + GDScript unless an existing repo base makes another stack better.
- No graphic violence, blood, real tragedies, realistic attack instructions, or glorification of hostile actors.
- Use fictional locations and abstract hostile NPCs only.
- EXE build is not required.
- Keep reporting cadence: first 3 real hours every 20 minutes, then every 2 hours.

## Out of scope
- Exported Windows EXE/installer.
- Real-world branded, political, terrorist, extremist, or tragedy-based content.
- High-fidelity character art or facial animation.
- Online services, multiplayer, and save-game progression beyond settings persistence.

## Assumptions
- The repository has no existing game code, so a fresh Godot 4.x project is appropriate.
- Generated procedural audio/textures are acceptable because the task permits generated license-safe assets.
- The first milestone may use stylized low-poly/silhouette characters if the environment, UI, audio, and gameplay feel intentional.
- Manual visual verification may be limited by local runner availability; any blocker will be recorded honestly.

## Definition of done link
See `codex_project_md_pack/10_DEFINITION_OF_DONE.md`.
