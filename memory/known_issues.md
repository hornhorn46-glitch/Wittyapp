# Known Issues

## Medium: Headless Godot cleanup noise

- Issue: Headless Godot tests pass with exit code 0 but print dummy-renderer cleanup/resource warnings after successful assertions.
- Impact: No known gameplay impact; test output is noisy.
- Next action: Investigate cleaner Godot test shutdown/render settings.

## Low: First-pass generated art and audio

- Issue: Audio/textures are original generated assets, not curated production packs.
- Impact: Playable milestone is coherent, but later polish can improve feel.
- Next action: Replace the strongest candidates with curated CC0 assets and update `ASSET_CREDITS.md`.
