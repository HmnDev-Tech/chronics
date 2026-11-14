# TASTE · why Chronics looks the way it does

## Idea
Not a "list of notifications", but **a paper archive with a pulse** powered by
**Material 3 Expressive**. Every notification is an entry in a living field
journal: organic morphing shapes, asymmetric bento proportions, spring physics,
and high-contrast editorial typography replace uniform rounded rectangles.

## Palette
- `Paper #F6F4EC` / `Ink #12150F` — warm archival paper and crisp charcoal ink.
- `Lime #C8FF2E` — living electric signal: pins, readiness, peak activity.
- `Coral #FF5436` — alert & pulse accent: unread items, missing permissions.
- `Forest #1E4628` — grounded primary action tone instead of generic purple.
- `Clay #5C4278` — deep berry/plum secondary accent for high-contrast tags and stats.
- Dark theme — `Night #0B0E09` / `ObsidianNight #141812` with electric lime.

Dynamic color remains opt-in and off by default so the brand palette leads.

## Expressive Shapes & Composition
- **Continuous & stateful polygon morphing**: `MorphPolygonShape` + `androidx.graphics.shapes.Morph`
  continuously morphs `MaterialShapes` (`Cookie9Sided`, `Sunny`, `Clover8Leaf`,
  `Flower`, `SoftBurst`, `Cookie6Sided`, `Cookie4Sided`, `PuffyDiamond`, `Diamond`)
  in hero backdrops, empty states, dock icons, action buttons, and app badges.
- **Deterministic app polygon badges**: `appPolygon(packageName)` clips app icons
  into expressive Material 3 shapes that morph into `MaterialShapes.Sunny` when
  pinned or selected.
- **Asymmetric organic cards**: `asymmetricExpressiveShape` varies corner radii
  across timeline entries, bento mosaic cards, and detail containers instead of
  uniform 24 dp rectangles; connected lists (`AppPickerScreen`) use
  `segmentedItemShape`.
- **Wavy progress & tactile toggles**: `CircularWavyProgressIndicator` and
  `LinearWavyProgressIndicator` visualize daily share, archive capacity, and setup
  readiness; `ToggleButton` with shape morphing replaces generic filter chips.
- **Floating Expressive Dock**: three destinations with spring width expansion,
  icon badge morphing (`Circle` <-> `Cookie9Sided`), and a rotating `Sunny` spark.

## Typography
Display — Black/ExtraBold (`-2.8` to `-0.5` sp tracking) paired with wide-tracked
monospace utility captions (`+1.2` sp).

## Motion
`MaterialTheme.motionScheme` (`MotionScheme.expressive()`): spatial springs on
shape morphs, screen transitions, `pressScale` (`0.955f`), chart growth, and
floating badges. Zero XML layouts or XML shape/drawable edits — pure Jetpack Compose.

## Functional honesty
The design does not imitate features: every screen works with a real SQLite
database, real notifications, real MediaStore, and real Shizuku/Shevery.
