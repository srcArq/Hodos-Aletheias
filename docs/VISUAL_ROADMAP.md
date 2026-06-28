# 🎨 Visual roadmap — Hodos Aletheias (pixel-art)

Planned visual upgrades for the game, faithful to its pixel-art style.

**Principles:** everything on the 16 px grid, no anti-aliasing / sub-pixel, limited and
coherent palettes per biome, **readability first** (player / enemy / threat always distinct),
fixed virtual resolution (320×180) scaled by integers.

---

## ✅ Implemented — Phase 1: juice & readability

Additive, draw-only effects (no gameplay-logic changes):

- **Blob shadows** under the player, satyrs and wraiths — grounds the sprites and separates them from the floor.
- **Pixel particle system** (`entities/Particle.kt`, pre-allocated pool of 240, zero runtime allocation):
  - death **poof** + white impact **sparks** when an enemy dies (brown for satyrs, purple smoke for wraiths)
  - **footstep dust** while the player moves
  - **pickup sparkle** (gold for coins, green for life)
  - **teleport burst** (cyan vortex) on warp
- **Per-biome ambient tint** — a subtle colour wash behind the actors: warm for the Greek overworld, torch-warm for the Roman temple, cool dusk for the Roman arena. Actors draw on top and stay crisp.

Procedural primitives (`GameAssetManager.buildPrimitives()`): a 1×1 white pixel for particles/overlays and a soft elliptical shadow.

---

## ✅ Implemented — Phase 2: more game-feel

- **Hit-stop** — the simulation freezes for ~0.06 s on each kill (effects keep animating) for extra punch, pairing with the existing screen shake. Implemented by gating the sim and extracting the render into `drawWorld()`.
- **Enemy hit-flash** — a bright white "pop" flashes at the enemy the moment it dies (on top of the coloured death poof).
- **Damage / low-life vignette** — a red screen-edge glow that flashes when the player takes a hit and pulses while the life bar is low (HUD overlay driven by `setDamageVignette`).

## ✅ Implemented — Phase 3: atmosphere

- **Torch-light flicker** — the per-biome ambient tint subtly pulses over time for a living-light feel.
- **Ambient dust motes** — faint particles drift up across the scene.
- **Dark focus vignette** — a permanent subtle screen-edge darkening (HUD overlay) for mood/focus.

## ✅ Implemented — Phase 4: attack feel

- **Muzzle flash** — a bright burst at the spear's spawn point on every shot.

## ✅ Implemented — Phase 6: HUD & transitions

- **Hearts** for health and a **segmented life bar** replacing the old text labels (procedurally generated pixel icons).
- **Damage / low-life red vignette** (Phase 2) layered over the dark focus vignette.
- **Fade-in** from black at the start of each run, plus a **fade-from-black transition** on every menu / selector / info / game-over screen change.
- **Custom pixel bitmap font** — a procedurally generated 5×7 small-caps BMFont (`ui/pixel.fnt` + `pixel.png`, with lowercase/accent aliases) replaces the default libGDX font across all menus and HUD labels; nearest-filtered for crisp scaling, with a safe fallback.

## 🔜 Remaining — needs new art assets / map authoring

Best done as a focused, art-directed pass (these require authored sprites or Tiled/tileset changes):

- **New sprite animations**: player attack/thrust, enemy telegraphs (satyr wind-up, wraith lunge) and per-enemy death animations.
- **Environment**: animated tiles (water, banners, foliage), auto-tiling transitions, more props, subtle parallax.
- **Menus**: animated title screen, animated character/map previews, and richer pixel screen-transitions (dither/pixelate) — a basic fade-from-black between screens is already in.
