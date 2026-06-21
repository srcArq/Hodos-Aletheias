package dam_mo8_eac4_ex2.conejo_l.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import dam_mo8_eac4_ex2.conejo_l.Player

class GameAssetManager {

    // Animation maps
    private val satyrFrames = mutableMapOf<String, Animation<TextureRegion>>()
    private val wraithFrames = mutableMapOf<String, Animation<TextureRegion>>()
    private val playerFrames = mutableMapOf<String, Animation<TextureRegion>>()
    private val spearRegions = mutableMapOf<Player.Direction, TextureRegion>()

    // Textures and regions
    lateinit var collectiblesTexture: Texture
    lateinit var satyrTexture: Texture
    lateinit var wraithTexture: Texture
    lateinit var playerTexture: Texture
    lateinit var bulletTexture: Texture
    lateinit var coinRegion: TextureRegion
    lateinit var lifeRegion: TextureRegion

    // --- SOUNDS (SFX) ---
    private var shootSound: Sound? = null
    private var hitSound: Sound? = null
    private var hurtSound: Sound? = null
    private var coinSound: Sound? = null
    private var lifeSound: Sound? = null
    private var gameOverSound: Sound? = null
    private var startSound: Sound? = null
    private var teleportSound: Sound? = null

    // Helper to build an animation from a vararg list of frames
    private fun createAnimation(duration: Float, vararg frames: TextureRegion): Animation<TextureRegion> {
        val animFrames = Array<TextureRegion>()
        for (frame in frames) animFrames.add(frame)
        return Animation(duration, animFrames, Animation.PlayMode.LOOP)
    }

    // Alternative helper that takes an array of frames (used by Satyr)
    private fun createAnimationFromArray(duration: Float, framesArray: kotlin.Array<TextureRegion>): Animation<TextureRegion> {
        val gdxArray = Array<TextureRegion>()
        for (frame in framesArray) gdxArray.add(frame)
        return Animation(duration, gdxArray, Animation.PlayMode.LOOP)
    }

    // Load the SFX defensively: if any file is missing, the game keeps running without sound
    private fun loadSounds() {
        try {
            shootSound    = Gdx.audio.newSound(Gdx.files.internal("sfx/shoot.wav"))
            hitSound      = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.wav"))
            hurtSound     = Gdx.audio.newSound(Gdx.files.internal("sfx/hurt.wav"))
            coinSound     = Gdx.audio.newSound(Gdx.files.internal("sfx/coin.wav"))
            lifeSound     = Gdx.audio.newSound(Gdx.files.internal("sfx/life.wav"))
            gameOverSound = Gdx.audio.newSound(Gdx.files.internal("sfx/gameover.wav"))
            startSound    = Gdx.audio.newSound(Gdx.files.internal("sfx/start.wav"))
            teleportSound = Gdx.audio.newSound(Gdx.files.internal("sfx/teleport.wav"))
        } catch (e: Exception) {
            Gdx.app.error("AssetManager", "No s'han pogut carregar els sons: ${e.message}")
        }
    }

    fun playShoot()    { shootSound?.play(0.5f) }
    fun playHit()      { hitSound?.play(0.5f) }
    fun playHurt()     { hurtSound?.play(0.7f) }
    fun playCoin()     { coinSound?.play(0.6f) }
    fun playLife()     { lifeSound?.play(0.6f) }
    fun playGameOver() { gameOverSound?.play(0.7f) }
    fun playStart()    { startSound?.play(0.6f) }
    fun playTeleport() { teleportSound?.play(0.6f) }

