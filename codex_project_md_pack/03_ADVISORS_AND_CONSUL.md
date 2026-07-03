# Advisors and Central Consul

Use this decision protocol whenever making important product, architecture, gameplay, visual, or scope decisions.

## Advisors

### 1. Product Strategist
Checks whether the feature supports the player fantasy, market fit, onboarding, retention, and a clear playable milestone.

Questions:

- Does this make the game more playable or only bigger?
- Is the core loop clearer after this change?
- Does this help the user show the project to another person?

### 2. Game Designer
Checks mechanics, pacing, difficulty, level readability, controls, feedback, and player agency.

Questions:

- Is there a real decision for the player?
- Are levels meaningfully different?
- Is failure understandable and recoverable?
- Is the player doing the intended fantasy, not fighting the controls?

### 3. Visual/UI Director
Checks whether menus, HUD, buttons, typography, composition, animation, and assets look coherent.

Questions:

- Does the menu look like part of the game world?
- Are buttons visually integrated instead of pasted on top?
- Are colors, fonts, panels, and background consistent?
- Are visual artifacts visible during movement/camera changes?

### 4. Engineering Architect
Checks code structure, maintainability, performance, data-driven content, and minimal safe changes.

Questions:

- Is the current structure preserved where possible?
- Is logic separated enough to test?
- Are assets/configs data-driven where useful?
- Will this scale to more levels/entities without copy-paste?

### 5. QA / Test Auditor
Checks automated tests, deterministic simulations, playthroughs, edge cases, and regression risks.

Questions:

- Can this be tested without human eyes only?
- Did we simulate or play through the actual loop?
- What breaks if FPS drops, assets are missing, or input is weird?
- Did we test win, loss, restart, pause, menu, and level transitions?

### 6. User Advocate
Checks whether the result matches the user's explicit intent, not Codex's convenient shortcut.

Questions:

- Did the user ask for this exact thing?
- Did we ignore any explicit instruction?
- Is this useful to the user immediately?
- Is the final report honest about what is done and not done?

## Central Consul decision format

Before major changes, write a compact decision in `memory/decisions.md`:

```md
## Decision: <name>

### Context
...

### Advisor notes
- Product Strategist: ...
- Game Designer: ...
- Visual/UI Director: ...
- Engineering Architect: ...
- QA / Test Auditor: ...
- User Advocate: ...

### Central Consul verdict
<chosen decision>

### Why this is safe
...

### Rejected alternatives
- ...
```

Do not overuse this for tiny fixes. Use it for choices that affect direction, architecture, visuals, scope, or risk.
