# Level Design Protocol

Levels must not be clones with different names.

## Every level needs a purpose

For each level, document in `memory/progress.md` or a level file:

```md
# Level: <name>

## Player goal
...

## New idea or twist
...

## Layout logic
...

## Main challenge
...

## Visual identity
...

## Win condition
...

## Fail condition
...

## Test/playthrough route
...
```

## Distinctness rule

A new level must differ by at least two meaningful dimensions:

- layout
- enemy behavior
- resource pressure
- objective type
- visual theme
- pacing
- route choice
- mechanic introduced
- risk/reward decision

Changing only background color or enemy count is not enough.

## Racer-specific rule

If the game is a racer:

- road curvature must be real in gameplay, not just sideways sprite drift
- scenery movement must match forward motion and turns
- camera/road should communicate speed and steering direction
- checkpoints/track progress must verify the player is moving along a route
- tests should detect that turns/checkpoints actually happen

## Stealth/survival-specific rule

If the game is stealth/survival:

- line of sight must matter
- sound/noise must matter if included
- cover/hiding must matter if included
- non-violent options must be viable if requested
- AI states must be readable: idle, suspicious, searching, alerted
- success must not require unrealistic real-world tactical violence

## Tower defense-specific rule

If the game is tower defense:

- towers must have distinct roles, not only different DPS
- enemies must have meaningful resistances/weaknesses
- economy must be simulated for early/mid/late waves
- at least one scripted strategy should beat each tested wave with target surplus
