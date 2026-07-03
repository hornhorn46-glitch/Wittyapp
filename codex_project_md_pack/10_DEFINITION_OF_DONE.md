# Definition of Done

A task is done only when the milestone is playable, validated, and documented.

## General done checklist

- [ ] User goal is restated in `memory/task_lock.md`.
- [ ] Assumptions are documented.
- [ ] Important decisions are documented.
- [ ] Existing structure was preserved where reasonable.
- [ ] Code runs from clean start using documented commands.
- [ ] Main gameplay loop is playable.
- [ ] Menu, gameplay, pause/restart, win/fail flows work.
- [ ] Visual style is coherent enough for a small indie milestone.
- [ ] Assets are documented with licenses or marked as temporary.
- [ ] Automated tests cover core logic.
- [ ] Integration/smoke test covers actual game startup/loop.
- [ ] A bot/scripted playthrough or deterministic simulation was attempted.
- [ ] Known issues are documented honestly.
- [ ] Final report tells the user exactly what is ready and how to run it.

## Game-specific done checklist

- [ ] Player can understand objective within 10 seconds.
- [ ] Controls are visible or intuitive.
- [ ] Player receives feedback for important actions.
- [ ] Losing is explainable.
- [ ] Winning is explainable.
- [ ] Restart does not require relaunching the program.
- [ ] Repeated play/restart cycles do not crash.
- [ ] No obvious graphical artifacts in normal play.
- [ ] Performance is acceptable for the target platform.

## Asset task done checklist

If the user asked to find assets:

- [ ] Search was actually performed.
- [ ] Licenses were checked.
- [ ] Assets were downloaded or linked according to project needs.
- [ ] Attribution requirements were documented.
- [ ] Assets were integrated or a clear integration plan was created.
- [ ] Fallback placeholders are clearly marked as temporary.

## Not done examples

The task is not done if:

- tests were not run
- the game opens but cannot be played
- levels are identical
- UI is unreadable or obviously placeholder
- assets have unknown licensing
- final report hides broken features
