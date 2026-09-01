# Docs: start here

You know the [web version](../../../merge-fruit) (React + TypeScript). This doc
set explains the Android version the same way, mapping each Android concept
to the closest web equivalent you already know. No prior Android experience
assumed.

Reading order:

1. **[Android 101](android-101.md)** — what a Gradle/Android project even
   *is*: the pieces (Gradle, the manifest, `res/`, the `R` class) and what
   each one is "instead of" from the web world.
2. **[Architecture](architecture.md)** — how this specific app is laid out:
   the folders, the layers, and how data flows from a tap on screen down to
   a network request and back.
3. **[The game & physics](game-and-physics.md)** — how the fruit-dropping,
   merging, physics simulation actually works under the hood (this is the
   part with no web equivalent, since it replaces Matter.js entirely).
4. **[Auth, networking & storage](auth-networking-storage.md)** — how
   login/signup/leaderboard calls work, and where the login token is kept
   between app launches.
5. **[Building & running](building-and-running.md)** — how to actually open
   this in Android Studio, run it on an emulator or your phone, and point it
   at a backend.

## The one-paragraph version

This is a single Android app (one "Activity") whose entire UI is built with
**Jetpack Compose** — Google's modern UI toolkit, which is close enough to
React that most of what you know transfers directly (composable functions
≈ components, recomposition ≈ re-render, `remember`/`State` ≈ `useState`).
Screens are wired together with **Navigation-Compose** (≈ React Router).
Physics runs on **dyn4j**, a Kotlin/Java physics engine, instead of
Matter.js. Networking uses **Retrofit** (≈ `fetch` + a typed API client)
talking to the *exact same* Spring Boot backend the web app uses — nothing
on the server changed.
