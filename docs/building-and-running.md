# Building & running

## 1. Install Android Studio

Download it from [developer.android.com/studio](https://developer.android.com/studio)
and run the installer. On first launch it offers a setup wizard — accept
the defaults; this installs the Android SDK and an emulator system image
for you. This replaces `npm install` — instead of a package manager pulling
JS libraries, Android Studio pulls the SDK/build tools the whole ecosystem
needs.

You do **not** need to separately install Gradle, the Android SDK, or a
JDK — Android Studio bundles all three and manages them for you.

## 2. Open the project

`File → Open`, pick the `merge-fruit-android` folder (this one, the one
containing `settings.gradle.kts`). Android Studio will notice it's a
Gradle project and start a "Gradle sync" automatically — a progress bar at
the bottom does dependency resolution + indexing, similar in spirit to an
`npm install` + IDE type-indexing pass combined. First sync can take a few
minutes since it's downloading the Compose/AndroidX libraries.

If it asks to install a specific SDK version (this project targets
**API 35 / Android 15**), let it — one click.

## 3. Run it

You need something for the app to run *on*: either the built-in emulator,
or your own phone.

### Option A — the emulator (easiest, no phone needed)

1. `Tools → Device Manager` → `Create Device` → pick any phone (e.g. Pixel
   8) → pick a system image (anything API 26+; API 34/35 recommended) →
   Finish. This downloads a virtual device image once, then reuses it.
2. Pick that device in the dropdown next to the ▶️ Run button at the top
   of the window, then click ▶️. Android Studio builds the app (this is
   the actual `gradlew assembleDebug` step, just triggered through the UI)
   and installs+launches it on the emulator, which pops up as its own
   window.

### Option B — your own Android phone

1. On the phone: `Settings → About phone` → tap "Build number" 7 times
   (this unlocks Developer Options — a standard, harmless Android
   feature). Then `Settings → Developer options → USB debugging` → enable
   it.
2. Plug the phone into your computer via USB. It'll prompt "Allow USB
   debugging?" on the phone screen — accept it.
3. The phone now shows up in Android Studio's device dropdown next to
   ▶️ Run, same as the emulator would. Click ▶️.

Either way, this is the Android equivalent of `npm run dev` + opening
`localhost:5173` — except the "browser" is a real (or virtual) phone, and
there's a literal `Run` button instead of a terminal command (though
`./gradlew installDebug` from a terminal does the same thing, if you
prefer).

## 4. Pointing it at a backend

Just like the web app's `VITE_API_BASE_URL`, this app needs to know where
`merge-fruit-api` lives. It's configured per build type instead of a
`.env` file:

- **Debug builds** (what `Run` in Android Studio builds by default) point
  at `http://10.0.2.2:8080`. That's not a typo — `10.0.2.2` is the Android
  **emulator's** special alias for "the computer the emulator is running
  on", i.e. your machine's `localhost`. So if you run
  `merge-fruit-api` locally (however you normally do — Docker, `mvn
  spring-boot:run`, etc.) and launch this app on the **emulator**, it just
  works, no config needed.
- A **physical phone** can't resolve `10.0.2.2` (it's not the same
  machine!) — either run the backend somewhere your phone can reach over
  the network and override the URL (see below), or point at the deployed
  backend instead.
- **Release builds** point at whatever `releaseApiBaseUrl` (or
  `RELEASE_API_BASE_URL`) resolves to — a placeholder by default in the
  tracked files; set the real one in your own `local.properties` (see
  below), which is git-ignored.

To override either without editing any tracked file, create a
`local.properties` file in the project root (this file is already
git-ignored — Android Studio actually creates one automatically on first
sync, to record where your SDK is installed, so you may already have one)
and add:

```properties
debugApiBaseUrl=http://192.168.1.23:8080
releaseApiBaseUrl=https://your-backend.example.com
```

(Use your computer's LAN IP, not `localhost`, if testing from a physical
phone against a backend running on your machine — the phone is a separate
device on the network.)

## 5. Making changes and seeing them

Compose supports "Live Edit" for many kinds of changes (tweaking a color,
text, or layout updates live on the running app without a full rebuild —
similar to Vite's hot module replacement). For anything structural
(new files, changed function signatures), just hit ▶️ Run again — Gradle
only rebuilds what changed, so it's usually fast after the first build.

## If something doesn't build

This project's Kotlin code has already been compiled successfully against
the real Android SDK and every declared library (see the root
[README](../README.md)'s verification section) — so a build failure in
Android Studio is much more likely to be a **local environment** issue
(SDK version not installed yet, first-sync still downloading things) than
a code problem. Android Studio's `Build` panel shows the actual error;
if it's unclear, `Build → Clean Project` then `Build → Rebuild Project` is
the standard "turn it off and on again" fix.
