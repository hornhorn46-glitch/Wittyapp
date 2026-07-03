# Visual, UI, and Art Direction

The project must not look like a school exercise unless the user explicitly asks for that style.

## Visual direction workflow

Before implementing UI/art, create `memory/visual_direction.md`:

```md
# Visual Direction

## Genre and mood
...

## Reference qualities
Do not copy copyrighted assets. Capture qualities only:
- integrated menu composition
- readable fantasy-specific buttons
- coherent materials/colors
- clear hierarchy

## Palette
- Primary:
- Secondary:
- Accent:
- Danger/warning:
- Background:

## UI rules
- Button shape:
- Panel style:
- Font style:
- HUD placement:
- Animation style:

## Asset plan
- Needed assets:
- Source strategy:
- Temporary placeholders:
```

## Menu quality requirements

Main menu must look like part of the game world.

A good menu has:

- background art or scene composition
- title treatment
- buttons that match the setting
- consistent spacing and alignment
- hover/pressed visual feedback
- no random default colors
- no unreadable text
- no UI overlap at target resolutions

## HUD quality requirements

HUD must be:

- readable while playing
- minimal but informative
- visually consistent with the menu
- not blocking important gameplay areas
- resilient to resolution changes

## Layout rule

Do not guess coordinates randomly.

Use named layout constants or a layout helper:

- safe margins
- anchor points
- scalable units
- relative positioning
- resolution-aware calculations

## Visual QA checklist

Before final report, check:

- main menu at target resolution
- gameplay screen at target resolution
- pause/restart screen
- win/fail screen
- motion/camera artifacts
- asset seams/tiling issues
- readable text contrast
- click/touch target sizes
- no debug visuals left enabled unless requested
