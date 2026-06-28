package dam_mo8_eac4_ex2.conejo_l

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import dam_mo8_eac4_ex2.conejo_l.core.GameAssetManager
import dam_mo8_eac4_ex2.conejo_l.core.GameMapParser
import dam_mo8_eac4_ex2.conejo_l.core.GameHUDController
import dam_mo8_eac4_ex2.conejo_l.core.Teleport
import dam_mo8_eac4_ex2.conejo_l.entities.Bullet
import dam_mo8_eac4_ex2.conejo_l.entities.Satyr
import dam_mo8_eac4_ex2.conejo_l.entities.Wraith
import dam_mo8_eac4_ex2.conejo_l.entities.Particle
import java.util.Random

class GameScreen(val game: HodosAletheias) : Screen {

    // --- GAME STATE ---
    enum class GameState { PAUSED, RUNNING, GAME_OVER }
    private var state = GameState.PAUSED

    // --- TIMERS ---
    private var lifeDecayTimer = 0f
    private val LIFE_DECAY_INTERVAL = 10f

    // --- INJECTED MODULES ---
    private lateinit var assetManager: GameAssetManager
    private lateinit var mapParser: GameMapParser
    private lateinit var hudController: GameHUDController

    // --- GAME RESOURCES ---
    private var map: TiledMap? = null
    private var renderer: OrthogonalTiledMapRenderer? = null
    private var camera: OrthographicCamera? = null
    private var batch: SpriteBatch? = null

    // --- ENTITIES AND STRUCTURES ---
    private val walls = Array<Shape2D>()
    private val collectibles = Array<Collectible>()
    private val satyrs = Array<Satyr>()
    private val satyrSpawnPositions = Array<Vector2>()
    // Second enemy type: wraiths that chase the player
    private val wraiths = Array<Wraith>()
    private val wraithSpawnPositions = Array<Vector2>()
    private val WRAITH_POINTS = 200
    private lateinit var player: Player

    // Bullet System
    private lateinit var bulletPool: BulletPool
    private val activeBullets = Array<Bullet>()
    private val MAX_BULLETS = 2

    // --- SCORE AND COMBO ---
    private val KILL_POINTS = 100
    private var comboKills = 0
    private var comboTimer = 0f
    private val COMBO_WINDOW = 4f

    // --- PROGRESSIVE DIFFICULTY ---
    private var difficultyTimer = 0f
    private val DIFFICULTY_INTERVAL = 12f
    private var difficultyLevel = 0
    private val MAX_SATYRS = 40
    private val SPAWN_GRACE = 2.5f   // seconds of invulnerability granted at the start of each run

    // --- COLLECTIBLE RESPAWN ---
    private val collectibleTemplates = Array<Collectible>()
    private var collectibleRespawnTimer = 0f
    private val COLLECTIBLE_RESPAWN_INTERVAL = 7f

    // --- PERSISTENT HIGH SCORE ---
    private lateinit var prefs: Preferences
    private var bestScore = 0

    // --- EFFECTS (juice) ---
    private var shakeTime = 0f
    private var shakeIntensity = 0f
    private val floatingTexts = Array<FloatingText>()
    private var font: BitmapFont? = null

    // --- PARTICLES & BIOME TINT (visual juice) ---
    private val particles = List(240) { Particle() }
    private var dustTimer = 0f
    private val biomeTints = arrayOf(
        Color(1f, 0.97f, 0.88f, 1f),   // Greek overworld — warm
        Color(1f, 0.90f, 0.78f, 1f),   // Roman temple — torch-warm
        Color(0.82f, 0.86f, 1f, 1f)    // Roman arena — cool dusk
    )
    private var hitStopTimer = 0f
    private var prevHealth = 3
    private var damageFlash = 0f
    private var pulseT = 0f

    // --- WORLD BOUNDS (keeps the player from walking off the map into the void) ---
    private var worldW = 0f
    private var worldH = 0f

