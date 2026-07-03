# Testing and Playtest Protocol

Testing must prove that the game works, not merely that files import.

## Required test layers

### 1. Unit tests
Test pure logic:

- economy/resource math
- damage and health
- collision/trigger logic
- level unlock rules
- AI state transitions
- save/load serialization
- config validation

### 2. Integration tests
Test systems together:

- start game from menu
- load level
- spawn entities
- complete objective
- fail objective
- restart level
- return to menu
- save progress and reload

### 3. Headless smoke test
Run the game loop without manual input if the engine allows it.

Minimum checks:

- app initializes
- main menu scene loads
- first level loads
- 60 seconds simulated time without crash, or a shorter deterministic smoke run for constrained environments
- no missing asset exceptions
- no uncaught exceptions

### 4. Bot playthrough / deterministic simulation
If gameplay rules are known, create a simple automated player that attempts the level.

Examples:

- tower defense: place towers by scripted strategy and verify wave completion
- racer: follow centerline and verify checkpoints/turns occur
- stealth: navigate a scripted safe route and verify detection rules
- puzzle: execute known solution steps

### 5. Visual smoke checks
When screenshot or rendering capture is possible, save screenshots from:

- main menu
- gameplay start
- active gameplay
- pause screen
- win/fail screen

Manually or programmatically check:

- no black screen
- no missing textures
- no severe overlap
- no obvious wrong camera direction
- no static background when motion should occur

## Testing log format

Record every command in `memory/testing_log.md`:

```md
## Test run: <date/time or sequential number>

Command:
```bash
...
```

Result:
- Passed/failed:
- Failures:
- Fixes applied:
- Remaining risks:
```

## Required final test command

At the end, run the strongest available validation command, for example:

```bash
python -m pytest
python main.py --smoke-test
python tools/playtest_bot.py --level 1
```

Adapt commands to the actual stack.

## Failure policy

Do not hide failing tests.

If a test fails:

1. Fix the smallest root cause.
2. Re-run the relevant test.
3. Re-run full validation before final report.
4. If still failing due to a hard blocker, document it honestly in `memory/known_issues.md`.
