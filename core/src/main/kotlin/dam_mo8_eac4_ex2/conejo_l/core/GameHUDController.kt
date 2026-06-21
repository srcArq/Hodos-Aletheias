package dam_mo8_eac4_ex2.conejo_l.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.FitViewport

// Full HUD (Scene2D): start, character/scenario selection, info, gameplay and game over.
// playAction(charIdx, mapIdx) starts a run with the player's selection.
class GameHUDController(
    viewportWidth: Float,
    viewportHeight: Float,
    private val shootAction: () -> Unit,
    private val playAction: (Int, Int) -> Unit,
    private val restartAction: () -> Unit
) {

    lateinit var hudStage: Stage
    lateinit var touchpad: Touchpad

    // HUD labels
    private lateinit var healthLabel: Label
    private lateinit var lifeScoreLabel: Label
    private lateinit var scoreLabel: Label
    private lateinit var bestLabel: Label
    private lateinit var comboLabel: Label

    // Controls generated procedurally
    private var joystickBgTexture: Texture? = null
    private var joystickKnobTexture: Texture? = null
    private var shootButtonTexture: Texture? = null
    private lateinit var uiFont: BitmapFont
    private lateinit var titleFont: BitmapFont
    private lateinit var uiSkin: Skin

    // Custom UI art
    private var startTex: Texture? = null
    private var restartTex: Texture? = null
    private var bgTex: Texture? = null
    private var bgGameOverTex: Texture? = null
    private var bgInfoTex: Texture? = null
    private var backTex: Texture? = null
    private var infoTex: Texture? = null

    // Selection
    private val charTex = arrayOfNulls<Texture>(3)
    private val mapThumb = arrayOfNulls<Texture>(3)
    private val charNames = arrayOf("Hoplita", "Guerrera", "Golem")
    private val mapNames = arrayOf("Grecia", "Temple", "Arena")
    private var selChar = 0
    private var selMap = 0
    private var selBgTex: Texture? = null
    private var cellBgTex: Texture? = null
    private lateinit var selDrawable: Drawable
    private lateinit var cellDrawable: Drawable

    init {
        uiFont = BitmapFont().apply { data.setScale(1.5f) }
        titleFont = BitmapFont().apply { data.setScale(2.5f) }
        hudStage = Stage(FitViewport(viewportWidth, viewportHeight))
        Gdx.input.inputProcessor = hudStage
        loadUiArt()
        createSkin()
        createStartScreen()
    }

    private fun loadUiArt() {
        fun load(p: String): Texture? = try { Texture(Gdx.files.internal(p)) } catch (e: Exception) { null }
        startTex = load("ui/start.png"); restartTex = load("ui/restart.png")
        bgTex = load("ui/background.png"); bgGameOverTex = load("ui/backgroundGameover.png"); bgInfoTex = load("ui/backgroundInfo.png")
        backTex = load("ui/back.png"); infoTex = load("ui/info.png")
        charTex[0] = load("characters/character.png"); charTex[1] = load("characters/warrior.png"); charTex[2] = load("characters/golem.png")
        mapThumb[0] = load("ui/thumb_greek.png"); mapThumb[1] = load("ui/thumb_temple.png"); mapThumb[2] = load("ui/thumb_arena.png")
    }

    private fun bgDrawable(color: Color): TextureRegionDrawable {
        val pm = Pixmap(8, 8, Pixmap.Format.RGBA8888); pm.setColor(color); pm.fill()
        val t = Texture(pm); pm.dispose(); return TextureRegionDrawable(t)
    }

    private fun createSkin() {
        uiSkin = Skin().apply {
            add("default", Label.LabelStyle(uiFont, Color.WHITE))
            add("title", Label.LabelStyle(titleFont, Color.WHITE))
            val btnPm = Pixmap(100, 40, Pixmap.Format.RGBA8888).apply {
                setColor(Color(0.15f, 0.55f, 0.2f, 1f)); fill(); setColor(Color.WHITE); drawRectangle(0, 0, 100, 40)
            }
            val btnTx = Texture(btnPm).apply { btnPm.dispose() }
            add("default", TextButton.TextButtonStyle().apply { up = TextureRegionDrawable(btnTx); font = uiFont; fontColor = Color.WHITE })
        }
        val selD = bgDrawable(Color(0.92f, 0.74f, 0.22f, 0.55f)); selBgTex = (selD.region.texture); selDrawable = selD
        val celD = bgDrawable(Color(0f, 0f, 0f, 0.40f)); cellBgTex = (celD.region.texture); cellDrawable = celD
    }

    private fun addBackground(tex: Texture?) {
        tex?.let { hudStage.addActor(Image(TextureRegionDrawable(it)).apply { setFillParent(true); setScaling(Scaling.stretch); touchable = Touchable.disabled }) }
    }

    private fun makeButton(tex: Texture?, fallback: String, onClick: () -> Unit): Actor {
        return if (tex != null) {
            Image(TextureRegionDrawable(tex)).apply {
                setScaling(Scaling.fit); touchable = Touchable.enabled
                addListener(object : ClickListener() { override fun clicked(event: InputEvent?, x: Float, y: Float) = onClick() })
            }
        } else {
            TextButton(fallback, uiSkin).apply { addListener(object : ClickListener() { override fun clicked(event: InputEvent?, x: Float, y: Float) = onClick() }) }
        }
    }

    private fun btnH(tex: Texture?, w: Float, defH: Float) = if (tex != null) w * tex.height / tex.width else defH

    // --- START SCREEN ---
    private fun createStartScreen() {
        disposeRunningTextures(); hudStage.clear()
        addBackground(bgTex)
        // START near the top (over the temple facade) so it doesn't cover the soldier's face
        val table = Table().apply {
            setFillParent(true); top().padTop(150f)
            add(makeButton(startTex, "START") { showSelectScreen() }).size(320f, btnH(startTex, 320f, 88f)).row()
        }
        hudStage.addActor(table)
        // INFO button at the bottom right
        val infoTable = Table().apply {
            setFillParent(true); bottom().right().pad(24f)
            add(makeButton(infoTex, "?") { showInfoScreen() }).size(70f, btnH(infoTex, 70f, 70f))
        }
        hudStage.addActor(infoTable)
    }

    // --- SELECTION SCREEN ---
    private fun showSelectScreen() {
        disposeRunningTextures(); hudStage.clear()
        addBackground(bgTex)
        val root = Table().apply {
            setFillParent(true); center(); padTop(40f)
            add(Label("TRIA PERSONATGE", uiSkin, "default")).padBottom(6f).row()
            val chars = Table()
            for (i in 0..2) { chars.add(charCell(i)).pad(8f) }
            add(chars).row()
            add(Label("TRIA ESCENARI", uiSkin, "default")).padTop(14f).padBottom(6f).row()
            val maps = Table()
            for (i in 0..2) { maps.add(mapCell(i)).pad(8f) }
            add(maps).padBottom(16f).row()
            add(makeButton(startTex, "PLAY") { playAction(selChar, selMap); buildRunningHud() }).size(300f, btnH(startTex, 300f, 84f))
        }
        hudStage.addActor(root)
        addBackButton { createStartScreen() }
    }

    private fun charCell(i: Int): Actor {
        val cell = Table()
        cell.background = if (selChar == i) selDrawable else cellDrawable
        val t = charTex[i]
        if (t != null) cell.add(Image(TextureRegionDrawable(TextureRegion(t, 0, 0, 16, 32))).apply { setScaling(Scaling.fit) }).size(44f, 88f).row()
        else cell.add(Label("?", uiSkin, "default")).size(44f, 88f).row()
        cell.add(Label(charNames[i], uiSkin, "default")).padTop(2f)
        cell.pad(6f)
        cell.touchable = Touchable.enabled
        cell.addListener(object : ClickListener() { override fun clicked(event: InputEvent?, x: Float, y: Float) { selChar = i; showSelectScreen() } })
        return cell
    }

    private fun mapCell(i: Int): Actor {
        val cell = Table()
        cell.background = if (selMap == i) selDrawable else cellDrawable
        val t = mapThumb[i]
        if (t != null) cell.add(Image(TextureRegionDrawable(t)).apply { setScaling(Scaling.fit) }).size(66f, 66f).row()
        else cell.add(Label("?", uiSkin, "default")).size(66f, 66f).row()
        cell.add(Label(mapNames[i], uiSkin, "default")).padTop(2f)
        cell.pad(6f)
        cell.touchable = Touchable.enabled
        cell.addListener(object : ClickListener() { override fun clicked(event: InputEvent?, x: Float, y: Float) { selMap = i; showSelectScreen() } })
        return cell
    }

    private fun addBackButton(onBack: () -> Unit) {
        val t = Table().apply {
            setFillParent(true); bottom().left().pad(24f)
            add(makeButton(backTex, "<") { onBack() }).size(100f, btnH(backTex, 100f, 56f))
        }
        hudStage.addActor(t)
    }

    // --- INFO / RULES SCREEN ---
    private fun showInfoScreen() {
        disposeRunningTextures(); hudStage.clear()
        addBackground(bgInfoTex)
        val rules = "Mou-te amb el joystick i dispara amb el botó.\n" +
                "Mata sàtirs (+100, combo x fins a 5) i recull monedes (+50) i vida.\n" +
                "La vida baixa amb el temps; xoca amb murs i columnes.\n" +
                "Trepitja els portals per teletransportar-te. Sobreviu el màxim!"
        val table = Table().apply {
            setFillParent(true); center(); padTop(60f)
            add(Label(rules, uiSkin, "default").apply { setAlignment(Align.center); setWrap(true) }).width(560f)
        }
        hudStage.addActor(table)
        addBackButton { createStartScreen() }
    }

    // --- GAMEPLAY HUD ---
    private fun buildRunningHud() {
        disposeRunningTextures(); hudStage.clear()
        createLabels(); createJoystick(); createShootButton()
    }

    private fun createLabels() {
        healthLabel = Label("Salut: 3", uiSkin, "default")
        lifeScoreLabel = Label("Vida: 20", uiSkin, "default")
        scoreLabel = Label("Punts: 0", uiSkin, "default").apply { setAlignment(Align.right) }
        bestLabel = Label("Record: 0", uiSkin, "default").apply { setAlignment(Align.right) }
        comboLabel = Label("", uiSkin, "default").apply { setAlignment(Align.center); color = Color.ORANGE }
        val topTable = Table().apply {
            setFillParent(true); top().pad(12f)
            add(healthLabel).expandX().left()
            add(comboLabel).expandX().center()
            add(scoreLabel).expandX().right().row()
            add(lifeScoreLabel).left().padTop(4f)
            add(Label("", uiSkin, "default")).expandX()
            add(bestLabel).right().padTop(4f)
        }
        hudStage.addActor(topTable)
    }

    private fun createJoystick() {
        val bgPm = Pixmap(200, 200, Pixmap.Format.RGBA8888).apply { setColor(Color(0.5f, 0.5f, 0.5f, 0.5f)); fillCircle(100, 100, 100) }
        joystickBgTexture = Texture(bgPm).apply { bgPm.dispose() }
        val knobPm = Pixmap(50, 50, Pixmap.Format.RGBA8888).apply { setColor(Color.CYAN); fillCircle(25, 25, 25) }
        joystickKnobTexture = Texture(knobPm).apply { knobPm.dispose() }
        val touchStyle = Touchpad.TouchpadStyle().apply { background = TextureRegionDrawable(joystickBgTexture); knob = TextureRegionDrawable(joystickKnobTexture) }
        touchpad = Touchpad(10f, touchStyle).apply { setBounds(60f, 90f, 200f, 200f) }
        hudStage.addActor(touchpad)
    }

    private fun createShootButton() {
        val btnPm = Pixmap(120, 120, Pixmap.Format.RGBA8888).apply {
            setColor(Color(0f, 0.85f, 0.95f, 0.9f)); fillCircle(60, 60, 60); setColor(Color.WHITE); drawCircle(60, 60, 59)
        }
        shootButtonTexture = Texture(btnPm).apply { btnPm.dispose() }
        val baseStyle = uiSkin.get("default", TextButton.TextButtonStyle::class.java)
        val shootButton = TextButton("", TextButton.TextButtonStyle(baseStyle).apply { up = TextureRegionDrawable(shootButtonTexture) }).apply {
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean { shootAction(); return true }
            })
        }
        val table = Table().apply { setFillParent(true); bottom().right(); add(shootButton).size(120f, 120f).padBottom(90f).padRight(60f) }
        hudStage.addActor(table)
    }

    fun updateLabels(health: Int, lifeScore: Int, score: Int, best: Int, comboMultiplier: Int) {
        if (!::healthLabel.isInitialized) return
        healthLabel.setText("Salut: $health")
        lifeScoreLabel.setText("Vida: $lifeScore")
        scoreLabel.setText("Punts: $score")
        bestLabel.setText("Record: $best")
        comboLabel.setText(if (comboMultiplier > 1) "COMBO x$comboMultiplier" else "")
        lifeScoreLabel.color = when { lifeScore < 5 -> Color.RED; lifeScore < 10 -> Color.YELLOW; else -> Color.WHITE }
    }

    // --- GAME OVER (the background already has the "GAME OVER" title and frame; don't duplicate it) ---
    fun showGameOver(finalScore: Int, bestScore: Int) {
        disposeRunningTextures(); hudStage.clear()
        addBackground(bgGameOverTex)
        val isRecord = finalScore >= bestScore && finalScore > 0
        // The background already has the "Score:" header; show the value below it (no overlap)
        val table = Table().apply {
            setFillParent(true); top().padTop(196f)
            add(Label("$finalScore", uiSkin, "title")).padBottom(14f).row()
            add(Label(if (isRecord) "NOU RECORD!  $bestScore" else "Record: $bestScore", uiSkin, "default").apply { if (isRecord) color = Color.GOLD }).padBottom(22f).row()
            add(makeButton(restartTex, "RESTART") { restartAction(); buildRunningHud() }).size(300f, btnH(restartTex, 300f, 78f))
        }
        hudStage.addActor(table)
        // The arrow goes to the SELECTOR: pick again / change map after dying
        addBackButton { showSelectScreen() }
    }

    private fun disposeRunningTextures() {
        joystickBgTexture?.dispose(); joystickBgTexture = null
        joystickKnobTexture?.dispose(); joystickKnobTexture = null
        shootButtonTexture?.dispose(); shootButtonTexture = null
    }

    fun dispose() {
        hudStage.dispose()
        disposeRunningTextures()
        uiFont.dispose(); titleFont.dispose(); uiSkin.dispose()
        startTex?.dispose(); restartTex?.dispose(); bgTex?.dispose(); bgGameOverTex?.dispose(); bgInfoTex?.dispose()
        backTex?.dispose(); infoTex?.dispose()
        charTex.forEach { it?.dispose() }; mapThumb.forEach { it?.dispose() }
        selBgTex?.dispose(); cellBgTex?.dispose()
    }
}
