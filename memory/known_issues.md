# Known Issues

## Medium: Headless Godot cleanup noise

- Issue: Headless Godot tests pass with exit code 0 but print dummy-renderer cleanup/resource warnings after successful assertions.
- Impact: No known gameplay impact; test output is noisy.
- Next action: Investigate cleaner Godot test shutdown/render settings.

## Low: Stylized low-poly presentation ceiling

- Issue: The scene now has curated CC0 props/PBR materials and an additional detail pass, but characters and many props remain stylized rather than production-realistic.
- Impact: The milestone reads as a polished indie vertical slice, not a high-fidelity commercial art pass.
- Next action: Replace character stand-ins with a cohesive licensed character/animation pack and add baked lightmap/occluder work in the Godot editor.
