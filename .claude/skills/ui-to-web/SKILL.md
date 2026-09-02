---
name: ui-to-web
description: Turn recent Android UI-only changes (uncommitted diff, latest commit(s), or a PR) into a ready-to-paste prompt describing the same visual change for the sibling merge-fruit web app. Use when a UI/visual fix landed on the Android app and needs to be ported to the web app.
---

# Port an Android UI change into a web-app prompt

merge-fruit-android is a Compose port of the React web app at
`../merge-fruit` (sibling folder, `../../merge fruit/merge-fruit` from this
repo's grandparent, i.e. `C:\a1exymoroz\merge fruit\merge-fruit`). Every
visual element has a same-purpose counterpart there. This skill's job is
**only** to read what changed on Android and write a clear, self-contained
prompt describing the equivalent web change — not to edit the web repo
directly (this session isn't rooted there, and the web repo has its own
`ui-only` skill meant to receive exactly this kind of prompt).

## 1. Pick the diff source

Args may name a PR number or branch; otherwise auto-detect in this order
and say which one was picked:

1. **Named PR** (e.g. `/ui-to-web 42`): `gh pr diff 42`.
2. **Named branch**: `git diff main...<branch>`.
3. **Uncommitted changes** exist (`git status --short` non-empty): use
   `git diff HEAD` (covers staged + unstaged).
4. **Otherwise**: local commits ahead of `main` — `git diff main...HEAD`
   (or `git show HEAD` if there's exactly one commit).

## 2. Filter to UI-relevant files only

In scope (visual/presentational — mirrors the web repo's `ui-only` skill):

- `app/src/main/java/.../ui/**/*.kt` — Compose screens/components. Within
  a file, only the markup/Canvas-drawing/styling parts count, not
  ViewModel or state logic that happens to live nearby.
- `app/src/main/java/.../ui/theme/**` (colors, `GameTheme.kt` skin specs).
- `app/src/main/res/values*/strings.xml` — copy/text.
- `app/src/main/res/drawable*/**`, `mipmap*/**` — images/icons.

Out of scope — ignore these even if touched in the same diff:

- `AuthRepository.kt` / anything under `data/` — logic/state bugs, not UI.
- `GameViewModel.kt`, `PhysicsEngine.kt`, `GameConstants.kt` — gameplay
  balance and physics (points, radius, gravity) unless the specific hunk
  is a pure color/label constant.
- `build.gradle.kts`, `local.properties*`, `gradle.properties`,
  `AndroidManifest.xml`, signing/keystore — build/release plumbing.
- `docs/**`, `README.md`.

If nothing in the diff is UI-relevant, say so plainly and stop — don't
invent a prompt for non-existent visual changes.

## 3. Translate each in-scope hunk to implementation-neutral terms

Describe *intent*, not Kotlin/Compose API calls — the web app is React +
TypeScript + plain per-component CSS (`Component.tsx` + `Component.css`),
Matter.js for physics, so Compose-specific vocabulary (`Modifier`,
`Canvas`, `drawPath`, dp units) doesn't translate directly. State what
changed visually and why. Check the equivalent web file first
(`../merge-fruit/src/...`, same component-name pattern minus the Kotlin
suffix) so the prompt can name the actual file to edit. Example mapping:

| Android file | Web equivalent |
|---|---|
| `ui/fruit/FruitArt.kt` | `src/constants/fruitArt.tsx` |
| `ui/game/GameScreen.kt` | `src/components/containers/MergeFruitGame.tsx` + `src/components/ui/GameOverLine.tsx` etc. |
| `ui/game/WinterDecorations.kt` | `src/components/ui/WinterDecorations.tsx` + `.css` |
| `ui/theme/GameTheme.kt` / `Color.kt` | `src/constants/theme.ts`, `src/index.css` `[data-theme]` blocks |
| `res/values/strings.xml` | `src/i18n/locales/en.json` (+ `pl.json`, `ru.json`) |

## 4. Flag binary assets separately

A prompt is text — it can't carry a PNG. If the Android diff added or
changed a drawable (e.g. `app/src/main/res/drawable-nodpi/candy_cane.png`),
call this out explicitly in the output: name the source file's exact path
in this repo and instruct the user to copy it into the web repo
themselves (e.g. `public/` or wherever that asset type already lives
there) before running the web prompt, since the receiving session can't
reach into this repo to fetch it.

## 5. Write the output

Produce one self-contained markdown prompt, phrased as an instruction to
a fresh Claude Code session rooted in the web repo, that:

- Opens by telling it to use the `ui-only` skill for this work.
- Lists each visual change as its own bullet, naming the actual web file
  from the mapping above where you could identify it.
- Calls out any asset the user needs to copy over first, and where.
- Leaves out anything from the "out of scope" list — don't let a
  non-UI Android fix leak into the web prompt.

Print the prompt in its own fenced block in the reply, and also save it to
a file (the session's scratchpad dir, or `.claude/scratch/ui-to-web-prompt.md`
in this repo if no scratchpad path is available) so it's easy to copy.
Tell the user the file path plainly. Don't touch anything in the web repo
yourself — if the user then wants the change actually applied there
too, that's a separate ask.
