# The game & physics

This is the one part of the app with no direct web equivalent, since it
replaces Matter.js with a different physics engine entirely. Here's how the
web version worked, and how this maps to the Android version.

## Recap: how the web version did it

`useGamePhysics.ts` created a Matter.js `Engine` + `Runner`, added three
static walls (left/right/bottom) and a circle per fruit, listened for
`collisionStart` events to detect two same-type fruits touching, and ran a
`requestAnimationFrame` loop to read body positions back out for rendering
every frame.

## The Android version, piece by piece

### `GameConstants.kt` — the numbers

[`GameConstants.kt`](../app/src/main/java/com/a1exymoroz/mergefruit/game/GameConstants.kt)
is a direct port of the web's `gameConstants.ts`: the 11 fruit types (id,
name, points, radius), the container's width/height, where the "game over"
line sits, how long a fruit can sit above it before it's actually game
over, etc. Same numbers, just in Kotlin.

### `PhysicsEngine.kt` — the simulation

This wraps [dyn4j](https://dyn4j.org/), a 2D physics engine written in
Kotlin/Java (so it runs natively on Android — no JavaScript, no WebView).
It plays the same role Matter.js's `Engine` played:

- **Bodies.** Each fruit is a dyn4j `Body` with a circular shape
  (`Geometry.createCircle`), plus how bouncy/slippery it is (restitution,
  friction — same concepts Matter.js used, just dyn4j's API for them).
- **Walls.** Three static (immovable) bodies for the container's left,
  right, and bottom edges — created once in `PhysicsEngine.init`.
- **Gravity.** Set once via `world.gravity = Vector2(0, -GRAVITY)`. dyn4j
  uses a "y increases upward" coordinate system (like real-world physics),
  while the game's layout — and the web version's — uses "y increases
  downward" (screen coordinates, y=0 at the top). `PhysicsEngine` converts
  between the two at every boundary (see the `dpToM`/`worldCenterDp`
  helper functions) so the rest of the app never has to think about it.
- **Merging.** The web version relied on Matter.js firing a
  `collisionStart` *event*. This version doesn't use dyn4j's event system
  at all — instead, `PhysicsEngine.step()` just checks, every single frame,
  whether any two live fruits of the same type are now overlapping (simple
  distance-between-centers math). This is simpler to reason about and
  avoids a whole class of "did this event fire twice?" bugs the web
  version had to specifically guard against (`mergeQueueRef` + a 50ms
  delay) — see the comment in `step()` for why that guard isn't needed
  here.
- **Units.** dyn4j's numbers are meant to represent "meters", not pixels —
  using pixel-sized numbers directly (e.g. gravity ~800) would make the
  simulation behave badly, since the engine's internal tuning assumes
  objects in roughly the 0.1–10 range. `PhysicsEngine` picks a scale
  (`PIXELS_PER_METER`) and converts every position/size into that space
  before handing it to dyn4j, then converts back for rendering. This has no
  equivalent in the web version, since Matter.js is fine working directly
  in pixels.

### `GameViewModel.kt` — the frame loop

Where `requestAnimationFrame` drove the browser's loop, this app uses
Android's `Choreographer` — the platform's own "run this once per screen
refresh" API (same idea, same ~60fps cadence, just the native API instead
of a browser one). Each tick:

1. Ask `PhysicsEngine` to advance the simulation by however much time
   actually passed (`step(dtSeconds)`).
2. Add any merge points to the running score.
3. Check the "is a fruit stuck above the line" timer — same 2-second grace
   period logic as the web's `useGamePhysics.ts`.
4. Publish the new fruit positions as a `GameUiState`, which the Compose
   screen is watching.

`GameViewModel` also owns "what's the next fruit to drop" (same 80%/20%
weighted random pick as `fruitUtils.ts`'s `generateNextFruit`), and the
drop/reset actions the UI calls into.

### `ui/fruit/FruitArt.kt` — drawing a fruit

The web version drew each fruit as inline SVG (`fruitArt.tsx`). Compose has
no SVG renderer built in, so each fruit is instead drawn with a `Canvas` —
Compose's low-level "draw shapes yourself" API (closest web analogy: the
HTML5 `<canvas>` 2D context, `drawCircle`/`drawLine`/etc. read almost the
same). Every fruit's colored rings, seeds, and the little level-number
badge are ported shape-by-shape from the original SVG. A few of the purely
decorative flourishes (the blueberry's highlight swirl, the cherry's
stem+leaf) were simplified to plain shapes — the parts that make each fruit
*recognizable* (the concentric rings, the seed dots) were kept faithfully.

### `ui/game/GameScreen.kt` — putting it on screen

This is the Compose equivalent of `MergeFruitGame.tsx` +
`GameContainerWrapper.tsx` + `DropZone.tsx` combined: it reads
`GameViewModel`'s state every frame and draws each fruit at its current
position (`FruitVisual(...)`, offset and rotated to match the physics
body), draws the game-over line, and handles the drag-to-position/
release-to-drop touch gesture (using Compose's low-level pointer-input
API — the touch equivalent of the web version's `onTouchStart`/
`onTouchMove`/`onTouchEnd` handlers on `DropZone.tsx`).

## Why this needed its own numbers, not the web's

The web app's `restitution: 0.4`, `friction: 0.6`, `gravity.y: 0.8`, etc.
were tuned for Matter.js's own internal units — they don't transfer
literally to dyn4j, which uses a different scale and solver. The values in
`PhysicsEngine.kt`'s `companion object` are a reasoned first pass (matching
the *proportions* the web version used — restitution and friction are
still 0.4/0.6, just gravity and the pixel-to-meter scale are dyn4j-specific
choices) rather than a byte-for-byte port, and are the first thing worth
tweaking once you can actually play it and see how drops/rolling/stacking
feel.