    // --- TELEPORTS ---
    private val teleports = Array<Teleport>()
    private var teleportArmed = true   // must leave every pad to re-arm (prevents ping-pong)

    // --- LAYERED RENDERING (depth: the "overhead" layer is drawn on top of the player) ---
    private var beforeLayers = IntArray(0)
    private var overheadLayers = IntArray(0)

    // --- SELECTION (menu): available maps and characters ---
    private val MAPS = arrayOf("maps/map.tmx", "maps/roman_temple.tmx", "maps/roman_arena.tmx")
    private val CHARS = arrayOf("characters/character.png", "characters/warrior.png", "characters/golem.png")
    private var chosenCharIdx = 0
    private var chosenMapIdx = 0
    private var worldLoaded = false

    override fun show() {
        // Lightweight setup: the world is loaded when the player picks in the menu (beginGame)
        camera = OrthographicCamera().apply { setToOrtho(false, 320f, 180f) }
        batch = SpriteBatch()

        prefs = Gdx.app.getPreferences("atheleia")
        bestScore = prefs.getInteger("best", 0)

        font = BitmapFont().apply {
            data.setScale(0.4f)
            setUseIntegerPositions(false)
        }

        hudController = GameHUDController(800f, 480f, ::shoot, ::beginGame, ::restartGame)
        state = GameState.PAUSED   // initial menu
    }

    // Loads the world with the chosen character and map, then starts the run.
    fun beginGame(charIdx: Int, mapIdx: Int) {
        chosenCharIdx = charIdx.coerceIn(0, CHARS.size - 1)
        chosenMapIdx = mapIdx.coerceIn(0, MAPS.size - 1)

        // Release the previous world (if we play again with a different choice)
        if (worldLoaded) {
            map?.dispose(); renderer?.dispose()
            if (::assetManager.isInitialized) assetManager.dispose()
            if (::player.isInitialized) player.dispose()
            if (::bulletPool.isInitialized) bulletPool.clear()
        }
        walls.clear(); collectibles.clear(); collectibleTemplates.clear()
        satyrSpawnPositions.clear(); satyrs.clear(); teleports.clear()
        wraiths.clear(); wraithSpawnPositions.clear()
        activeBullets.clear(); floatingTexts.clear()

        val chosenMap = MAPS[chosenMapIdx]
        map = TmxMapLoader().load(chosenMap)
        renderer = OrthogonalTiledMapRenderer(map)
        try {
            val props = map!!.properties
            worldW = (props.get("width", Integer::class.java).toInt() * props.get("tilewidth", Integer::class.java).toInt()).toFloat()
            worldH = (props.get("height", Integer::class.java).toInt() * props.get("tileheight", Integer::class.java).toInt()).toFloat()
        } catch (e: Exception) { worldW = 0f; worldH = 0f }

        val layerNames = ArrayList<String>()
        for (l in map!!.layers) layerNames.add(l.name ?: "")
        val ohIdx = layerNames.indexOf("overhead")
        beforeLayers = (0 until layerNames.size).filter { it != ohIdx }.toIntArray()
        overheadLayers = if (ohIdx >= 0) intArrayOf(ohIdx) else IntArray(0)

        assetManager = GameAssetManager().apply { loadAllAssets(CHARS[chosenCharIdx]) }
        mapParser = GameMapParser(map!!, assetManager)
        walls.addAll(mapParser.parseCollisions())
        collectibles.addAll(mapParser.parseCollectibles())
        collectibleTemplates.addAll(mapParser.parseCollectibles())
        satyrSpawnPositions.addAll(mapParser.parseSatyrSpawns())
        wraithSpawnPositions.addAll(mapParser.parseWraithSpawns())
        teleports.addAll(mapParser.parseTeleports())

        player = Player(384f, 12f, assetManager.getPlayerAnimations())
        player.grantInvulnerability(SPAWN_GRACE)   // brief shield so the player isn't instantly swarmed at spawn
        difficultyLevel = 0
        createSatyrs()
        createWraiths()
        bulletPool = BulletPool(assetManager.getSpearRegions())

        lifeDecayTimer = 0f; difficultyTimer = 0f; comboKills = 0; comboTimer = 0f
        collectibleRespawnTimer = 0f; shakeTime = 0f; shakeIntensity = 0f; teleportArmed = true
        for (p in particles) p.active = false; dustTimer = 0f
        hitStopTimer = 0f; prevHealth = 3; damageFlash = 0f

        worldLoaded = true
        state = GameState.RUNNING
        assetManager.playStart()
        Gdx.app.log("GAME_STATE", "Joc Iniciat: char=${CHARS[chosenCharIdx]} map=$chosenMap")
    }

