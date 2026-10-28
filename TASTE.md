# TASTE · why Chronika looks the way it does

## Idea
Not a "list of notifications", but **a paper archive with a pulse**. A
notification is an entry in an observation journal, not a row in an inbox.
Hence: a paper palette, large typography, a timeline with a rail, and "pulse"
as a metaphor for a living stream.

## Palette
- `Paper #F7F4EA` / `Ink #1B1D17` — the base, warm paper instead of sterile white.
- `Lime #D7F26A` — the "living" accent: pins, readiness, active filter.
- `Coral #B84A2B` — the alarming one: missing permissions, important items, errors.
- `Forest #2D4A32` — the main action tone, not default Material purple.
- Dark theme — `Night #0F120E` with a lime accent, not an inversion of the light one.

Dynamic color is opt-in and off by default: the brand palette matters more than
"wallpaper".

## Composition
- **Asymmetry instead of a grid**: a 246 dp hero block with a `Cookie9Sided`
  blob, offset stat cards, a wave in the background.
- **Timeline rail**: time on the left, a dot, a vertical line; cards shift by
  4–8 dp depending on position — rhythm, not a table.
- **Floating dock** instead of a NavigationBar: three items, the selected one
  stretches and shows a label. Content lives under the dock.
- **Insight mosaic**: one large card plus two small ones, not an even 2×2 grid.
- **Large radii** (22–42 dp) and "pills" for metadata.

## Typography
Display — Black/ExtraBold with negative tracking; utility captions —
monospace with wide tracking. The contrast between the "poster headline" and
the "technical caption" is the core of the character.

## Motion
`MotionScheme.expressive()`: springs on the dock and cards, `pressScale` 0.965,
smooth screen transitions, a pulsing dot in the hero. Animation emphasizes the
action rather than decorating it.

## What we avoided
- The standard `NavigationBar` + `TopAppBar` + `ListItem` list.
- Default purple Material You.
- Uniform cards with the same radius and shadow.
- Placeholder icons: the app shows real package icons, and when they are
  missing — initials in a colored square.

## Functional honesty
The design does not imitate features: every screen works with a real SQLite
database, real notifications, real MediaStore, and real Shizuku. The test button
posts a real notification instead of drawing a fake entry.
