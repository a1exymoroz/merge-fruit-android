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

## Enabling the privacy-policy URL

The policy is committed as static HTML under `docs/`. To publish it:

1. GitHub → repo **Settings → Pages**
2. **Source:** Deploy from a branch → Branch `main`, folder `/docs`
3. Save. After ~1 min the policy is live at
   `https://a1exymoroz.github.io/merge-fruit-android/privacy-policy.html`
   (landing page at `.../merge-fruit-android/`).