    // Satyrs keep a CONSTANT patrol speed — difficulty no longer accelerates them, so the
    // game never spirals into escalating-speed chaos. Added challenge over time comes from
    // enemy numbers and the chasing wraiths, not from faster-and-faster satyrs.
    private fun currentSpeedMultiplier(): Float = 1f

    private fun spawnNewSatyr() {
        if (satyrSpawnPositions.isEmpty) return
        val r = Random()
        val randomIndex = r.nextInt(satyrSpawnPositions.size)
        val spawnPos = satyrSpawnPositions[randomIndex]
        val isHorizontal = r.nextBoolean()
        satyrs.add(Satyr(spawnPos.x, spawnPos.y, isHorizontal, assetManager.getSatyrAnimations(), currentSpeedMultiplier()))
    }

    private fun createSatyrs() {
        val r = Random()
        val numToSpawn = 15
        val shuffledPositions = Array(satyrSpawnPositions)
        shuffledPositions.shuffle()
        val actualSpawnCount = if (shuffledPositions.size >= numToSpawn) numToSpawn else shuffledPositions.size

        for (i in 0 until actualSpawnCount) {
            val pos = shuffledPositions[i]
            val isHorizontal = r.nextBoolean()
            satyrs.add(Satyr(pos.x, pos.y, isHorizontal, assetManager.getSatyrAnimations(), currentSpeedMultiplier()))
        }
    }

    private fun createWraiths() {
        val n = minOf(wraithSpawnPositions.size, 6)
        for (i in 0 until n) {
            val p = wraithSpawnPositions[i]
            wraiths.add(Wraith(p.x, p.y, assetManager.getWraithAnimations()))
        }
    }

    private fun shoot() {
        if (state != GameState.RUNNING) return
        if (activeBullets.size >= MAX_BULLETS) return

        player.let { p ->
            val newBullet = bulletPool.obtain()
            if (newBullet != null) {
                newBullet.init(p.getMuzzlePosition(), p.currentDirection)
                activeBullets.add(newBullet)
                assetManager.playShoot()
            }
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        hudController.hudStage.act(delta)

        if (state != GameState.RUNNING) {
            hudController.hudStage.draw()
            return
        }

        // Hit-stop: freeze the simulation briefly after a kill for extra punch; effects keep animating.
        if (hitStopTimer > 0f) {
            hitStopTimer -= delta
            for (p in particles) p.update(delta)
            drawWorld(delta)
            return
        }

        // --- LIFE TIMER ---
        lifeDecayTimer += delta
        if (lifeDecayTimer >= LIFE_DECAY_INTERVAL) {
            player.lifeScore = (player.lifeScore - 1).coerceAtLeast(0)
            lifeDecayTimer = 0f
        }

        // --- PROGRESSIVE DIFFICULTY: more satyrs, faster, over time ---
        difficultyTimer += delta
        if (difficultyTimer >= DIFFICULTY_INTERVAL) {
            difficultyTimer = 0f
            difficultyLevel++
            repeat(2) { if (satyrs.size < MAX_SATYRS) spawnNewSatyr() }
            Gdx.app.log("DIFFICULTY", "Nivell $difficultyLevel | sàtirs ${satyrs.size} | vel x${currentSpeedMultiplier()}")
        }

        // --- COMBO: the streak expires if too long passes without a kill ---
        if (comboKills > 0) {
            comboTimer -= delta
            if (comboTimer <= 0f) comboKills = 0
        }

        // --- COLLECTIBLE RESPAWN: the map never empties completely ---
        collectibleRespawnTimer += delta
        if (collectibleRespawnTimer >= COLLECTIBLE_RESPAWN_INTERVAL) {
            collectibleRespawnTimer = 0f
            if (collectibles.size < collectibleTemplates.size) respawnRandomCollectible()
        }

        // Game Over check
        if (player.lifeScore <= 0 || player.health <= 0) {
            triggerGameOver()
        }

        if (state == GameState.GAME_OVER) {
            hudController.hudStage.draw()
            return
        }

        // --- INPUT HANDLING ---
        var inputX = hudController.touchpad.knobPercentX
        var inputY = hudController.touchpad.knobPercentY

        // Keyboard input
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputX = 1f
        else if (Gdx.input.isKeyPressed(Input.Keys.A)) inputX = -1f
        if (Gdx.input.isKeyPressed(Input.Keys.W)) inputY = 1f
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) inputY = -1f
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) shoot()


