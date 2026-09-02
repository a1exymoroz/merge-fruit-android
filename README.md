# Merge Fruit — Android

Native Android (Kotlin + Jetpack Compose) rewrite of the [merge-fruit](../../merge-fruit)
web game. No WebView, no React Native — this is a from-scratch native client
that talks to the same `merge-fruit-api` Spring Boot backend as the web app.

**New to Android? Start with [`docs/`](docs/README.md)** — it explains how
this whole project works, mapped to the web/React concepts you already
know, plus a step-by-step guide to opening and running it in Android Studio.

## Stack

- Kotlin, Jetpack Compose (Material 3), Navigation-Compose
- [dyn4j](https://dyn4j.org/) for 2D physics (native counterpart to the web
  version's Matter.js)
- Retrofit + OkHttp + Moshi for the REST API (`/api/auth/*`, `/api/scores`)
- Jetpack DataStore for the persisted JWT (replaces the web's `localStorage`)
- Android's per-app language API for EN/PL/RU (replaces i18next)

## Opening the project

Short version: open this folder in **Android Studio**, let it sync Gradle,
pick an emulator or plug in a phone, hit ▶️ Run. Full walkthrough — including
installing Android Studio and setting up an emulator/device if you've never
done it before — is in [`docs/building-and-running.md`](docs/building-and-running.md).

Command-line alternative: `./gradlew assembleDebug` works once a JDK 17+ is
on `PATH`/`JAVA_HOME` and the Android SDK is installed with
`ANDROID_HOME`/`local.properties` set.

## Pointing at a backend

Debug builds default to `http://10.0.2.2:8080` (the emulator's alias for
your machine's `localhost`); release builds default to the deployed
your configured production backend (see `local.properties`). Override either via a local
`local.properties` entry — see
[`docs/building-and-running.md`](docs/building-and-running.md#4-pointing-it-at-a-backend)
for details and physical-device caveats.

## What has (and hasn't) been verified

This machine has no Android Studio or emulator, but it does have JDK 21 and
network access, so I installed a throwaway Android SDK (compileSdk 35 +
build-tools) and drove Gradle directly:

- `:app:dependencies` — every declared library version (Compose BOM
  2024.06.00, Navigation 2.7.7, DataStore 1.1.1, Retrofit/Moshi/OkHttp,
  dyn4j 4.2.2, etc.) resolves cleanly from Maven Central/Google's repo.
- `:app:compileDebugKotlin` — **the full Kotlin source set compiles
  successfully** against the real Android SDK, Compose, and dyn4j jars. This
  caught and fixed four real bugs before you'd have hit them: a wrong XML
  namespace URI in both launcher-icon vector drawables, a nonexistent
  `Theme.Material3.DayNight.NoActionBar` XML style (that name is a Compose
  concept, not a real style resource — swapped for `Theme.AppCompat.DayNight.NoActionBar`,
  which the project already depends on), dyn4j's `World` class living at
  `org.dyn4j.world.World<Body>` rather than `org.dyn4j.dynamics.World` even
  in 4.2.2, and a hex color literal that Kotlin inferred as `Long` instead of
  `Int`. Everything else about the dyn4j API surface (`Body`, `BodyFixture`,
  `Geometry`, `Transform`, `Vector2`, `MassType`) was confirmed correct
  by inspecting the actual `dyn4j-4.2.2.jar` with `javap`.
- Full `:app:assembleDebug` (producing an installable APK) got as far as
  resource linking and dexing before failing on a missing `jlink` — the
  JDK I used (VS Code's bundled one) is a stripped-down runtime without it.
  That's an environment gap, not a code issue: Android Studio's bundled JBR
  includes `jlink`, so this step should just work there.

**Not verified — needs a real device/emulator run:**

1. **dyn4j physics feel** (`game/PhysicsEngine.kt`) — gravity, restitution,
   friction, and damping are a first-pass guess at values that make drops,
   rolling, and stacking feel like the web version's Matter.js tuning. Expect
   to retune `GRAVITY_MPS2` / `RESTITUTION` / `FRICTION` / `LINEAR_DAMPING`
   (and maybe `PIXELS_PER_METER`) by feel once you can actually play it.
2. Anything only observable at runtime — touch gesture feel, layout on
   different screen sizes, the actual auth/leaderboard round-trip against a
   live backend.

If you want to reuse the SDK I already downloaded instead of Android Studio
fetching its own, it's at `C:\a1exymoroz\android-sdk-temp` (compileSdk 35 +
build-tools 34/35 + platform-tools) — point Android Studio's SDK location at
it, or just let Android Studio download its own; either works.
