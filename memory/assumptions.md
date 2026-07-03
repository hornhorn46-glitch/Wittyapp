# Assumptions

- Assumption: Godot 4.x + GDScript is the selected stack.
  Reason: The repository has no existing codebase, and the task explicitly recommends Godot 4.x when no base exists.
  Risk: Godot may not be installed locally or network download may fail.
  How to change later: Replace the runner/project with another engine only if a clear blocker appears and document the scope change.

- Assumption: Procedurally generated audio and textures count as license-safe original assets for this milestone.
  Reason: The task allows own generation and generated placeholders when real assets are not available.
  Risk: Generated assets may be less polished than curated external packs.
  How to change later: Replace with CC0/Kenney/OpenGameArt assets and update `ASSET_CREDITS.md`.

- Assumption: First-person controls are preferred.
  Reason: The brief says first-person is preferred if faster and more atmospheric.
  Risk: Some players may prefer third-person visibility.
  How to change later: Add a third-person camera mode in a later milestone.