        // --- LOGIC UPDATE ---
        player.update(delta, inputX, inputY, walls)

        // Keep the player inside the map bounds (avoids walking into the black void)
        if (worldW > 0f && worldH > 0f) {
            player.position.x = player.position.x.coerceIn(0f, worldW - player.FRAME_WIDTH)
            player.position.y = player.position.y.coerceIn(0f, worldH - player.FRAME_HEIGHT)
            player.bounds.setPosition(player.position.x + 4f, player.position.y)
        }

        // Footstep dust while moving
        dustTimer -= delta
        if ((kotlin.math.abs(inputX) > 0.25f || kotlin.math.abs(inputY) > 0.25f) && dustTimer <= 0f) {
            dustTimer = 0.11f
            emit(player.position.x + 8f, player.position.y + 2f, 2, 0.82f, 0.78f, 0.68f, 14f, 0.32f, 2f, 6f)
        }

        // --- TELEPORTS --- (must leave every pad to re-arm: prevents ping-pong)
        val onPad = teleports.any { Intersector.overlaps(player.bounds, it.rect) }
        if (!onPad) teleportArmed = true
        if (teleportArmed && onPad) {
            for (tp in teleports) {
                if (Intersector.overlaps(player.bounds, tp.rect)) {
                    val dest = teleports.firstOrNull { it !== tp && it.link == tp.link }
                    if (dest != null) {
                        val dx = dest.rect.x + dest.rect.width / 2f - 8f
                        val dy = dest.rect.y + dest.rect.height / 2f - 16f
                        player.position.set(dx, dy)
                        player.bounds.setPosition(dx + 4f, dy)
                        teleportArmed = false
                        assetManager.playTeleport()
                        spawnFloatingText("WARP", dx, dy + 34f, Color.CYAN)
                        emit(dx + 8f, dy + 16f, 18, 0f, 0.9f, 1f, 80f, 0.55f, 2f, 0f)
                        Gdx.app.log("TELEPORT", "Warp link ${tp.link} -> (${dx.toInt()}, ${dy.toInt()})")
                        break
                    }
                }
            }
        }

        // Collectible-Player collision
        val collected = Array<Collectible>()
        for (item in collectibles) {
            if (Intersector.overlaps(player.bounds, item.bounds)) {
                player.applyEffect(item.type)
                collected.add(item)
                if (item.type == Collectible.Type.COIN) {
                    assetManager.playCoin()
                    spawnFloatingText("+${Player.COIN_SCORE}", item.bounds.x, item.bounds.y + 16f, Color.GOLD)
                    emit(item.bounds.x + 8f, item.bounds.y + 8f, 8, 1f, 0.85f, 0.25f, 55f, 0.45f, 2f, 60f)
                } else {
                    assetManager.playLife()
                    spawnFloatingText("+5 vida", item.bounds.x, item.bounds.y + 16f, Color.GREEN)
                    emit(item.bounds.x + 8f, item.bounds.y + 8f, 8, 0.4f, 1f, 0.45f, 50f, 0.5f, 2f, 40f)
                }
            }
        }
        collectibles.removeAll(collected, true)

