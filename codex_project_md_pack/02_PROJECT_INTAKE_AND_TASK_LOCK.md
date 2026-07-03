# Project Intake and Task Lock

This file prevents vague work and prevents Codex from quietly shrinking the task.

## Step 1 — Restate the task

Create or update `memory/task_lock.md` before implementation.

Template:

```md
# Task Lock

## User goal
<What the user actually wants, not a diluted version.>

## Playable milestone
<What must be true when this task is complete.>

## Concrete deliverables
- ...
- ...
- ...

## Explicit user constraints
- ...

## Out of scope
- ...

## Assumptions
- ...

## Definition of done link
See `10_DEFINITION_OF_DONE.md`.
```

## Step 2 — Detect hidden requirements

For game tasks, always infer and document:

- target player fantasy
- core loop
- fail/win conditions
- minimum content amount
- visual mood
- UI style
- input method
- performance target
- test strategy

## Step 3 — Task lock rule

After `memory/task_lock.md` is written, do not silently downgrade the milestone.

Examples of forbidden downgrades:

- User asks for asset search → Codex draws colored rectangles instead.
- User asks for a playable racer → Codex makes a straight road with sideways drifting only.
- User asks for distinct levels → Codex makes two identical maps with different names.
- User asks for modern UI → Codex makes default buttons on a black background.
- User asks for tests → Codex only launches the game once manually.

If scope must be reduced, write the reason in `memory/scope_changes.md` and keep the fallback playable.