    fun loadAllAssets(charPath: String): Map<String, Animation<TextureRegion>> {
        loadSounds()
        try {
            // 1. COLLECTIBLES & SPEAR
            collectiblesTexture = Texture(Gdx.files.internal("objects/collectibles.png"))
            coinRegion = TextureRegion(collectiblesTexture, 0, 0, 16, 16)
            lifeRegion = TextureRegion(collectiblesTexture, 16, 0, 16, 16)

            spearRegions[Player.Direction.UP] = TextureRegion(collectiblesTexture, 80, 0, 16, 32)
            spearRegions[Player.Direction.DOWN] = TextureRegion(collectiblesTexture, 96, 0, 16, 32)
            spearRegions[Player.Direction.RIGHT] = TextureRegion(collectiblesTexture, 0, 16, 32, 16)
            spearRegions[Player.Direction.LEFT] = TextureRegion(collectiblesTexture, 32, 16, 32, 16)

            // 2. SATYRS (animations)
            satyrTexture = Texture(Gdx.files.internal("characters/satiro.png"))
            val satyrSplit = TextureRegion.split(satyrTexture, 16, 32)
            val satyrRow = satyrSplit[1]

            satyrFrames["down"] = createAnimation(0.15f, satyrRow[0], satyrRow[1])
            satyrFrames["right"] = createAnimation(0.15f, satyrRow[2], satyrRow[3])
            satyrFrames["left"] = createAnimation(0.15f, satyrRow[4], satyrRow[5])
            satyrFrames["up"] = createAnimation(0.15f, satyrRow[6], satyrRow[7])

            // 2b. WRAITHS (second enemy type, same sheet layout as the satyr)
            wraithTexture = Texture(Gdx.files.internal("characters/wraith.png"))
            val wraithRow = TextureRegion.split(wraithTexture, 16, 32)[1]
            wraithFrames["down"] = createAnimation(0.18f, wraithRow[0], wraithRow[1])
            wraithFrames["right"] = createAnimation(0.18f, wraithRow[2], wraithRow[3])
            wraithFrames["left"] = createAnimation(0.18f, wraithRow[4], wraithRow[5])
            wraithFrames["up"] = createAnimation(0.18f, wraithRow[6], wraithRow[7])

            // 3. PLAYER — the character chosen in the menu
            Gdx.app.log("CHARACTER", "Personatge carregat: $charPath")
            playerTexture = Texture(Gdx.files.internal(charPath))

            // Exact split: 16x32
            val tmp = TextureRegion.split(playerTexture, 16, 32)

            // Row 0: IDLE (0.5f frame duration)
            playerFrames["idle_down"]  = createAnimation(0.5f, tmp[0][0], tmp[0][1])
            playerFrames["idle_right"] = createAnimation(0.5f, tmp[0][2], tmp[0][3])
            playerFrames["idle_left"]  = createAnimation(0.5f, tmp[0][4], tmp[0][5])
            playerFrames["idle_up"]    = createAnimation(0.5f, tmp[0][6], tmp[0][7])

            // Row 1: WALK (0.15f frame duration)
            playerFrames["walk_down"]  = createAnimation(0.15f, tmp[1][0], tmp[1][1])
            playerFrames["walk_right"] = createAnimation(0.15f, tmp[1][2], tmp[1][3])
            playerFrames["walk_left"]  = createAnimation(0.15f, tmp[1][4], tmp[1][5])
            playerFrames["walk_up"]    = createAnimation(0.15f, tmp[1][6], tmp[1][7])

            return satyrFrames

        } catch (e: Exception) {
            Gdx.app.error("AssetManager", "Error fatal carregant assets: ${e.message}")
            val pm = Pixmap(16, 32, Pixmap.Format.RGBA8888).apply { setColor(Color.RED); fill() }
            bulletTexture = Texture(pm).apply { pm.dispose() }
            return emptyMap()
        }
    }

    fun getSatyrAnimations(): Map<String, Animation<TextureRegion>> { return satyrFrames }

    fun getWraithAnimations(): Map<String, Animation<TextureRegion>> { return wraithFrames }

    // Getter for the player animations
    fun getPlayerAnimations(): Map<String, Animation<TextureRegion>> { return playerFrames }

    fun getSpearRegions(): Map<Player.Direction, TextureRegion> {
        if (!::bulletTexture.isInitialized && spearRegions.isEmpty()) {
            // Minimal fallback if loading failed
            return emptyMap()
        }
        return spearRegions
    }

    fun dispose() {
        if (::collectiblesTexture.isInitialized) collectiblesTexture.dispose()
        if (::satyrTexture.isInitialized) satyrTexture.dispose()
        if (::wraithTexture.isInitialized) wraithTexture.dispose()
        if (::playerTexture.isInitialized) playerTexture.dispose()
        if (::bulletTexture.isInitialized) bulletTexture.dispose()
        shootSound?.dispose()
        hitSound?.dispose()
        hurtSound?.dispose()
        coinSound?.dispose()
        lifeSound?.dispose()
        gameOverSound?.dispose()
        startSound?.dispose()
        teleportSound?.dispose()
    }
}