        // 1. ENEMY-PLAYER collision (satyrs and wraiths deal damage)
        for (satyr in satyrs) {
            if (Intersector.overlaps(player.bounds, satyr.bounds) && !player.isInvulnerable) {
                player.takeDamage(1); assetManager.playHurt(); comboKills = 0; triggerShake(2.5f, 0.3f)
            }
        }
        for (w in wraiths) {
            if (Intersector.overlaps(player.bounds, w.bounds) && !player.isInvulnerable) {
                player.takeDamage(1); assetManager.playHurt(); comboKills = 0; triggerShake(2.5f, 0.3f)
            }
        }

        // 2. BULLET-ENEMY collision (each bullet hits at most one enemy)
        val bulletsToDestroy = Array<Bullet>()
        val satyrsToDestroy = Array<Satyr>()
        val wraithsToDestroy = Array<Wraith>()
        for (bullet in activeBullets) {
            var hit = false
            for (satyr in satyrs) {
                if (Intersector.overlaps(bullet.bounds, satyr.bounds)) {
                    bulletsToDestroy.add(bullet); satyrsToDestroy.add(satyr); hit = true; break
                }
            }
            if (hit) continue
            for (w in wraiths) {
                if (Intersector.overlaps(bullet.bounds, w.bounds)) {
                    bulletsToDestroy.add(bullet); wraithsToDestroy.add(w); break
                }
            }
        }

        // 3. Process destruction and respawn
        for (satyr in satyrsToDestroy) {
            if (!satyrs.removeValue(satyr, true)) continue  // already removed (double hit in the same frame)
            comboKills++
            comboTimer = COMBO_WINDOW
            val gained = KILL_POINTS * comboMultiplier()
            player.score += gained
            assetManager.playHit()
            spawnFloatingText("+$gained", satyr.position.x, satyr.position.y + 24f, Color.WHITE)
            emit(satyr.position.x + 8f, satyr.position.y + 16f, 10, 0.72f, 0.40f, 0.24f, 70f, 0.55f, 3f, 110f)
            emit(satyr.position.x + 8f, satyr.position.y + 16f, 5, 1f, 1f, 0.9f, 95f, 0.28f, 2f, 0f)
            flashAt(satyr.position.x + 8f, satyr.position.y + 16f)
            hitStopTimer = 0.06f
            spawnNewSatyr()
        }
        for (w in wraithsToDestroy) {
            if (!wraiths.removeValue(w, true)) continue
            comboKills++
            comboTimer = COMBO_WINDOW
            val gained = WRAITH_POINTS * comboMultiplier()
            player.score += gained
            assetManager.playHit()
            spawnFloatingText("+$gained", w.position.x, w.position.y + 24f, Color.CYAN)
            emit(w.position.x + 8f, w.position.y + 16f, 14, 0.55f, 0.32f, 0.72f, 60f, 0.7f, 3f, 18f)
            flashAt(w.position.x + 8f, w.position.y + 16f)
            hitStopTimer = 0.06f
        }

        // 4. Update satyrs, wraiths and bullets
        for (satyr in satyrs) satyr.update(delta, walls)
        for (w in wraiths) w.update(delta, walls, player.position)

        // 5. Bullet recycling
        // Avoid a double-free: a bullet that already hit (in bulletsToDestroy) is not
        // added again even if it also goes out of range in the same frame.
        val bulletsToRemove = Array<Bullet>().apply { addAll(bulletsToDestroy) }
        for (bullet in activeBullets) {
            bullet.update(delta)
            if (bullet.isOffScreen() && !bulletsToDestroy.contains(bullet, true)) bulletsToRemove.add(bullet)
        }

