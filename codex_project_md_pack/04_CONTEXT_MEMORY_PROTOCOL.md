# Context and Markdown Memory Protocol

The project context must live primarily in Markdown files so the chat does not become the only memory.

## Required memory folder

Create this folder if it does not exist:

```text
memory/
```

## Required files

```text
memory/task_lock.md
memory/assumptions.md
memory/decisions.md
memory/progress.md
memory/testing_log.md
memory/visual_direction.md
memory/assets_log.md
memory/known_issues.md
memory/final_report.md
```

## What goes where

### `memory/task_lock.md`
The frozen interpretation of the user's request and milestone.

### `memory/assumptions.md`
Assumptions made because the user is unavailable or the brief is incomplete.

### `memory/decisions.md`
Important decisions made through the Advisors and Central Consul protocol.

### `memory/progress.md`
Chronological progress log.

### `memory/testing_log.md`
Every test command, result, failure, fix, and final validation.

### `memory/visual_direction.md`
UI/visual style guide for this specific game.

### `memory/assets_log.md`
Every asset source, license, author, URL/path, modifications, and attribution needs.

### `memory/known_issues.md`
Known bugs or rough edges, with severity and next action.

### `memory/final_report.md`
Final user-facing report after the milestone.

## Update cadence

Update memory files when:

- the milestone is locked
- a major decision is made
- an asset is added or replaced
- a level is added
- a system becomes testable
- a bug is found or fixed
- tests are run
- the final milestone is reached

## Rule

Do not rely on chat history for critical project facts. Persist them in `memory/`.
