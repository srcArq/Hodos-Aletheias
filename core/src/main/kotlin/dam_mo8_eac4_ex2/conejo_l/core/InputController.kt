package dam_mo8_eac4_ex2.conejo_l.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.FitViewport

class InputController(viewportWidth: Float, viewportHeight: Float, private val shootAction: () -> Unit) {

    lateinit var hudStage: Stage
    lateinit var touchpad: Touchpad

    // Resources to dispose
    private lateinit var joystickBgTexture: Texture
    private lateinit var joystickKnobTexture: Texture
    private lateinit var shootButtonTexture: Texture
    private lateinit var uiFont: BitmapFont
    private lateinit var uiSkin: Skin

    init {
        uiFont = BitmapFont()
        hudStage = Stage(FitViewport(viewportWidth, viewportHeight))
        Gdx.input.inputProcessor = hudStage
        createJoystickUI()
    }

    private fun createJoystickUI() {
        // --- JOYSTICK ---
        val bgPm = Pixmap(200, 200, Pixmap.Format.RGBA8888).apply {
            setColor(Color(0.5f, 0.5f, 0.5f, 0.5f)); fillCircle(100, 100, 100)
        }
        joystickBgTexture = Texture(bgPm).apply { bgPm.dispose() }

        val knobPm = Pixmap(50, 50, Pixmap.Format.RGBA8888).apply {
            setColor(Color.CYAN); fillCircle(25, 25, 25)
        }
        joystickKnobTexture = Texture(knobPm).apply { knobPm.dispose() }

        val touchStyle = Touchpad.TouchpadStyle().apply {
            background = TextureRegionDrawable(joystickBgTexture)
            knob = TextureRegionDrawable(joystickKnobTexture)
        }
        touchpad = Touchpad(10f, touchStyle).apply { setBounds(20f, 20f, 200f, 200f) }
        hudStage.addActor(touchpad)

        // --- SHOOT BUTTON ---
        val btnPm = Pixmap(50, 50, Pixmap.Format.RGBA8888).apply {
            setColor(Color.CYAN)
            fillCircle(25, 25, 25)
        }
        shootButtonTexture = Texture(btnPm).apply { btnPm.dispose() }

        uiSkin = Skin().apply {
            add("default", TextButton.TextButtonStyle().apply {
                up = TextureRegionDrawable(shootButtonTexture)
                font = uiFont
            })
        }

        val shootButton = TextButton("", uiSkin).apply {
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    shootAction() // Calls GameScreen's shoot()
                    return true
                }
            })
        }

        val table = Table().apply {
            setFillParent(true)
            bottom().right()
            add(shootButton).size(50f, 50f).pad(20f).padBottom(50f).padRight(50f)
        }
        hudStage.addActor(table)
    }

    fun dispose() {
        hudStage.dispose()
        joystickBgTexture.dispose()
        joystickKnobTexture.dispose()
        shootButtonTexture.dispose()
        uiFont.dispose()
        uiSkin.dispose()
    }
}