        if (bulletsToRemove.size > 0) {
            activeBullets.removeAll(bulletsToRemove, true)
            for (b in bulletsToRemove) {
                b.reset()
                bulletPool.free(b)
            }
        }

        for (p in particles) p.update(delta)

        drawWorld(delta)
    }

    private fun drawWorld(delta: Float) {
        // --- RENDERING ---
        var camX = player.position.x + 8f
        var camY = player.position.y + 16f
        // Camera shake (damage feedback)
        if (shakeTime > 0f) {
            shakeTime -= delta
            camX += MathUtils.random(-1f, 1f) * shakeIntensity
            camY += MathUtils.random(-1f, 1f) * shakeIntensity
            if (shakeTime <= 0f) shakeIntensity = 0f
        }
        camera?.position?.set(camX, camY, 0f)
        camera?.update()

        renderer?.setView(camera)
        renderer?.render(beforeLayers)   // everything except the overhead layer

        batch?.projectionMatrix = camera!!.combined
        batch?.begin()
        val b = batch!!

        // Biome ambient wash — tints the map behind the actors; the actors draw on top and stay crisp
        val tint = biomeTint(chosenMapIdx)
        b.setColor(tint.r, tint.g, tint.b, 0.14f)
        b.draw(assetManager.pixelRegion, camX - camera!!.viewportWidth / 2f, camY - camera!!.viewportHeight / 2f,
            camera!!.viewportWidth, camera!!.viewportHeight)

        // Blob shadows under the actors
        b.setColor(0f, 0f, 0f, 0.32f)
        for (satyr in satyrs) b.draw(assetManager.shadowRegion, satyr.position.x - 2f, satyr.position.y - 1f, 20f, 8f)
        for (w in wraiths) b.draw(assetManager.shadowRegion, w.position.x - 2f, w.position.y - 1f, 20f, 8f)
        b.draw(assetManager.shadowRegion, player.position.x - 2f, player.position.y - 1f, 20f, 8f)
        b.setColor(1f, 1f, 1f, 1f)

        for (item in collectibles) item.draw(batch!!)
        for (satyr in satyrs) satyr.draw(batch!!)
        for (w in wraiths) w.draw(batch!!)
        for (bullet in activeBullets) bullet.draw(batch!!)
        player.draw(batch!!)

        // Pixel particles on top of the actors
        for (p in particles) p.draw(b, assetManager.pixelRegion)
        b.setColor(1f, 1f, 1f, 1f)

        // Floating texts (+points, +life) in world coordinates
        updateFloatingTexts(delta)
        font?.let { f ->
            for (ft in floatingTexts) {
                f.setColor(ft.color.r, ft.color.g, ft.color.b, ft.alpha())
                f.draw(batch!!, ft.text, ft.x, ft.y)
            }
        }

        batch?.end()

        // "overhead" layer (column capitals) ON TOP of the player -> depth effect
        if (overheadLayers.isNotEmpty()) {
            renderer?.setView(camera)
            renderer?.render(overheadLayers)
        }

        // Damage / low-life red vignette
        if (player.health < prevHealth) damageFlash = 0.5f
        prevHealth = player.health
        damageFlash = (damageFlash - delta).coerceAtLeast(0f)
        pulseT += delta
        val lowLife = if (player.lifeScore in 1..5) 0.14f + 0.10f * (0.5f + 0.5f * MathUtils.sin(pulseT * 6f)) else 0f
        hudController.setDamageVignette(maxOf(damageFlash / 0.5f * 0.55f, lowLife))

        // Update labels and draw the HUD
        hudController.updateLabels(player.health, player.lifeScore, player.score, bestScore, comboMultiplier())
        hudController.hudStage.draw()
    }

    // Combo multiplier: +1 every 3 kills in a row, capped at x5
    private fun comboMultiplier(): Int = (comboKills / 3 + 1).coerceAtMost(5)

    private fun triggerShake(intensity: Float, time: Float) {
        shakeIntensity = maxOf(shakeIntensity, intensity)
        shakeTime = maxOf(shakeTime, time)
    }

    private fun spawnFloatingText(text: String, x: Float, y: Float, color: Color) {
        floatingTexts.add(FloatingText(text, x, y, color))
    }

    private fun biomeTint(idx: Int): Color = biomeTints[idx.coerceIn(0, biomeTints.size - 1)]

    // A brief white "pop" flash at a position (enemy hit-flash on death).
    private fun flashAt(x: Float, y: Float) {
        for (p in particles) {
            if (!p.active) { p.spawn(x, y, 0f, 0f, 0.10f, 22f, 0f, 1f, 1f, 1f); break }
        }
    }

    // Spawns a burst of pixel particles from the pre-allocated pool (no runtime allocation).
    private fun emit(x: Float, y: Float, count: Int, r: Float, g: Float, b: Float, speed: Float, life: Float, size: Float, gravity: Float) {
        var spawned = 0
        for (p in particles) {
            if (spawned >= count) break
            if (!p.active) {
                val ang = MathUtils.random(0f, MathUtils.PI2)
                val spd = MathUtils.random(speed * 0.35f, speed)
                p.spawn(x, y, MathUtils.cos(ang) * spd, MathUtils.sin(ang) * spd,
                        MathUtils.random(life * 0.6f, life), MathUtils.random(size * 0.7f, size), gravity,
                        r + MathUtils.random(-0.06f, 0.06f), g + MathUtils.random(-0.06f, 0.06f), b + MathUtils.random(-0.06f, 0.06f))
                spawned++
            }
        }
    }

    private fun updateFloatingTexts(delta: Float) {
        val dead = Array<FloatingText>()
        for (ft in floatingTexts) {
            ft.update(delta)
            if (!ft.alive()) dead.add(ft)
        }
        floatingTexts.removeAll(dead, true)
    }

    private fun respawnRandomCollectible() {
        if (collectibleTemplates.isEmpty) return
        val t = collectibleTemplates.random() ?: return
        val region = if (t.type == Collectible.Type.COIN) assetManager.coinRegion else assetManager.lifeRegion
        collectibles.add(Collectible(t.bounds.x, t.bounds.y, t.type, region))
    }

    private fun triggerGameOver() {
        state = GameState.GAME_OVER
        if (player.score > bestScore) {
            bestScore = player.score
            prefs.putInteger("best", bestScore)
            prefs.flush()
        }
        assetManager.playGameOver()
        hudController.showGameOver(player.score, bestScore)
        Gdx.app.log("GAME_STATE", "GAME OVER | Punts: ${player.score} | Rècord: $bestScore")
    }

    // Restart the run with the same chosen character and map
    private fun restartGame() {
        beginGame(chosenCharIdx, chosenMapIdx)
    }

    // Floating text that rises and fades: visual feedback for points/life
    private class FloatingText(val text: String, var x: Float, var y: Float, val color: Color) {
        private val maxLife = 0.8f
        private var life = maxLife
        fun update(delta: Float) {
            life -= delta
            y += 20f * delta
        }
        fun alpha(): Float = (life / maxLife).coerceIn(0f, 1f)
        fun alive(): Boolean = life > 0f
    }

    override fun resize(width: Int, height: Int) {
        camera?.viewportWidth = 320f
        camera?.viewportHeight = 320f * height / width
        camera?.update()
        hudController.hudStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        map?.dispose()
        renderer?.dispose()
        if (::assetManager.isInitialized) assetManager.dispose()
        if (::hudController.isInitialized) hudController.dispose()
        if (::player.isInitialized) player.dispose()
        if (::bulletPool.isInitialized) bulletPool.clear()
        batch?.dispose()
        font?.dispose()
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
}
