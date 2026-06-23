# Hodos Aletheias · Ὁδὸς Ἀληθείας

> *"The Path of Truth"* — a top-down, pixel-art **arcade survival shooter** built with **libGDX + Kotlin** for Android.

[![Build APK](https://github.com/srcArq/Hodos-Aletheias/actions/workflows/build.yml/badge.svg)](https://github.com/srcArq/Hodos-Aletheias/actions/workflows/build.yml)

Pick a hero, pick a scenario, and survive as long as you can: patrolling satyrs and relentless wraiths close in while your life steadily drains. Chain kills for combo multipliers, grab coins and life, use the portals to escape — and beat your own high score.

> Academic project for the **DAM** (*Desarrollo de Aplicaciones Multiplataforma*) cycle — Android package `dam_mo8_eac4_ex2.conejo_l`.

<p align="center">
  <img src="docs/img/start.png" alt="Title screen — HODOS ALÉTHEIA" width="85%">
</p>

---

## 🎯 Why it's fun

- **Pick-up-and-play tension** — one stick to move, one button to fire; a run is a frantic minute (or several) of dodging and shooting.
- **Risk vs. reward** — your life bar always drains, so you can't just hide: you must push into danger for coins, life and combo multipliers.
- **Readable, fair threat** — satyrs patrol predictable lanes while wraiths hunt you down; everything is out-runnable, so every death feels earned.
- **Juicy feedback** — screen shake, hit flashes, floating score pop-ups and an invulnerability blink make every action feel good.
- **Three biomes, three heroes** — pair a Greek hoplite, a Roman warrior or a golden golem with a Greek overworld or two Roman arenas, each with its own maze.

---

## 📸 Screenshots

| Character & scenario select | In-game |
|:---:|:---:|
| ![Selection screen](docs/img/select.png) | ![Gameplay](docs/img/gameplay.png) |
| **Pick 1 of 3 heroes + 1 of 3 maps** | **Survive satyrs and chasing wraiths** |

<p align="center">
  <img src="docs/img/gameover.png" alt="Game Over screen" width="70%"><br>
  <em>Game Over — restart the same run, or take the arrow back to the selector to change hero/map.</em>
</p>

---

## ✨ Features

- 🏛️ **3 selectable scenarios** — *Grècia* (original Greek overworld), *Temple* (Roman temple) and *Arena* (Roman arena), each with its own tileset, decoration and a hand-built **labyrinth**.
- 🎭 **3 playable heroes** — the *Hoplita* (Greek hoplite), the *Guerrera* (Roman warrior woman) and the *Golem* (golden golem), each with full 8-direction idle/walk animations.
- 👹 **2 enemy types** — **satyrs** that patrol (worth +100) and **wraiths** that *chase* the player with per-axis wall-sliding (worth +200).
- 🔥 **Combo system** — chained kills raise a multiplier up to **×5**; the streak expires if you stop killing.
- 📈 **Progressive difficulty (fair)** — more satyrs join the longer you last, but enemy speed is constant and always below yours, so the game ramps up by *numbers*, never by unavoidable speed.
- 🪙 **Collectibles** — coins (+50) and life pickups (+5 life, +25 points) that respawn so the map never empties.
- ⏳ **Life decay** — your life bar ticks down over time, forcing you to keep moving and scoring.
- 🌀 **Teleport portals** — paired warp pads to dodge enemies and cross the map.
- 🏛️ **Depth columns** — walk *behind* the upper part of columns via a dedicated "overhead" render layer.
- 🏆 **Persistent high score** — your best run is saved between sessions.
- 🎨 **Procedurally generated** pixel-art sprites, tilesets, maps and sound effects.

---

## 🎮 Controls

| Action | Touch (Android) | Keyboard (debug) |
|---|---|---|
| Move | On-screen **joystick** (bottom-left) | `W` `A` `S` `D` |
| Shoot | Cyan **fire button** (bottom-right) | `Space` |
| Navigate menus | Tap buttons / cells | — |

The in-game UI text is in **Catalan** (the project's language); the source code and comments are in English.

---

## 🧮 Scoring

| Pickup / Kill | Points |
|---|---:|
| Coin | +50 |
| Life pickup | +25 (and +5 life) |
| Satyr | +100 |
| Wraith | +200 |
| Combo | up to **×5** the kill value |

You start with **3 health** and a **life bar of 20** that decays one point every 10 seconds. Run ends when either reaches zero.

---

## 📥 Download & Play

Grab the latest **[Release (v1.0)](https://github.com/srcArq/Hodos-Aletheias/releases/latest)** — or the copy committed in the repo:

➡️ **[`dist/HodosAletheias-debug.apk`](dist/HodosAletheias-debug.apk)**

1. Download the APK to an Android device (**Android 5.0 / API 21 or newer**).
2. Allow installation from unknown sources if prompted.
3. Install and launch — the game runs in landscape.

---

## 🛠️ Build from source

**Requirements:** JDK 21 (e.g. the Android Studio JBR), the Android SDK with **compileSdk 35**, and the bundled Gradle wrapper.

```bash
# Build the Android debug APK
./gradlew :android:assembleDebug
# Output: android/build/outputs/apk/debug/android-debug.apk
```

Or open the project in **Android Studio** and run the `android` configuration on an emulator or device.

---

## 🧩 Technologies

- **Language:** Kotlin 2.1.0 (built with the Android Studio JBR / JDK 21)
- **Engine:** libGDX 1.13.1
  - `Game` / `Screen` lifecycle, `OrthographicCamera` + `FitViewport` (320×180 virtual resolution)
  - **Scene2D** UI for every menu and the in-game HUD (`Stage`, `Table`, `Touchpad`, `Image`, `Label`)
  - **Tiled** maps (`.tmx`) via `TmxMapLoader` + `OrthogonalTiledMapRenderer`, with selective per-layer rendering for depth
  - `Animation` sprite sheets, `Sound` SFX, `Pixmap` runtime textures, object **pooling** for bullets, and `Preferences` for the persistent high score
- **Platform:** Android — `compileSdk 35` / `minSdk 21`, forced landscape, edge-to-edge handled via `WindowInsetsController`
- **Build:** Gradle 8.14.3 · Android Gradle Plugin 8.7.3 · committed Gradle wrapper
- **CI:** GitHub Actions builds the debug APK on every push and uploads it as an artifact
- **Assets:** sprites, tilesets, maps and sound effects are all **procedurally generated** with Python + Pillow

---

## 📂 Project structure

```
Atheleia/
├─ core/                     # Shared game logic (Kotlin)
│  └─ src/main/kotlin/dam_mo8_eac4_ex2/conejo_l/
│     ├─ GameScreen.kt           # Main loop: state, update, render, scoring
│     ├─ HodosAletheias.kt       # libGDX Game entry point
│     ├─ core/
│     │  ├─ GameHUDController.kt  # Scene2D HUD: menus, selector, gameplay, game over
│     │  ├─ GameAssetManager.kt   # Textures, animations and sound loading
│     │  └─ GameMapParser.kt      # Parses collisions / collectibles / enemies / teleports
│     └─ entities/
│        ├─ Player.kt  Satyr.kt  Wraith.kt
│        └─ Bullet.kt  BulletPool.kt  Collectible.kt
├─ android/                  # Android launcher + assets (maps, sprites, sfx, ui)
└─ dist/                     # Pre-built APK
```

---

## 🔧 Engineering & polish

Passes that hardened the game and the repo:

- **Headless gameplay simulation** — a faithful port of the entity + game-loop logic runs ~5 minutes per map across the full difficulty ramp, parsing the real maps and checking for crashes, NaNs, out-of-bounds and runaway entity counts. Result: **no crashes found**.
- **Balance fixes from that simulation** — enemy speed is now **constant** (no escalating-speed chaos; satyrs stay slower than the player), and a short **spawn-grace invulnerability** stops the player from being instantly swarmed.
- **Robustness** — fixed a bullet double-free in the recycling pool and added a player world-bounds clamp.
- **Clean codebase** — all source comments in English, dead code removed, `.gitattributes` for consistent line endings.
- **Reproducible builds** — committed Gradle wrapper + CI that builds the APK on every push; published as a tagged Release.

## 🗺️ Roadmap / possible improvements

- Per-map high scores, a run timer and a death/kill counter
- A pause menu and a sound / music on–off toggle
- A difficulty selector on the start screen
- More enemy variety (and a simple boss)
- Optional English UI (the in-game text is currently Catalan)

## 📜 Notes

This is an educational project. Feel free to read, build and learn from it.
