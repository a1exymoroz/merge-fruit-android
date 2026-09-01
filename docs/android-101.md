# Android 101 (for web developers)

An Android Studio project looks nothing like a Vite project at first glance.
Here's what every top-level thing in this repo actually is, described in
terms of the web project's equivalent.

## Gradle = npm + Vite + webpack config, combined

| Web (`merge-fruit`) | Android (`merge-fruit-android`) | What it does |
|---|---|---|
| `package.json` | `app/build.gradle.kts` | Declares dependencies and build settings |
| `package-lock.json` | *(no direct equivalent — Gradle resolves at build time)* | |
| `vite.config.ts` | `app/build.gradle.kts` (`android { }` block) | Build configuration: target platform versions, build variants, etc. |
| `npm install` | *(automatic — Gradle does this itself)* | Downloads dependencies |
| `npm run dev` | Android Studio's ▶️ Run button, or `./gradlew assembleDebug` | Builds and (for Studio) launches the app |
| `node_modules/` | `~/.gradle/caches/` (a machine-wide cache, not in the project) | Where downloaded libraries actually live |

**Gradle** is the build tool (like Vite/webpack), and it's driven by Kotlin
files (`build.gradle.kts`) instead of JSON/JS config. There are two of them
here:

- [`build.gradle.kts`](../build.gradle.kts) (root) — barely anything, just
  declares which Gradle *plugins* exist (the Android plugin, the Kotlin
  plugin).
- [`app/build.gradle.kts`](../app/build.gradle.kts) — the real one. This is
  where `dependencies { }` lives (≈ your `package.json` dependencies list),
  plus Android-specific settings like `minSdk`/`targetSdk` (which Android
  versions the app supports) and `applicationId` (like a bundle
  ID/package name — this app's is `com.a1exymoroz.mergefruit`).

`gradlew`/`gradlew.bat` are the "Gradle wrapper" — a pinned copy of the
Gradle tool itself, so anyone opening the project gets the exact same
Gradle version without installing it globally. It's the Android-world
equivalent of committing a `.nvmrc` — except the wrapper *is* the tool, not
just a version pin.

## The manifest: your app's "table of contents"

[`app/src/main/AndroidManifest.xml`](../app/src/main/AndroidManifest.xml)
is a bit like `index.html` + `package.json`'s metadata fields combined. It
declares:

- The app's entry point (`MainActivity`) — closest analogy: the `<script>`
  tag in `index.html` that boots your React app.
- Permissions the app needs (`INTERNET`, so it's allowed to make network
  calls at all — the web app didn't need to ask for this, browsers just
  allow it).
- App-level metadata: icon, display name, theme, supported languages.

## `res/` — everything that *isn't* code

Android strictly separates code from "resources": strings, colors, images,
XML layouts. This project's [`app/src/main/res/`](../app/src/main/res/)
folder is the closest thing to a mix of your `public/` folder and your
`i18n/locales/*.json` files:

- `res/values/strings.xml` — every piece of user-facing text (English). ≈
  `src/i18n/locales/en.json`.
- `res/values-pl/strings.xml`, `res/values-ru/strings.xml` — the same
  strings in Polish/Russian. Android automatically picks the right one
  based on the device's language — there's no runtime "switch language"
  code needed the way `i18next` needed one, it's baked into the platform.
- `res/values/colors.xml` — named colors, though this project mostly just
  writes hex colors directly in Kotlin instead (more on that in
  [Architecture](architecture.md)).
- `res/drawable/`, `res/mipmap-*/` — images/icons. This app's launcher icon
  is drawn as XML vector shapes rather than a PNG, which is why you'll see
  `ic_launcher_background.xml`/`ic_launcher_foreground.xml` instead of a
  `.png` file.
- `res/xml/locales_config.xml` — tells Android which languages this app
  supports, for the per-app language picker in the phone's Settings app.

Each resource gets an auto-generated ID. If `res/values/strings.xml` has
`<string name="game_score">Score: %1$s</string>`, Kotlin code refers to it
as `R.string.game_score` — `R` is a class Android generates for you at
build time (you never write it by hand, similar to how Vite's asset imports
generate URLs for you).

## Kotlin ≈ TypeScript, Jetpack Compose ≈ React

**Kotlin** is Android's primary language now (Java is still supported but
Kotlin is the default for new projects). If you know TypeScript, Kotlin
will feel very familiar: static types, type inference, null-safety
(`String?` means "nullable", just like TS's `string | null`), data classes
(≈ TS interfaces/types, but also generate `equals`/`copy`/etc.), lambdas,
and coroutines (Kotlin's `async`/`await`, spelled `suspend fun` and
`launch { }`).

**Jetpack Compose** is Google's modern *declarative* UI toolkit — and
"declarative UI toolkit" is exactly what React is. The mental model
transfers almost directly:

| React | Compose |
|---|---|
| Function component | `@Composable fun MyScreen() { }` |
| JSX (`<div>...</div>`) | Nested composable calls (`Column { Text(...) }`) |
| `useState` | `remember { mutableStateOf(...) }` |
| Re-render on state change | "Recomposition" — same idea, same trigger |
| Props | Function parameters |
| Context API | `CompositionLocal` |
| `useEffect` | `LaunchedEffect` |
| Redux/Zustand store | `ViewModel` + `StateFlow` (see [Architecture](architecture.md)) |

There is **no HTML/CSS here at all** — Compose doesn't render to a DOM, it
draws directly. Layout is done with `Column`/`Row`/`Box` (≈ flexbox
column/row/stack) and `Modifier.padding()`/`.width()`/etc. instead of CSS.

## Activities: the one "page" this app has

An **Activity** is roughly "a screen the OS knows how to launch/switch to"
— historically Android apps had one Activity per screen, but that's an old
pattern. This app follows the modern approach: **one single Activity**
([`MainActivity`](../app/src/main/java/com/a1exymoroz/mergefruit/MainActivity.kt)),
and everything else — login, signup, the game, the leaderboard — is a
Compose screen swapped in and out by Navigation-Compose, exactly like a
single-page React app where `BrowserRouter` swaps components without a full
page reload. The Activity is closest to `index.html` + `main.tsx`: it's
just the thing that boots Compose and hands off control.

## Where to go next

Now that the vocabulary is out of the way: [**Architecture**](architecture.md)
walks through this specific app's folder structure and how a screen
actually gets its data.
