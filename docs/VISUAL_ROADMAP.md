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

## 🔜 Phase 2 — more game-feel

- **Hit-stop** — a 2–3 frame freeze on kills for extra punch (pairs with the existing screen shake).
- **Enemy hit-flash** — flash the enemy white the moment it's hit.
- **Damage / low-life vignette** — red screen-edge glow when hurt or when the life bar is low.

## 🔜 Phase 3 — atmosphere & lighting

- Animated **torches / braziers** with flickering light halos.
- Soft **vignette** for focus; optional gentle **day/night** tint per run.

## 🔜 Phase 4 — animation depth

- A **shoot / thrust** animation for the player (instead of the bullet just appearing).
- Enemy **telegraphs** (satyr wind-up, wraith lunge) and per-enemy **death animations**.

## 🔜 Phase 5 — environment & maps

- Animated tiles (water, banners, foliage), **auto-tiling** transitions, more props, soft prop shadows, subtle parallax.

## 🔜 Phase 6 — UI / menus / transitions

- A custom **pixel bitmap font** (replacing the default font).
- **Hearts** + a pixel **life bar** instead of text labels; a combo meter with growing flames.
- Pixel-art frames / animated button states; **pixel transitions** (dither / pixelate fade) between menu, game, death and teleport.
- Animated title screen and animated character/map previews in the selector.
