# Known Issues

## Medium: Headless Godot cleanup noise

- Issue: Godot headless tests pass with exit code 0 but print dummy renderer cleanup messages such as `Parameter "m" is null` and resource leak warnings after quitting.
- Impact: Test output is noisy. Final assertions still pass, and gameplay/visual capture works.
- Next fix: Investigate a cleaner test shutdown path or Godot-specific headless renderer settings.

## Low: Audio is still first-pass generated

- Issue: Visuals now use curated CC0/PBR and Kenney packs, but the audio bed and effects are still original procedural placeholders.
- Impact: The milestone is playable and visually stronger, while the soundscape still feels like a prototype.
- Next fix: Replace selected audio with curated license-safe ambience, footsteps, doors, UI, and throw/drop effects, then update credits.
