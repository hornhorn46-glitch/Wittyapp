# Assets and Licenses

If the user asks to find assets on the internet, do that task directly.

Do not replace internet asset search with procedural placeholders unless external access is unavailable or no suitable legal assets can be found.

## Allowed asset source priorities

Prefer assets with clear licenses:

1. CC0 / public domain assets
2. permissive open licenses allowing commercial use
3. paid/user-provided assets already in the project
4. generated placeholder assets only as temporary fallback

## Good asset source examples

- Kenney assets with clear license terms
- OpenGameArt entries with explicit license metadata
- Itch.io asset packs with clear license terms
- Wikimedia/Public Domain sources when appropriate
- User-provided assets in the repository

## Forbidden asset behavior

Do not:

- use copyrighted game art directly
- scrape random images without license clarity
- copy Hearthstone/CS/AAA assets
- use real tragedy footage or real victim imagery
- leave asset origin undocumented
- claim generated placeholders are final if the task requested real assets

## Asset log format

Every added asset must be recorded in `memory/assets_log.md`:

```md
## Asset: <name>

- File path:
- Source URL or origin:
- Author:
- License:
- Commercial use allowed: yes/no/unknown
- Modified: yes/no; details
- Attribution required: yes/no; exact text
- Used for:
```

## If internet access is unavailable

Create temporary assets only when necessary, and mark them clearly:

```md
## Temporary asset fallback

- Reason internet asset search failed:
- Placeholder created:
- What final asset is needed:
- Search query to run later:
```

## Visual consistency rule

Assets must be curated, not dumped.

Before adding an asset, check:

- style compatibility
- resolution and scaling
- palette compatibility
- readability in gameplay
- license clarity
- whether it supports the game fantasy
