# Galaxy Store submission — copy & answers

Draft text and questionnaire answers for the Samsung Galaxy Store listing of
**Merge Fruit** (`com.a1exymoroz.mergefruit`, versionName `1.0`, versionCode `1`,
minSdk 26 / targetSdk 35). Transcribe these into Seller Portal — nothing here is
submitted automatically.

Privacy policy URL (once GitHub Pages is enabled — see `docs/README` note below):
`https://a1exymoroz.github.io/merge-fruit-android/privacy-policy.html`

---

## Store listing

### App title
```
Merge Fruit
```

### Short description / summary (~80 chars)
```
Drop fruits, merge matching pairs, chain reactions, and race to the watermelon.
```

### Full description
```
Merge Fruit is a relaxing physics merge-puzzle. Drag to aim, release to drop,
and when two identical fruits touch they merge into the next fruit up. Line up
chain reactions, use the walls and slopes to your advantage, and keep the pile
below the game-over line.

FEATURES
- Simple one-finger controls: drag to position, release to drop
- Real 2D physics — fruits roll, tip, and stack
- Chain reactions when a fresh merge lands next to more matching fruit
- Boosters: Bomb, Upgrade, Swap, and Hold
- Online leaderboard — sign in to save your best scores, or play as a guest
- Three languages: English, Polski, Русский
- Classic and Winter themes
- No ads. No tracking. Internet is used only for sign-in and the leaderboard.

Goal: reach the Watermelon.
```

### Keywords / tags
```
merge, puzzle, fruit, physics, casual, drop, watermelon, relaxing, offline-style
```

### Category
Games → Puzzle (or Casual)

### Support contact
`a1exy.moroz.pl@gmail.com`

---

## Content rating questionnaire (IARC — used by Galaxy Store)

Samsung routes age rating through the IARC questionnaire. Answers for Merge Fruit:

| Question area | Answer |
|---|---|
| Violence (cartoon, fantasy, realistic) | None |
| Blood / gore | None |
| Sexual content, nudity, suggestive themes | None |
| Profanity, crude humor | None |
| Alcohol, tobacco, or drug reference/use | None |
| Simulated or real-money gambling | None |
| Horror / fear / scary content | None |
| Hate speech / discrimination | None |
| **Do users interact with each other?** | Yes — a leaderboard shows other players' chosen display names and scores. No chat, no direct messaging, no user-generated media. |
| Do users share their physical location with other users? | No |
| Is users' personal information shared with third parties? | No |
| Does the app share info on social media? | No |
| Unrestricted access to the internet / web browsing? | No — the app only contacts its own backend API |
| Digital purchases / in-app purchases? | No |
| In-game currency, loot boxes, or randomized rewards purchasable with real money? | No |
| Does the app contain ads? | No |

Expected outcome: rated for **all ages / Everyone / PEGI 3 / ESRB E**, possibly
with a "Users Interact" / "Online Interactivity" interactive-elements notice
because of the leaderboard.

---

## Data-safety / privacy declaration (if asked separately)

- **Data collected:** email address, password (transmitted for auth; stored only
  as a salted hash server-side), gameplay scores + chosen display name.
- **Data shared with third parties:** none.
- **Collection optional?** Yes — guest play collects nothing.
- **Encrypted in transit:** yes (HTTPS).
- **Deletion method:** email request to `a1exy.moroz.pl@gmail.com`.
- **Permissions requested:** `INTERNET` only.
- **SDKs:** no ad, analytics, or crash-reporting SDKs.

---

---

## Step-by-step: creating the app in Seller Portal

### 0. Assets you need on hand
| Asset | Spec | Status |
|---|---|---|
| Signed release APK | `app/build/outputs/apk/release/app-release.apk` (v2-signed, `com.a1exymoroz.mergefruit`, versionCode 1) | ✅ built |
| App icon | 512 × 512 PNG | ✅ `docs/store-assets/icon-512.png` (regenerate with `make_icon.py`) |
| Screenshots | 4–8 phone screenshots, PNG/JPG, min ~480 px, portrait | ✅ `docs/store-assets/screenshots/` — 6 shots, 1080×2242 (status bar + "Playing as Guest" header cropped off), captured on an API-37 emulator (welcome, early game, full board, fruit dropping, game over, how-to-play) |
| Privacy policy URL | public HTTPS page | ⬜ enable GitHub Pages (below) |

### 1. Register as a seller (one-time)
1. Go to <https://seller.samsungapps.com> and sign in with your Samsung account (`a1exy.moroz.pl@gmail.com`).
2. Choose **Individual** seller (Commercial needs business/tax documents; Individual is fine for a free app with no in-app purchases).
3. Fill in name / address / phone, accept the Seller Portal agreement. Individual accounts are approved immediately.

### 2. Add New Application
1. Dashboard → **Add New Application** → set **Default language** = English → **Create**.
2. **App Information**
   - Title: `Merge Fruit`
   - Short + full description: paste from the sections above
   - App category: **Games → Puzzle** (or Casual)
   - Privacy policy URL: `https://a1exymoroz.github.io/merge-fruit-android/privacy-policy.html`
3. **Binary**
   - Upload `app-release.apk`. The portal reads the package name, versionCode, min/target SDK automatically.
   - Leave it as the default binary for all resolutions / API levels.
4. **Country / Region**: **Add All**, or pick a set (at minimum Poland). Choose "release automatically after approval".
5. **Store Listing / Graphics**
   - App icon: upload `docs/store-assets/icon-512.png`
   - Screenshots: upload `docs/store-assets/screenshots/01…06` (1080×2242, status
     bar and in-app guest header already cropped off).
   - (Feature/promo graphics are optional — skip for v1)
6. **Age rating**: click into the **IARC questionnaire** and answer per the table above. Save the certificate it issues.
7. **Price**: Free.
8. Review the summary, then **Submit for review**. Samsung's review typically takes 1–3 business days; you'll get an email on approval or rejection.

### 3. After approval
- Confirm the listing is live and the privacy-policy link resolves.
- Keep `app/keystore/release.jks` + the `local.properties` signing passwords backed up off-machine — every future update must be signed with this exact key.

---

## Enabling the privacy-policy URL

The policy is committed as static HTML under `docs/`. To publish it:

1. GitHub → repo **Settings → Pages**
2. **Source:** Deploy from a branch → Branch `main`, folder `/docs`
3. Save. After ~1 min the policy is live at
   `https://a1exymoroz.github.io/merge-fruit-android/privacy-policy.html`
   (landing page at `.../merge-fruit-android/`).
